package fr.univmobile.it.commons;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.CharEncoding.UTF_8;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.StringUtils.substringBetween;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;

public abstract class EnvironmentUtils {

	/**
	 * e.g. "7.1"
	 */
	public static String getCurrentPlatformVersion(final String platformName)
			throws IOException {

		checkNotNull(platformName, "platformName");

		if ("iOS".equals(platformName)) {
			return getCurrentPlatformVersion_iOS();
		}

		if ("Android".equals(platformName)) {
			return "XXX";
		}

		if ("Mac OS X".equals(platformName)) {
			return System.getProperty("os.version");
		}

		if ("Debian".equals(platformName)) {
			return System.getProperty("os.version");
		}

		throw new IllegalArgumentException("Unknown platformName: "
				+ platformName);
	}

	private static String getCurrentPlatformVersion_iOS() throws IOException {

		final String output = exec(new File("/usr/bin/xcodebuild"), "-showsdks");

		String platformVersion = null;

		for (final String line : split(output, "\r\n")) {

			if (line.contains("Simulator - iOS ")) {

				platformVersion = substringBetween(line, "iOS ", " ").trim();
			}
		}

		if (isBlank(platformVersion)) {

			System.err.println("Cannot find iOS platformVersion:");

			System.err.println(output);

			throw new IOException("Cannot find iOS platformVersion.");
		}

		return platformVersion;
	}

	public static String exec(final File executable, final String... arguments)
			throws ExecuteException, IOException {

		System.out.print("Executing: " + executable.getCanonicalPath());

		for (final String argument : arguments) {

			System.out.print(" \"" + argument + "\"");
		}

		System.out.println("...");

		final Executor executor = new DefaultExecutor();

		final ByteArrayOutputStream bos = new ByteArrayOutputStream();

		executor.setStreamHandler(new PumpStreamHandler(bos, null));

		final CommandLine commandLine = new CommandLine(executable);

		for (final String argument : arguments) {

			commandLine.addArgument(argument);
		}

		executor.execute(commandLine);

		final String output = bos.toString(UTF_8);

		if (isBlank(output)) {

			System.out.println("(No output.)");

		} else {

			System.out.println("Output:");

			System.out.println(output);
		}

		return output;
	}
}
