package fr.univmobile.it.commons;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;

import javax.annotation.Nullable;

import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public abstract class WebDriverEnabledTest implements
		WebDriverEnabledTestEngine {

	@Override
	public final WebDriver getDriver() {

		return checkedEngine().getDriver();
	}

	private WebDriverEnabledTestEngine checkedEngine() {

		if (engine == null) {
			throw new IllegalStateException("engine == null");
		}

		return engine;
	}

	@Nullable
	private WebDriverEnabledTestEngine engine;

	final void setEngine(final WebDriverEnabledTestEngine engine) {

		this.engine = checkNotNull(engine, "engine");
	}

	abstract WebDriverEnabledTestEngine newEngine(boolean useSafari);

	@Before
	@Override
	public final void setUp() throws Exception {

		if (engine == null) {

			engine = newEngine(AppiumSafariEnabledTest.class
					.isAssignableFrom(this.getClass()));
		}

		checkedEngine().setUp();
	}

	@After
	@Override
	public final void tearDown() throws Exception {

		checkedEngine().tearDown();
	}

	@Override
	public final void takeScreenshot(final String filename) throws IOException {

		checkedEngine().takeScreenshot(filename);
	}

	private WebDriverEnabledTest checkUseSafari() {

		if (!AppiumSafariEnabledTest.class.isAssignableFrom(this.getClass())) {
			throw new IllegalStateException(
					"Call this method only from an AppiumSafariEnabledTest subclass.");
		}

		return this;
	}

	@Override
	public final void waitForElementById(final int seconds, final String id)
			throws IOException {

		checkUseSafari().checkedEngine().waitForElementById(seconds, id);
	}

	@Override
	public final void get(final String url) throws IOException {

		checkUseSafari().checkedEngine().get(url);
	}

	@Override
	public final void swipe(final int startX, final int startY, final int endX,
			final int endY, final int durationMs) throws IOException {

		checkedEngine().swipe(startX, startY, endX, endY, durationMs);
	}

	@Override
	public final void savePageSource(final String filename) throws IOException {

		checkedEngine().savePageSource(filename);
	}

	@Override
	public final WebElement findElementById(final String id) throws IOException {

		return checkedEngine().findElementById(id);
	}

	@Override
	public final WebElement findElementByName(final String name)
			throws IOException {

		return checkedEngine().findElementByName(name);
	}

	@Override
	public final ElementChecker elementById(final String id) throws IOException {

		return checkedEngine().elementById(id);
	}

	@Override
	public final ElementChecker elementByName(final String name)
			throws IOException {

		return checkedEngine().elementByName(name);
	}

	@Override
	public final void pause(final int ms) throws InterruptedException {

		checkedEngine().pause(ms);
	}

	@Override
	public final void futureScreenshot(final int ms, final String filename)
			throws IOException {

		checkedEngine().futureScreenshot(ms, filename);
	}
}
