package fr.univmobile.web.commons;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.*;

import org.junit.Test;

public class AbstractControllerTest {

	@Test
	public void test_getPathIntVariable() throws Exception {

		assertEquals(123, AbstractController.getPathIntVariable("pois/123",
				"pois/${id}", "${id}"));
	}
	
	@Test
	public void test_pathMatches_equals_noSlash() throws Exception {
		
		assertTrue(AbstractController.pathMatches("regions","regions"));
	}
	
	@Test
	public void test_pathMatches_notEquals_noSlash() throws Exception {
		
		assertFalse(AbstractController.pathMatches("pois","regions"));
	}
	
	@Test
	public void test_pathMatches_equals_endingSlash() throws Exception {
		
		assertTrue(AbstractController.pathMatches("regions/","regions/"));
	}
	
	@Test
	public void test_pathMatches_equals_a_slash_b() throws Exception {
		
		assertTrue(AbstractController.pathMatches("a/b","a/b"));
	}
	
	@Test
	public void test_pathMatches_slash_variable() throws Exception {
		
		assertTrue(AbstractController.pathMatches("pois/${id}","pois/123"));
	}
	
	@Test
	public void test_pathMatchesNot_slash_variable() throws Exception {
		
		assertFalse(AbstractController.pathMatches("pois/${id}","pois/12/3"));
	}
	
	@Test
	public void test_pathMatchesNot_slash_variable_slash_empty() throws Exception {
		
		assertFalse(AbstractController.pathMatches("pois/${id}","pois/"));
	}
	
	@Test
	public void test_pathMatchesNot_slash_variable_noSlash() throws Exception {
		
		assertFalse(AbstractController.pathMatches("pois/${id}","pois"));
	}
	
	@Test
	public void test_pathMatchesNot_slash_variable_longer() throws Exception {
		
		assertFalse(AbstractController.pathMatches("pois/${id}","regions"));
	}
}
