package fr.univmobile.tools;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.annotation.Nullable;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.avcompris.binding.annotation.Namespaces;
import net.avcompris.binding.annotation.XPath;
import net.avcompris.binding.dom.helper.DomBinderUtils;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.logging.Log;
import org.w3c.dom.Document;

import fr.univmobile.util.MarkdownToXHTMLConverter;

final class Markdown2xhtmlEngine {

	public Markdown2xhtmlEngine(final Log logger, //
			final File sourceDirectory, final File outputDirectory, //
			final Properties properties) {

		this.logger = logger;
		this.sourceDirectory = sourceDirectory;
		this.outputDirectory = outputDirectory;
	}

	private final Log logger;
	private final File sourceDirectory;
	private final File outputDirectory;

	public void process() throws Exception {

		for (final File file : sourceDirectory.listFiles()) {

			if (!file.isFile()) {
				continue;
			}

			final String filename = file.getName();

			if (!filename.endsWith(".md")) {
				continue;
			}

			final String xhtmlFilename = "README.md".equals(filename) //
			? "index.xhtml"
					: filename.replace(".md", ".xhtml");

			md2xhtml(file, new File(outputDirectory, xhtmlFilename));
		}
	}

	private void md2xhtml(final File markdownFile, final File xhtmlFile)
			throws Exception {

		final InputStream xsltIs = getResourceAsStream("markdown.xhtml.xsl");
		try {

			md2xxx(markdownFile, xhtmlFile, xsltIs);

		} finally {
			xsltIs.close();
		}
	}

	private static InputStream getResourceAsStream(final String href)
			throws IOException {

		final InputStream is = Markdown2xhtmlEngine.class.getClassLoader()
				.getResourceAsStream(href);

		if (is == null) {
			throw new FileNotFoundException("InputStream==null: " + href);
		}

		return is;
	}

	private void md2xxx(final File markdownFile, final File destFile,
			final InputStream xsltIs) throws Exception {

		final File destDir = destFile.getParentFile();

		if (!destDir.exists()) {
			FileUtils.forceMkdir(destDir);
		}

		final Document document = MarkdownToXHTMLConverter
				.convertToDocument(markdownFile);

		final TransformerFactory transformerFactory = TransformerFactory
				.newInstance();

		transformerFactory.setURIResolver(new URIResolver() {

			@Override
			public Source resolve(final String href, final String base)
					throws TransformerException {

				final InputStream is;

				try {

					is = getResourceAsStream(href);

				} catch (final IOException e) {

					throw new RuntimeException(e);
				}

				return new StreamSource(is);
			}
		});

		final Transformer transformer = transformerFactory
				.newTransformer(new StreamSource(xsltIs));

		final String gitHubRepository = getGitHubRepository(markdownFile);

		logger.info("Transforming to: " + destFile.getCanonicalPath());
		logger.info("GitHub Repository: " + gitHubRepository);
		
		transformer.setParameter("currentGitHubRepository", gitHubRepository);

		transformer.setParameter("projectVersion",
				getProjectVersion(markdownFile));

		transformer.transform(new DOMSource(document), new StreamResult(
				destFile));
	}

	private static String getGitHubRepository(final File file)
			throws IOException {

		return getGitHubRepository(file, file.getCanonicalFile()
				.getParentFile());
	}

	private static String getGitHubRepository(final File file,
			@Nullable final File dir) throws IOException {

		if (dir == null) {
			throw new FileNotFoundException(
					"Cannot get GitHub Repository for: "
							+ file.getCanonicalPath());
		}

		if (new File(dir, ".git").isDirectory()) {

			return dir.getName();
		}

		// return getGitHubRepository(file, dir.getParentFile());

		final Workspace workspace = DomBinderUtils.xmlContentToJava(
				getResourceAsStream("workspace.xml"), Workspace.class);

		final String dirName = dir.getName();

		if (!workspace.isNullGitRepositoryByName(dirName)) {
			return dirName;
		}

		final Workspace.MavenProject mavenProject = workspace
				.getMavenProjectById(dirName);

		if (mavenProject == null) {
			return getGitHubRepository(file, dir.getParentFile());
		}

		return mavenProject.getGitRepositoryRef();
	}

	private static String getProjectVersion(final File file) throws IOException {

		return getProjectVersion(file, file.getCanonicalFile().getParentFile());
	}

	private static String getProjectVersion(final File file, final File dir)
			throws IOException {

		final File pomFile = new File(dir, "pom.xml");

		if (!pomFile.exists()) {
			return getProjectVersion(dir.getParentFile());
		}

		final Pom pom = DomBinderUtils.xmlContentToJava(pomFile, Pom.class);

		final String version = pom.getVersion().trim();

		if (isBlank(version)) {
			throw new IOException("Cannot find POM version for: "
					+ file.getCanonicalPath());
		}

		return version;
	}

	@Namespaces("xmlns:pom=http://maven.apache.org/POM/4.0.0")
	@XPath("/pom:project")
	private interface Pom {

		@XPath("pom:version | self::*[not(pom:version)]/pom:parent/pom:version")
		String getVersion();

		@XPath("pom:profiles/pom:profile[pom:id = $arg0]")
		Profile getProfileById(String id);

		interface Profile {

			@XPath("pom:properties/pom:*[name() = $arg0]")
			String getProperty(String name);
		}
	}

	@XPath("/workspace")
	private interface Workspace {

		@XPath("gitRepositories/gitRepository[@name = $arg0]")
		boolean isNullGitRepositoryByName(String id);

		@Nullable
		@XPath("mavenProjects/mavenProject[@id = $arg0]")
		MavenProject getMavenProjectById(String id);

		interface MavenProject {

			@XPath("@gitRepository-ref")
			String getGitRepositoryRef();
		}
	}
}
