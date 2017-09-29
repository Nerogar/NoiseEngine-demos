package de.nerogar.noiseTest.logger;

import de.nerogar.noise.util.Logger;

public class Main {

	private static void processLogOutput(String s) {
		// do something with the log string, e.g. print it to the console
		System.out.println(s);
	}

	private static void exceptionMethod() throws Exception {
		throw new Exception("this is a test exception");
	}

	private static void testLogger() {

		Logger logger = new Logger("test");

		// tell the logger to print timestamps for each log message
		Logger.instance.setPrintTimestamp(true);

		// add output streams
		logger.addStream(Logger.DEBUG, Logger.INFO, System.out);
		logger.addStream(Logger.WARNING, System.err);

		// add an output listener
		logger.addListener(Logger.DEBUG, Logger.INFO, Main::processLogOutput);

		// log something
		logger.log(Logger.INFO, "This is a test message.");

		// log an exception with the error stream
		try {
			exceptionMethod();
		} catch (Exception e) {
			logger.log(Logger.ERROR, "--- exception caught ---");
			e.printStackTrace(logger.getErrorStream());
		}

	}

	public static void main(String[] args) {
		testLogger();
	}

}
