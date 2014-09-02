package fr.univmobile.it.commons;

import static fr.univmobile.it.commons.AppiumCapabilityType.APP;
import static fr.univmobile.it.commons.AppiumCapabilityType.DEVICE;
import static fr.univmobile.it.commons.AppiumCapabilityType.DEVICE_NAME;
import static fr.univmobile.it.commons.AppiumCapabilityType.PLATFORM_NAME;
import static fr.univmobile.it.commons.AppiumCapabilityType.PLATFORM_VERSION;
import io.appium.java_client.AppiumDriver;

import java.io.File;
import java.net.URL;

import org.junit.Before;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

final class AppiumEnabledTestDefaultEngine_Android extends
		WebDriverEnabledTestDefaultEngine {

	private static final File app = // new File("apk/UnivMobile-debug.apk");
	// new File("target", "UnivMobile_localhost.apk");
	new File("apk/UnivMobile-release.apk");

	public AppiumEnabledTestDefaultEngine_Android() {

		setCurrentPlatformName("Android");
	}

	@Before
	@Override
	public void setUp() throws Exception {

		// @Nullable
		// final String requiredAppCommitId = System.getProperty("appCommitId");

		WebDriver driver = getDriver();

		if (driver != null) {

			// System.out.println("DEBUG: ??? driver != null => driver.quit()...");

			driver.quit();

			driver = null;
		}

		// 1. LAUNCH THE ANDROID APP

		System.out.println("Using: " + app.getCanonicalPath());

		final DesiredCapabilities capabilities = new DesiredCapabilities();

		capabilities.setCapability(APP, app.getAbsolutePath());

		capabilities.setCapability(PLATFORM_NAME, "Android");
		capabilities.setCapability(PLATFORM_VERSION,
				"4.4 KitKat (API Level 19)");

		capabilities.setCapability(DEVICE, "Android");
		capabilities.setCapability(DEVICE_NAME, "Android Emulator");

		driver = new AppiumDriver(new URL("http://localhost:4723/wd/hub"),
				capabilities);

		setDriver(driver);
	}
}
