package fr.univmobile.it.commons;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.CharEncoding.UTF_8;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBetween;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;

public class TestCheckerEngine extends TestPhasedEngine {

	private final ElementCheckObserverStdout stdoutObserver = new ElementCheckObserverStdout();

	private final ElementCheckObserverXMLDump xmlDumpObserver = new ElementCheckObserverXMLDump();

	private final ElementCheckObserver observer = new ElementCheckObserverComposite(
			stdoutObserver, xmlDumpObserver);

	@Override
	public void setUp() throws Exception {

		final File logFile = new File("target/screenshots",
				customizeFilename("capture.log"));

		logLines = FileUtils.readLines(logFile, UTF_8).iterator();

		stdoutObserver.clearErrors();

		final File xmlOutFile = new File("target/screenshots",
				customizeFilename("checked.xml"));

		xmlDumpObserver.init(getDeviceName(), //
				getScenariosClass(), getScenarioMethod(), //
				xmlOutFile);
	}

	@Nullable
	private Iterator<String> logLines;

	@Override
	public void tearDown() throws Exception {

		xmlDumpObserver.close();
	}

	@Override
	public void takeScreenshot(final String filename) throws IOException {

		observer.notifyScreenshot(filename);
	}

	@Override
	public void savePageSource(final String filename) throws IOException {

		// do nothing: Rely on previous capture
	}

	@Override
	public final void pause(final int ms) throws InterruptedException {

		// do nothing
	}

	@Override
	public final void waitForElementById(final int seconds, final String id)
			throws IOException {

		// do nothing
	}

	@Override
	public void get(final String url) throws IOException {

		// do nothing
	}

	@Override
	public final void futureScreenshot(final int ms, final String filename)
			throws IOException {

		observer.notifyScreenshot(filename, ms);
	}

	@Override
	public void swipe(final int startX, final int startY, final int endX,
			final int endY, final int durationMs) throws IOException {

		observer.notifyAction("swipe");
	}

	@Override
	public ElementCheckerWithAttributes elementById(final String id)
			throws IOException {

		final String lineId;

		while (logLines.hasNext()) {

			final String line = logLines.next();

			if (isBlank(line) || line.startsWith("#")) {
				continue;
			}

			lineId = line.substring(0, line.length() - 1);

			if (!lineId.equals(id)) {
				throw new IOException("Expected id: <" + id + ">, but was: <"
						+ lineId + ">");
			}

			break;
		}

		final ElementCheckerWithAttributes element = new ElementCheckerWithAttributes(
				observer, id);

		while (logLines.hasNext()) {

			final String line = logLines.next();

			if (isBlank(line) || line.startsWith("#") || !line.startsWith("  ")) {
				break;
			}

			final String attributeName = substringBetween(line, "  ", ":");

			final String attributeValue = substringAfter(line, ": ");

			element.addAttribute(attributeName, attributeValue);
		}

		return element;
	}

	@Override
	public ElementCheckerWithAttributes elementByXPath(final String xpath)
			throws IOException {

		return elementById("xpath=" + xpath);
	}

	@Override
	public ElementCheckerWithAttributes elementByName(final String name)
			throws IOException {

		return elementById("name=" + name);
	}

	@Override
	public String getSimpleName() {

		return "checker";
	}

	@Override
	public boolean hasErrors() {

		return stdoutObserver.hasErrors();
	}

	private class ElementCheckerWithAttributes implements ElementChecker {

		public ElementCheckerWithAttributes(
				final ElementCheckObserver observer, final String id) {

			this.observer = checkNotNull(observer, "observer");
			this.id = checkNotNull(id, "id");
		}

		private final ElementCheckObserver observer;
		private final String id;

		public void addAttribute(final String name, final String value) {

			attributes.put(name, value);
		}

		private final Map<String, String> attributes = new HashMap<String, String>();

		@Override
		public void textShouldEqualTo(final String ref) throws IOException {

			final String text = attributes.get("text");

			observer.notifyCheck(id + ".text.shouldEqualTo: " + ref,
					ref.equals(text), "expected: <" + ref + ">, but was: <"
							+ text + ">");
		}

		@Override
		public void textShouldContain(final String ref) throws IOException {

			final String text = attributes.get("text");

			observer.notifyCheck(id + ".text.shouldContain: " + ref,
					text.contains(ref), "expected: <..." + ref
							+ "...>, but was: <" + text + ">");
		}

		@Override
		public void textShouldNotContain(final String ref) throws IOException {

			final String text = attributes.get("text");

			observer.notifyCheck(id + ".text.shouldNotContain: " + ref,
					text.contains(ref), "expected: !<" + ref + ">, but was: <"
							+ text + ">");
		}

		@Override
		public void attrShouldEqualTo(final String name,@Nullable 
				final String ref) throws IOException {

			checkNotNull(name,"name");
			
			final String value = attr(name); // attributes.get(name);

			observer.notifyCheck(id + "."
					+ name
					+ ".shouldEqualTo: " + ref,
					ref.equals(value), "expected: <" + ref + ">, but was: <"
							+ value + ">");
		}

		@Override
		public void shouldBeVisible() throws IOException {

			observer.notifyCheck(id + ".shouldBeVisible",
					"true".equals(attributes.get("visible")),
					"Element is hidden.");
		}

		@Override
		public void shouldBeHidden() throws IOException {

			observer.notifyCheck(id + ".shouldBeHidden",
					!"true".equals(attributes.get("visible")),
					"Element is visible.");
		}

		@Override
		public void sendKeys(final String keysToSend) {

			// do nothing?
		}

		@Override
		public void click() throws IOException {

			observer.notifyAction("click: " + id);
		}

		@Override
		public String attr(final String attrName) {

			final ElementCheckerWithAttributes element;

			try {

				element = TestCheckerEngine.this.elementById(id); // Reload.

			} catch (final IOException e) {
				throw new RuntimeException(e);
			}

			final String attrValue = element.attributes.get(attrName);

			if (attrValue == null) {
				throw new NullPointerException(
						"Cannot find value of attribute: " + attrName);
			}

			return attrValue;
		}
	}
}