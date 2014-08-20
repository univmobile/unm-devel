package fr.univmobile.testutil;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

/**
 * Miscellaneous methods used by several tests.
 */
public abstract class TestUtils {

	/**
	 * Copy all XML test files in a directory under source control,
	 * into a temporary directory not in source control.
	 * This is used by the test class set-ups in unm-backend* projects.
	 */
	public static File copyDirectory(final File originalDataDir,
			final File tmpDataDir) throws IOException {

		boolean tmpDataDir_isInTargetDir = false;

		for (File dir = tmpDataDir.getParentFile(); dir != null; dir = dir
				.getParentFile()) {

			if ("target".equals(dir.getName())) {
				tmpDataDir_isInTargetDir = true;
				break;
			}
		}

		if (!tmpDataDir_isInTargetDir) {
			throw new IllegalArgumentException(
					"tmpDataDir should be in \"target/\" directory: "
							+ tmpDataDir.getCanonicalPath());
		}
		
		if (tmpDataDir.isDirectory()) {
			FileUtils.forceDelete(tmpDataDir);
		}

		FileUtils.copyDirectory(originalDataDir, tmpDataDir);

		return tmpDataDir;
	}
}
