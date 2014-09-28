package fr.univmobile.it.commons;

import static com.google.common.base.Preconditions.checkNotNull;
import static fr.univmobile.it.commons.ScenarioContext.normalizeDeviceName;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
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

		final Class<?> firstClazz = classes[0];

		final Class<? extends WebDriverEnabledTest> testClass;

		final String platformName;

		if (AppiumIOSEnabledTest.class.isAssignableFrom(firstClazz)) {

			platformName = "iOS";

			testClass = AppiumIOSEnabledTest.class;

		} else if (AppiumAndroidEnabledTest.class.isAssignableFrom(firstClazz)) {

			platformName = "Android";

			testClass = AppiumAndroidEnabledTest.class;

		} else if (SeleniumEnabledTest.class.isAssignableFrom(firstClazz)) {

			platformName = System.getProperty("os.name").startsWith("Mac") //
			? "Mac OS X"
					: "Debian";

			testClass = SeleniumEnabledTest.class;

		} else {

			throw new IllegalStateException("Unknown platformName for class: "
					+ firstClazz);
		}

		final boolean useSafari = AppiumSafariEnabledTest.class
				.isAssignableFrom(firstClazz);

		System.out.println(firstClazz);
		System.out.println("useSafari: " + useSafari);

		// 1. VALIDATION

		for (final Class<?> clazz : classes) {

			if (AppiumSafariEnabledTest.class.isAssignableFrom(clazz) != useSafari) {
				throw new IllegalArgumentException(
						"None, or all classes, should extend AppiumSafariEnabledTest: "
								+ firstClazz + ", " + clazz);
			}

			if (!testClass.isAssignableFrom(clazz)) {
				throw new IllegalArgumentException(
						"None, or all classes, should extend "
								+ testClass.getSimpleName() + ": " + firstClazz
								+ ", " + clazz);
			}
		}

		// 2. LOAD PARAMETERS

		final List<Object[]> parameters = new ArrayList<Object[]>();

		loadParameters(parameters, //
				new TestCaptureEngine(platformName, useSafari), classes);

		loadParameters(parameters, //
				new TestCheckerEngine(), classes);

		return parameters;
	}

	private static void loadParameters(final Collection<Object[]> parameters,
			final TestPhasedEngine engine, //
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
			final TestPhasedEngine engine, //
			final Class<?>... classes) throws IOException {

		for (final Class<?> clazz : classes) {

			if (clazz.isAnnotationPresent(Ignore.class)) {
				continue;
			}

			// 1. SCENARIOS CLASS

			final Dumper scenarioDumper = dumper.addElement("scenariosClass") //
					.addAttribute("className", clazz.getName()) //
					.addAttribute("classSimpleName", clazz.getSimpleName());

			if (!WebDriverEnabledTest.class.isAssignableFrom(clazz)) {
				throw new IllegalArgumentException(
						"Scenarios class should extends "
								+ WebDriverEnabledTest.class.getSimpleName()
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

			for (final Method method : clazz.getMethods()) {

				if (method.isAnnotationPresent(Ignore.class)) {
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

					parameters.add(new Object[] { new ScenarioContext(
							deviceName, //
							clazz.asSubclass(WebDriverEnabledTest.class), //
							method, //
							engine) });
				}
			}
		}
	}

	protected AbstractScenariosTest(final ScenarioContext context) {

		checkNotNull(context, "context");

		this.deviceName = context.normalizedDeviceName;
		this.scenariosClass = context.scenariosClass;
		this.scenarioMethod = context.scenarioMethod;
		this.engine = context.engine;
	}

	private final String deviceName;
	private final Class<? extends WebDriverEnabledTest> scenariosClass;
	private final Method scenarioMethod;
	private final TestPhasedEngine engine;

	@Before
	public final void setUp() throws Exception {

		System.out.println();

		// 0. INITIALIZE OBJECT INSTANCE

		final String platformName = WebDriverEnabledTestDefaultEngine
				.getCurrentPlatformName();
		engine.setPlatformName(platformName);
		engine.setPlatformVersion(EnvironmentUtils
				.getCurrentPlatformVersion(platformName));
		engine.setDeviceName(deviceName);
		engine.setScenariosClass(scenariosClass);
		engine.setScenarioMethod(scenarioMethod);

		WebDriverEnabledTestDefaultEngine.setCurrentDeviceName(deviceName);

		instance = scenariosClass.newInstance();

		instance.setEngine(engine);

		// 1. SETUP METHODS

		recursiveSetUp(scenariosClass); // Also call instance.setUp()
	}

	private void recursiveSetUp(final Class<?> clazz) throws Exception {

		final Class<?> superclazz = clazz.getSuperclass();

		if (superclazz != null) {
			recursiveSetUp(superclazz);
		}

		for (final Method method : clazz.getDeclaredMethods()) {

			if (method.isAnnotationPresent(Before.class)
					&& Modifier.isPublic(method.getModifiers())
					&& method.getParameterTypes().length == 0) {

				System.out.println("Invoking: @Before " //
						+ method.getName() + "() - " + new DateTime());

				method.invoke(instance);
			}
		}
	}

	@After
	public final void tearDown() throws Exception {

		// 9. RELEASE OBJECT INSTANCE

		recursiveTearDown(scenariosClass); // Also call instance.tearDown()

		instance = null;
	}

	private void recursiveTearDown(final Class<?> clazz) throws Exception {

		for (final Method method : clazz.getDeclaredMethods()) {

			if (method.isAnnotationPresent(After.class)
					&& Modifier.isPublic(method.getModifiers())
					&& method.getParameterTypes().length == 0) {

				System.out.println("Invoking: @After " //
						+ method.getName() + "() - " + new DateTime());

				method.invoke(instance);
			}
		}

		final Class<?> superclazz = clazz.getSuperclass();

		if (superclazz != null) {
			recursiveTearDown(superclazz);
		}
	}

	private WebDriverEnabledTest instance;

	@Test
	public final void run() throws Throwable {

		System.out.println("Running test: " + scenariosClass.getSimpleName()
				+ "." + scenarioMethod.getName() //
				+ "." + engine.getSimpleName() //
				+ "(" + deviceName + ")...");

		try {

			scenarioMethod.invoke(instance);

		} catch (final InvocationTargetException e) {

			throw e.getTargetException();
		}

		assertFalse("There were errors.", engine.hasErrors());
	}
}
