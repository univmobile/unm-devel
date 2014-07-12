package fr.univmobile.testutil;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import net.avcompris.binding.annotation.Namespaces;
import net.avcompris.binding.annotation.XPath;
import net.avcompris.binding.dom.helper.DomBinderUtils;

public class PropertiesUtils {

	/**
	 * Get the property filtered by Maven according to active profile(s).
	 */
	public static String getTestProperty(final String propertyName)
			throws IOException {

		checkNotNull(propertyName, "propertyName");

		// 1. LOOK INTO THE TEST PROPERTIES FILE

		final String FILENAME = "test.properties";

		final Properties properties = new Properties();

		final InputStream is = // PropertiesUtils.class.getClassLoader()
				Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(FILENAME);

		if (is == null) {
			throw new FileNotFoundException("Cannot find property file: "
					+ FILENAME);
		}

		try {

			properties.load(is);

		} finally {
			is.close();
		}

		final String value = properties.getProperty(propertyName);

		if (value == null) {
			throw new NullPointerException("Cannot find property: "
					+ propertyName + " in property file: " + FILENAME);
		}

		if (value.contains("${")) {
			
			System.err.println("Property: " + propertyName + "=" + value
					+ " is not filtered in property file: " + FILENAME);
			
		} else {
			
			return value;
		}

		// 2. FALLBACK: POOR MAN SEARCH IN THE POM FILE ITSELF / ENV VARIABLES

		final String USER = System.getenv("USER");

		System.out.println("User: " + USER);

		final File pomFile = new File("pom.xml");

		final Pom pom = DomBinderUtils.xmlContentToJava(pomFile, Pom.class);

		if (pom.isNullProfileById(USER)) {
			throw new NullPointerException("Cannot find profile: " + USER
					+ " in POM file: " + pomFile.getName());
		}

		final Pom.Profile profile = pom.getProfileById(USER);

		if (profile.isNullProperty(propertyName)) {
			throw new NullPointerException("Cannot find property: "
					+ propertyName + " in POM file: " + pomFile.getName()
					+ " for profile: " + USER);
		}

		final String fallbackValue = profile.getProperty(propertyName);

		System.err.println("Found fallback property: " + propertyName
				+ " in POM file: " + pomFile.getName() + " for profile: "
				+ USER + ":");
		System.out.println( fallbackValue);

		return fallbackValue;
	}

	@XPath("/pom:project")
	@Namespaces("xmlns:pom=http://maven.apache.org/POM/4.0.0")
	private interface Pom {

		@XPath("pom:profiles/pom:profile[pom:id = $arg0]")
		Profile getProfileById(String id);

		boolean isNullProfileById(String id);

		interface Profile {

			@XPath(value = "pom:properties/*[name() = $arg0]", //
			function = "normalize-space()")
			String getProperty(String name);

			boolean isNullProperty(String name);
		}
	}
}
