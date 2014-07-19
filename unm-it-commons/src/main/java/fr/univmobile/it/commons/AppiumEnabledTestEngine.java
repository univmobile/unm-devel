package fr.univmobile.it.commons;

import io.appium.java_client.AppiumDriver;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.remote.RemoteWebElement;

interface AppiumEnabledTestEngine {

	@Before
	void setUp() throws Exception;

	@After
	void tearDown() throws Exception;

	void takeScreenshot(String filename) throws IOException;

	void savePageSource(String filename) throws IOException;

	void swipe(int startX, int startY, int endX, int endY, int durationMs)
			throws IOException;

	RemoteWebElement findElementById(String id) throws IOException;

	RemoteWebElement findElementByName(String name) throws IOException;

	void waitForElementById(int seconds, String id) throws IOException;

	ElementChecker elementById(String id) throws IOException;

	ElementChecker elementByName(String name) throws IOException;

	AppiumDriver getDriver();

	void pause(int ms) throws InterruptedException;

	void futureScreenshot(int ms, String filename) throws IOException;
}
