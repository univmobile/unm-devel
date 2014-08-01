package fr.univmobile.it.commons;


public abstract class AppiumIOSEnabledTest extends WebDriverEnabledTest {

	@Override
	final WebDriverEnabledTestEngine newEngine(final boolean useSafari) {

		return new AppiumEnabledTestDefaultEngine_iOS(useSafari);
	}
}
