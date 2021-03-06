package fr.univmobile.it.commons;

import static com.google.common.base.Preconditions.checkNotNull;
import static fr.univmobile.it.commons.SeleniumWebDriverUtils.getWrappedSelenium;
import static fr.univmobile.it.commons.SeleniumWebDriverUtils.wrapToWebDriver;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleniumException;

import fr.univmobile.testutil.PropertiesUtils;

final class SeleniumEnabledTestDefaultEngine extends
		WebDriverEnabledTestDefaultEngine {

	public SeleniumEnabledTestDefaultEngine(final String defaultBrowser) {

		this.defaultBrowser = checkNotNull(defaultBrowser, "defaultBrowser");
	}

	private final String defaultBrowser;

	@Override
	public void waitForElementById(final int seconds, final String id)
			throws IOException {

		final Selenium selenium = getWrappedSelenium(getDriver());

		final String timeout = Integer.toString(seconds * 1000);

		selenium.waitForPageToLoad(timeout);

		try {

			selenium.waitForCondition("window.document.getElementById('" + id
					+ "') != null", timeout);

		} catch (final SeleniumException e) {

			final String KWARGS = "";

			final String prefix = "crash_" + System.currentTimeMillis();

			final File file = new File("target", prefix + ".png");

			System.err.println("Capturing screenshot: " + file.getPath());

			selenium.captureEntirePageScreenshot(file.getCanonicalPath(),
					KWARGS);

			FileUtils.write(new File("target", prefix + ".html"),
					selenium.getHtmlSource());

			throw e;
		}
	}

	@Before
	@Override
	public void setUp() throws Exception {

		// @Nullable
		// final String requiredAppCommitId = System.getProperty("appCommitId");

		Selenium selenium = getWrappedSelenium(getDriver());

		if (selenium != null) {

			selenium.stop();

			selenium = null;
		}

		// 1. LAUNCH THE WEB APP

		final int seleniumPort = 8888;

		final String url = "http://localhost:"
				+ PropertiesUtils.getTestProperty("tomcat.port") + "/";

		System.out.println("Using browser: " + defaultBrowser + "...");

		selenium = new DefaultSelenium("localhost", seleniumPort,
				defaultBrowser, // "*firefox",
				url);

		selenium.start();

		selenium.getEval("window.resizeTo(1280, 960);");

		selenium.open("/unm-backend" //
				+ "?NO_SHIB_uid=tformica" //
				+ "&NO_SHIB_eppn=tformica@univ-paris1.fr" //
				+ "&NO_SHIB_displayName=Toto+Formica" //
				+ "&NO_SHIB_remoteUser=tformica@univ-paris1.fr" //
		);

		selenium.waitForPageToLoad(Integer.toString(60000));

		setDriver(wrapToWebDriver(selenium));
	}
}
