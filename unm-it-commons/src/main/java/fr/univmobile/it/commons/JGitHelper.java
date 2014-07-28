package fr.univmobile.it.commons;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.CharEncoding.UTF_8;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.google.common.collect.Iterables;

public class JGitHelper {

	/**
	 * @parameter gitDir the local directory named ".git"
	 */
	public JGitHelper(final File gitDir) throws IOException {

		checkNotNull(gitDir, "gitDir");

		if (!".git".equals(gitDir.getName())) {

			throw new IllegalArgumentException(
					"gitDir should be of the form /my/path/.git");
		}

		if (!gitDir.isDirectory()) {

			throw new FileNotFoundException("gitDir is not a directory: "
					+ gitDir.getPath());
		}

		this.gitDir = gitDir;

		final FileRepositoryBuilder builder = new FileRepositoryBuilder();

		repo = builder.setGitDir(gitDir).readEnvironment().findGitDir().build();

	}

	public final File gitDir;

	public final Repository repo;

	public void close() {

		repo.close();
	}

	private RevWalk getWalkFromHead() throws IOException {

		final ObjectId head = repo.resolve(Constants.HEAD);

		final RevWalk walk = new RevWalk(repo);

		final RevCommit root = walk.parseCommit(head);

		walk.markStart(root);

		// revWalk.setRetainBody(false);

		return walk;
	}

	public RevCommit[] getCommitsFromHead(final int max) throws IOException {

		final RevWalk walk = getWalkFromHead();

		final List<RevCommit> commits = new ArrayList<RevCommit>();

		int count = commits.size();

		for (final RevCommit commit : walk) {

			if (count >= max) {
				break;
			}

			commits.add(commit);

			++count;
		}

		return Iterables.toArray(commits, RevCommit.class);
	}

	public ObjectId getRevFileIdInCommit(final String filePath,
			final RevCommit commit) throws IOException {

		final TreeWalk treeWalk = TreeWalk.forPath(repo, filePath,
				commit.getTree());

		if (treeWalk == null) {
			return null;
		}

		treeWalk.setRecursive(true);

		final CanonicalTreeParser canonicalTreeParser = treeWalk.getTree(0,
				CanonicalTreeParser.class);

		if (canonicalTreeParser.eof()) {
			return null;
		}

		return canonicalTreeParser.getEntryObjectId();
	}

	public RevCommit[] getCommitsForFileFromHead(final String filePath,
			final int max) throws IOException {

		final RevWalk walk = getWalkFromHead();

		final Map<ObjectId, RevCommit> revFileIdCommits = new HashMap<ObjectId, RevCommit>();

		final List<RevCommit> commits = new ArrayList<RevCommit>();

		int count = commits.size();

		for (final RevCommit commit : walk) {

			if (count >= max) {
				break;
			}

			final ObjectId revFileId = getRevFileIdInCommit(filePath, commit);

			if (revFileId == null) {
				continue;
			}

			final RevCommit revFileIdCommit = revFileIdCommits.get(revFileId);

			if (revFileIdCommit != null) {

				commits.remove(revFileIdCommit);
			}

			revFileIdCommits.put(revFileId, commit);

			commits.add(commit);

			++count;
		}

		return Iterables.toArray(commits, RevCommit.class);
	}

	public RevCommit getCommitById(final String commitId) throws IOException {

		final RevWalk walk = new RevWalk(repo);

		final RevCommit commit = walk
				.parseCommit(ObjectId.fromString(commitId));

		return commit;
	}

	public byte[] loadRevFileContent(final ObjectId revFileId)
			throws IOException {

		final ObjectLoader loader = repo.open(revFileId);

		return loader.getBytes();
	}

	public RevTag getTag(final String tagName) throws IOException,
			GitAPIException {

		final String fullName = "refs/tags/" + tagName;

		final Git git = new Git(repo);

		for (final Ref ref : git.tagList().call()) {

			if (fullName.equals(ref.getName())) {

				final ObjectId objectId = ref.getObjectId();

				final RevWalk walk = new RevWalk(repo);

				return walk.parseTag(objectId);
			}
		}

		return null;
	}

	public boolean hasTag(final String tagName) throws IOException,
			GitAPIException {

		return getTag(tagName) != null;
	}

	public void addTagToCommit(final String tagName, final String commitId,
			final String message) throws GitAPIException, IOException {

		final Git git = new Git(repo);

		git.tag().setName(tagName).setObjectId(getCommitById(commitId))
				.setMessage(message).call();
	}

	public void pushTags() throws GitAPIException, IOException,
			URISyntaxException {

		final Git git = new Git(repo);

		// final RefSpec spec = new RefSpec("refs/tags/processTestResults/*");

		final String username = "dandriana-jenkins";
		final String password = FileUtils.readFileToString(
				new File(FileUtils.getUserDirectoryPath(), ".config/github-"
						+ username), UTF_8).trim();

		final CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(
				username, password);

		git.push().setRemote("origin").setPushTags()
				.setCredentialsProvider(credentialsProvider).call();
	}

	public static JGitHelper cloneRepo(final String url, final File destDir)
			throws IOException, InvalidRemoteException, TransportException,
			GitAPIException {

		return cloneRepo(url, "master", destDir);
	}

	public static JGitHelper cloneRepo(final String url, final String branch,
			final File destDir) throws IOException, InvalidRemoteException,
			TransportException, GitAPIException {

		System.out.println("Cloning git repo: " + url);
		System.out.println(" Cloning into: " + destDir.getCanonicalPath()
				+ "...");

		Git.cloneRepository().setBare(false).setCloneAllBranches(false)
				.setBranch(branch).setDirectory(destDir).setURI(url).call();

		final File gitDir = new File(destDir, ".git");

		return new JGitHelper(gitDir);
	}

	/**
	 * 
	 * @param name <em>cannot</em> be a tag name; Must be a branch name.
	 */
	public void checkout(final String name) throws RefAlreadyExistsException, RefNotFoundException, InvalidRefNameException, CheckoutConflictException, GitAPIException {

		final Git git = new Git(repo);

		git.checkout().setName(name).call();
	}

	/**
	 * 
	 * @param name can be a tag name.
	 */
	public void checkoutTag(final String tagName) throws RefAlreadyExistsException, RefNotFoundException, InvalidRefNameException, CheckoutConflictException, GitAPIException {

		final Git git = new Git(repo);

		git.checkout().addPath(tagName).call();
	}
}
