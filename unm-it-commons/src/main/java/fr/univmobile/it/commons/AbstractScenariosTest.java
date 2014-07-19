package fr.univmobile.it.commons;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import fr.univmobile.testutil.Dumper;
import fr.univmobile.testutil.XMLDumper;

@RunWith(Parameterized.class)
public abstract class AbstractScenariosTest {

	protected static Iterable<Object[]> loadParametersForScenarioClasses(
			final Class<?>... classes) throws Exception {

		final List<Object[]> parameters = new ArrayList<Object[]>();

		loadParameters(parameters, //
				new AppiumEnabledTestCaptureEngine(), classes);

		loadParameters(parameters, //
				new AppiumEnabledTestCheckerEngine(), classes);

		return parameters;
	}

	private static void loadParameters(final Collection<Object[]> parameters,
			final AppiumEnabledTestPhasedEngine engine, //
			final Class<?>... classes) throws IOException {

		FileUtils.forceMkdir(new File("target", "screenshots"));

		final Dumper dumper = XMLDumper.newXMLDumper("scenarios", new File(
				"target/screenshots/scenarios.xml"));
		try {

			loadParameters(dumper, parameters, engine, classes);

		} finally {
			dumper.close();
		}
	}

	private static void loadParameters(final Dumper dumper,
			final Collection<Object[]> parameters,
			final AppiumEnabledTestPhasedEngine engine, //
			final Class<?>... classes) throws IOException {

		for (final Class<?> clazz : classes) {

			if (clazz.getAnnotation(Ignore.class) != null) {
				continue;
			}

			// 1. SCENARIOS CLASS

			final Dumper scenarioDumper = dumper.addElement("scenariosClass") //
					.addAttribute("className", clazz.getName()) //
					.addAttribute("classSimpleName", clazz.getSimpleName());

			if (!AppiumEnabledTest.class.isAssignableFrom(clazz)) {
				throw new IllegalArgumentException(
						"Scenarios class should extends "
								+ AppiumEnabledTest.class.getSimpleName()
								+ ": " + clazz);
			}

			final Scenarios scenariosAnnotation = clazz
					.getAnnotation(Scenarios.class);

			if (scenariosAnnotation == null) {
				throw new IllegalArgumentException(
						"Scenarios class should be annotated with @"
								+ Scenarios.class.getSimpleName() + ": "
								+ clazz);
			}

			scenarioDumper.addAttribute("scenariosLabel",
					scenariosAnnotation.value());

			// 2. DEVICE NAMES

			final DeviceNames deviceNamesAnnotation = clazz
					.getAnnotation(DeviceNames.class);

			final String[] deviceNames;

			if (deviceNamesAnnotation == null) {

				// default

				final String IPHONE_RETINA_4_INCH = "iPhone Retina (4-inch)";

				System.err.println( //
						"No @DeviceNames annotation was specified on " + clazz
								+ ". Using " + IPHONE_RETINA_4_INCH);

				deviceNames = new String[] { IPHONE_RETINA_4_INCH };

			} else {

				deviceNames = deviceNamesAnnotation.value();
			}

			// 2.5. DUMP DEVICE NAMES BEFORE SCENARIO METHODS

			for (final String deviceName : deviceNames) {

				final String normalizedDeviceName = normalizeDeviceName(deviceName);

				scenarioDumper
						.addElement("device")
						.addAttribute("deviceName", deviceName)
						.addAttribute("normalizedDeviceName",
								normalizedDeviceName);
			}

			// 3. SCENARIO METHODS

			final String classSimpleName = clazz.getSimpleName();

			for (final Method method : clazz.getMethods()) {

				if (method.getAnnotation(Ignore.class) != null) {
					continue;
				}

				final Scenario scenarioAnnotation = method
						.getAnnotation(Scenario.class);

				if (scenarioAnnotation == null) {
					continue;
				}

				final String methodName = method.getName();

				scenarioDumper
						.addElement("scenarioMethod")
						.addAttribute("methodName", methodName)
						.addAttribute("scenarioLabel",
								scenarioAnnotation.value());

				for (final String deviceName : deviceNames) {

					final String normalizedDeviceName = normalizeDeviceName(deviceName);

					parameters.add(new Object[] { normalizedDeviceName, //
							classSimpleName, //
							methodName, //
							engine.getSimpleName(), //
							deviceName, //
							clazz.asSubclass(AppiumEnabledTest.class), //
							method, //
							engine });
				}
			}
		}
	}

	public static String normalizeDeviceName(final String deviceName) {

		// e.g. "iPhone Retina (3.5-inch)" -> "iPhone_Retina_3.5-inch"

		String normalizedDeviceName = deviceName.replace(' ', '_') //
				.replace('(', '_').replace(')', '_') //
				.replace("__", "_");

		if (normalizedDeviceName.startsWith("_")) {
			normalizedDeviceName = normalizedDeviceName.substring(1);
		}

		if (normalizedDeviceName.endsWith("_")) {
			normalizedDeviceName = normalizedDeviceName.substring(0,
					normalizedDeviceName.length() - 1);
		}

		return normalizedDeviceName;
	}

	protected AbstractScenariosTest(final String normalizedDeviceName, //
			final String scenarioClassSimpleName, //
			final String scenarioMethodName, //
			final String engineSimpleName, //
			final String deviceName, //
			final Class<? extends AppiumEnabledTest> scenariosClass, //
			final Method scenarioMethod, //
			final AppiumEnabledTestPhasedEngine engine) {

		this.deviceName = checkNotNull(normalizedDeviceName, "deviceName");
		this.scenariosClass = checkNotNull(scenariosClass, "scenariosClass");
		this.scenarioMethod = checkNotNull(scenarioMethod, "scenarioMethod");
		this.engine = checkNotNull(engine, "engine");

	}

	private final String deviceName;
	private final Class<? extends AppiumEnabledTest> scenariosClass;
	private final Method scenarioMethod;
	private final AppiumEnabledTestPhasedEngine engine;

	@Test
	public void run() throws Throwable {

		System.out.println();

		System.out.println("Running test: " + scenariosClass.getSimpleName()
				+ "." + scenarioMethod.getName() //
				+ "." + engine.getSimpleName() //
				+ "(" + deviceName + ")...");

		// 0. OBJECT INSTANCE

		final AppiumEnabledTest instance = scenariosClass.newInstance();

		engine.setPlatformName(AppiumEnabledTestDefaultEngine
				.getCurrentPlatformName());
		engine.setPlatformVersion(EnvironmentUtils
				.getCurrentPlatformVersion());
		engine.setDeviceName(deviceName);
		engine.setScenariosClass(scenariosClass);
		engine.setScenarioMethod(scenarioMethod);

		AppiumEnabledTestDefaultEngine.setCurrentDeviceName(deviceName);

		instance.setEngine(engine);

		// 1. SETUP

		instance.setUp();

		try {

			scenarioMethod.invoke(instance);

		} catch (final InvocationTargetException e) {

			throw e.getTargetException();

		} finally {

			// 9. TEARDOWN

			instance.tearDown();
		}

		assertFalse("There were errors.", engine.hasErrors());
	}
}
