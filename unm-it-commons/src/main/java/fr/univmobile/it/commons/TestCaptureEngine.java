package fr.univmobile.it.commons;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.CharEncoding.UTF_8;
import static org.apache.commons.lang3.StringUtils.normalizeSpace;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.openqa.selenium.WebElement;

final class TestCaptureEngine extends TestPhasedEngine {

	public TestCaptureEngine(final String platformName, final boolean useSafari) {

		checkNotNull(platformName, "platformName");

		WebDriverEnabledTestDefaultEngine.setCurrentPlatformName(platformName);

		if ("iOS".equals(platformName)) {

			defaultEngine = new AppiumEnabledTestDefaultEngine_iOS(useSafari);

		} else if ("Android".equals(platformName)) {

			defaultEngine = new AppiumEnabledTestDefaultEngine_Android();

		} else if ("Mac OS X".equals(platformName)
				|| "Debian".equals(platformName)) {

			defaultEngine = new SeleniumEnabledTestDefaultEngine("*firefox");

		} else {

			throw new IllegalStateException("Unknown platformName: "
					+ platformName);
		}
	}

	private final WebDriverEnabledTestEngine defaultEngine;

	@Override
	public void setUp() throws Exception {

		defaultEngine.setUp();

		final File file = new File("target/screenshots",
				customizeFilename("capture.log"));

		FileUtils.forceMkdir(file.getParentFile());

		os = new FileOutputStream(file);

		pw = new PrintWriter(new OutputStreamWriter(os, UTF_8));

		pw.println("# " + getScenarioId());
		pw.println("# " + new DateTime());

		pw.println();
	}

	private OutputStream os;
	private PrintWriter pw;

	@Override
	public void tearDown() throws Exception {

		try {

			pw.flush();

			os.close();

		} finally {

			defaultEngine.tearDown();
		}
	}

	@Override
	public void takeScreenshot(final String filename) throws IOException {

		defaultEngine.takeScreenshot(customizeFilename(filename));
	}

	@Override
	public void savePageSource(final String filename) throws IOException {

		defaultEngine.savePageSource(customizeFilename(filename));
	}

	@Override
	public void swipe(final int startX, final int startY, final int endX,
			final int endY, final int durationMs) throws IOException {

		defaultEngine.swipe(startX, startY, endX, endY, durationMs);
	}

	@Override
	public ElementChecker elementById(final String id) throws IOException {

		return new WebElementCapturer(pw, id, defaultEngine.findElementById(id));
	}

	@Override
	public ElementChecker elementByName(final String name) throws IOException {

		return new WebElementCapturer(pw, "name=" + name,
				defaultEngine.findElementByName(name));
	}

	@Override
	public final void waitForElementById(final int seconds, final String id)
			throws IOException {

		defaultEngine.waitForElementById(seconds, id);
	}

	@Override
	public void get(final String url) throws IOException {

		defaultEngine.get(url);
	}

	@Override
	public String getSimpleName() {

		return "capture";
	}

	/*
	 * @Override public void clearErrors() {
	 * 
	 * // do nothing }
	 */

	@Override
	public boolean hasErrors() {

		return false;
	}

	@Override
	public final void pause(final int ms) throws InterruptedException {

		defaultEngine.pause(ms);
	}

	@Override
	public final void futureScreenshot(final int ms, final String filename)
			throws IOException {

		defaultEngine.futureScreenshot(ms, customizeFilename(filename));
	}
}

final class WebElementCapturer implements ElementChecker {

	public WebElementCapturer(final PrintWriter pw, final String id,
			final WebElement element) {

		this.id = checkNotNull(id, "id");
		this.element = checkNotNull(element, "element");
		this.pw = checkNotNull(pw, "pw");

		pw.println(id + ":");
		pw.println("  text: " + normalizeSpace(element.getText()));
		pw.println("  visible: " + element.isDisplayed());
		pw.println("  id: " + SeleniumWebDriverUtils.getId(element));

		pw.println();
	}

	private final String id;
	private final WebElement element;
	private final PrintWriter pw;

	@Override
	public void textShouldEqualTo(@Nullable final String ref)
			throws IOException {

		pw.println("# " + id + ".text.shouldEqualTo: " + ref);

		if (ref == null) {
			return; // swallow the NPE
		}

		final String text = element.getText();

		if (!ref.equals(text)) {
			message(id + ".text: expected: <" + ref + ">, but was: <" + text
					+ ">");
		}

		pw.println();
	}

	@Override
	public void textShouldContain(@Nullable final String ref)
			throws IOException {

		pw.println("# " + id + ".text.shouldContain: " + ref);

		if (ref == null) {
			return; // swallow the NPE
		}

		final String text = element.getText();

		if (!text.contains(ref)) {
			message(id + ".text: expected: <..." + ref + "...>, but was: <"
					+ text + ">");
		}

		pw.println();
	}

	@Override
	public void textShouldNotContain(@Nullable final String ref)
			throws IOException {

		pw.println("# " + id + ".text.shouldNotContain: " + ref);

		if (ref == null) {
			return; // swallow the NPE
		}

		final String text = element.getText();

		if (text.contains(ref)) {
			message(id + ".text: expected: !<" + ref + ">, but was: <" + text
					+ ">");
		}

		pw.println();
	}

	private void message(final String message) {

		final String m = "WARNING: " + normalizeSpace(message);

		System.err.println("** " + m);

		pw.println("# " + m);
	}

	@Override
	public void shouldBeVisible() {

		pw.println("# " + id + ".shouldBeVisible");

		if (!element.isDisplayed()) {
			message("!" + id + ".visible");
		}

		pw.println();
	}

	@Override
	public void shouldBeHidden() {

		pw.println("# " + id + ".shouldBeHidden");

		if (element.isDisplayed()) {
			message(id + ".visible");
		}

		pw.println();
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