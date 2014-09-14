package fr.univmobile.commons;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FormatUtilsTest {

	@Test
	public void testHumanReadableByteCount() {
		
		assertEquals("6.8 MiB", FormatUtils.humanReadableByteCount(7077888, false));
		assertEquals("7.1 MB", FormatUtils.humanReadableByteCount(7077888, true));
	}
}
