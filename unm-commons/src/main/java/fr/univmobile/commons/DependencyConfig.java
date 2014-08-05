package fr.univmobile.commons;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;

import javax.annotation.Nullable;

import com.avcompris.lang.NotImplementedException;

class DependencyConfig {

	@Override
	public String toString() {

		final StringBuilder sb = new StringBuilder();

		sb.append("inject:");
		sb.append(injectClass == null ? null : injectClass.getSimpleName());
		sb.append(":");
		sb.append(injectName);
		sb.append(" into:");
		sb.append(intoClass == null ? null : intoClass.getSimpleName());
		sb.append(":");
		sb.append(intoName);
		sb.append(" = ");
		sb.append(implClass == null ? null : implClass.getSimpleName());
		sb.append(":");
		sb.append(implName);
		sb.append(" (").append(implInstance).append(")");

		return sb.toString();
	}

	public DependencyConfig( //

			final Class<?> injectClass, //
			@Nullable final String injectName,

			// final boolean injectRef,

			final boolean isIntoFactory, //
			@Nullable final Class<?> intoClass, //
			@Nullable final String intoName, //

			final boolean isImplFactory, //
			@Nullable final Class<?> implClass, //
			@Nullable final String implName, //
			@Nullable final FutureInstance<?> implInstance) {

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
		// this.injectRef = injectRef;
		this.intoName = intoName;
		this.implName = implName;
		this.implInstance = implInstance;
	}

	public final boolean isIntoFactory;
	public final boolean isImplFactory;

	public final Class<?> injectClass;
	// public final boolean injectRef;
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
	public final FutureInstance<?> implInstance;
}

class FutureInstance<E> {

	private final Class<E> injectClass;
	private final String value;

	@Override
	public String toString() {

		return injectClass.getSimpleName() + "=" + value;
	}

	public static <E> FutureInstance<E> create(final Class<E> injectClass,
			final String value) {

		return new FutureInstance<E>(injectClass, value);
	}

	private FutureInstance(final Class<E> injectClass, final String value) {

		this.injectClass = checkNotNull(injectClass, "injectClass");
		this.value = checkNotNull(value, "value");

		if (String.class.equals(injectClass)) {

			// OK

		} else if (File.class.equals(injectClass)) {

			// OK

		} else if (Class.class.equals(injectClass)) {

			if (!value.startsWith("class:")) {
				throw new IllegalArgumentException(
						"For parameters of type Class, value must start with prefix \"class:\": \"class:"
								+ value + "\"");
			}

			// OK

		} else {

			throw new NotImplementedException("Ref type: "
					+ injectClass.getName() + ", value: " + value);
		}
	}

	public <F> F getActualInstance(
			final DependencyInjection dependencyInjection,
			final Class<F> instanceClass) {

		if (!instanceClass.isAssignableFrom(injectClass)) {
			throw new IllegalArgumentException("instanceClass: "
					+ instanceClass.getName() + " should be a subclass of: "
					+ injectClass.getName());
		}

		return instanceClass.cast(getActualInstance(dependencyInjection));
	}

	public E getActualInstance(final DependencyInjection dependencyInjection) {

		final String filteredValue = dependencyInjection.filter(value);

		if (String.class.equals(injectClass)) {

			return injectClass.cast(filteredValue);

		} else if (File.class.equals(injectClass)) {

			return injectClass.cast(new File(filteredValue));

		} else if (Class.class.equals(injectClass)) {

			return injectClass.cast(new File(filteredValue));

		} else {

			throw new NotImplementedException("Ref type: "
					+ injectClass.getName() + ", value: " + value);
		}
	}
}

/*
 * private static FutureInstance futureInstance(final Class<?> injectClass,
 * final String value) {
 * 
 * final Object implInstance;
 * 
 * 
 * /* } else if (String.class.equals(injectClass)) {
 * 
 * implInstance = value;
 * 
 * } else if (File.class.equals(injectClass)) {
 * 
 * implInstance = new File(value);
 * 
 * } else if (Class.class.equals(injectClass)) {
 * 
 * if (!value.startsWith("class:")) { throw new IllegalArgumentException(
 * "For parameters of type Class, value must start with prefix \"class:\": \"class:"
 * + value + "\""); }
 * 
 * implInstance = lookupClass(injectPackages, substringAfter(value, "class:"));
 * 
 * } else {
 * 
 * throw new NotImplementedException("Factory param type: " + injectClassName +
 * ", value: " + value);
 */

/*
 * 
 * if (String.class.equals(injectClass)) {
 * 
 * implInstance = value;
 * 
 * } else if (File.class.equals(injectClass)) {
 * 
 * implInstance = new File(value);
 * 
 * } else {
 * 
 * throw new NotImplementedException("Ref type: " + injectClassName +
 * ", value: " + value); } }
 */
