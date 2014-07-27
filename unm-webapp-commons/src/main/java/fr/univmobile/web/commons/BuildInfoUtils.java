package fr.univmobile.web.commons;

import static fr.univmobile.web.commons.DataBeans.instantiate;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletContext;

public abstract class BuildInfoUtils {

	public static BuildInfo loadBuildInfo(final ServletContext servletContext)
			throws IOException {

		final Properties properties = new Properties();

		final InputStream is = servletContext
				.getResourceAsStream("META-INF/MANIFEST.MF");
		try {

			properties.load(is);

		} finally {
			is.close();
		}

		return instantiate(BuildInfo.class)
				.setBuildDisplayName(
						properties.getProperty("Buildinfo-BuildDisplayName"))
				.setBuildId(properties.getProperty("Buildinfo-BuildId"))
				.setGitCommitId(properties.getProperty("Buildinfo-Rev"));
	}
}

interface BuildInfo {

	String getBuildDisplayName();

	BuildInfo setBuildDisplayName(String buildDisplayName);

	String getBuildId();

	BuildInfo setBuildId(String buildId);

	String getGitCommitId();

	BuildInfo setGitCommitId(String gitCommitId);
}