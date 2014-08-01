package fr.univmobile.it.commons;

import java.io.IOException;

public interface ElementChecker {

	void textShouldEqualTo(String ref) throws IOException;

	void textShouldContain(String ref) throws IOException;

	void textShouldNotContain(String ref) throws IOException;

	void shouldBeVisible() throws IOException;

	void shouldBeHidden() throws IOException;

	void click() throws IOException;
}