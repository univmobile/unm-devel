package fr.univmobile.commons.http;

import static fr.univmobile.commons.http.HttpUtils.*;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class HttpUtilsTest {

	@Test
	public void test_urlEncode_simple() throws Exception {

		assertEquals("toto", urlEncode("toto"));
	}

	@Test
	public void test_urlEncode_spaces() throws Exception {

		assertEquals( // "a%20b%20%c",
				"a+b+c", urlEncode("a b c"));
	}

	@Test
	public void test_urlEncode_plus() throws Exception {

		assertEquals("Hello%2BWorld%21", urlEncode("Hello+World!"));
	}

	@Test
	public void test_urlEncode_dash() throws Exception {

		assertEquals("a-b-c", urlEncode("a-b-c"));
	}

	@Test
	public void test_urlEncode_amp() throws Exception {

		assertEquals("Bob%26Alice", urlEncode("Bob&Alice"));
	}

	@Test
	public void test_urlEncode_questionMark() throws Exception {

		assertEquals("Really%3F", urlEncode("Really?"));
	}

	@Test
	public void test_urlEncode_slash() throws Exception {

		assertEquals("a%2Fb", urlEncode("a/b"));
	}

	@Test
	public void test_urlEncode_colon() throws Exception {

		assertEquals("a%3Ab", urlEncode("a:b"));
	}

	@Test
	public void test_base64encode() throws Exception {

		assertEquals("cmlyaTpmaWZpOmxvdWxvdQ==", base64Encode("riri:fifi:loulou"));
	}
	
	@Test
	public void test_composeURL_noParam() throws Exception {
		
		assertEquals("http://toto",composeURL("http://toto"));
	}
	
	@Test
	public void test_composeURL_oneParam() throws Exception {
		
		assertEquals("http://toto?a=123",composeURL("http://toto", "a","123"));
	}
	
	@Test
	public void test_composeURL_twoParams() throws Exception {
		
		assertEquals("http://toto?a=123&b=Hello+World%21",composeURL(
				"http://toto", "a","123", "b","Hello World!"));
	}
}
