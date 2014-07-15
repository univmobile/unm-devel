package fr.univmobile.testutil;

import java.io.File;
import java.io.IOException;

public interface Dumper {

	Dumper addElement(String name) throws IOException;

	Dumper addAttribute(String name, Object value) throws IOException;

	Dumper addXMLFragment(File file) throws IOException;
	
	Dumper close() throws IOException;
}
