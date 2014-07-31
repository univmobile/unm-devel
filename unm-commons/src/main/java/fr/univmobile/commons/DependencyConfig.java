package fr.univmobile.commons;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;

class DependencyConfig {

	public DependencyConfig(final Class<?> injectClass, //

			final boolean isIntoFactory, //
			final Class<?> intoClass, //
			@Nullable final String intoClassName, //

			final boolean isImplFactory, //
			@Nullable final Class<?> implClass, //
			@Nullable final String implClassName, //
			@Nullable final Object implInstance) {

		this.isIntoFactory = isIntoFactory;
		this.isImplFactory = isImplFactory;

		this.injectClass = checkNotNull(injectClass, "injectClass");
		this.intoClass = checkNotNull(intoClass, "intoClass");
		this.implClass = implClass;

		this.intoClassName = intoClassName;
		this.implClassName = implClassName;
		this.implInstance = implInstance;
	}

	public final boolean isIntoFactory;
	public final boolean isImplFactory;

	public final Class<?> injectClass;
	public final Class<?> intoClass;
	@Nullable
	public final Class<?> implClass;

	@Nullable
	public final String intoClassName;
	@Nullable
	public final String implClassName;
	@Nullable
	public final Object implInstance;
}
