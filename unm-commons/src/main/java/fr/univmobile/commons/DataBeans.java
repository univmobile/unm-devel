package fr.univmobile.commons;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.uncapitalize;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Nullable;

import com.google.common.collect.Iterables;

public abstract class DataBeans {

	public static <T> T instantiate(final Class<T> clazz) {

		checkNotNull(clazz, "clazz");

		final ClassLoader classLoader = clazz.getClassLoader();

		final InvocationHandler invocationHandler = //
		new DataBeanInvocationHandler(clazz);

		final Object proxy = Proxy
				.newProxyInstance(classLoader, new Class<?>[] { clazz,
						Serializable.class }, invocationHandler);

		return clazz.cast(proxy);
	}

	private static class DataBeanInvocationHandler implements InvocationHandler {

		public DataBeanInvocationHandler(final Class<?> clazz) {

			this.clazz = checkNotNull(clazz);

			// 1. VALIDATION: GETTERS RETURN TYPES == SETTERS PARAMETER TYPES

			for (final Method getter : getGetters()) {

				final Class<?> getterReturnType = getter.getReturnType();

				if (getterReturnType == null
						|| void.class.equals(getterReturnType)
						|| Void.class.equals(getterReturnType)) {
					throw new IllegalArgumentException("Getter returns void: "
							+ getter);
				}

				final String propertyName = getPropertyName(getter);

				final Method setter = getSetterForProperty(propertyName);

				if (setter == null) {
					continue;
				}

				final Class<?> setterParamType = setter.getParameterTypes()[0];

				if (!getterReturnType.isAssignableFrom(setterParamType)) {
					throw new IllegalArgumentException("Getter " + getter
							+ " and setter " + setter
							+ " do not share the same property type.");
				}
			}

			// 2. VALIDATION: SETTERS PARAMETER TYPES == GETTERS RETURN TYPES

			for (final Method setter : getSetters()) {

				// 2.1. SETTER RETURN TYPE == VOID OR INSTANCE_TYPE

				final Class<?> setterReturnType = setter.getReturnType();

				if (setterReturnType != null
						&& !void.class.equals(setterReturnType)
						&& !Void.class.equals(setterReturnType)
						&& !setterReturnType.isAssignableFrom(clazz)) {
					throw new IllegalArgumentException(
							"Setter should return void or " + clazz + ": "
									+ setter);
				}

				// 2.1. SETTER PARAMETER TYPE == GETTER RETURN TYPE

				final Class<?> setterParamType = setter.getParameterTypes()[0];

				final String propertyName = getPropertyName(setter);

				final Method getter = getGetterForProperty(propertyName);

				if (getter == null) {
					continue;
				}

				final Class<?> getterReturnType = getter.getReturnType();

				if (!getterReturnType.isAssignableFrom(setterParamType)) {
					throw new IllegalArgumentException("Getter " + getter
							+ " and setter " + setter
							+ " do not share the same property type.");
				}
			}
		}

		private final Class<?> clazz;

		private final Map<String, Object> properties = new HashMap<String, Object>();

		private Method[] getGetters() {

			final List<Method> getters = new ArrayList<Method>();

			for (final Method method : clazz.getMethods()) {

				if (method.getParameterTypes().length != 0) {
					continue;
				}

				final String methodName = method.getName();

				if ("getClass".equals(methodName)
						|| !methodName.startsWith("get")) {
					continue;
				}

				getters.add(method);
			}

			// TODO getGetters() results should go in a static cache.
			return sortMethods(getters);
		}

		private Method[] getSetters() {

			final List<Method> setters = new ArrayList<Method>();

			for (final Method method : clazz.getMethods()) {

				if (method.getParameterTypes().length != 1) {
					continue;
				}

				final String methodName = method.getName();

				if (!methodName.startsWith("set")) {
					continue;
				}

				setters.add(method);
			}

			// TODO getSetters() results should go in a static cache.
			return sortMethods(setters);
		}

		private static Method[] sortMethods(final Collection<Method> methods) {

			final Map<String, Method> map = new TreeMap<String, Method>();

			for (final Method method : methods) {

				map.put(method.getName(), method);
			}

			return Iterables.toArray(map.values(), Method.class);
		}

		private String getPropertyName(final Method accessor) {

			final String methodName = accessor.getName();

			return uncapitalize(methodName.substring(3));
		}

		@Nullable
		private Method getGetterForProperty(final String propertyName) {

			final String getterName = "get" + capitalize(propertyName);

			for (final Method getter : getGetters()) {

				if (getterName.equals(getter.getName())) {
					return getter;
				}
			}

			return null;
		}

		@Nullable
		private Method getSetterForProperty(final String propertyName) {

			final String setterName = "set" + capitalize(propertyName);

			for (final Method setter : getSetters()) {

				if (setterName.equals(setter.getName())) {
					return setter;
				}
			}

			return null;
		}

		@Override
		public Object invoke(final Object proxy, final Method method,
				final Object[] args) throws Throwable {

			final String methodName = method.getName();

			final boolean noArgs = args == null || args.length == 0;
			final boolean oneArg = !noArgs && args.length == 1;

			if ("toString".equals(methodName) && noArgs) {

				final StringBuilder sb = new StringBuilder("{");

				int count = 0;

				for (final Method getter : getGetters()) {

					if (count > 0) {
						sb.append(", ");
					}

					++count;

					final String propertyName = getPropertyName(getter);

					sb.append(propertyName).append(": ");

					final Object value = // getter.invoke(proxy); // !@Nullable
					properties.get(propertyName);

					sb.append(value);
				}

				sb.append("}");

				return sb.toString();
			}

			if ("hashCode".equals(methodName) && noArgs) {

				int hashCode = 0;

				for (final Method getter : getGetters()) {

					hashCode += getPropertyName(getter).hashCode() * 17;

					final Object value = getter.invoke(proxy);

					if (value != null) {

						hashCode += value.hashCode();
					}
				}

				return hashCode;
			}

			if ("equals".equals(methodName) && oneArg) {

				final Object o = args[0];

				if (o == null) {
					return false;
				}

				if (!proxy.getClass().equals(o.getClass())) {
					return null;
				}

				final DataBeanInvocationHandler t = (DataBeanInvocationHandler) Proxy
						.getInvocationHandler(o);

				for (final Method getter : getGetters()) {

					final String propertyName = getPropertyName(getter);

					final Object value = properties.get(propertyName);

					final Object tValue = t.properties.get(propertyName);

					if (value == tValue) {
						continue;
					}

					if (value != null && value.equals(tValue)) {
						continue;
					}

					return false;
				}

				return true;
			}

			if ("getClass".equals(methodName) && noArgs) {

				throw new IllegalStateException(
						"getClass() should not be accessible: " + clazz);
			}

			if (methodName.startsWith("get") && noArgs) {

				final String propertyName = getPropertyName(method);

				final Object value = properties.get(propertyName);

				if (value == null) {

					if (!method.isAnnotationPresent(Nullable.class)) {
						throw new IllegalStateException("Property "
								+ propertyName + " should not be null in: "
								+ clazz + " -- No @Nullable annotation on: "
								+ method);
					}
				}

				return value;
			}

			if (methodName.startsWith("set") && oneArg) {

				final String propertyName = getPropertyName(method);

				final Object value = args[0];

				if (value == null) {

					if (!method.isAnnotationPresent(Nullable.class)) {
						throw new IllegalArgumentException("Property "
								+ propertyName + " cannot be set to null in: "
								+ clazz + " -- No @Nullable annotation on: "
								+ method);
					}

					properties.remove(propertyName);

				} else {

					properties.put(propertyName, value);
				}

				final Class<?> setterReturnType = method.getReturnType();

				if (setterReturnType == null
						|| void.class.equals(setterReturnType)
						|| Void.class.equals(setterReturnType)) {
					return null;
				}

				return proxy;
			}

			throw new IllegalStateException("Cannot handle method: " + method
					+ " in class: " + clazz);
		}
	}
}
