package fr.univmobile.testutil;

import java.io.IOException;

public interface Dumper {

	Dumper addElement(String name) throws IOException;

	Dumper addAttribute(String name, Object value) throws IOException;

	Dumper close() throws IOException;
}
