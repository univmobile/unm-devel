package fr.univmobile.devel;

import static org.apache.commons.lang3.CharEncoding.UTF_8;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.github.rjeschke.txtmark.Configuration;
import com.github.rjeschke.txtmark.Processor;

/**
 * This class converts a ./README.md Markdown file into a src/site/apt/index.apt
 * APT file.
 * <p>
 * In UnivMobile Maven projects, the ./README.md file is the only Markdown file
 * to be converted this way before a mvn site:site generation, since all other
 * Markdown files are stored in src/site/markdown/, which the maven-doxia-plugin
 * can handle.
 */
public abstract class MarkdownToXHTMLConverter {

	/**
	 * Convert a Markdown file into a XHTML stream, using txtmark.
	 */
	public static String convert(final File markdownFile) throws IOException {

		System.out.println("Converting: " + markdownFile.getCanonicalPath()
				+ "...");

		return convert(FileUtils.readFileToString(markdownFile, UTF_8));
	}

	/**
	 * Convert a Markdown stream into a XHTML stream, using txtmark.
	 */
	public static String convert(final String markdown) {

		String prep = markdown; // Prepare the Markdown stream for conversion

		prep = prep.replace("\\<", "&lt;").replace("\\>", "&gt;");

		boolean inEmphasis = false;

		for (int i = 0; i < prep.length(); ++i) {

			i = prep.indexOf('_', i);

			if (i == -1) {
				break;
			}

			if (!inEmphasis) {

				if (i == 0 || Character.isWhitespace(prep.charAt(i - 1))) {
					inEmphasis = true;
					continue;
				}

			} else {

				if (i + 1 < prep.length() && Character.isWhitespace(prep.charAt(i + 1))) {
					inEmphasis = false;
					continue;
				}

				continue; // still in emphasis
			}
			
			if (prep.charAt(i - 1) != '\\') {
				prep = prep.substring(0, i) + '\\' + prep.substring(i);
				++i;
			}
		}

		final Configuration configuration = // Configuration.DEFAULT_SAFE;
		Configuration.builder().setSafeMode(true).build();

		final String processed = Processor.process(prep, configuration);

		// System.out.println(processed);

		return processed;
	}

	/**
	 * Convert a Markdown file into a XHTML document, using txtmark.
	 */
	public static Document convertToDocument(final File markdownFile)
			throws IOException, ParserConfigurationException, SAXException {

		System.out.println("Converting: " + markdownFile.getCanonicalPath()
				+ "...");

		return convertToDocument(FileUtils
				.readFileToString(markdownFile, UTF_8));
	}

	/**
	 * Convert a Markdown stream into a XHTML document, using txtmark.
	 */
	public static Document convertToDocument(final String markdown)
			throws ParserConfigurationException, SAXException, IOException {

		final String processed = convert(markdown);

		final String xml = "<processed>" + processed + "</processed>";

		final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
				.newInstance();
		final DocumentBuilder documentBuilder = documentBuilderFactory
				.newDocumentBuilder();

		return documentBuilder.parse(new InputSource(new StringReader(xml)));
	}
}
