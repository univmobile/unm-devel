package fr.univmobile.testutil;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.CharEncoding.UTF_8;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.apache.commons.io.FileUtils;

public class XMLDumper implements Dumper {

	private boolean inStartElement = true;

	private Dumper currentChild = null;

	@Override
	public Dumper addElement(final String name) throws IOException {

		if (currentChild != null) {

			currentChild.close();
		}

		if (inStartElement) {

			inStartElement = false;

			pw.println(">");
		}

		currentChild = new XMLDumper(name, this, pw);

		return currentChild;
	}

	@Override
	public Dumper addAttribute(final String name, final Object value)
			throws IOException {

		if (!inStartElement) {

			throw new IllegalStateException("Cannot add attribute: @" + name
					+ " when element.start is closed.");
		}

		if (value != null) {

			pw.print(" " + name + "=\"" + value.toString() //
					.replace("&", "&amp;") //
					.replace("<", "&lt;") //
					.replace(">", "&gt;") //
					.replace("\"", "&quot;") //
					+ "\"");
		}

		return this;
	}

	@Override
	public Dumper addXMLFragment(final File file) throws IOException {

		if (currentChild != null) {

			currentChild.close();
		}

		if (inStartElement) {

			inStartElement = false;

			pw.println(">");
		}

		checkNotNull(file, "file");

		final String xmlContent = FileUtils.readFileToString(file, UTF_8);

		if (xmlContent.contains("<?xml")) {
			throw new IOException("Not implemented: [<?xml] in XML fragment.");
		}

		pw.println(xmlContent);

		return this;
	}

	@Override
	public Dumper close() throws IOException {

		// TODO add thread safety

		if (currentChild != null) {

			currentChild.close();
		}

		if (inStartElement) {

			inStartElement = false;

			pw.println("/>");

		} else if (!isClosed) {

			pw.println("</" + name + ">");
		}

		pw.flush();
		
		isClosed = true;

		return parent;
	}

	private boolean isClosed = false;
	private final String name;
	private final Dumper parent;

	private XMLDumper(final String name, final Dumper parent,
			final PrintWriter pw) {

		this.name = checkNotNull(name, "name");
		this.parent = checkNotNull(parent, "parent");
		this.pw = checkNotNull(pw, "pw");

		pw.print("<" + name);
	}

	private final PrintWriter pw;

	public static Dumper newXMLDumper(final String rootElementName,
			final File outFile) throws IOException {

		return new XMLRootDumper(rootElementName, outFile);
	}

	private static final class XMLRootDumper implements Dumper {

		private XMLRootDumper(final String rootElementName, final File outFile)
				throws IOException {

			FileUtils.forceMkdir(outFile.getParentFile());

			os = new FileOutputStream(outFile);

			final boolean AUTO_FLUSH = true;

			pw = new PrintWriter(new OutputStreamWriter(os, UTF_8), AUTO_FLUSH);

			rootDumper = new XMLDumper(rootElementName, this, pw);
		}

		private final Dumper rootDumper;

		@Override
		public Dumper addElement(final String name) throws IOException {

			return rootDumper.addElement(name);
		}

		@Override
		public Dumper addAttribute(final String name, final Object value)
				throws IOException {

			return rootDumper.addAttribute(name, value);
		}

		@Override
		public Dumper addXMLFragment(final File file) throws IOException {

			return rootDumper.addXMLFragment(file);
		}

		@Override
		public Dumper close() throws IOException {

			rootDumper.close();

			pw.flush();

			os.close();

			return null;
		}

		private final PrintWriter pw;

		private final OutputStream os;
	}
}
