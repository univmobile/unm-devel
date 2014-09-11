package fr.univmobile.devel;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.annotation.Nullable;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.avcompris.binding.annotation.Namespaces;
import net.avcompris.binding.annotation.XPath;
import net.avcompris.binding.dom.helper.DomBinderUtils;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;

/**
 * Command-line utility used to convert all local ./README.md files in Maven
 * projects into src/site/apt/index.apt files.
 */
public class READMEFilesConverter {

	public static void main(final String... args) throws Exception {

		final FileHandler handler = new FileHandler() {

			@Override
			public void handleFile(final File file) throws Exception {

				if ("README.md".equals(file.getName())
						&& new File(file.getParentFile(), "pom.xml").isFile()) {

					convert(file, new File(file.getParentFile(),
							"src/site/apt/index.apt"));
				}
			}
		};

		final Pom pom = DomBinderUtils.xmlContentToJava(new File("pom.xml"),
				Pom.class);

		final String user = System.getProperty("user.name");

		final Pom.Profile profile = pom.getProfileById(user);

		for (final String property : new String[] { "unm-devel", //
				"unm-backend", //
				"unm-mobileweb", //
				"unm-ios", //
				"unm-android", //
				"unm-integration" }) {

			final String path = profile.getProperty(property);

			parseDirectory(new File(path), handler);
		}
	}

	private static void convert(final File markdownFile, final File aptFile)
			throws Exception {

		final File destDir=aptFile.getParentFile();
		
		if (!destDir.exists()) {
			FileUtils.forceMkdir(destDir);
		}

		final Document document = MarkdownToXHTMLConverter
				.convertToDocument(markdownFile);

		final TransformerFactory transformerFactory = TransformerFactory
				.newInstance();

		final Transformer transformer = transformerFactory
				.newTransformer(new StreamSource(new File(
						"src/main/xslt/markdown.apt.xsl")));

		System.out.println("Transforming to: " + aptFile.getCanonicalPath());

		transformer.setParameter("currentGitHubRepository",
				getGitHubRepository(markdownFile));

		transformer.setParameter("projectVersion",
				getProjectVersion(markdownFile));

		transformer.transform(new DOMSource(document),
				new StreamResult(aptFile));
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

		final Workspace workspace = DomBinderUtils.xmlContentToJava(new File(
				"src/main/workspace/workspace.xml"), Workspace.class);

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

	private static void parseDirectory(final File dir, final FileHandler handler)
			throws Exception {

		if (!dir.isDirectory()) {
			return;
		}

		for (final File file : dir.listFiles()) {

			if (file.isHidden()) {
				continue;
			}

			if (file.isDirectory()) {

				final String filename = file.getName();

				if ("bin".equals(filename) || "target".equals(filename)
						|| "build".equals(filename)) {
					continue;
				}

				parseDirectory(file, handler);

			} else {

				handler.handleFile(file);
			}
		}
	}

	private interface FileHandler {

		void handleFile(File file) throws Exception;
	}
}
