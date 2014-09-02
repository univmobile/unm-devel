package fr.univmobile.it.commons;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

interface WebDriverEnabledTestEngine {

	@Before
	void setUp() throws Exception;

	@After
	void tearDown() throws Exception;

	void takeScreenshot(String filename) throws IOException;

	void savePageSource(String filename) throws IOException;

	void swipe(int startX, int startY, int endX, int endY, int durationMs)
			throws IOException;

	WebElement findElementById(String id) throws IOException;

	WebElement findElementByName(String name) throws IOException;

	WebElement findElementByXPath(String xpath) throws IOException;

	void waitForElementById(int seconds, String id) throws IOException;

	void get(String url) throws IOException;

	ElementChecker elementById(String id) throws IOException;

	ElementChecker elementByName(String name) throws IOException;

	ElementChecker elementByXPath(String xpath) throws IOException;

	WebDriver getDriver();

	void pause(int ms) throws InterruptedException;

	void futureScreenshot(int ms, String filename) throws IOException;
}
