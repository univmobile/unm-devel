package fr.univmobile.web.commons;

import static org.apache.commons.lang3.CharEncoding.UTF_8;
import static org.apache.commons.lang3.StringUtils.substringAfter;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class UnivMobileHttpUtils {

	public static void sendError404(final HttpServletRequest request,
			final HttpServletResponse response, final String path)
			throws IOException {

		response.setContentType("text/plain");
		response.setCharacterEncoding(UTF_8);
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);

		final PrintWriter out = response.getWriter();
		try {

			out.println("404 NOT FOUND -- path: " + path);

			out.println();

			out.println("requestURI: " + request.getRequestURI());

		} finally {
			out.flush();
			out.close();
		}
	}

	public static void sendError403(final HttpServletRequest request,
			final HttpServletResponse response, final String reason)
			throws IOException {

		response.setContentType("text/plain");
		response.setCharacterEncoding(UTF_8);
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);

		final PrintWriter out = response.getWriter();
		try {

			out.println("403 FORBIDDEN -- reason: " + reason);

			out.println();

			out.println("requestURI: " + request.getRequestURI());

		} finally {
			out.flush();
			out.close();
		}
	}

	public static String extractUriPath(final HttpServletRequest request) {

		final String contextPath = request.getContextPath();

		final String uri = request.getRequestURI();

		String uriPath = substringAfter(uri, contextPath);

		while (uriPath.startsWith("/")) {
			uriPath = substringAfter(uriPath, "/");
		}

		return uriPath;
	}
}
