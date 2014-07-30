package fr.univmobile.commons;

import static com.google.common.base.Preconditions.checkNotNull;

class DependencyConfig {

	public DependencyConfig(final Class<?> injectClass, //
			final Class<?> intoClass, //
			final Class<?> implClass) {

		this.injectClass = checkNotNull(injectClass, "injectClass");
		this.intoClass = checkNotNull(intoClass, "intoClass");
		this.implClass = checkNotNull(implClass, "implClass");
	}

	public final Class<?> injectClass;
	public final Class<?> intoClass;
	public final Class<?> implClass;
}
