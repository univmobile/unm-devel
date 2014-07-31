package fr.univmobile.it.commons;


public abstract class AppiumIOSEnabledTest extends AppiumEnabledTest {

	@Override
	final AppiumEnabledTestEngine newEngine(final boolean useSafari) {

		return new AppiumEnabledTestDefaultEngine_iOS(useSafari);
	}
}
