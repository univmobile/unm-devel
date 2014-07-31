package fr.univmobile.it.commons;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Method;

public class ScenarioContext {

	ScenarioContext(final String deviceName, //
			final Class<? extends AppiumEnabledTest> scenariosClass, //
			final Method scenarioMethod, //
			final AppiumEnabledTestPhasedEngine engine) {

		this.deviceName = checkNotNull(deviceName, "deviceName");
		this.normalizedDeviceName = normalizeDeviceName(deviceName);
		this.scenariosClass = checkNotNull(scenariosClass, "scenariosClass");
		this.scenarioMethod = checkNotNull(scenarioMethod, "scenarioMethod");
		this.engine = checkNotNull(engine, "engine");
	}

	@Override
	public String toString() {

		return scenariosClass.getSimpleName() //
				+ "." + scenarioMethod.getName() //
				+ "." + engine.getSimpleName() //
				+ "_" + normalizeDeviceName(deviceName);
	}

	final String normalizedDeviceName;
	final String deviceName;
	final Class<? extends AppiumEnabledTest> scenariosClass;
	final Method scenarioMethod;
	final AppiumEnabledTestPhasedEngine engine;

	public static String normalizePlatformName(final String platformName,
			final String platformVersion) {

		return normalizeName(platformName + "_" + platformVersion);
	}

	private static String normalizeName(final String name) {

		// e.g. "iPhone Retina (3.5-inch)" -> "iPhone_Retina_3.5-inch"

		String normalizedName = name.replace(' ', '_') //
				.replace('(', '_').replace(')', '_') //
				.replace("__", "_");

		if (normalizedName.startsWith("_")) {
			normalizedName = normalizedName.substring(1);
		}

		if (normalizedName.endsWith("_")) {
			normalizedName = normalizedName.substring(0,
					normalizedName.length() - 1);
		}

		return normalizedName;
	}

	public static String normalizeDeviceName(final String deviceName) {

		return normalizeName(deviceName);
	}
}
