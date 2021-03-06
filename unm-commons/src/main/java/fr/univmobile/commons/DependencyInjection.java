package fr.univmobile.commons;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static org.apache.commons.lang3.StringUtils.substringBetween;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.avcompris.lang.NotImplementedException;
import com.google.common.collect.Iterables;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;

/**
 * Initialize a {@link DependencyInjection} object with a configuration that
 * takes the form of a map of Strings (such as init-params in a web.xml file).
 * The syntax is the following:
 * <ul>
 * <li>Keys: of the form "<code>inject:Xxx into:Yyy</code>"
 * <li>Values: of the form "<code>XxxImpl</code>" (or "<code>ZzzImpl</code>
 * " that implements "<code>Xxx</code>")
 * </ul>
 * <p>
 * "<code>XxxImpl | ZzzImpl</code>" must be a concrete class, and subclass or
 * implements "<code>Xxx</code>". It will be the actual implementation injected
 * into the "<code>Yyy</code>" class in lieu of its " <code>Xxx</code>"
 * parameter.
 * <p>
 * "<code>Yyy</code>" must be a concrete class with a constructor that takes a "
 * <code>Xxx</code>" object.
 * <p>
 * Also, you can inject a factory, for instance, using instead of "
 * <code>Yyy</code>": "<code>factory:YyyFactory</code>"
 * <p>
 * Also, you can inject a global ref with: "<code>inject:Xxx ref:aaa</code>
 * ", where "<code>aaa</code>" is a name for the ref you will reuse afterwards,
 * by using: "<code>${ref:aaa}</code>" in impl values.
 * <p>
 * Also, you can inject a class that subclasses a certain type, for instance,
 * using instead of "<code>inject:Xxx</code>": " <code>inject:class:Uuu</code>"
 * <p>
 * Also, you can name your injected objects (and reuse the name aftewards), for
 * instance, using instead of "<code>ZzzImpl</code>": "
 * <code>ZzzImpl:toto</code>", then reusing it in an injection declaration: "
 * <code>inject:... into:ZzzImpl:toto</code>"
 * <p>
 * Note, in order to ease the writing of the configuration, you can use a
 * special parameter, named "<code>inject-packages</code>", with a list of
 * packages to use as prefixes when looking up the classes from their declared
 * names.
 */
public class DependencyInjection {

	public DependencyInjection(final Map<String, String> map) {

		this(mapToConfig(map));
	}

	private static class Config {

		public final String[] injectPackages;
		public final DependencyConfig[] configs;

		public Config(final String[] injectPackages,
				final DependencyConfig[] configs) {

			this.injectPackages = checkNotNull(injectPackages, "injectPackages");
			this.configs = checkNotNull(configs, "configs");
		}
	}

	private final Config config;

	private DependencyInjection(final Config config) {

		this.config = checkNotNull(config, "config ");

		configs = config.configs;

		final AbstractModule module = new AbstractModule() {

			@Override
			protected void configure() {

				for (final DependencyConfig config : configs) {

					if (config.intoClass == null) {

						// do nothing. Named ref.

					} else if (config.isImplFactory) {

						bindToFactory(config.injectClass, config.implClass,
								config.implName);

					} else if (config.isIntoFactory) {

						// do nothing. Hardcoded params have already been read.

					} else if (config.implInstance != null) {

						bindToInstance(config.injectClass, config.injectName,
								config.intoClass, config.implInstance);

					} else if (!config.isIntoFactory && !config.isImplFactory) {

						bindTo(config.injectClass, config.injectName,
								config.intoClass, config.implClass);

					} else {

						throw new NotImplementedException("Illegal config: "
								+ config);
					}
				}
			}

			private <I> void bindTo(final Class<I> injectClass,
					@Nullable final String injectName,
					final Class<?> intoClass, final Class<?> implClass) {

				// String named = null;

				if (injectName != null) {

					// named = injectName;

				} else {

					final String named = getNamedAnnotationValue(injectClass,
							intoClass);

					final Class<? extends I> targetClass = implClass
							.asSubclass(injectClass);

					if (named == null) {

						bind(injectClass).to(targetClass);

					} else {

						bind(injectClass).annotatedWith(Names.named(named)).to(
								targetClass);
					}
				}
			}

			private <I> void bindToInstance(final Class<I> injectClass,
					@Nullable final String injectName,
					final Class<?> intoClass,
					final FutureInstance<?> futureInstance) {

				checkNotNull(futureInstance, "futureInstance");

				final I value = futureInstance.getActualInstance(
						DependencyInjection.this, injectClass);

				final String named = getNamedAnnotationValue(injectClass,
						intoClass);

				if (named == null) {

					bind(injectClass).toInstance(value);

				} else {

					bind(injectClass).annotatedWith(Names.named(named))
							.toInstance(value);
				}
			}

			private <I> void bindToFactory(final Class<I> injectClass,
					final Class<?> implClass, @Nullable final String factoryName) {

				bind(injectClass).toInstance(
						invokeFactory(injectClass, implClass, factoryName));
			}
		};

		injector = Guice.createInjector(module);
	}

	@Nullable
	private static String getNamedAnnotationValue(final Class<?> injectClass,
			final Class<?> intoClass) {

		for (final Constructor<?> constructor : intoClass.getConstructors()) {

			if (!constructor.isAnnotationPresent(Inject.class)) {
				continue;
			}

			final Annotation[][] paramAnnotations = constructor
					.getParameterAnnotations();

			final Class<?>[] paramTypes = constructor.getParameterTypes();

			for (int i = 0; i < paramTypes.length; ++i) {

				if (!injectClass.equals(paramTypes[i])) {
					continue;
				}

				for (int j = 0; j < paramAnnotations[i].length; ++j) {

					final Annotation paramAnnotation = paramAnnotations[i][j];

					if (Named.class.equals(paramAnnotation.annotationType())) {

						return ((Named) paramAnnotation).value();
					}
				}
			}
		}

		return null;
	}

	public Class<?> lookupClass(final String className) {

		return lookupClass(config.injectPackages, className);
	}

	private <T> T invokeFactory(final Class<T> injectClass,
			final Class<?> factoryClass, @Nullable final String factoryName) {

		// Here, we are using static methods, not Guice.

		Method factoryMethod = null;

		for (final Method method : factoryClass.getMethods()) {

			if (!Modifier.isStatic(method.getModifiers())) {
				continue;
			}

			if (!method.getReturnType().isAssignableFrom(injectClass)) {
				continue;
			}

			factoryMethod = method;

			break;
		}

		if (factoryMethod == null) {
			throw new RuntimeException("Cannot find static factory method in: "
					+ factoryClass.getName() + ":" + factoryName
					+ " for inject:" + injectClass.getName());
		}

		final Class<?>[] paramTypes = factoryMethod.getParameterTypes();

		final Object args[] = new Object[paramTypes.length];

		for (int i = 0; i < paramTypes.length; ++i) {

			final Class<?> paramType = paramTypes[i];

			args[i] = loadFactoryParam(factoryClass, factoryName, paramType);
		}

		final Object instance;

		try {

			instance = factoryMethod.invoke(null, args);

		} catch (final InvocationTargetException e) {
			throw new RuntimeException(e.getTargetException());
		} catch (final IllegalAccessException e) {
			throw new RuntimeException(e);
		}

		return injectClass.cast(instance);
	}

	private Object loadFactoryParam(final Class<?> factoryClass,
			@Nullable final String factoryName, final Class<?> paramType) {

		for (final DependencyConfig config : configs) {

			if (!paramType.equals(config.injectClass)) {
				continue;
			}

			if (!factoryClass.equals(config.intoClass)) {
				continue;
			}

			if (factoryName != null) {
				if (!factoryName.equals(config.intoName)) {
					continue;
				}
			} else if (config.intoName != null) {
				continue;
			}

			if (config.implInstance == null) {
				throw new IllegalStateException("Factory param is null for: "
						+ factoryClass.getName() + ":" + factoryName
						+ ", paramType: " + paramType.getName());
			}

			return config.implInstance.getActualInstance(this);
		}

		throw new IllegalStateException("Cannot find param config fot: "
				+ factoryClass.getName() + ":" + factoryName + ", paramType: "
				+ paramType.getName());
	}

	private final Injector injector;

	private final DependencyConfig[] configs;

	public <E> DependencyInject<E> getInject(final Class<E> injectClass) {

		checkNotNull(injectClass, "injectClass");

		for (final DependencyConfig config : configs) {

			if (injectClass.equals(config.injectClass)) {

				return new DependencyInject<E>(injectClass);
			}
		}

		throw new RuntimeException("Cannot find config for inject:"
				+ injectClass.getName());
	}

	public class DependencyInject<E> {

		private DependencyInject(final Class<E> injectClass) {

			this.injectClass = checkNotNull(injectClass, "injectClass");
		}

		private final Class<E> injectClass;

		public E into(final Class<?> intoClass) {

			checkNotNull(intoClass, "intoClass");

			return injector.getInstance(injectClass);
		}

		public E ref(final String ref) {

			return getRef(injectClass, ref);
		}
	}

	private <E> E getRef(final Class<E> injectClass, final String ref) {

		checkNotNull(ref, "ref");

		if (log.isDebugEnabled()) {
			log.debug("getRef(" + injectClass.getName() + ", " + ref + ")");
		}

		for (final DependencyConfig config : configs) {

			if (config.intoClass == null && ref.equals(config.intoName)
					&& injectClass.equals(config.injectClass)) {

				if (!FutureInstance.class.isInstance(config.implInstance)) {

					throw new IllegalStateException(
							"implInstance should be a subclass of FutureInstance: "
									+ config.implInstance.getClass());
				}

				@SuppressWarnings("unchecked")
				final FutureInstance<E> implInstance = (FutureInstance<E>) config.implInstance;

				return implInstance.getActualInstance(this);
			}
		}

		throw new IllegalStateException("Ref not found: " + ref
				+ " for class: " + injectClass.getName());
	}

	private Object getRef(final String ref) {

		checkNotNull(ref, "ref");

		if (log.isDebugEnabled()) {
			log.debug("getRef(" + ref + ")");
		}

		for (final DependencyConfig config : configs) {

			if (config.intoClass == null && ref.equals(config.intoName)) {

				if (!FutureInstance.class.isInstance(config.implInstance)) {

					throw new IllegalStateException(
							"implInstance should be a subclass of FutureInstance: "
									+ config.implInstance.getClass());
				}

				return config.implInstance.getActualInstance(this);
			}
		}

		throw new IllegalStateException("Ref not found: " + ref);
	}

	public String filter(final String value) {

		if (!value.contains("${ref:")) {
			return value;
		}

		final StringBuilder sb = new StringBuilder();

		int pos = 0;

		while (true) {

			final int index = value.indexOf("${ref:", pos);

			if (index == -1) {
				break;
			}

			final int end = value.indexOf("}", index);

			sb.append(value.substring(pos, index));

			final Object ref = getRef(value.substring(index + 6, end));

			if (ref instanceof String) {

				sb.append(ref);

			} else if (ref instanceof File) {

				final File file = (File) ref;

				try {

					sb.append(file.getCanonicalPath());

				} catch (final IOException e) {

					log.error("Could not find file.canonicalPath: "
							+ file.getPath() + ", using file.path");

					sb.append(file.getPath());
				}

			} else {

				throw new IllegalStateException(
						"ref.class should be: String or File (ref: " + ref
								+ ", in value: " + value + ")");
			}

			pos = end + 1;
		}

		sb.append(value.substring(pos));

		return sb.toString();
	}

	private static Config mapToConfig(final Map<String, String> map) {

		checkNotNull(map, "map");

		final String[] injectPackages;

		final String injectPackagesParam = map.get("inject-packages");

		if (injectPackagesParam == null) {

			injectPackages = new String[] { "java.lang", "java.io" };

		} else {

			injectPackages = split("java.lang java.io " + injectPackagesParam);
		}

		final List<DependencyConfig> configs = new ArrayList<DependencyConfig>();

		for (final Map.Entry<String, String> entry : map.entrySet()) {

			final String key = entry.getKey().trim();
			final String value = entry.getValue().trim();

			if (!key.startsWith("inject:")) {
				continue;
			}

			if (log.isInfoEnabled()) {
				log.info("Loading " + key + " -> " + value);
			}

			final String[] s = split(key);

			final String injectClassName = substringAfter(s[0], "inject:");

			final String intoClassName = substringAfter(s[1], "into:");
			final String ref = substringAfter(s[1], "ref:");
			final String implClassName = value;

			final DependencyConfig config;

			final Class<?> injectClass = lookupClass(injectPackages,
					injectClassName);

			if (injectClassName.contains(":")
					&& !injectClassName.startsWith("factory:")) {
				throw new NotImplementedException("injectClassName: "
						+ injectClassName);
			}

			final String injectName = null;

			if (implClassName.startsWith("factory:")) {

				final Class<?> intoClass = lookupClass(injectPackages,
						intoClassName);

				final String implFactoryClassName;

				@Nullable
				final String implFactoryName;

				if (substringAfter(implClassName, "factory:").contains(":")) {
					implFactoryClassName = substringBetween(implClassName,
							"factory:", ":");
					implFactoryName = substringAfterLast(implClassName, ":");
				} else {
					implFactoryClassName = substringAfter(implClassName,
							"factory:");
					implFactoryName = null;
				}

				final Class<?> implFactoryClass = lookupClass(injectPackages,
						implFactoryClassName);

				config = new DependencyConfig( //
						injectClass, injectName, // injectRef,//
						false, intoClass, null, //
						true, implFactoryClass, implFactoryName, null);

			} else if (intoClassName.startsWith("factory:")) {

				final String intoFactoryClassName;
				@Nullable
				final String intoFactoryName;
				if (substringAfter(intoClassName, "factory:").contains(":")) {
					intoFactoryClassName = substringBetween(intoClassName,
							"factory:", ":");
					intoFactoryName = substringAfterLast(intoClassName, ":");
				} else {
					intoFactoryClassName = substringAfter(intoClassName,
							"factory:");
					intoFactoryName = null;
				}

				final Class<?> intoFactoryClass = lookupClass(injectPackages,
						intoFactoryClassName);

				// Hardcoded params.

				final FutureInstance<?> implInstance = FutureInstance.create(
						injectClass, value);

				config = new DependencyConfig( //
						injectClass, injectName, // injectRef, //
						true, intoFactoryClass, intoFactoryName, //
						false, null, null, implInstance);

			} else if (!isBlank(ref) && isBlank(intoClassName)) {

				// Hardcoded params.

				final FutureInstance<?> implInstance = FutureInstance.create(
						injectClass, value);

				config = new DependencyConfig( //
						injectClass, injectName,// injectRef, //
						false, null, ref, //
						false, null, null, implInstance);

			} else if (String.class.equals(injectClass)
					|| File.class.equals(injectClass)) {

				final Class<?> intoClass = lookupClass(injectPackages,
						intoClassName);

				final FutureInstance<?> implInstance = FutureInstance.create(
						injectClass, value);

				config = new DependencyConfig( //
						injectClass, injectName, // injectRef, //
						false, intoClass, null, //
						false, null, null, implInstance);

			} else if (Class.class.equals(injectClass)) {

				if (!value.startsWith("class:")) {
					throw new IllegalArgumentException(
							"For parameters of type Class, value must start with prefix \"class:\": \"class:"
									+ value + "\"");
				}

				final Class<?> intoClass = lookupClass(injectPackages,
						intoClassName);
				final Class<?> implInstance = lookupClass(injectPackages,
						substringAfter(value, "class:"));

				config = new DependencyConfig( //
						Class.class, injectName, // injectRef, //
						false, intoClass, null, //
						false, null, null, new FutureInstance<Class<?>>(
								implInstance));

			} else {

				final Class<?> intoClass = lookupClass(injectPackages,
						intoClassName);
				final Class<?> implClass = lookupClass(injectPackages,
						implClassName);

				config = new DependencyConfig( //
						injectClass, injectName, // injectRef, //
						false, intoClass, null, //
						false, implClass, null, null);
			}

			configs.add(config);
		}

		return new Config(injectPackages, //
				Iterables.toArray(configs, DependencyConfig.class));
	}

	private static final Log log = LogFactory.getLog(DependencyInjection.class);

	private static Class<?> lookupClass(final String[] injectPackages,
			final String className) {

		try {

			return Class.forName(className);

		} catch (final ClassNotFoundException e) {
			// do nothing
		}

		for (final String injectPackage : injectPackages) {

			final String fullClassName = injectPackage + '.' + className;

			try {

				return Class.forName(fullClassName);

			} catch (final ClassNotFoundException e) {
				// do nothing
			}
		}

		throw new RuntimeException("Cannot find class: " + className
				+ " in packages: " + join(injectPackages, ", "));
	}
}
