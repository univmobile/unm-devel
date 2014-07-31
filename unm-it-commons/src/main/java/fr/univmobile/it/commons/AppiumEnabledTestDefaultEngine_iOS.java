package fr.univmobile.it.commons;

import static fr.univmobile.it.commons.AppiumCapabilityType.APP;
import static fr.univmobile.it.commons.AppiumCapabilityType.DEVICE;
import static fr.univmobile.it.commons.AppiumCapabilityType.DEVICE_NAME;
import static fr.univmobile.it.commons.AppiumCapabilityType.PLATFORM_NAME;
import static fr.univmobile.it.commons.AppiumCapabilityType.PLATFORM_VERSION;
import static org.openqa.selenium.remote.CapabilityType.BROWSER_NAME;
import static org.openqa.selenium.remote.CapabilityType.PLATFORM;
import fr.univmobile.testutil.PropertiesUtils;
import io.appium.java_client.AppiumDriver;

import java.io.File;
import java.net.URL;

import javax.annotation.Nullable;

import org.junit.Before;
import org.openqa.selenium.remote.DesiredCapabilities;

final class AppiumEnabledTestDefaultEngine_iOS extends
		AppiumEnabledTestDefaultEngine {

	private static File app; // the "UnivMobile.app" local directory

	public AppiumEnabledTestDefaultEngine_iOS(final boolean useSafari) {

		this.useSafari = useSafari;
	}

	private final boolean useSafari;

	@Before
	@Override
	public void setUp() throws Exception {

		@Nullable
		final String requiredAppCommitId = System.getProperty("appCommitId");

		AppiumDriver driver = getDriver();

		if (driver != null) {

			// System.out.println("DEBUG: ??? driver != null => driver.quit()...");

			driver.quit();

			driver = null;
		}

		// 1. LAUNCH THE iOS APP

		// final String BUNDLE_ID = "fr.univmobile.UnivMobile";

		if (app == null && !useSafari) {

			final String appPath;

			final String appPathProperty = PropertiesUtils
					.getTestProperty("AppPath");

			if (!appPathProperty.contains("UnivMobile-(lastimport).app")) {

				appPath = appPathProperty;

			} else {

				// e.g.
				// "/var/xcodebuild_test-apps/UnivMobile-20140712-090711.app"

				appPath = IOSUtils.getMostRecentAppPath(appPathProperty,
						requiredAppCommitId);
			}

			app = new File(appPath);

			System.out.println("Using: " + app.getCanonicalPath());
		}

		final DesiredCapabilities capabilities = new DesiredCapabilities();

		capabilities.setCapability(BROWSER_NAME, useSafari ? "Safari" : "iOS");

		capabilities.setCapability(PLATFORM, "Mac");
		
		final String platformName = getCurrentPlatformName();
		
		capabilities.setCapability(PLATFORM_NAME, platformName);
		capabilities.setCapability(PLATFORM_VERSION,
				EnvironmentUtils.getCurrentPlatformVersion("iOS"));

		capabilities.setCapability(DEVICE, "iPhone Simulator");
		capabilities.setCapability(DEVICE_NAME, getCurrentDeviceName());

		if (!useSafari) {
			capabilities.setCapability(APP, app.getAbsolutePath());
		}

		// System.out.println("DEBUG: new AppiumDriver()...");

		driver = new AppiumDriver(new URL("http://localhost:4723/wd/hub"),
				capabilities);

		setDriver(driver);
	}
}
