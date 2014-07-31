package fr.univmobile.it.commons;


public abstract class AppiumAndroidEnabledTest extends AppiumEnabledTest {

	@Override
	final AppiumEnabledTestEngine newEngine(final boolean useSafari) {

		return new AppiumEnabledTestDefaultEngine_Android();
	}
}
