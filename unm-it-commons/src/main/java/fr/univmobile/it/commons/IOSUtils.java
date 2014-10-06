package fr.univmobile.it.commons;

import static com.google.common.base.Preconditions.checkNotNull;
import static fr.univmobile.it.commons.EnvironmentUtils.exec;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBeforeLast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import fr.univmobile.testutil.PropertiesUtils;

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

		final String jsonBaseURL = PropertiesUtils
				.getTestProperty("jsonBaseURL");

		exec( //
		new File("/usr/libexec/PlistBuddy"), //
				"-c", "Set UNMJsonBaseURL '" + jsonBaseURL + "'", //
				new File(appDest, "Info.plist").getCanonicalPath());

		// 9. END

		return appPath;
	}

	@Nullable
	private static String readAppCommitId(final File appDir) throws IOException {

		final String output = exec(new File("/usr/libexec/PlistBuddy"), "-c",
				"Print", new File(appDir, "Info.plist").getCanonicalPath());

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
