package fr.univmobile.web.commons;

import static org.apache.commons.lang3.CharEncoding.UTF_8;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.substringBeforeLast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.avcompris.common.annotation.Nullable;

public abstract class AbstractUnivMobileServlet extends HttpServlet {

	/**
	 * for serialization.
	 */
	private static final long serialVersionUID = 9153876255701077089L;

	/**
	 * the controllers.
	 */
	@Nullable
	private Iterable<AbstractController> controllers;

	@Override
	public abstract void init() throws ServletException;

	protected final void init(final Class<? extends AbstractController> c0)
			throws ServletException {

		final List<Class<? extends AbstractController>> controllers = new ArrayList<Class<? extends AbstractController>>();

		controllers.add(c0);

		init(controllers);
	}

	protected final void init(final Class<? extends AbstractController> c0,
			final Class<? extends AbstractController> c1)
			throws ServletException {

		final List<Class<? extends AbstractController>> controllers = new ArrayList<Class<? extends AbstractController>>();

		controllers.add(c0);
		controllers.add(c1);

		init(controllers);
	}

	private void init(
			final Iterable<Class<? extends AbstractController>> controllerClasses)
			throws ServletException {

		super.init();

		final List<AbstractController> controllers = new ArrayList<AbstractController>();

		this.controllers = controllers;

		final ServletContext servletContext = getServletContext();

		for (final Class<? extends AbstractController> controllerClass : controllerClasses) {

			final AbstractController controller;

			try {

				controller = controllerClass.newInstance();

			} catch (final IllegalAccessException e) {

				e.printStackTrace();

				throw new ServletException("Cannot instantiate: "
						+ controllerClass, e);

			} catch (final InstantiationException e) {

				e.printStackTrace();

				throw new ServletException("Cannot instantiate: "
						+ controllerClass, e);
			}

			controllers.add(controller.init(servletContext));
		}

		final ServletConfig servletConfig = getServletConfig();

		String baseURL = servletConfig.getInitParameter("baseURL");

		if (isBlank(baseURL)) {

			System.err.println( //
					"UnivMobileServlet.init(): Cannot find servlet init parameter: baseURL"
							+ " -- Using servletContext.contextPath instead.");

			baseURL = servletContext.getContextPath();
		}

		baseURL = baseURL.trim();

		if (baseURL.contains("$") || baseURL.contains("{")
				|| baseURL.contains("}")) {
			throw new ServletException(
					"Servlet init parameter baseURL has not been filtered: "
							+ baseURL);
		}

		while (baseURL.endsWith("/")) {
			baseURL = substringBeforeLast(baseURL, "/");
		}

		servletContext.setAttribute("baseURL", baseURL);
	}

	@Override
	public final void service(final HttpServletRequest request,
			final HttpServletResponse response) throws IOException,
			ServletException {

		// 1. REQUEST

		request.setCharacterEncoding(UTF_8);

		final String uriPath = UnivMobileHttpUtils.extractUriPath(request);

		// 2. CONTROLLER

		String jspFilename = null;

		for (final AbstractController controller : controllers) {

			if (controller.hasPath(uriPath)) {

				controller.setThreadLocalRequest(request);

				jspFilename = controller.action();

				break;
			}
		}

		// 3. DISPATCH TO JSP

		if (jspFilename == null) {

			UnivMobileHttpUtils.sendError404(request, response, uriPath);

			return;
		}

		final String jspPath = "/WEB-INF/jsp/" + jspFilename;

		final RequestDispatcher rd = request.getRequestDispatcher(jspPath);

		response.setContentType("text/html");
		response.setCharacterEncoding(UTF_8);
		// response.setHeader("Content-Language", "en");
		response.setLocale(Locale.ENGLISH);

		rd.forward(request, response);
	}
}