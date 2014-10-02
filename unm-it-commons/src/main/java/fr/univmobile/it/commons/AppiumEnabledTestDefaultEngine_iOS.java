package fr.univmobile.it.commons;

import static fr.univmobile.it.commons.AppiumCapabilityType.APP;
import static fr.univmobile.it.commons.AppiumCapabilityType.DEVICE;
import static fr.univmobile.it.commons.AppiumCapabilityType.DEVICE_NAME;
import static fr.univmobile.it.commons.AppiumCapabilityType.PLATFORM_NAME;
import static fr.univmobile.it.commons.AppiumCapabilityType.PLATFORM_VERSION;
import static org.openqa.selenium.remote.CapabilityType.BROWSER_NAME;
import static org.openqa.selenium.remote.CapabilityType.PLATFORM;
import io.appium.java_client.AppiumDriver;

import java.io.File;
import java.net.URL;

import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import fr.univmobile.testutil.PropertiesUtils;

final class AppiumEnabledTestDefaultEngine_iOS extends
		WebDriverEnabledTestDefaultEngine {

	private static File app; // the "UnivMobile.app" local directory

	public AppiumEnabledTestDefaultEngine_iOS(final boolean useSafari) {

		this.useSafari = useSafari;

		setCurrentPlatformName("iOS");
	}

	private final boolean useSafari;

	@Before
	@Override
	public void setUp() throws Exception {

		@Nullable
		final String requiredAppCommitId = System.getProperty("appCommitId");

		WebDriver driver = getDriver();

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

		System.out.println("Setting capability: location services...");

		capabilities.setCapability("useLocationServices", true); // doesn’t work
		capabilities.setCapability("locationServicesEnabled", true); // sauceLabs

		final File clients_plist_source = new File(
				PropertiesUtils.getTestProperty("clients_plist_source"));
		final File clients_plist_target = new File(
				PropertiesUtils.getTestProperty("clients_plist_target"));
		System.out.println("clients_plist_source: "
				+ clients_plist_source.getCanonicalPath());
		System.out.println("clients_plist_target: "
				+ clients_plist_target.getCanonicalPath());
		FileUtils.copyFile(clients_plist_source, clients_plist_target);

		// System.out.println("DEBUG: new AppiumDriver()...");

		driver = new AppiumDriver(new URL("http://localhost:4723/wd/hub"),
				capabilities);

		setDriver(driver);
	}
}
