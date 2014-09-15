package fr.univmobile.it.commons;

import java.io.IOException;

import javax.annotation.Nullable;

public interface ElementChecker {

	void textShouldEqualTo(String ref) throws IOException;

	void textShouldContain(String ref) throws IOException;

	void textShouldNotContain(String ref) throws IOException;

	void attrShouldEqualTo(String name, String ref) throws IOException;

	void shouldBeVisible() throws IOException;

	void shouldBeHidden() throws IOException;

	void sendKeys(String keysToSend) throws IOException;

	void click() throws IOException;

	@Nullable
	String attr(String attrName) throws IOException;
}