package fr.univmobile.it.commons;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.StringUtils.substringAfter;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.lang3.RandomUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;

import com.avcompris.lang.NotImplementedException;
import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleniumException;

abstract class SeleniumWebDriverUtils {

	/**
	 * sugar for: {@link RemoteWebDriver#getScreenshotAs(OutputType)}, but also
	 * recognize if there is an underlying {@link Selenium} object and use if if
	 * it is the case.
	 * 
	 * @return The file created for the screenshot.
	 */
	public static File getScreenshotAsFile(final WebDriver driver)
			throws IOException {

		checkNotNull(driver, "driver");

		if (driver instanceof RemoteWebDriver) {

			return ((RemoteWebDriver) driver).getScreenshotAs(OutputType.FILE);
		}

		if (driver instanceof SeleniumBackedWebDriver) {

			final String KWARGS = "";

			final File file = new File("target", "screenshot_"
					+ System.currentTimeMillis() + "_"
					+ RandomUtils.nextInt(1000000, 99999999) + ".png");

			final String path = file.getCanonicalPath();

			final Selenium selenium = getWrappedSelenium(driver);

			try {

				selenium.captureEntirePageScreenshot(path, KWARGS);

			} catch (final SeleniumException e) {

				e.printStackTrace();

				System.err.println( //
						"Error while capturing entirePageScreenshot: " + path
								+ ", trying to capture regular screenshot.");

				selenium.captureScreenshot(path);
			}

			return file;
		}

		throw new NotImplementedException();
	}

	public static Selenium getWrappedSelenium(@Nullable final WebDriver driver) {

		if (driver == null) {

			return null;
		}

		if (driver instanceof SeleniumBackedWebDriver) {

			return ((SeleniumBackedWebDriver) driver).getWrappedSelenium();
		}

		throw new IllegalStateException(
				"webDriver is not a wrapped instance of Selenium");
	}

	public static WebDriver wrapToWebDriver(final Selenium selenium) {

		return new SeleniumBackedWebDriver(selenium);
	}

	public static String getId(final WebElement element) {

		checkNotNull(element, "element");

		if (element instanceof RemoteWebElement) {

			return ((RemoteWebElement) element).getId();
		}

		if (element instanceof SeleniumBackedWebDriver.SeleniumBackedWebElement) {

			return ((SeleniumBackedWebDriver.SeleniumBackedWebElement) element)
					.getId();
		}

		throw new IllegalStateException(
				"webElement is not a wrapped instance of Selenium");
	}

	private static class SeleniumBackedWebDriver implements WebDriver {

		public SeleniumBackedWebDriver(final Selenium selenium) {

			this.selenium = checkNotNull(selenium, "selenium");
		}

		private final Selenium selenium;

		@Override
		public void get(final String url) {
			throw new NotImplementedException();
		}

		@Override
		public String getCurrentUrl() {
			throw new NotImplementedException();
		}

		@Override
		public String getTitle() {
			throw new NotImplementedException();
		}

		@Override
		public List<WebElement> findElements(final By by) {
			throw new NotImplementedException();
		}

		@Override
		public WebElement findElement(final By by) {

			return new SeleniumBackedWebElement(by);
		}

		@Override
		public String getPageSource() {

			return selenium.getHtmlSource();
		}

		@Override
		public void close() {
			throw new NotImplementedException();
		}

		@Override
		public void quit() {
			selenium.stop();
		}

		@Override
		public Set<String> getWindowHandles() {
			throw new NotImplementedException();
		}

		@Override
		public String getWindowHandle() {
			throw new NotImplementedException();
		}

		@Override
		public TargetLocator switchTo() {
			throw new NotImplementedException();
		}

		@Override
		public Navigation navigate() {
			throw new NotImplementedException();
		}

		@Override
		public Options manage() {
			throw new NotImplementedException();
		}

		public Selenium getWrappedSelenium() {

			return selenium;
		}

		private static String calcLocator(final By by) {

			checkNotNull(by, "by");

			final String s = by.toString();

			if (s.startsWith("By.id: ")) {

				final String id = substringAfter(s, ": ");

				return "id=" + id;
			}

			throw new NotImplementedException();
		}

		private static String calcId(final By by) {

			checkNotNull(by, "by");

			final String s = by.toString();

			if (s.startsWith("By.id: ")) {

				final String id = substringAfter(s, ": ");

				return id;
			}

			throw new NotImplementedException("By: " + by);
		}

		public class SeleniumBackedWebElement implements WebElement {

			public SeleniumBackedWebElement(final By by) {

				this.by = checkNotNull(by, "by");

				locator = calcLocator(by);
			}

			private final By by;
			private final String locator;

			@Override
			public void click() {

				if (!selenium.isElementPresent(locator)) {
					throw new SeleniumException("click(): element[" + locator
							+ "] is not present");
				}

				final String script = "window.document.getElementById('"
						+ calcId(by) + "').click();";

				selenium.getEval(script);
			}

			@Override
			public void submit() {
				throw new NotImplementedException();
			}

			@Override
			public void sendKeys(final CharSequence... keysToSend) {

				final String value = keysToSend[0].toString();

				selenium.type(locator, value);
			}

			@Override
			public void clear() {
				throw new NotImplementedException();
			}

			@Override
			public String getTagName() {
				throw new NotImplementedException();
			}

			@Override
			public String getAttribute(final String name) {

				if (!selenium.isElementPresent(locator)) {
					throw new SeleniumException("getAttribute(): element["
							+ locator + "] is not present");
				}

				final String script = "window.document.getElementById('"
						+ calcId(by) + "').getAttribute('" + name + "');";

				return selenium.getEval(script);
			}

			@Override
			public boolean isSelected() {
				throw new NotImplementedException();
			}

			@Override
			public boolean isEnabled() {
				throw new NotImplementedException();
			}

			@Override
			public String getText() {
				return selenium.getText(locator);
			}

			@Override
			public List<WebElement> findElements(final By by) {
				throw new NotImplementedException();
			}

			@Override
			public WebElement findElement(final By by) {
				throw new NotImplementedException();
			}

			@Override
			public boolean isDisplayed() {

				if (!selenium.isElementPresent(locator)) {
					return false;
				}

				return selenium.isVisible(locator);
			}

			@Override
			public Point getLocation() {
				throw new NotImplementedException();
			}

			@Override
			public Dimension getSize() {
				throw new NotImplementedException();
			}

			@Override
			public String getCssValue(final String propertyName) {
				throw new NotImplementedException();
			}

			public String getId() {
				return selenium.getAttribute(locator + "@id");
			}
		}
	}
}
