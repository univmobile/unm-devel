package fr.univmobile.commons;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Method;

public abstract class ReflectUtils {

	public static boolean hasMethod(final Class<?> clazz,
			final String methodName) {

		return hasMethod(clazz, methodName, 0);
	}

	public static boolean hasMethod(final Class<?> clazz,
			final String methodName, final int argCount) {

		checkNotNull(clazz, "clazz");
		checkNotNull(methodName, "methodName");

		for (final Method method : clazz.getMethods()) {

			if (methodName.equals(method.getName())
					&& method.getParameterTypes().length == argCount) {

				return true;
			}
		}

		return false;
	}
}
