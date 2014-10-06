package fr.univmobile.it.commons;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.CharEncoding.UTF_8;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBeforeLast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.annotation.Nullable;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

abstract class IOSUtils {

	public static String getMostRecentAppPath(final String appPathProperty,
			@Nullable final String requiredAppCommitId)
			throws FileNotFoundException, IOException {

		System.out.println("Using UnivMobile-(lastimport).app: "
				+ appPathProperty + "...");

		System.out.println("requiredAppCommitId: " + requiredAppCommitId);

		final File appRepo = new File(substringBeforeLast(appPathProperty, "/"));

		if (!appRepo.isDirectory()) {
			throw new FileNotFoundException("Cannot find APP_REPO for: "
					+ appPathProperty);
		}

		final File touched_after_lastimport = new File(appRepo,
				"touched_after_lastimport");

		if (!touched_after_lastimport.exists()) {
			throw new FileNotFoundException(appRepo.getCanonicalPath() + "/"
					+ touched_after_lastimport.getName());
		}

		final long touchedAt = touched_after_lastimport.lastModified();

		final String touchedAtAsString = new DateTime(touchedAt)
				.toString(DateTimeFormat.forPattern("YYYYMMdd-HHmmss"));

		System.out.println(touched_after_lastimport + ".modified: "
				+ touchedAtAsString);

		String mostRecentAppDirName = null;

		// e.g. "UnivMobile-20140712-090711.app"

		for (final File appDir : appRepo.listFiles()) {

			final String appDirName = appDir.getName();

			if (!appDirName.startsWith("UnivMobile-")
					|| !appDirName.endsWith(".app")) {
				continue;
			}

			System.out.println("  appDir.name: " + appDirName);

			final String dirModifiedAtString = appDirName.substring(
					appDirName.length() - 19, appDirName.length() - 4);

			System.out.println("         modified: " + dirModifiedAtString);

			final String appCommitId = readAppCommitId(appDir);

			System.out.print("      appCommitId: " + appCommitId);

			if (requiredAppCommitId != null
					&& !requiredAppCommitId.equals(appCommitId)) {

				System.out.println(" (skipping)");

				continue;
			}

			System.out.println();

			if (touchedAtAsString.compareTo(dirModifiedAtString) >= 0) {

				if (mostRecentAppDirName == null
						|| mostRecentAppDirName.compareTo(appDirName) < 0) {

					mostRecentAppDirName = appDirName;
				}
			}
		}

		if (mostRecentAppDirName == null) {
			throw new FileNotFoundException(appRepo.getCanonicalPath()
					+ "/UnivMobile-(lastimport).app, requiredAppCommitId: "
					+ requiredAppCommitId);
		}

		final String mostRecentAppPath = appRepo.getCanonicalPath() + "/"
				+ mostRecentAppDirName;

		System.out.println("Found most recent: " + mostRecentAppPath);

		final File mostRecentApp = new File(mostRecentAppPath);

		final String HOME = System.getenv("HOME");

		final String appPath = HOME + "/tmp/UnivMobile.app";

		final File appDest = new File(appPath);

		if (appDest.exists()) {

			FileUtils.deleteDirectory(appDest);
		}

		// 4. COPY APP FILES

		System.out.println("Copying into: " + appPath + "...");

		FileUtils.copyDirectory(mostRecentApp, appDest);

		// 6. UPDATE "UNMJsonBaseURL"

		EnvironmentUtils
				.exec(new File("/usr/libexec/Plistbuddy"),
						"-c",
						"Set UNMJsonBaseURL 'https://univmobile-dev.univ-paris1.fr/json/'",
						appPath + "/Info.plist");

		// 9. END

		return appPath;
	}

	@Nullable
	private static String readAppCommitId(final File appDir) throws IOException {

		final Executor executor = new DefaultExecutor();

		final ByteArrayOutputStream bos = new ByteArrayOutputStream();

		executor.setStreamHandler(new PumpStreamHandler(bos, null));

		// $ /usr/libexec/PlistBuddy -c Print Info.plist

		executor.execute(new CommandLine(new File("/usr/libexec/PlistBuddy"))
				.addArgument("-c").addArgument("Print")
				.addArgument(new File(appDir, "Info.plist").getCanonicalPath()));

		final String output = bos.toString(UTF_8);

		for (final String line : split(output, "\r\n")) {

			if (line.contains("GIT_COMMIT ")) {

				return substringAfter(line, "=").trim();
			}
		}

		return null;
	}

	public static String getCurrentPlatformName() {

		return "iOS";
	}

	private static String currentDeviceName = "iPhone Retina (4-inch)";

	public static void setCurrentDeviceName(final String deviceName) {

		currentDeviceName = checkNotNull(deviceName, "deviceName");
	}

	/**
	 * defaults to ""iPhone Retina (4-inch)"
	 */
	public static String getCurrentDeviceName() {

		return currentDeviceName;
	}
}
