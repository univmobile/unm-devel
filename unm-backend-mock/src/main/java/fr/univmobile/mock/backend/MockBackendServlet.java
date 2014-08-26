package fr.univmobile.mock.backend;

import static org.apache.commons.lang3.CharEncoding.UTF_8;
import static org.apache.commons.lang3.StringUtils.containsWhitespace;
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

/**
 * This servlet will serve JSON contents according to paths passed in the
 * request URIs.
 * <ul>
 * <li><code>http://xxx/unm-backend-mock</code> (HTML page) will list all saved
 * JSON contents, plus their paths, such as mon-chemin-a-moi
 * <li><code>http://xxx/unm-backend-mock/mon-chemin-a-moi</code> (JSON content)
 * will serve the JSON content attached to mon-chemin-a-moi. This URL is used in
 * mocked tests for mobile development.
 * <li><code>http://xxx/unm-backend-mock/?path=mon-chemin-a-moi</code> (HTML
 * page) will allow to edit the JSON content attached to mon-chemin-a-moi.
 * </ul>
 * To add a new path + attached JSON content, enter them in the form.
 * <p>
 * To remove an existing path, attach an empty JSON content.
 * <p>
 * When it is installed, the servlet contains a few sample JSON contents for
 * ease of use. They can be found in the src/main/webapp/WEB-INF/json/
 * directory.
 * 
 * @author dandriana
 */
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
				"listUniversities_bretagne.json", //
				"listUniversities_ile_de_france.json", //
				"listUniversities_unrpcl.json", //
				"pois.json", //
				"university_default.json", //
				"comments1.json" //
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

		request.setCharacterEncoding(UTF_8);
		
		String path = request.getParameter("path");
		final String content = request.getParameter("content");

		path = isBlank(path) ? null : path.trim();

		final boolean hasSubmit = "POST".equalsIgnoreCase(request.getMethod())
				&& request.getParameter("submit") != null //
				&& path != null && content != null;

		final Map<String, String> contents = getContents();

		if (hasSubmit) {

			final boolean pathIsInvalid;

			// is path valid?

			if (path.contains("<") || path.contains(">") //
					|| path.contains("&") || path.contains("?") //
					|| containsWhitespace(path)) {

				pathIsInvalid = true;

			} else {

				pathIsInvalid = false;
			}

			// HTML page: Save / update / remove path

			if (!pathIsInvalid) {

				if (isBlank(content)) {

					contents.remove(path);

				} else {

					contents.put(path, content);
				}

				setContents(contents);
			}

			request.setAttribute("path", escape(path));
			request.setAttribute("content", content);

			if (pathIsInvalid) {
				request.setAttribute("pathIsInvalid", Boolean.TRUE);
			}

		} else if (!isBlank(path)) {

			// HTML page: Saved path

			final String savedContent = contents.get(path);

			request.setAttribute("path", escape(path));

			if (savedContent != null) {

				request.setAttribute("content", savedContent);
			}

		} else {

			final String uri = request.getRequestURI();

			final String uriPath = substringAfter(uri, contextPath);

			request.setAttribute("path", escape(uriPath));

			final String savedContent = contents.get(uriPath);

			if (savedContent != null) {

				// JSON stream: Saved path

				response.setContentType("application/json");
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

	private static String escape(final String s) {

		return s.replace("&", "&amp;") //
				.replace("<", "&lt;").replace(">", "&gt;");
	}
}
