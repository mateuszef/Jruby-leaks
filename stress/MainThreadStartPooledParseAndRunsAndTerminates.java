package liger.ldb.core.stress;

import java.io.Writer;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import org.jruby.RubyInstanceConfig.CompileMode;
import org.jruby.embed.EmbedEvalUnit;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.ScriptingContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainThreadStartPooledParseAndRunsAndTerminates implements StressTest {
	private static AtomicInteger id = new AtomicInteger();
	private final Logger logger = LoggerFactory.getLogger(MainThreadStartNotPooledParseAndRunsAndTerminates.class);
	ScriptingContainer sc;
	EmbedEvalUnit parsed;
	private int idd = id.getAndIncrement();
	private volatile CountDownLatch latch = new CountDownLatch(1);

	private Executor executor;
	private Properties props;

	public MainThreadStartPooledParseAndRunsAndTerminates(Properties props, Executor executor) {
		this.executor = executor;
		this.props = props;
	}

	@Override
	public void go(Writer out) {
		logger.debug("{} Created and initializated SC", idd);
		sc = new ScriptingContainer(LocalContextScope.SINGLETHREAD);
		sc.getProvider().getRubyInstanceConfig().setCompileMode(CompileMode.OFF);
		sc.getProvider().getRubyInstanceConfig().setObjectSpaceEnabled(false);
		sc.setLoadPaths(Arrays.asList(props.getProperty("RubyDir").split(":")));

		executor.execute(new Runnable() {

			@Override
			public void run() {
				logger.debug("{} (compile) Parsing script", idd);
				parsed = sc.parse("" + " s = 0 \n" + "1.upto(1000) do |a| \n" + "  s += a \n" + "end \n"
						+ "puts \"#{s} #{$ldb}\"");

				sc.clear();
				sc.put("$ldb", "ldb");
				sc.put("$ldb_log", "log");
				logger.debug("{} Running script", idd);
				parsed.run();
				logger.debug("{} Terminating script", idd);
				sc.terminate();
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
