package liger.ldb.core.stress;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import org.jruby.Ruby;
import org.jruby.RubyInstanceConfig.CompileMode;
import org.jruby.embed.EmbedEvalUnit;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.ScriptingContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SequentialWithCustomOutputPooledDoingAll implements StressTest {
	private static AtomicInteger id = new AtomicInteger();
	private final Logger logger = LoggerFactory.getLogger(MainThreadStartNotPooledParseAndRunsAndTerminates.class);
	ScriptingContainer sc;
	EmbedEvalUnit parsed;
	private int idd = id.getAndIncrement();
	private volatile CountDownLatch latch = new CountDownLatch(1);

	private Executor executor;
	private Properties props;
	private Writer realOut;
	
	
	private  Writer stdOutErr = new Writer() {

		@Override
		public void write(char[] cbuf, int off, int len) throws IOException {
			if (realOut != null)
				realOut.write(cbuf, off, len);
		}

		@Override
		public void flush() throws IOException {
			if (realOut != null)
				realOut.flush();
		}

		@Override
		public void close() throws IOException {
			if (realOut != null)
				realOut.close();
		}

	};

	public SequentialWithCustomOutputPooledDoingAll(Properties props, Executor executor) throws FileNotFoundException {
		this.executor = executor;
		this.props = props;
	}


	@Override
	public void go(final Writer out) {
		executor.execute(new Runnable() {

			@Override
			public void run() {
				realOut = out;
				
				logger.debug("{} Created and initializated SC", idd);
				sc = new ScriptingContainer(LocalContextScope.SINGLETHREAD);
				sc.getProvider().getRubyInstanceConfig().setCompileMode(CompileMode.OFF);
				sc.getProvider().getRubyInstanceConfig().setObjectSpaceEnabled(false);
				sc.setLoadPaths(Arrays.asList(props.getProperty("RubyDir").split(":")));
				
				sc.setOutput(stdOutErr);
				
				logger.debug("{} (compile) Parsing script", idd);
				parsed = sc.parse("" + " s = 0 \n" + "1.upto(1000) do |a| \n" + "  s += a \n" + "end \n"
						+ "puts \"#{s} #{$ldb}\"");

				sc.clear();
				sc.put("$ldb", "ldb");
				sc.put("$ldb_log", "log");
				logger.debug("{} Running script", idd);
				parsed.run();
				logger.debug("{} Terminating script", idd);
				try {
					stdOutErr.flush();
					stdOutErr.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				sc.terminate();
				Ruby.clearGlobalRuntime();
				latch.countDown();
			}
		});

		try {
			latch.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
