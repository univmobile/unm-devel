package fr.univmobile.tools;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

/**
 * generate XHTML files from Markdown files.
 * 
 * @goal generate
 * @phase pre-site
 */
public final class Markdown2xhtmlMojo extends AbstractMojo {

	/**
	 * location of Markdown source files (subdirectories will be searched).
	 *
	 * @parameter expression="${basedir}"
	 * @required
	 */
	private File sourceDirectory;

	/**
	 * location of generated XHTML files.
	 * 
	 * @parameter expression="${basedir}/src/site/xhtml"
	 * @required
	 */
	private File outputDirectory;

	/**
	 * the Maven 2 project
	 * 
	 * @parameter expression="${project}"
	 * @required
	 */
	protected MavenProject project = null;

	/**
	 * execute the generation.
	 * 
	 * @throws MojoExecutionException
	 */
	@Override
	public void execute() throws MojoExecutionException {

		final Log logger = getLog();

		final Markdown2xhtmlEngine engine = new Markdown2xhtmlEngine(logger,
				sourceDirectory, outputDirectory, project.getProperties());

		try {

			engine.process();

		} catch (final Exception e) {

			throw new MojoExecutionException(
					"Unable to generate the XHTML files", e);
		}
	}
}
