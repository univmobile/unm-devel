package fr.univmobile.it.commons;

import static com.google.common.base.Preconditions.checkNotNull;
import static fr.univmobile.it.commons.AppiumCapabilityType.APP;
import static fr.univmobile.it.commons.AppiumCapabilityType.DEVICE;
import static fr.univmobile.it.commons.AppiumCapabilityType.DEVICE_NAME;
import static fr.univmobile.it.commons.AppiumCapabilityType.PLATFORM_NAME;
import static fr.univmobile.it.commons.AppiumCapabilityType.PLATFORM_VERSION;
import static org.apache.commons.lang3.CharEncoding.UTF_8;
import static org.apache.commons.lang3.StringUtils.substringBeforeLast;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.openqa.selenium.remote.CapabilityType.BROWSER_NAME;
import static org.openqa.selenium.remote.CapabilityType.PLATFORM;
import io.appium.java_client.AppiumDriver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import fr.univmobile.testutil.PropertiesUtils;

final class AppiumEnabledTestDefaultEngine implements AppiumEnabledTestEngine {

	private static File app; // the "UnivMobile.app" local directory

	public AppiumEnabledTestDefaultEngine(final boolean useSafari) {

		this.useSafari = useSafari;
	}

	private final boolean useSafari;

	@Before
	@Override
	public void setUp() throws Exception {

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

				appPath = getMostRecentAppPah(appPathProperty);
			}

			app = new File(appPath);

			System.out.println("Using: " + app.getCanonicalPath());
		}

		final DesiredCapabilities capabilities = new DesiredCapabilities();

		capabilities.setCapability(BROWSER_NAME, useSafari ? "Safari" : "iOS");

		capabilities.setCapability(PLATFORM, "Mac");
		capabilities.setCapability(PLATFORM_NAME, getCurrentPlatformName());
		capabilities.setCapability(PLATFORM_VERSION,
				EnvironmentUtils.getCurrentPlatformVersion());

		capabilities.setCapability(DEVICE, "iPhone Simulator");
		capabilities.setCapability(DEVICE_NAME, getCurrentDeviceName());

		if (!useSafari) {
			capabilities.setCapability(APP, app.getAbsolutePath());
		}

		// System.out.println("DEBUG: new AppiumDriver()...");

		driver = new AppiumDriver(new URL("http://localhost:4723/wd/hub"),
				capabilities);
	}

	private static String getMostRecentAppPah(final String appPathProperty)
			throws FileNotFoundException, IOException {

		System.out.println("Using UnivMobile-(lastimport).app: "
				+ appPathProperty + "...");

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

			if (touchedAtAsString.compareTo(dirModifiedAtString) >= 0) {

				if (mostRecentAppDirName == null
						|| mostRecentAppDirName.compareTo(appDirName) < 0) {

					mostRecentAppDirName = appDirName;
				}
			}
		}

		if (mostRecentAppDirName == null) {
			throw new FileNotFoundException(appRepo.getCanonicalPath()
					+ "/UnivMobile-(lastimport).app");
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

		System.out.println("Copying into: " + appPath + "...");

		FileUtils.copyDirectory(mostRecentApp, appDest);

		return appPath;
	}

	@After
	@Override
	public void tearDown() throws Exception {

		cleanExistingFuture();

		if (driver != null) {

			// System.out.println("DEBUG: driver.quit()...");

			driver.quit();

			driver = null;
		}
	}

	private AppiumDriver driver;

	@Override
	public void takeScreenshot(final String filename) throws IOException {

		System.out.println("Taking screenshot: " + filename + "...");

		// final WebDriver augmentedDriver = new Augmenter().augment(driver);

		final File srcFile = // ((TakesScreenshot) augmentedDriver)
		driver.getScreenshotAs(OutputType.FILE);

		final File destFile = new File(new File("target", "screenshots"),
				filename);

		FileUtils.forceMkdir(destFile.getParentFile());

		FileUtils.copyFile(srcFile, destFile, true);

		srcFile.delete();
	}

	@Override
	public void swipe(final int startX, final int startY, final int endX,
			final int endY, final int durationMs) throws IOException {

		final JavascriptExecutor javascriptExecutor = (JavascriptExecutor) driver;

		final Map<String, Double> swipe = new HashMap<String, Double>();

		final double durationDouble = durationMs / 1000.0d;

		swipe.put("touchCount", 1d);
		swipe.put("startX", (double) startX);
		swipe.put("startY", (double) startY);
		swipe.put("endX", (double) endX);
		swipe.put("endY", (double) endY);
		swipe.put("duration", durationDouble);

		javascriptExecutor.executeScript("mobile: swipe", swipe);
	}

	@Override
	public void savePageSource(final String filename) throws IOException {

		System.out.println("Saving pageSource: " + filename + "...");

		final String xml = driver.getPageSource();

		final File file = new File(new File("target", "screenshots"), filename);

		FileUtils.forceMkdir(file.getParentFile());

		FileUtils.write(file, xml, UTF_8);
	}

	@Override
	public RemoteWebElement findElementById(final String id) throws IOException {

		try {

			return (RemoteWebElement) driver.findElementById(id);

		} catch (final NoSuchElementException e) {

			throw new NoSuchElementException("Could not find element for id: "
					+ id, e);
		}
	}

	@Override
	public RemoteWebElement findElementByName(final String name)
			throws IOException {

		try {

			return (RemoteWebElement) driver.findElementByName(name);

		} catch (final NoSuchElementException e) {

			throw new NoSuchElementException(
					"Could not find element for name: " + name, e);
		}
	}

	@Override
	public void waitForElementById(final int seconds, final String id)
			throws IOException {

		new WebDriverWait(getDriver(), seconds).until(ExpectedConditions
				.presenceOfElementLocated(By.id(id)));
	}

	@Override
	public ElementChecker elementById(final String id) throws IOException {

		return new WebElementChecker(id, findElementById(id));
	}

	@Override
	public ElementChecker elementByName(final String name) throws IOException {

		return new WebElementChecker("name=" + name, findElementById(name));
	}

	@Override
	public AppiumDriver getDriver() {

		return driver;
	}

	@Override
	public void pause(final int ms) throws InterruptedException {

		Thread.sleep(ms);
	}

	private Thread future;

	private void cleanExistingFuture() {

		if (future != null) {

			try {

				future.join(10000);

			} catch (final InterruptedException e) {
				// do nothing
			}

			if (future.isAlive()) {
				future.interrupt();
			}

			future = null;
		}
	}

	@Override
	public void futureScreenshot(final int ms, final String filename)
			throws IOException {

		cleanExistingFuture();

		future = new Thread() {

			@Override
			public void run() {

				try {

					Thread.sleep(ms);

					takeScreenshot(filename);

				} catch (final IOException e) {

					e.printStackTrace();

					// do nothing

				} catch (final InterruptedException e) {

					// do nothing
				}
			}
		};

		future.start();
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

final class WebElementChecker implements ElementChecker {

	public WebElementChecker(final String id, final WebElement element) {

		this.id = checkNotNull(id, "id");
		this.element = checkNotNull(element, "element");
	}

	private final String id;
	private final WebElement element;

	@Override
	public void textShouldEqualTo(final String ref) {

		assertEquals(id + ".text", ref, element.getText());
	}

	@Override
	public void shouldBeVisible() {

		assertTrue("!" + id + ".visible", element.isDisplayed());
	}

	@Override
	public void shouldBeHidden() {

		assertFalse(id + ".visible", element.isDisplayed());
	}

	@Override
	public void click() {

		element.click();
	}
}