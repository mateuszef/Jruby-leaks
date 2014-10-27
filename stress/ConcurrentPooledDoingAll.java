package liger.ldb.core.stress;

import java.io.Writer;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import org.jruby.RubyInstanceConfig.CompileMode;
import org.jruby.embed.EmbedEvalUnit;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.ScriptingContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConcurrentPooledDoingAll implements StressTest {
	private static AtomicInteger id = new AtomicInteger();
	private final Logger logger = LoggerFactory.getLogger(ConcurrentPooledDoingAll.class);
	ScriptingContainer sc;
	EmbedEvalUnit parsed;
	private int idd = id.getAndIncrement();

	private Executor executor;
	private Semaphore semaphore;
	private Properties props;

	public ConcurrentPooledDoingAll(Properties props, Executor executor, Semaphore semaphore) {
		this.executor = executor;
		this.semaphore = semaphore;
		this.props = props;
	}

	@Override
	public void go(Writer out) {

		try {
			semaphore.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		executor.execute(new Runnable() {

			@Override
			public void run() {
				sc = new ScriptingContainer(LocalContextScope.SINGLETHREAD);
				sc.getProvider().getRubyInstanceConfig().setCompileMode(CompileMode.OFF);
				sc.getProvider().getRubyInstanceConfig().setObjectSpaceEnabled(false);
				sc.setLoadPaths(Arrays.asList(props.getProperty("RubyDir").split(":")));

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
				semaphore.release();
			}
		});
	}

}
