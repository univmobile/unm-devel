package fr.univmobile.commons;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.StringUtils.substringAfter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Iterables;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class DependencyInjection {

	/**
	 * Initialize a {@link DependencyInjection} object with a configuration that
	 * takes the form of a map of Strings (such as init-params in a web.xml
	 * file). The syntax is the following:
	 * <ul>
	 * <li>Keys: of the form "<code>inject:Xxx into:Yyy</code>"
	 * <li>Values: of the form "<code>XxxImpl</code>" (or "<code>ZzzImpl</code>
	 * " that implements "<code>Xxx</code>")
	 * </ul>
	 * <p>
	 * "<code>XxxImpl | ZzzImpl</code>" must be a concrete class, and subclass
	 * or implements "<code>Xxx</code>". It will be the actual implementation
	 * injected into the "<code>Yyy</code>" class in lieu of its "
	 * <code>Xxx</code>" parameter.
	 * <p>
	 * "<code>Yyy</code>" must be a concrete class with a constructor that takes
	 * a "<code>Xxx</code>" object.
	 * <p>
	 * Also, you can inject a factory, for instance, using instead of "
	 * <code>Yyy</code>": "<code>factory:YyyFactory</code>"
	 * <p>
	 * Also, you can inject a class that subclasses a certain type, for
	 * instance, using instead of "<code>inject:Xxx</code>": "
	 * <code>inject:class:Uuu</code>"
	 * <p>
	 * Also, you can name your injected objects (and reuse the name aftewards),
	 * for instance, using instead of "<code>ZzzImpl</code>": "
	 * <code>ZzzImpl:toto</code>", then reusing it in an injection declaration:
	 * "<code>inject:... into:ZzzImpl:toto</code>"
	 * <p>
	 * Note, in order to ease the writing of the configuration, you can use a
	 * special parameter, named "<code>inject-packages</code>", with a list of
	 * packages to use as prefixes when looking up the classes from their
	 * declared names.
	 */
	public DependencyInjection(final Map<String, String> map) {

		this(mapToConfig(map));
	}

	private DependencyInjection(final DependencyConfig[] configs) {

		this.configs = checkNotNull(configs, "configs");

		final AbstractModule module = new AbstractModule() {

			@Override
			protected void configure() {

				for (final DependencyConfig config : configs) {

					bindTo(config.injectClass, config.implClass);
				}
			}

			private <I> void bindTo(final Class<I> injectClass,
					final Class<?> implClass) {

				bind(injectClass).to(implClass.asSubclass(injectClass));
			}
		};

		injector = Guice.createInjector(module);
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

			System.out.println(injector);

			return injector.getInstance(injectClass);
		}
	}

	private static DependencyConfig[] mapToConfig(final Map<String, String> map) {

		checkNotNull(map, "map");

		final String[] injectPackages;

		final String injectPackagesParam = map.get("inject-packages");

		if (injectPackagesParam == null) {

			injectPackages = new String[0];

		} else {

			injectPackages = split(injectPackagesParam);
		}

		final List<DependencyConfig> configs = new ArrayList<DependencyConfig>();

		for (final Map.Entry<String, String> entry : map.entrySet()) {

			final String key = entry.getKey().trim();
			final String value = entry.getValue().trim();

			if (!key.startsWith("inject:")) {
				continue;
			}

			final String[] s = split(key);

			final String injectClassName = substringAfter(s[0], "inject:");
			final String intoClassName = substringAfter(s[1], "into:");
			final String implClassName = value;

			final Class<?> injectClass = lookupClass(injectPackages,
					injectClassName);
			final Class<?> intoClass = lookupClass(injectPackages,
					intoClassName);
			final Class<?> implClass = lookupClass(injectPackages,
					implClassName);

			configs.add(new DependencyConfig(injectClass, intoClass, implClass));
		}

		return Iterables.toArray(configs, DependencyConfig.class);
	}

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
