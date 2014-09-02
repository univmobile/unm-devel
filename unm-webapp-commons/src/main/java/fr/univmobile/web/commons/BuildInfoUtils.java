package fr.univmobile.web.commons;

import static fr.univmobile.commons.DataBeans.instantiate;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletContext;

public abstract class BuildInfoUtils {

	public static BuildInfo loadBuildInfo(final ServletContext servletContext)
			throws IOException {

		final Properties properties = new Properties();

		final InputStream manifestIs = servletContext
				.getResourceAsStream("META-INF/MANIFEST.MF");

		final InputStream appIs = Thread.currentThread()
				.getContextClassLoader().getResourceAsStream("app.properties");

		if (manifestIs == null) {
			throw new FileNotFoundException("META-INF/MANIFEST.MF");
		}

		if (appIs == null) {
			throw new FileNotFoundException("app.properties");
		}

		try {

			properties.load(manifestIs);

			properties.load(appIs);

		} finally {
			manifestIs.close();
		}

		return instantiate(BuildInfo.class)
				.setAppVersion(properties.getProperty("version"))
				.setBuildDisplayName(
						properties.getProperty("Buildinfo-BuildDisplayName"))
				.setBuildId(properties.getProperty("Buildinfo-BuildId"))
				.setGitCommitId(properties.getProperty("Buildinfo-Rev"));
	}
}

interface BuildInfo {

	String getAppVersion();

	BuildInfo setAppVersion(String appVersion);

	String getBuildDisplayName();

	BuildInfo setBuildDisplayName(String buildDisplayName);

	String getBuildId();

	BuildInfo setBuildId(String buildId);

	String getGitCommitId();

	BuildInfo setGitCommitId(String gitCommitId);
}