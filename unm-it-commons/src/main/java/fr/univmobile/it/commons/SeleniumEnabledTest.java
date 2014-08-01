package fr.univmobile.it.commons;

public abstract class SeleniumEnabledTest extends WebDriverEnabledTest {

	public String getDefaultBrowser() {

		return "*firefox";
	}

	@Override
	final WebDriverEnabledTestEngine newEngine(final boolean useSafari) {

		return new SeleniumEnabledTestDefaultEngine(getDefaultBrowser());
	}
}
