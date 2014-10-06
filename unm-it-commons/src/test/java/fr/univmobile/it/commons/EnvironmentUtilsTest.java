package fr.univmobile.it.commons;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Ignore;
import org.junit.Test;

import fr.univmobile.testutil.PropertiesUtils;

public class EnvironmentUtilsTest {

	@Test
	public void testGetPlatformVersion() throws Exception {

		final String platformVersion = EnvironmentUtils
				.getCurrentPlatformVersion("iOS");

		final String platformVersionRef = PropertiesUtils
				.getTestProperty("ios.platformVersion.shouldBe");

		assertEquals(platformVersionRef, platformVersion);
	}

	/**
	 * Local test for {@link EnvironmentUtils#exec(File, String...)}
	 */
	@Test
	@Ignore
	public void testPlistBuddySet() throws Exception {

		EnvironmentUtils
				.exec(new File("/usr/libexec/PlistBuddy"),
						"-c",
						"Set UNMJsonBaseURL 'https://univmobile-dev.toto/'",
						"/Users/dandriana/Library/Developer/Xcode/DerivedData/UnivMobile-hkgpereuqofjldgxrixppsvcdulv/Build/Products/Debug-iphonesimulator/UnivMobile.app/Info.plist");
	}
}
