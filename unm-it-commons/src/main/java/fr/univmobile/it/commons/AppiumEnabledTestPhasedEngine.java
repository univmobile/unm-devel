package fr.univmobile.it.commons;

import static com.google.common.base.Preconditions.checkNotNull;
import io.appium.java_client.AppiumDriver;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.annotation.Nullable;

import org.openqa.selenium.remote.RemoteWebElement;
import static fr.univmobile.it.commons.ScenarioContext.*;
public abstract class AppiumEnabledTestPhasedEngine implements
		AppiumEnabledTestEngine {

	public abstract String getSimpleName();

	@Override
	public final RemoteWebElement findElementById(String id) throws IOException {

		throw new IllegalStateException(
				"Because it’s using a phased engine, a scenario test should not call findElementById(): "
						+ id);
	}

	@Override
	public final RemoteWebElement findElementByName(String name)
			throws IOException {

		throw new IllegalStateException(
				"Because it’s using a phased engine, a scenario test should not call findElementById(): "
						+ name);
	}

	@Override
	public final AppiumDriver getDriver() {

		throw new IllegalStateException(
				"Because it’s using a phased engine, a scenario test should not call getDriver().");
	}

	@Nullable
	public final String getPlatformName() {

		return platformName;
	}

	public final void setPlatformName(final String platformName) {

		this.platformName = checkNotNull(platformName, "platformName");
	}

	@Nullable
	public final String getPlatformVersion() {

		return platformVersion;
	}

	@Nullable
	public final void setPlatformVersion(final String platformVersion) {

		this.platformVersion = checkNotNull(platformVersion, "platformVersion");
	}

	public final String getDeviceName() {

		return deviceName;
	}

	public final void setDeviceName(final String deviceName) {

		this.deviceName = checkNotNull(deviceName, "deviceName");
	}

	public final Class<? extends AppiumEnabledTest> getScenariosClass() {

		return scenariosClass;
	}

	public final void setScenariosClass(
			final Class<? extends AppiumEnabledTest> scenariosClass) {

		this.scenariosClass = checkNotNull(scenariosClass, "scenariosClass");
	}

	public final Method getScenarioMethod() {

		return scenarioMethod;
	}

	public final void setScenarioMethod(final Method scenarioMethod) {

		this.scenarioMethod = checkNotNull(scenarioMethod, "scenarioMethod");
	}

	private String platformName; // e.g. "iOS", "Android"
	private String platformVersion; // e.g. "7.1"
	private String deviceName; // e.g. "iPhone Retina (4-inch)"
	private Class<? extends AppiumEnabledTest> scenariosClass;
	private Method scenarioMethod;

	protected final String customizeFilename(final String filename) {

		final String normalizedDeviceName = 
				normalizeDeviceName(deviceName);
		final String normalizedPlatformName = 
				normalizePlatformName(platformName, platformVersion);

		// e.g. "iOS_7.0/iPhoneRetina_4-inch/MyScenario001/scenario4/login.png"

		return 
				normalizedPlatformName+ "/" //
				+ normalizedDeviceName + "/" //
				+ scenariosClass.getSimpleName() + "/" //
				+ scenarioMethod.getName() + "/" //
				+ filename;
	}

	public final String getScenarioId() {

		return scenariosClass.getSimpleName() + "." //
				+ scenarioMethod.getName() + "." //
				+ getSimpleName() + "_" //
				+ ScenarioContext.normalizeDeviceName(deviceName);
	}

	abstract boolean hasErrors();
}
