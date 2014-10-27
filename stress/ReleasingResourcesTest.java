package liger.ldb.core.stress;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.PropertyConfigurator;

public class ReleasingResourcesTest {

	public static void main(String[] args) throws FileNotFoundException, IOException {

		String configFile = "etc/stress-test.properties";
		PropertyConfigurator.configure(configFile);

		Properties props = new Properties();
		props.load(new FileInputStream(configFile));

		final AtomicBoolean stopper = new AtomicBoolean(false);

		new Thread() {
			public void run() {
				while (true) {
					try {
						String line = new LineNumberReader(new InputStreamReader(System.in)).readLine();
						if (line.equals("stop")) {
							System.err.println("stopping");
							stopper.set(true);
						} else {
							System.err.println("starting");
							stopper.set(false);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();

		
		
		try {
			for (;;) {
				if (stopper.get()) {
					Thread.sleep(250);
				} else {
					Writer writer = new PrintWriter(props.getProperty("FileToWriteTo"));
					getEvaluator("SequentialWithCustomOutputPooledDoingAll", props).go(writer);
				}
			}
		} catch (InterruptedException e) {

		} finally {
		}
	}

	private static Executor executor = Executors.newCachedThreadPool();
	private static Executor executor2 = Executors.newFixedThreadPool(3);
	private static Semaphore sem = new Semaphore(5);

	private static StressTest getEvaluator(String id, Properties props) throws FileNotFoundException {
		switch (id) {
		case "MainThreadDoingAll": //Seems to be OK
			return new MainThreadDoingAll(props);
		case "MainThreadStartAndParseAndTerminatesNotPooledRuns": //Seems to be OK
			return new MainThreadStartAndParseAndTerminatesNotPooledRuns(props);
		case "MainThreadStartAndParseAndTerminatesPooledRuns": //Problem A (not releasing resources)
			return new MainThreadStartAndParseAndTerminatesPooledRuns(props, executor);
		case "MainThreadStartAndParseNotPooledRunsAndTerminates": //Problem A (not releasing resources)
			return new MainThreadStartAndParseNotPooledRunsAndTerminates(props);
		case "MainThreadStartNotPooledParseAndRunsAndTerminates": //Seems to be OK
			return new MainThreadStartNotPooledParseAndRunsAndTerminates(props);
		case "MainThreadStartPooledParseAndRunsAndTerminates": //Seems to be OK
			return new MainThreadStartPooledParseAndRunsAndTerminates(props, executor);
		case "NotPooledDoingAll": //Seems to be OK
			return new NotPooledDoingAll(props);
		case "SequentialPooledDoingAll": //Seems to be OK
			return new SequentialPooledDoingAll(props, executor);
		case "ConcurrentPooledDoingAll": //Seems to be OK
			return new ConcurrentPooledDoingAll(props, executor2, sem);
		case "SequentialWithCustomOutputPooledDoingAll":
			return new SequentialWithCustomOutputPooledDoingAll(props, executor); // Problem B (not releasing resources)
		default:
			throw new RuntimeException();
		}
	}

}
