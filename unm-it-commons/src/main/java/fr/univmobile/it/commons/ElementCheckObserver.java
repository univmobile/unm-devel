package fr.univmobile.it.commons;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;

import fr.univmobile.testutil.Dumper;
import fr.univmobile.testutil.XMLDumper;

interface ElementCheckObserver {

	void notifyCheck(String label, boolean condition, String failMessage)
			throws IOException;

	void notifyAction(String label) throws IOException;

	void notifyScreenshot(String filename) throws IOException;

	void notifyScreenshot(String filename, int ms) throws IOException;
}

class ElementCheckObserverComposite implements ElementCheckObserver {

	public ElementCheckObserverComposite(
			final ElementCheckObserver... observers) {

		this.observers.addAll(Arrays.asList(observers));
	}

	private final List<ElementCheckObserver> observers = new ArrayList<ElementCheckObserver>();

	@Override
	public void notifyCheck(final String label, final boolean condition,
			final String failMessage) throws IOException {

		for (final ElementCheckObserver observer : observers) {

			observer.notifyCheck(label, condition, failMessage);
		}
	}

	@Override
	public void notifyAction(final String label) throws IOException {

		for (final ElementCheckObserver observer : observers) {

			observer.notifyAction(label);
		}
	}

	@Override
	public void notifyScreenshot(final String filename) throws IOException {

		for (final ElementCheckObserver observer : observers) {

			observer.notifyScreenshot(filename);
		}
	}

	@Override
	public void notifyScreenshot(final String filename, final int ms)
			throws IOException {

		for (final ElementCheckObserver observer : observers) {

			observer.notifyScreenshot(filename, ms);
		}
	}
}

class ElementCheckObserverStdout implements ElementCheckObserver {

	private final List<String> errors = new ArrayList<String>();

	@Override
	public void notifyCheck(final String label, final boolean condition,
			final String failMessage) {

		if (condition) {

			System.out.println(label + ": OK");

		} else {

			System.err.println("** ERROR: " + label + " (" + failMessage + ")");

			errors.add(label);
		}
	}

	@Override
	public void notifyAction(final String label) {

		System.out.println("(" + label + ")");
	}

	@Override
	public void notifyScreenshot(final String filename) throws IOException {

		System.out.println("[screenshot: " + filename + "]");
	}

	@Override
	public void notifyScreenshot(final String filename, final int ms)
			throws IOException {

		System.out.println("[screenshot: " + filename + "] in " + ms + " ms");
	}

	public void clearErrors() {

		errors.clear();
	}

	public boolean hasErrors() {

		return !errors.isEmpty();
	}
}

class ElementCheckObserverXMLDump implements ElementCheckObserver {

	public void init(final String deviceName,
			final Class<? extends WebDriverEnabledTest> scenariosClass, //
			final Method scenarioMethod, final File outFile) throws IOException {

		// close(); // close the previous use

		final String platformName = WebDriverEnabledTestDefaultEngine
				.getCurrentPlatformName();
		final String platformVersion = EnvironmentUtils
				.getCurrentPlatformVersion(platformName);

		dumper = XMLDumper.newXMLDumper("scenario", outFile) //
				.addAttribute("date", new DateTime()) //
				.addAttribute("scenarioMethod", scenarioMethod.getName()) //
				.addAttribute("scenariosClass", scenariosClass.getName()) //
				.addAttribute("platformName", platformName) //
				.addAttribute("platformVersion", platformVersion) //
				.addAttribute("deviceName", deviceName); //

		dumper.addAttribute("scenarioLabel", scenarioMethod //
				.getAnnotation(Scenario.class).value());

		dumper.addAttribute("scenariosLabel", scenariosClass //
				.getAnnotation(Scenarios.class).value());
	}

	@Override
	public void notifyCheck(final String label, final boolean condition,
			final String failMessage) throws IOException {

		dumper.addElement("check") //
				.addAttribute("label", label) //
				.addAttribute("condition", condition) //
				.addAttribute("failMessage", failMessage);
	}

	@Override
	public void notifyAction(final String label) throws IOException {

		dumper.addElement("action") //
				.addAttribute("label", label);
	}

	@Override
	public void notifyScreenshot(final String filename) throws IOException {

		dumper.addElement("screenshot") //
				.addAttribute("filename", filename);
	}

	@Override
	public void notifyScreenshot(final String filename, final int ms)
			throws IOException {

		dumper.addElement("screenshot") //
				.addAttribute("filename", filename) //
				.addAttribute("ms", ms);
	}

	public void close() throws IOException {

		if (dumper != null) {

			dumper.close();

			dumper = null;
		}
	}

	private Dumper dumper;
}