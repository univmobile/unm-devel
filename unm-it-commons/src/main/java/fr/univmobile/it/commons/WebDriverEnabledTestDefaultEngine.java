package fr.univmobile.it.commons;

import static com.google.common.base.Preconditions.checkNotNull;
import static fr.univmobile.it.commons.SeleniumWebDriverUtils.getScreenshotAsFile;
import static org.apache.commons.lang3.CharEncoding.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

abstract class WebDriverEnabledTestDefaultEngine implements
		WebDriverEnabledTestEngine {

	protected final void setDriver(final WebDriver driver) {

		this.driver = checkNotNull(driver, "driver");
	}

	@After
	@Override
	public final void tearDown() throws Exception {

		cleanExistingFuture();

		if (driver != null) {

			// System.out.println("DEBUG: driver.quit()...");

			driver.quit();

			driver = null;
		}
	}

	private WebDriver driver;

	@Override
	public final void takeScreenshot(final String filename) throws IOException {

		System.out.println("Taking screenshot: " + filename + "...");

		// final WebDriver augmentedDriver = new Augmenter().augment(driver);

		final File srcFile = // ((TakesScreenshot) augmentedDriver)
		getScreenshotAsFile(driver);

		final File destFile = new File(new File("target", "screenshots"),
				filename);

		FileUtils.forceMkdir(destFile.getParentFile());

		FileUtils.copyFile(srcFile, destFile, true);

		srcFile.delete();
	}

	@Override
	public final void swipe(final int startX, final int startY, final int endX,
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
	public final void savePageSource(final String filename) throws IOException {

		System.out.println("Saving pageSource: " + filename + "...");

		final String xml = driver.getPageSource();

		final File file = new File(new File("target", "screenshots"), filename);

		FileUtils.forceMkdir(file.getParentFile());

		FileUtils.write(file, xml, UTF_8);
	}

	@Override
	public final WebElement findElementById(final String id) throws IOException {

		try {

			return driver.findElement(By.id(id));

		} catch (final NoSuchElementException e) {

			throw new NoSuchElementException("Could not find element for id: "
					+ id, e);
		}
	}

	@Override
	public final WebElement findElementByName(final String name)
			throws IOException {

		try {

			return (WebElement) driver.findElement(By.name(name));

		} catch (final NoSuchElementException e) {

			throw new NoSuchElementException(
					"Could not find element for name: " + name, e);
		}
	}

	@Override
	public final void waitForElementById(final int seconds, final String id)
			throws IOException {

		new WebDriverWait(getDriver(), seconds).until(ExpectedConditions
				.presenceOfElementLocated(By.id(id)));
	}

	@Override
	public final void get(final String url) throws IOException {

		getDriver().get(url);
	}

	@Override
	public final ElementChecker elementById(final String id) throws IOException {

		return new WebElementChecker(id, findElementById(id));
	}

	@Override
	public final ElementChecker elementByName(final String name)
			throws IOException {

		return new WebElementChecker("name=" + name, findElementById(name));
	}

	@Override
	@Nullable
	public final WebDriver getDriver() {

		return driver;
	}

	@Override
	public final void pause(final int ms) throws InterruptedException {

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
	public final void futureScreenshot(final int ms, final String filename)
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

		if (currentPlatformName == null) {
			throw new IllegalStateException("currentPlatformName is null");
		}

		return currentPlatformName;
	}

	public static void setCurrentPlatformName(final String currentPlatformName) {

		WebDriverEnabledTestDefaultEngine.currentPlatformName = checkNotNull(
				currentPlatformName, "currentPlatformName");
	}

	/**
	 * e.g. "iOS"
	 */
	private static String currentPlatformName;

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
	public void textShouldContain(final String ref) {

		assertTrue(id + ".text <= " + ref, element.getText().contains(ref));
	}

	@Override
	public void textShouldNotContain(final String ref) {

		assertFalse(id + ".text !<= " + ref, element.getText().contains(ref));
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

	@Override
	public String attr(final String attrName) {

		return element.getAttribute(attrName);
	}
}