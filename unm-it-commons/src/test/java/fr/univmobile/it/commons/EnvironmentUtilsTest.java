package fr.univmobile.it.commons;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fr.univmobile.testutil.PropertiesUtils;

public class EnvironmentUtilsTest {

	@Test
	public void testGetPlatformVersion() throws Exception {

		final String platformVersion = EnvironmentUtils
				.getCurrentPlatformVersion();

		final String platformVersionRef = PropertiesUtils
				.getTestProperty("ios.platformVersion.shouldBe");

		assertEquals(platformVersionRef, platformVersion);
	}
}
