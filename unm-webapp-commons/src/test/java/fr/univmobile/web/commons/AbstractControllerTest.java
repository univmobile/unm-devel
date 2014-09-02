package fr.univmobile.web.commons;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AbstractControllerTest {

	@Test
	public void test_getPathIntVariable() throws Exception {

		assertEquals(123, AbstractJspController.getPathIntVariable("pois/123",
				"pois/${id}", "${id}"));
	}

	@Test
	public void test_getPathStringVariable() throws Exception {

		assertEquals("abc", AbstractJspController.getPathStringVariable(
				"pois/abc", "pois/${id}", "${id}"));
	}

	@Test(expected = NumberFormatException.class)
	public void test_getPathNotIntVariable() throws Exception {

		AbstractJspController
				.getPathIntVariable("pois/abc", "pois/${id}", "${id}");
	}

	@Test
	public void test_pathMatches_equals_noSlash() throws Exception {

		assertTrue(AbstractJspController.pathMatches("regions", "regions"));
	}

	@Test
	public void test_pathMatches_notEquals_noSlash() throws Exception {

		assertFalse(AbstractJspController.pathMatches("pois", "regions"));
	}

	@Test
	public void test_pathMatches_equals_endingSlash() throws Exception {

		assertTrue(AbstractJspController.pathMatches("regions/", "regions/"));
	}

	@Test
	public void test_pathMatches_equals_a_slash_b() throws Exception {

		assertTrue(AbstractJspController.pathMatches("a/b", "a/b"));
	}

	@Test
	public void test_pathMatches_slash_variable() throws Exception {

		assertTrue(AbstractJspController.pathMatches("pois/${id}", "pois/123"));
	}

	@Test
	public void test_pathMatchesNot_slash_variable() throws Exception {

		assertFalse(AbstractJspController.pathMatches("pois/${id}", "pois/12/3"));
	}

	@Test
	public void test_pathMatchesNot_slash_variable_slash_empty()
			throws Exception {

		assertFalse(AbstractJspController.pathMatches("pois/${id}", "pois/"));
	}

	@Test
	public void test_pathMatchesNot_slash_variable_noSlash() throws Exception {

		assertFalse(AbstractJspController.pathMatches("pois/${id}", "pois"));
	}

	@Test
	public void test_pathMatchesNot_slash_variable_longer() throws Exception {

		assertFalse(AbstractJspController.pathMatches("pois/${id}", "regions"));
	}
}
