package fr.univmobile.it.commons;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.CharEncoding.UTF_8;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.StringUtils.substringBetween;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.exec.ExecuteException;

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
			throws IOException {

		final String executablePath = executable.getPath();

		System.out.print("Executing: " + executablePath);

		for (final String argument : arguments) {

			System.out.print(" \"" + argument + "\"");
		}

		System.out.println("...");

		final ByteArrayOutputStream bos_out = new ByteArrayOutputStream();

		final ByteArrayOutputStream bos_err = new ByteArrayOutputStream();

		final List<String> command = new ArrayList<String>();

		command.add(executablePath);

		for (final String argument : arguments) {

			command.add(argument);
		}

		final Process process = new ProcessBuilder(command).start();

		final InputStream err = process.getErrorStream();
		final InputStream out = process.getInputStream();

		while (true) {

			final int b1 = out.read();
			final int b2 = err.read();

			if (b1 != -1) {
				bos_out.write(b1);
			}

			if (b2 != -1) {
				bos_err.write(b2);
			}

			if (b1 == -1 && b2 == -1) {
				break;
			}
		}

		final int result;

		try {

			result = process.waitFor();

		} catch (final InterruptedException e) {

			throw new RuntimeException(e);
		}

		/*
		 * final Executor executor = new DefaultExecutor();
		 * 
		 * executor.setStreamHandler(new PumpStreamHandler(out, err));
		 * 
		 * final CommandLine commandLine = new CommandLine(executable);
		 * 
		 * for (final String argument : arguments) {
		 * 
		 * commandLine.addArgument(argument); }
		 * 
		 * ExecuteException executeException = null;
		 * 
		 * try {
		 * 
		 * executor.execute(commandLine);
		 * 
		 * } catch (final ExecuteException e) {
		 * 
		 * System.err.println(e);
		 * 
		 * executeException = e; }
		 */

		final String error = bos_err.toString(UTF_8);

		if (!isBlank(error)) {

			System.err.println("Error:");

			System.err.println(error);
		}

		final String output = bos_out.toString(UTF_8);

		if (isBlank(output)) {

			System.out.println("(No output.)");

		} else {

			System.out.println("Output:");

			System.out.println(output);
		}

		if (result != 0) {

			throw new ExecuteException(split(error)[0], result);
		}

		return output;
	}
}
