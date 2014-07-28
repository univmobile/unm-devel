package fr.univmobile.it.commons;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.Ignore;
import org.junit.Test;

public class JGitTest {

	@Test
	@Ignore
	public void testBranch_isMaster() throws Exception {

		final FileRepositoryBuilder builder = new FileRepositoryBuilder();

		final File gitDir = new File("../.git");

		final Repository repo = builder.setGitDir(gitDir).readEnvironment()
				.findGitDir().build();
		try {

			final String branch = repo.getBranch();

			assertEquals("master", branch);

		} finally {
			repo.close();
		}
	}

	@Test
	public void testHead_isNotNull() throws Exception {

		final FileRepositoryBuilder builder = new FileRepositoryBuilder();

		final File gitDir = new File("../.git");

		final Repository repo = builder.setGitDir(gitDir).readEnvironment()
				.findGitDir().build();
		try {

			final ObjectId head = repo.resolve(Constants.HEAD);

			assertNotNull(head);

		} finally {
			repo.close();
		}
	}
}
