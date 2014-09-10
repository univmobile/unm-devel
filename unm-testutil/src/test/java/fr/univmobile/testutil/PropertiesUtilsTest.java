package fr.univmobile.testutil;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;

public class PropertiesUtilsTest {

	@Test
	public void test_getTestProperty() throws Exception {

		final String myproperty = PropertiesUtils.getTestProperty("myproperty");

		assertEquals("Hello World!", myproperty);
	}

	@Test(expected = NullPointerException.class)
	public void test_getTestProperty_unknown() throws Exception {

		PropertiesUtils.getTestProperty("myproperty_unknown");
	}

	@Test
	public void test_getSettingsTestRefProperty() throws Exception {

		final String password = PropertiesUtils.getSettingsTestRefProperty(
				"mypassword.ref", new File("src/test/xml/settings.xml"));

		assertEquals("myrootpassword", password);
	}

	@Test(expected = NullPointerException.class)
	public void test_getSettingsTestRefProperty_blank() throws Exception {

		PropertiesUtils.getSettingsTestRefProperty("mypassword_blank.ref",
				new File("src/test/xml/settings.xml"));
	}
}
