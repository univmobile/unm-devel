package fr.univmobile.testutil;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.avcompris.binding.annotation.Namespaces;
import net.avcompris.binding.annotation.XPath;
import net.avcompris.binding.dom.helper.DomBinderUtils;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

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
		System.out.println(fallbackValue);

		return fallbackValue;
	}

	/**
	 * Get the property located in the local
	 * Maven Settings File (~/.m2/settings.xml) by a XPath
	 * expression, which is the
	 * value of a property in src/test/resources/test.properties
	 * filtered by Maven according to active
	 * profile(s).
	 * <p>
	 * This method is implemented as follows:
	 * <ol>
	 * <li> Generally, the <code>refPropertyName</code> parameter is a name
	 * ending with “.ref”. e.g. “mysql.password.ref”.
	 * <li> Read the property from the test.properties file. Usually, this
	 * property values in this file are filtered out by Maven,
	 * taking into account the active profile and the current POM file.
	 * <li> The (filtered) property value must be a valid XPath expression.
	 * e.g. “/settings/servers/server[id = 'mysql']/password”
	 * <li> Evaluate the XPath expression against the
	 * local Maven Settings File (~/.m2/settings.xml). 
	 * e.g. with a value of “/settings/servers/server[id = 'mysql']/password”,
	 * the evaluation would give the local password  
	 * </ol>
	 */
	public static String getSettingsTestRefProperty(final String refPropertyName)
			throws IOException, ParserConfigurationException, SAXException,
			XPathExpressionException {

		return getSettingsTestRefProperty(refPropertyName,
				new File(System.getProperty("user.home"), ".m2/settings.xml"));
	}

	static String getSettingsTestRefProperty(final String refPropertyName,
			final File settingsFile) throws IOException,
			ParserConfigurationException, SAXException,
			XPathExpressionException {

		final String xpathExpression = getTestProperty(refPropertyName);

		final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
				.newInstance();
		final DocumentBuilder documentBuilder = documentBuilderFactory
				.newDocumentBuilder();
		final Document document = documentBuilder.parse(settingsFile);
		final XPathFactory xpathFactory = XPathFactory.newInstance();
		final javax.xml.xpath.XPath xpath = xpathFactory.newXPath();
		final XPathExpression xe = xpath.compile(xpathExpression);

		final String value = xe.evaluate(document);

		if (isBlank(value)) {
			throw new NullPointerException("Cannot evaluate XPath expression: "
					+ xpathExpression + " on file: "
					+ settingsFile.getCanonicalPath());
		}

		return value;
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
