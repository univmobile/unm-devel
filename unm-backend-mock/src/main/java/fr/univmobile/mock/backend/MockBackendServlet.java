package fr.univmobile.mock.backend;

import static org.apache.commons.lang3.CharEncoding.UTF_8;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBeforeLast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

public class MockBackendServlet extends HttpServlet {

	/**
	 * for serialization.
	 */
	private static final long serialVersionUID = 2976067718898038173L;

	@Override
	public void init() throws ServletException {

		super.init();

		final Map<String, String> contents = new HashMap<String, String>();

		for (final String path : new String[] { //
		"getUniversityConfig-xxx.json", //
				"regions.json", //
				"listUniversities.json", //
				"pois.json", //
				"university_default.json" //
		}) {

			final String content;

			try {

				content = getResourceContent("/WEB-INF/json/" + path);

			} catch (final IOException e) {
				throw new ServletException(e);
			}

			contents.put(substringBeforeLast(path, ".json"), content);
		}

		setContents(contents);
	}

	private String getResourceContent(final String resourcePath)
			throws IOException {

		final ServletContext servletContext = getServletContext();

		final InputStream is = servletContext.getResourceAsStream(resourcePath);

		if (is == null) {
			throw new FileNotFoundException("Cannot find resource: "
					+ resourcePath);
		}

		try {

			return new String(IOUtils.toByteArray(is), UTF_8);

		} finally {
			is.close();
		}
	}

	private Map<String, String> getContents() {

		final ServletContext servletContext = getServletContext();

		@SuppressWarnings("unchecked")
		final Map<String, String> contents = (Map<String, String>) servletContext
				.getAttribute("contents");

		return contents;
	}

	private void setContents(final Map<String, String> contents) {

		final ServletContext servletContext = getServletContext();

		servletContext.setAttribute("contents", contents);

		final Set<String> keys = new TreeSet<String>(contents.keySet());

		servletContext.setAttribute("keys", keys);
	}

	/**
	 * handles the incoming HTTP request
	 * 
	 * @param request
	 *            the incoming HTTP request
	 * @param response
	 *            the outgoing HTTP response
	 */
	@Override
	public void service(final HttpServletRequest request,
			final HttpServletResponse response) throws IOException,
			ServletException {

		// 0. CONTEXT PATH

		String contextPath = request.getContextPath();

		if (!contextPath.endsWith("/")) {
			contextPath += "/";
		}

		request.setAttribute("contextRoot", contextPath);

		// 1. HANDLE PARAMETERS AND PATH

		String path = request.getParameter("path");
		final String content = request.getParameter("content");

		path = isBlank(path) ? null : path.trim();

		final boolean hasSubmit = "POST".equalsIgnoreCase(request.getMethod())
				&& request.getParameter("submit") != null //
				&& path != null && content != null;

		final Map<String, String> contents = getContents();

		if (hasSubmit) {

			// HTML page: Saved path

			if (isBlank(content)) {

				contents.remove(path);

			} else {

				contents.put(path, content);
			}

			setContents(contents);

			request.setAttribute("path", path);
			request.setAttribute("content", content);

		} else if (!isBlank(path)) {

			// HTML page: Saved path

			request.setAttribute("path", path);

			final String savedContent = contents.get(path);

			if (savedContent != null) {

				request.setAttribute("content", savedContent);
			}

		} else {

			final String uri = request.getRequestURI();

			final String uriPath = substringAfter(uri, contextPath);

			request.setAttribute("path", uriPath);

			final String savedContent = contents.get(uriPath);

			if (savedContent != null) {

				// JSON stream

				response.setContentType("text/plain");
				response.setCharacterEncoding(UTF_8);

				final PrintWriter out = response.getWriter();

				out.println(savedContent);

				out.close();

				return;
			}
		}

		// 2. DISPATCH TO JSP

		final String jspPath = "/WEB-INF/jsp/home.jsp";

		final RequestDispatcher rd = request.getRequestDispatcher(jspPath);

		response.setContentType("text/html");
		response.setCharacterEncoding(UTF_8);
		// response.setHeader("Content-Language", "en");
		response.setLocale(Locale.ENGLISH);

		rd.forward(request, response);
	}
}
