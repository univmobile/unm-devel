package fr.univmobile.web.commons;

import static org.apache.commons.lang3.CharEncoding.UTF_8;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.substringBeforeLast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

	protected final String getBaseURL() {

		if (baseURL == null) {
			throw new IllegalStateException("baseURL has not been initialized."
					+ " Make sure the init() method was called.");
		}

		return baseURL;
	}

	private String baseURL;

	protected final void init(final AbstractController... c)
			throws ServletException {

		super.init();

		final List<AbstractController> controllers = new ArrayList<AbstractController>();

		this.controllers = controllers;

		final ServletContext servletContext = getServletContext();

		final ServletConfig servletConfig = getServletConfig();

		baseURL = servletConfig.getInitParameter("baseURL");

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

		for (final AbstractController controller : c) {

			controllers.add(controller.init(baseURL, servletContext));
		}

		servletContext.setAttribute("baseURL", baseURL);
	}

	@Override
	public void service(final HttpServletRequest request,
			final HttpServletResponse response) throws IOException,
			ServletException {

		// 1. REQUEST

		request.setCharacterEncoding(UTF_8);

		final String uriPath = UnivMobileHttpUtils.extractUriPath(request);

		// 2. CONTROLLER

		View view = null;

		for (final AbstractController controller : controllers) {

			if (controller.hasPath(uriPath)) {

				controller.setThreadLocalRequest(request);

				try {
					
					view = controller.action();

				} catch (final PageNotFoundException e) {

					UnivMobileHttpUtils.sendError404(request, response, uriPath);

					return;

				} catch (final Exception e) {

					UnivMobileHttpUtils.sendError500(request, response, e);

					return;
				}

				break;
			}
		}

		// 3. DISPATCH TO JSP

		if (view == null) {

			UnivMobileHttpUtils.sendError404(request, response, uriPath);

			return;
		}

		final String jspPath = "/WEB-INF/jsp/" + view.jspFilename;

		final RequestDispatcher rd = request.getRequestDispatcher(jspPath);

		response.setContentType(view.contentType); // Note: Overwritten if JSP

		if (view.characterEncoding != null) {
			response.setCharacterEncoding(view.characterEncoding);
		}

		// response.setHeader("Content-Language", "en");
		if (view.locale != null) {
			response.setLocale(view.locale);
		}

		rd.forward(request, response);
	}
}