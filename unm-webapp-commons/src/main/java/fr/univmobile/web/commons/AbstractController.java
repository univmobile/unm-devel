package fr.univmobile.web.commons;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.avcompris.binding.helper.BinderUtils;

/**
 * The superclass for controllers. In addition to the abstract methids, each
 * controller must implement a constructor because of the
 * {@link ServletException} thrown by the default constructor of this
 * {@link AbstractController} class, and be annotated with the {@link Paths}
 * annotation to declare the paths the controller will respond to.
 */
public abstract class AbstractController {

	/**
	 * Handle a HTTP request, and return which JSP we should forward to.
	 * 
	 * @return the JSP filename, in WEB-INF/jsp/. e.g. "home.jsp"
	 */
	public abstract String action() throws IOException;

	final AbstractController init(final ServletContext servletContext)
			throws ServletException {

		this.servletContext = checkNotNull(servletContext, "servletContext");

		final Class<? extends AbstractController> thisClass = this.getClass();

		final Paths pathsAnnotation = thisClass.getAnnotation(Paths.class);

		if (pathsAnnotation == null) {
			throw new ServletException("AbstractController subclass: "
					+ thisClass.getName() + " should be annotated with: @Paths");
		}

		paths = pathsAnnotation.value();

		return this;
	}

	protected final ServletContext getServletContext() {

		return checkedServletContext();
	}

	private ServletContext checkedServletContext() {

		if (servletContext == null) {
			throw new IllegalStateException(
					"ActionController has not been initialized: servletContext == null: "
							+ this.getClass());
		}

		return servletContext;
	}

	private String[] checkedPaths() {

		if (paths == null) {
			throw new IllegalStateException(
					"ActionController has not been initialized: paths == null: "
							+ this.getClass());
		}

		return paths;
	}

	private ServletContext servletContext;
	private String[] paths;

	/**
	 * check that a given path is handled by this controller.
	 */
	public final boolean hasPath(final String path) {

		checkNotNull(path, "path");

		for (final String p : checkedPaths()) {

			if (path.equals(p)) {

				return true;
			}
		}

		return false;
	}

	private final ThreadLocal<HttpServletRequest> threadLocalRequest = new ThreadLocal<HttpServletRequest>();

	final void setThreadLocalRequest(final HttpServletRequest request) {

		checkNotNull(request, "request");

		threadLocalRequest.set(request);
	}

	/**
	 * Set an attribute into the underlying {@link HttpServletRequest} object.
	 */
	protected final void setAttribute(final String name, final Object value) {

		checkNotNull(name, "name");
		checkNotNull(value, "value");

		checkedRequest().setAttribute(name, value);
	}

	/**
	 * Set an attribute into the underlying {@link HttpSession} object.
	 */
	protected final void setSessionAttribute(final String name,
			final Object value) {

		checkNotNull(name, "name");
		checkNotNull(value, "value");

		checkedRequest().getSession().setAttribute(name,
				BinderUtils.detach(value));
	}

	protected final void removeSessionAttribute(final String name) {

		checkNotNull(name, "name");

		checkedRequest().getSession().removeAttribute(name);
	}

	protected final <T> T getSessionAttribute(final String name,
			final Class<T> clazz) {

		checkNotNull(name, "name");
		checkNotNull(clazz, "clazz");

		final Object value = checkedRequest().getSession().getAttribute(name);

		if (value == null) {
			throw new IllegalStateException("Session attribute is null: "
					+ name);
		}

		return clazz.cast(value);
	}

	protected final boolean hasSessionAttribute(final String name) {

		checkNotNull(name, "name");

		return checkedRequest().getSession().getAttribute(name) != null;
	}

	/**
	 * Get an attribute from the underlying {@link HttpServletRequest} object.
	 */
	protected final <T> T getAttribute(final String name, final Class<T> clazz) {

		checkNotNull(name, "name");

		final Object value = checkedRequest().getAttribute(name);

		if (value == null) {
			throw new IllegalStateException("Request attribute is null: "
					+ name);
		}

		return clazz.cast(value);
	}

	protected final String getRemoteUser() {

		return checkedRequest().getRemoteUser();
	}

	private HttpServletRequest checkedRequest() {

		final HttpServletRequest request = threadLocalRequest.get();

		if (request == null) {
			throw new IllegalStateException("ThreadLocal.request == null");
		}

		return request;
	}

	protected final <T> T getHttpInputs(final Class<T> clazz) {

		final HttpMethods httpMethods = clazz.getAnnotation(HttpMethods.class);

		final HttpServletRequest request = checkedRequest();

		if (httpMethods != null) {

			boolean validHttpMethod = false;

			for (final String httpMethod : httpMethods.value()) {

				if (httpMethod.equalsIgnoreCase(request.getMethod())) {

					validHttpMethod = true;

					break;
				}
			}

			if (!validHttpMethod) {

				return invalidHttpInputs(clazz);
			}
		}

		final Map<Method, String> httpParameterValues = new HashMap<Method, String>();

		for (final Method method : clazz.getMethods()) {

			final boolean required = method
					.isAnnotationPresent(HttpRequired.class);

			final HttpParameter httpParameter = method
					.getAnnotation(HttpParameter.class);

			if (httpParameter == null) {
				continue;
			}

			String httpParameterName = httpParameter.value();

			if (isBlank(httpParameterName)) {
				httpParameterName = method.getName();
			}

			String httpParameterValue = request.getParameter(httpParameterName);

			if (httpParameterValue == null) {

				if (required) {

					return invalidHttpInputs(clazz);
				}

			} else {

				if (httpParameter.trim()) {

					httpParameterValue = httpParameterValue.trim();
				}

				httpParameterValues.put(method, httpParameterValue);
			}
		}

		return validHttpInputs(clazz, httpParameterValues);
	}

	private static Map<Class<?>, Object> cachedInvalidHttpInputs = new HashMap<Class<?>, Object>();

	private static <T> T invalidHttpInputs(final Class<T> clazz) {

		checkNotNull(clazz, "clazz");

		final Object cached = cachedInvalidHttpInputs.get(clazz);

		if (cached != null) {
			return clazz.cast(cached);
		}

		final Object proxy = Proxy.newProxyInstance(
				AbstractController.class.getClassLoader(),
				new Class<?>[] { clazz }, new InvocationHandler() {

					@Override
					public Object invoke(final Object proxy,
							final Method method, final Object[] args)
							throws Throwable {

						if ("isHttpValid".equals(method.getName())) {

							return false;
						}

						throw new NoSuchMethodException(
								"Illegal call on an invalid HttpInputs: "
										+ method.getName() + "()");
					}
				});

		cachedInvalidHttpInputs.put(clazz, proxy);

		return clazz.cast(proxy);
	}

	private static <T> T validHttpInputs(final Class<T> clazz,
			final Map<Method, String> httpParameterValues) {

		checkNotNull(clazz, "clazz");
		checkNotNull(httpParameterValues, "httpParameterValues");

		final Object proxy = Proxy.newProxyInstance(
				AbstractController.class.getClassLoader(), new Class<?>[] {
						clazz, HttpParameterized.class },
				new InvocationHandler() {

					@Override
					public Object invoke(final Object proxy,
							final Method method, final Object[] args)
							throws Throwable {

						final String methodName = method.getName();

						if ("isHttpValid".equals(methodName)) {

							return true;
						}

						if ("httpParameterValues".equals(methodName)) {

							return httpParameterValues;
						}

						if (method.isAnnotationPresent(HttpParameter.class)) {

							return httpParameterValues.get(method);
						}

						throw new NoSuchMethodException(
								"Illegal call on a valid HttpInputs: "
										+ methodName + "()");
					}
				});

		return clazz.cast(proxy);
	}

	protected final boolean validate(final HttpInputs inputs,
			final String errorsName) {

		final HttpParameterized httpParameterized = (HttpParameterized) inputs;

		boolean foundErrors = false;

		for (final Map.Entry<Method, String> entry : httpParameterized
				.httpParameterValues().entrySet()) {

			final String httpParameterName = entry.getKey().getName();
			final String httpParameterValue = entry.getValue();

			if (isBlank(httpParameterValue)) {

				checkedRequest().setAttribute(
						errorsName + "_" + httpParameterName, true);

				foundErrors = true;
			}
		}

		return !foundErrors;
	}
}
