package fr.univmobile.it.commons;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTag;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JGitHelperTest {

	@Before
	public void setUp() throws Exception {

		jgitHelper = new JGitHelper(new File("../.git"));
	}

	private JGitHelper jgitHelper;

	@After
	public void tearDown() throws Exception {

		jgitHelper.close();
	}

	@Test
	public void testMostAncientCommit_isXxx() throws Exception {

		final RevCommit[] commits = jgitHelper.getCommitsFromHead(10000);

		// $ git rev-list HEAD
		
		assertEquals("4cf2cc2d0dcb2d1ba33396903199e3f93f66cccb",
				commits[commits.length - 1].getId().name());
	}

	@Test
	public void testAllCommitsForFile() throws Exception {

		final RevCommit[] commits = jgitHelper.getCommitsForFileFromHead(
				"unm-parent/pom.xml", 10000);

		// $ git log ../unm-parent/pom.xml
		
		final String[] refCommitIds = new String[] {
				// ...
				"d17a81e9c4e3e39ccbd9bcb11a8d7b45ce4be54c",
				"db7d22b50052a54c533d0b48c125028d9fb201ca",
				"675a469931911b1ba17c1388b2516dc2c4c7dbfb",
				"897bf6d0ce33d4b309be0969c4bdd926e5955e84",
				"db93719df5b93d8b87072005e5e7538cc7dd831f",
				"f1874d98bc66e916351dae0aed1806f48bf7f25d",
				"618bd8aadfc6d7d212fda1d1582cd3d67f10629c",
				"c0cbe1cd4bbfba445a995eacc0b9bf592e64dcbd" };
		
		// To print a file: $ git show ab6311aeb92990c9e949481e3f99d70c1bd19e42
		
		final String[] refRevFileIds = new String[] {
				// ...
				"ab6311aeb92990c9e949481e3f99d70c1bd19e42",
				"2ff3b061ec8afc1729453c6cb1970bddd19ceb1d",
				"a5b180d48486c66cd948a22a8837ab4db38f1fb8",
				"655726e1c22b78646b06c0f8c0a2809b772c9ccf",
				"638bbb2302315c42d510996f62cc4ff34320b157",
				"551ade08eae7856147c793c6680d2d6f95fdec30",
				"28bfb56a1f79ea52338ebb5b1dfcc701d50d2ceb",
				"63b7903806efaaa453ac454af84e83945dc623aa" };

		assertTrue(commits.length >= 8);
		
		for (int i = 0; i < 8; ++i) {

			final RevCommit commit = commits[commits.length - 8 + i];

			assertEquals("commitIds[" + i + "]", refCommitIds[i], commit
					.getId().getName());

			assertEquals(
					"revFileIds[" + i + "]",
					refRevFileIds[i],
					jgitHelper.getRevFileIdInCommit(
							"unm-parent/pom.xml",
							commit).name());

		}
	}

	@Test
	public void testGetCommitById() throws Exception {

		assertEquals("e694710646d7be54bec93bab26c3d0700a95bec7", jgitHelper
				.getCommitById("e694710646d7be54bec93bab26c3d0700a95bec7")
				.getId().getName());
	}

	@Test
	public void testTag() throws Exception {

		final Git git = new Git(jgitHelper.repo);

		final String TAG_NAME = "toto";

		final RevTag tag0 = jgitHelper.getTag(TAG_NAME);

		if (tag0 != null) {
			throw new Exception("Tag already exists: " + TAG_NAME);
		}

		assertFalse(jgitHelper.hasTag(TAG_NAME));

		git.tag()
				.setName(TAG_NAME)
				// name
				.setObjectId(
						jgitHelper
								.getCommitById("e694710646d7be54bec93bab26c3d0700a95bec7")) // commit
				.setMessage("Set by JGitHelperTest.java") //
				.call();

		final RevTag tag1 = jgitHelper.getTag(TAG_NAME);

		assertNotNull("tag1, after creation", tag1);

		assertTrue(jgitHelper.hasTag(TAG_NAME));

		git.tagDelete().setTags(TAG_NAME).call();

		final RevTag tag2 = jgitHelper.getTag(TAG_NAME);

		assertNull("tag2, after deletion", tag2);

		assertFalse(jgitHelper.hasTag(TAG_NAME));
	}

	@Test
	public void testCloneRepo() throws Exception {

		final String PROJECT_NAME = "unm-integration";

		final File dir = new File("target/" + PROJECT_NAME);

		if (dir.exists()) {
			FileUtils.deleteDirectory(dir);
		}

		final File pomFile = new File(dir, "unm-ios-ut-results/pom.xml");

		assertFalse(pomFile.exists());

		final JGitHelper jgitHelper = JGitHelper.cloneRepo(
				"https://github.com/univmobile/" + PROJECT_NAME, dir);

		assertTrue(pomFile.exists());

		final RevCommit[] commits = jgitHelper.getCommitsForFileFromHead(
				"unm-ios-ut-results/data/xcodebuild_test.log", 100);

		assertTrue(commits.length > 8);
	}
}
