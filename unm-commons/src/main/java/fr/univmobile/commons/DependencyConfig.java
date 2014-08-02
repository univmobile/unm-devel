package fr.univmobile.commons;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;

class DependencyConfig {

	@Override
	public String toString() {

		return "inject:" + injectClass + ":" + injectName //
				+ " into:" + intoClass + ":" + intoName //
				+ " = " + implClass + ":" + implName;
	}

	public DependencyConfig( //

			final Class<?> injectClass, //
			@Nullable final String injectName,
			final boolean injectRef,

			final boolean isIntoFactory, //
			@Nullable final Class<?> intoClass, //
			@Nullable final String intoName, //

			final boolean isImplFactory, //
			@Nullable final Class<?> implClass, //
			@Nullable final String implName, //
			@Nullable final Object implInstance) {

		this.isIntoFactory = isIntoFactory;
		this.isImplFactory = isImplFactory;

		this.injectClass = checkNotNull(injectClass, "injectClass");
		this.intoClass = intoClass;
		this.implClass = implClass;

		if (intoClass == null && intoName == null) {
			throw new IllegalArgumentException(
					"intoClass == null && intoName == null");
		}

		this.injectName = injectName;
		this.injectRef=injectRef;
		this.intoName = intoName;
		this.implName = implName;
		this.implInstance = implInstance;
	}

	public final boolean isIntoFactory;
	public final boolean isImplFactory;

	public final Class<?> injectClass;
	public final boolean injectRef;
	@Nullable
	public final Class<?> intoClass;
	@Nullable
	public final Class<?> implClass;

	@Nullable
	public final String injectName;
	@Nullable
	public final String intoName;
	@Nullable
	public final String implName;
	@Nullable
	public final Object implInstance;
}
