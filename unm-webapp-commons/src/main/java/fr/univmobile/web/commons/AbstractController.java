package fr.univmobile.web.commons;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.NotImplementedException;

/**
 * The superclass for controllers. In addition to the abstract methods, each
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
	public abstract View action() throws Exception;

	public final AbstractController init(final AbstractUnivMobileServlet servlet)
			throws ServletException {

		checkNotNull(servlet, "servlet");

		return servlet.initController(this);
	}

	final AbstractController init(final String baseURL,
			final ServletContext servletContext) throws ServletException {

		this.baseURL = checkNotNull(baseURL, "baseURL");

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

		for (final String pattern : checkedPaths()) {

			if (pathMatches(pattern, path)) {

				return true;
			}
		}

		return false;
	}

	static boolean pathMatches(final String pattern, final String path) {

		checkNotNull(path, "path");
		checkNotNull(pattern, "pattern");

		if (path.equals(pattern) && !pattern.contains("${")) {
			return true;
		}

		if (!pattern.contains("${")) {
			return false;
		}

		// TODO: Use StringUtils.split("/")

		final String before = substringBefore(pattern, "${");
		final String after = substringAfter(pattern, "}");

		if (after.contains("${")) {
			throw new NotImplementedException("path: " + path);
		}

		final int beforeLength = before.length();
		final int afterLength = after.length();
		final int pathLength = path.length();

		if (pathLength < beforeLength || pathLength < afterLength) {
			return false;
		}

		if (before.equals(path.substring(0, beforeLength))
				&& after.equals(path.substring(pathLength - afterLength))) {

			final String extracted = path.substring(beforeLength, pathLength
					- afterLength);

			if (isBlank(extracted)) { // Forbid empty values
				return false;
			}

			if (extracted.contains("/")) { // TODO: Use StringUtils.split("/")
				return false;
			}

			return true;
		}

		return false;
	}

	/**
	 * @param pathVariable
	 *            e.g. <code>"${id}"</tt>
	 * if the {@link Paths} annotation declared some path such as
	 * <code>"pois/${id}"</code>.
	 */
	protected final int getPathIntVariable(final String pathVariable) {

		final String uriPath = UnivMobileHttpUtils
				.extractUriPath(checkedRequest());

		final String pattern = getPatternForUriPath(uriPath);

		return getPathIntVariable(uriPath, pattern, pathVariable);
	}

	private String getPatternForUriPath(final String uriPath) {

		for (final String pattern : checkedPaths()) {

			if (pathMatches(pattern, uriPath)) {

				return pattern;
			}
		}

		throw new IllegalStateException(
				"Cannot find matching pattern for uriPath: " + uriPath);
	}

	/**
	 * @param pathVariable
	 *            e.g. <code>"${id}"</tt>
	 * if the {@link Paths} annotation declared some path such as
	 * <code>"pois/${id}"</code>.
	 */
	protected final String getPathStringVariable(final String pathVariable) {

		final String uriPath = UnivMobileHttpUtils
				.extractUriPath(checkedRequest());

		final String pattern = getPatternForUriPath(uriPath);

		return getPathStringVariable(uriPath, pattern, pathVariable);
	}

	/**
	 * @param pathVariable
	 *            e.g. <code>"${id}"</tt>
	 * if the {@link Paths} annotation declared some path such as
	 * <code>"pois/${id}"</code>.
	 */
	protected final boolean hasPathStringVariable(final String pathVariable) {

		final String uriPath = UnivMobileHttpUtils
				.extractUriPath(checkedRequest());

		final String pattern = getPatternForUriPath(uriPath);

		return hasPathStringVariable(uriPath, pattern, pathVariable);
	}

	static int getPathIntVariable(final String uriPath,
			final String pathPattern, final String pathVariable) {

		final String extracted = getPathStringVariable(uriPath, pathPattern,
				pathVariable);

		final int value;

		try {

			value = Integer.parseInt(extracted);

		} catch (final NumberFormatException e) {
			throw new NumberFormatException("Cannot parse int \""
					+ pathVariable + "\" applying pattern \"" + pathPattern
					+ "\" to uriPath: " + uriPath);
		}

		return value;
	}

	static String getPathStringVariable(final String uriPath,
			final String pathPattern, final String pathVariable) {

		checkNotNull(uriPath, "uriPath");
		checkNotNull(pathPattern, "pathPattern");
		checkNotNull(pathVariable, "pathVariable");

		if (!pathVariable.startsWith("${") || !pathVariable.endsWith("}")) {
			throw new IllegalArgumentException(
					"pathVariable should be of the form \"${...}\": "
							+ pathVariable);
		}

		final String before = substringBefore(pathPattern, pathVariable);
		final String after = substringAfter(pathPattern, pathVariable);

		final String extracted = uriPath.substring(before.length(),
				uriPath.length() - after.length());

		if (isBlank(extracted)) {
			throw new IllegalArgumentException(
					"No variable can be extracted from uriPath: " + uriPath
							+ ", applying pattern: " + pathPattern);
		}

		return extracted;
	}

	static boolean hasPathStringVariable(final String uriPath,
			final String pathPattern, final String pathVariable) {

		checkNotNull(uriPath, "uriPath");
		checkNotNull(pathPattern, "pathPattern");
		checkNotNull(pathVariable, "pathVariable");

		if (!pathVariable.startsWith("${") || !pathVariable.endsWith("}")) {
			throw new IllegalArgumentException(
					"pathVariable should be of the form \"${...}\": "
							+ pathVariable);
		}

		final String before = substringBefore(pathPattern, pathVariable);
		final String after = substringAfter(pathPattern, pathVariable);

		final String extracted = uriPath.substring(before.length(),
				uriPath.length() - after.length());

		return isBlank(extracted) ? false : true;
	}

	private static final ThreadLocal<HttpServletRequestHolder> threadLocalRequest = new ThreadLocal<HttpServletRequestHolder>();

	private final class HttpServletRequestHolder {

		public HttpServletRequestHolder(final HttpServletRequest request,
				final HttpServletResponse response) {

			this.request = checkNotNull(request, "request");
			this.response = checkNotNull(response, "response");
		}

		public final HttpServletRequest request;
		public final HttpServletResponse response;
	}

	final void setThreadLocalRequest(final HttpServletRequest request,
			final HttpServletResponse response) {

		// checkNotNull(request, "request");
		// checkNotNull(response, "response");

		threadLocalRequest.set(new HttpServletRequestHolder(request, response));
	}

	protected final String getRemoteUser() {

		return checkedRequest().getRemoteUser();
	}

	final HttpServletRequest checkedRequest() {

		final HttpServletRequestHolder holder = threadLocalRequest.get();

		if (holder == null) {
			throw new IllegalStateException("ThreadLocal.request == null");
		}

		return holder.request;
	}

	final HttpServletResponse checkedResponse() {

		final HttpServletRequestHolder holder = threadLocalRequest.get();

		if (holder == null) {
			throw new IllegalStateException("ThreadLocal.request == null");
		}

		return holder.response;
	}

	protected final boolean isHttpGet() {

		return "GET".equalsIgnoreCase(checkedRequest().getMethod());
	}

	protected final boolean isHttpPost() {

		return "POST".equalsIgnoreCase(checkedRequest().getMethod());
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

		final Map<Method, Object> httpParameterValues = new HashMap<Method, Object>();

		for (final Method method : clazz.getMethods()) {

			final boolean required = method
					.isAnnotationPresent(HttpRequired.class);

			final HttpParameter httpParameter = method
					.getAnnotation(HttpParameter.class);

			if (httpParameter == null) {
				continue;
			}

			final Class<?> type = method.getReturnType();

			String httpParameterName = httpParameter.value();

			if (isBlank(httpParameterName)) {
				httpParameterName = method.getName();
			}

			String httpParameterValueStr = request
					.getParameter(httpParameterName);

			if (httpParameterValueStr == null) {

				if (required) {

					return invalidHttpInputs(clazz);
				}

			} else {

				if (httpParameter.trim()) {

					httpParameterValueStr = httpParameterValueStr.trim();
				}

				final Object httpParameterValue;

				if (String.class.equals(type)) {

					httpParameterValue = httpParameterValueStr;

				} else if (int.class.equals(type)) {

					try {

						httpParameterValue = Integer
								.parseInt(httpParameterValueStr);

					} catch (final NumberFormatException e) {
						return invalidHttpInputs(clazz);
					}

				} else {

					throw new RuntimeException("Unknown httpParameter type: "
							+ type.getClass());
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
			final Map<Method, Object> httpParameterValues) {

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

	protected final String getBaseURL() {

		if (baseURL == null) {
			throw new IllegalStateException(
					"ActionController has not been initialized: baseURL == null: "
							+ this.getClass());
		}

		return baseURL;
	}

	private String baseURL = null;

	/**
	 * Send a 400 (BAD REQUEST) HTTP error code.
	 * 
	 * @return <code>null</code>
	 */
	@Nullable
	protected final View sendError400() throws IOException {

		final HttpServletRequest request = checkedRequest();
		final HttpServletResponse response = checkedResponse();

		UnivMobileHttpUtils.sendError400(request, response);

		return null;
	}
}
