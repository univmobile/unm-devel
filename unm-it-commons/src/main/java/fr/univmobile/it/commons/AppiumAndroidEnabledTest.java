package fr.univmobile.it.commons;


public abstract class AppiumAndroidEnabledTest extends WebDriverEnabledTest {

	@Override
	final WebDriverEnabledTestEngine newEngine(final boolean useSafari) {

		return new AppiumEnabledTestDefaultEngine_Android();
	}
}
