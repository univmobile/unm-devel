package fr.univmobile.it.commons;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class SeleniumEnabledTest extends WebDriverEnabledTest {

	@Override
	final WebDriverEnabledTestEngine newEngine(final boolean useSafari) {

		final String defaultBrowser;

		try {

			defaultBrowser = loadDefaultBrowserName();

		} catch (final IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (final InvocationTargetException e) {
			throw new RuntimeException(e.getTargetException());
		}

		return new SeleniumEnabledTestDefaultEngine(defaultBrowser);
	}

	private String loadDefaultBrowserName() throws IllegalAccessException,
			InvocationTargetException {

		for (final Method method : this.getClass().getMethods()) {

			final BrowserNames browserNames = method
					.getAnnotation(BrowserNames.class);

			if (browserNames == null) {
				continue;
			}

			if (method.getParameterTypes().length != 0) {
				throw new IllegalStateException(
						"@BrowserNames-annotated method should have no parameter: "
								+ method);
			}

			final Class<?> returnType = method.getReturnType();

			if (String.class.equals(returnType)) {

				final String value = (String) method.invoke(this);

				if (value == null) {
					continue;
				}

				return value;
			}

			if (returnType.isArray()
					&& String.class.equals(returnType.getComponentType())) {

				final String[] values = (String[]) method.invoke(this);

				if (values == null) {
					continue;
				}

				for (final String value : values) {

					if (value != null) {
						return value;
					}
				}

				continue;
			}

			throw new IllegalStateException(
					"@BrowserNames-annotated method should only return String or String[]: "
							+ method);
		}

		System.out
				.println("Cannot find @BrowserNames-annotated method, using: "
						+ DEFAULT_BROWSER);

		return DEFAULT_BROWSER;
	}

	private static final String DEFAULT_BROWSER = "*firefox";
}
