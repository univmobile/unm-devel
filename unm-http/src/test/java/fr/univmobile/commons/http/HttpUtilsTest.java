package fr.univmobile.commons.http;

import static fr.univmobile.commons.http.HttpUtils.urlEncode;
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
}
