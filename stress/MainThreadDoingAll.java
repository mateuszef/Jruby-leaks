package liger.ldb.core.stress;

import java.io.Writer;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.jruby.RubyInstanceConfig.CompileMode;
import org.jruby.embed.EmbedEvalUnit;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.ScriptingContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainThreadDoingAll implements StressTest {
	private static AtomicInteger id = new AtomicInteger();
	private final Logger logger = LoggerFactory.getLogger(MainThreadStartAndParseNotPooledRunsAndTerminates.class);
	ScriptingContainer sc;
	EmbedEvalUnit parsed;
	private int idd = id.getAndIncrement();
	private Properties props;

	public MainThreadDoingAll(Properties props) {
		this.props = props;
	}

	@Override
	public void go(Writer out) {
		out.close();
		logger.debug("{} Created and initializated SC", idd);
		ScriptingContainer sc = new ScriptingContainer(LocalContextScope.SINGLETHREAD);
		sc.getProvider().getRubyInstanceConfig().setCompileMode(CompileMode.OFF);
		sc.getProvider().getRubyInstanceConfig().setObjectSpaceEnabled(false);
		sc.setLoadPaths(Arrays.asList(props.getProperty("RubyDir").split(":")));

		logger.debug("{} (compile) Parsing script", idd);
		EmbedEvalUnit parsed = sc.parse("" + " s = 0 \n" + "1.upto(1000) do |a| \n" + "  s += a \n" + "end \n"
				+ "puts \"#{s} #{$ldb}\"");

		sc.clear();
		sc.put("$ldb", "ldb");
		sc.put("$ldb_log", "log");
		logger.debug("{} Running script", idd);
		parsed.run();

		logger.debug("{} Terminating script", idd);
		sc.terminate();
	}

}
