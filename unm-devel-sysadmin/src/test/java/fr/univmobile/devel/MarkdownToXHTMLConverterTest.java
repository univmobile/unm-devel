package fr.univmobile.devel;

import static fr.univmobile.devel.MarkdownToXHTMLConverter.convert;
import static fr.univmobile.devel.MarkdownToXHTMLConverter.convertToDocument;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Ignore;
import org.junit.Test;

public class MarkdownToXHTMLConverterTest {

	@Test
	public void testConvert_topREADME() throws Exception {

		final File READMEFile = new File("../README.md");

		convert(READMEFile);
	}

	@Test
	public void testConvert_topREADME_archive() throws Exception {

		final File READMEFile = new File("src/test/markdown/README.md_top");

		final String xhtml = convert(READMEFile);

		assertTrue("Processed Markdown should contain: </p>",
				xhtml.contains("</p>"));
	}

	@Test
	public void testConvert_topREADMEToDocument() throws Exception {

		final File READMEFile = new File("../README.md");

		convertToDocument(READMEFile);
	}

	@Test
	public void testConvert_localREADME() throws Exception {

		final File READMEFile = new File("./README.md");

		convert(READMEFile);
	}

	@Test
	public void testConvert_localREADME_archive() throws Exception {

		final File READMEFile = new File("src/test/markdown/README.md_local");

		final String xhtml = convert(READMEFile);

		assertTrue("Processed Markdown should contain: macos_",
				xhtml.contains("macos_"));
		assertFalse("Processed Markdown should not contain: macos<em>",
				xhtml.contains("macos<em>"));
	}

	@Test
	public void testConvert_localREADMEToDocument() throws Exception {

		final File READMEFile = new File("./README.md");

		convertToDocument(READMEFile);
	}

	@Test
	public void testConvertEscapedLtGt() throws Exception {

		final String processed = convert("L’élément \\<logCommitId\\>").trim();

		assertEquals("<p>L’élément &lt;logCommitId&gt;</p>", processed);
	}

	@Test
	public void testConvertUnderscoreInside() throws Exception {

		final String processed = convert(
				"il lance macos_job-xcodebuild_test.sh").trim();

		assertEquals("<p>il lance macos_job-xcodebuild_test.sh</p>", processed);
	}

	@Test
	public void testConvertUnderscoreInside2() throws Exception {

		final String processed = convert(
				"il lance macos_job-xcodebuild_test.sh"
						+ " et macos_job-xcodebuild_test.sh").trim();

		assertEquals("<p>il lance macos_job-xcodebuild_test.sh"
				+ " et macos_job-xcodebuild_test.sh</p>", processed);
	}

	@Test
	public void testConvertEscapedUnderscoreInside() throws Exception {

		final String processed = convert(
				"il lance macos\\_job-xcodebuild\\_test.sh").trim();

		assertEquals("<p>il lance macos_job-xcodebuild_test.sh</p>", processed);
	}

	@Test
	public void testConvertUnderscoreOutside() throws Exception {

		final String processed = convert("il lance _job-xcodebuild_").trim();

		assertEquals("<p>il lance <em>job-xcodebuild</em></p>", processed);
	}

	@Test
	public void testConvertEscapedUnderscoreOutside() throws Exception {

		final String processed = convert("il lance \\_job-xcodebuild\\_")
				.trim();

		assertEquals("<p>il lance _job-xcodebuild_</p>", processed);
	}

	@Test
	@Ignore
	public void testImage() throws Exception {

		final String processed = convert(
				"![](src/site/images/backend-mock.png)").trim();

		assertEquals("", processed);
	}

	@Test
	public void testImage_title() throws Exception {

		final String processed = convert(
				"![](src/site/images/backend-mock.png \"Hello World!\")")
				.trim();

		assertEquals(
				"<p><img src=\"src/site/images/backend-mock.png\" alt=\"\" title=\"Hello World!\" /></p>",
				processed);
	}

	@Test
	@Ignore
	public void testImage_alt() throws Exception {

		final String processed = convert(
				"![toto](src/site/images/backend-mock.png").trim();

		assertEquals("", processed);
	}

	@Test
	@Ignore
	public void testImage_raw() throws Exception {

		final String processed = convert(
				"![](src/site/images/backend-mock.png?raw=true)").trim();

		assertEquals("", processed);
	}

	@Test
	public void testImage_raw_title() throws Exception {

		final String processed = convert(
				"![](src/site/images/backend-mock.png?raw=true \"Hi\")").trim();

		assertEquals(
				"<p><img src=\"src/site/images/backend-mock.png?raw=true\" alt=\"\" title=\"Hi\" /></p>",
				processed);
	}

	@Test
	public void testImage_raw_width_title() throws Exception {

		final String processed = convert(
				"![](src/site/images/backend-mock.png?raw=true =600x \"Hi\")").trim();

		assertEquals(
				"<p><img src=\"images/backend-mock.png?raw=true\" width=\"600px\" alt=\"\" title=\"Hi\" /></p>",
				processed);
	}
}
