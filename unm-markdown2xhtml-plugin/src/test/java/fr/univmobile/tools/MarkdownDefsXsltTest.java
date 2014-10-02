package fr.univmobile.tools;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;

public class MarkdownDefsXsltTest {

	@Test
	public void testSubProject() throws Exception {

		assertEquals(
				"../../unm-devel-sysadmin/1.2.3/index.html",
				href("https://github.com/univmobile/unm-devel/tree/develop/unm-devel-sysadmin"));
	}
	
	@Test
	public void testGitHubRepository() throws Exception {
		
		assertEquals(
				"https://github.com/univmobile/unm-ios",
				href("https://github.com/univmobile/unm-ios"));
	}

	@Test
	public void testSubProjectREADME() throws Exception {

		assertEquals(
				"../../unm-backend-mock/1.2.3/index.html",
				href("https://github.com/univmobile/unm-devel/blob/develop/unm-backend-mock/README.md"));
	}


	@Test
	public void testSubProjectDevel() throws Exception {

		assertEquals(
				"../../unm-backend-mock/1.2.3/Devel.html",
				href("https://github.com/univmobile/unm-devel/blob/develop/unm-backend-mock/Devel.md"));
	}

	@Test
	public void testGitHubRepositoryDevel() throws Exception {

		assertEquals(
				"../../unm-ios/1.2.3/Devel.html",
				href("https://github.com/univmobile/unm-ios/blob/develop/Devel.md"));
	}
		
	private static String href(final String href) throws Exception {

		final TransformerFactory transformerFactory = TransformerFactory
				.newInstance();

		final Transformer transformer = transformerFactory
				.newTransformer(new StreamSource(new File(
						"src/test/xslt/markdown_test.xsl")));

		final String xml = "<href>" + href + "</href>";

		transformer.setParameter("href", href);
		
		transformer.setParameter("projectVersion","1.2.3");

		final StringWriter sw = new StringWriter();

		transformer.transform(new StreamSource(new StringReader(xml)),
				new StreamResult(sw));

		return sw.toString();
	}
}
