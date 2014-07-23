package fr.univmobile.web.commons;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

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

		checkedRequest().setAttribute(name, value);
	}

	private HttpServletRequest checkedRequest() {

		final HttpServletRequest request = threadLocalRequest.get();

		if (request == null) {
			throw new IllegalStateException("ThreadLocal.request == null");
		}

		return request;
	}
}
