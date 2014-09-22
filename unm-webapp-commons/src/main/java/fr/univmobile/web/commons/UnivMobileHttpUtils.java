package fr.univmobile.web.commons;

import static org.apache.commons.lang3.CharEncoding.UTF_8;
import static org.apache.commons.lang3.StringUtils.substringAfter;

import java.io.IOException;
import java.io.PrintWriter;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class UnivMobileHttpUtils {

	private static Log log = LogFactory.getLog(UnivMobileHttpUtils.class);

	public static void sendError404(final HttpServletRequest request,
			final HttpServletResponse response, final String path)
			throws IOException {

		final String message = "404 NOT FOUND -- path: " + path;

		log.error(message);

		response.setContentType("text/plain");
		response.setCharacterEncoding(UTF_8);
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);

		final PrintWriter out = response.getWriter();
		try {

			out.println(message);

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

		final String message = "403 FORBIDDEN -- reason: " + reason;

		log.error(message);

		response.setContentType("text/plain");
		response.setCharacterEncoding(UTF_8);
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);

		final PrintWriter out = response.getWriter();
		try {

			out.println(message);

			out.println();

			out.println("requestURI: " + request.getRequestURI());

		} finally {
			out.flush();
			out.close();
		}
	}

	public static void sendError400(final HttpServletRequest request,
			final HttpServletResponse response) throws IOException {

		final String method = request.getMethod();

		final String message = "400 BAD REQUEST -- method: " + method;

		log.error(message);

		response.setContentType("text/plain");
		response.setCharacterEncoding(UTF_8);
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

		final PrintWriter out = response.getWriter();
		try {

			out.println(message);

			out.println();

			out.println("requestURI: " + request.getRequestURI());

		} finally {
			out.flush();
			out.close();
		}
	}

	public static void sendError500(final HttpServletRequest request,
			final HttpServletResponse response, @Nullable final Throwable e)
			throws IOException {

		final String message = "500 INTERNAL SERVER ERROR -- cause: " + e;

		log.error(message);

		response.setContentType("text/plain");
		response.setCharacterEncoding(UTF_8);
		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

		final PrintWriter out = response.getWriter();
		try {

			out.println(message);

			out.println();

			e.printStackTrace(out);

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
