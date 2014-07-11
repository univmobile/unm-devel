package fr.univmobile.ios.ut;

import static org.junit.Assert.fail;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Test;

public class TestResultsAreUpToDateTest {

	@Test
	public void testTestResultsAreUpToDate() throws Exception {

		final String PROJECT_NAME = "unm-ios-ut-results";

		final File dir = new File("target/" + PROJECT_NAME);

		if (dir.exists()) {
			FileUtils.deleteDirectory(dir);
		}

		final JGitHelper jgitHelper = JGitHelper.cloneRepo(
				"https://github.com/univmobile/" + PROJECT_NAME, dir);

		final String PATH = "data/xcodebuild_test.log";

		final RevCommit[] commits = jgitHelper
				.getAllCommitsForFileFromHead(PATH);

		final String commitId0 = commits[0].getId().getName();

		final boolean UP_TO_DATE = jgitHelper.hasTag("processedTestResults/"
				+ commitId0);

		final String message;

		if (UP_TO_DATE) {

			message = PROJECT_NAME + " is up-to-date: Last " + PATH
					+ " commit has been tagged.";

		} else {

			message = PROJECT_NAME + " is out of date: Last " + PATH
					+ " commit has not been tagged yet.";
		}

		System.out.println(message);

		System.out.println();

		System.out.println("All commits for " + PATH + ":");

		System.out.println();

		for (int i = 0; i < commits.length; ++i) {

			if (i != 0 && i % 5 == 0) {
				System.out.println(StringUtils.repeat('-', 60));
			}

			final String commitId = commits[i].getId().getName();

			final boolean hasTag = jgitHelper.hasTag("processedTestResults/"
					+ commitId);

			System.out.print("    " + commitId);

			if (hasTag) {
				System.out.print(" + tag");
			}

			System.out.println();
		}

		System.out.println();
		
		if (!UP_TO_DATE) {
			fail(message);
		}
	}
}
