package fr.univmobile.commons;

public abstract class FormatUtils {

	/**
	 * From: <a href=
	 * "http://stackoverflow.com/questions/3758606/how-to-convert-byte-size-
	 * into-human-readable-format-in-java/3758880#3758880
	 * ">http://stackoverflow.com/questions/3758606/how-to-convert-byte-size-
	 * into-human-readable-format-in-java/3758880</a>
	 * <table>
	 * <thead>
	 * <tr>
	 * <th>bytes</th>
	 * <th>SI</th>
	 * <th>BINARY</th>
	 * </tr>
	 * </thead> <tbody>
	 * <tr>
	 * <td>0</td>
	 * <td>0 B</td>
	 * <td>0 B</td>
	 * </tr>
	 * <tr>
	 * <td>27</td>
	 * <td>27 B</td>
	 * <td>27 B</td>
	 * </tr>
	 * <tr>
	 * <td>999</td>
	 * <td>999 B</td>
	 * <td>999 B</td>
	 * </tr>
	 * <tr>
	 * <td>1000</td>
	 * <td>1.0 kB</td>
	 * <td>1000 B</td>
	 * </tr>
	 * <tr>
	 * <td>1023</td>
	 * <td>1.0 kB</td>
	 * <td>1023 B</td>
	 * </tr>
	 * <tr>
	 * <td>1024</td>
	 * <td>1.0 kB</td>
	 * <td>1.0 KiB</td>
	 * </tr>
	 * <tr>
	 * <td>1728</td>
	 * <td>1.7 kB</td>
	 * <td>1.7 KiB</td>
	 * </tr>
	 * <tr>
	 * <td>110592</td>
	 * <td>110.6 kB</td>
	 * <td>108.0 KiB</td>
	 * </tr>
	 * <tr>
	 * <td>7077888</td>
	 * <td>7.1 MB</td>
	 * <td>6.8 MiB</td>
	 * </tr>
	 * <tr>
	 * <td>452984832</td>
	 * <td>453.0 MB</td>
	 * <td>432.0 MiB</td>
	 * </tr>
	 * <tr>
	 * <td>28991029248</td>
	 * <td>29.0 GB</td>
	 * <td>27.0 GiB</td>
	 * </tr>
	 * <tr>
	 * <td>1855425871872</td>
	 * <td>1.9 TB</td>
	 * <td>1.7 TiB</td>
	 * </tr>
	 * <tr>
	 * <td>9223372036854775807 (Long.MAX_VALUE)</td>
	 * <td>9.2 EB</td>
	 * <td>8.0 EiB</td>
	 * </tr>
	 * </tbody>
	 * </table>
	 * 
	 * @param si
	 *            If <code>true</code>, unit=1000, otherwise unit=1024.
	 */
	static String humanReadableByteCount(final long bytes,
			final boolean si) {

		final int unit = si ? 1000 : 1024;

		if (bytes < unit) {
			return bytes + " B";
		}

		final int exp = (int) (Math.log(bytes) / Math.log(unit));

		final String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1)
				+ (si ? "" : "i");

		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

	public static String formatMemory(final long bytes) {

		return humanReadableByteCount(bytes, true);
	}

	public static String formatFileLength(final long bytes) {

		// return FileUtils.byteCountToDisplaySize(bytes); // Not rounded up

		return humanReadableByteCount(bytes, true);
	}
}
