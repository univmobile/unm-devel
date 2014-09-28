package fr.univmobile.mock.backend;

import static org.apache.commons.lang3.CharEncoding.UTF_8;
import static org.apache.commons.lang3.StringUtils.capitalize;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;

/**
 * This servlet will display Shibboleth-SAML attributes.
 * 
 * @author dandriana
 */
public class MockShibbolethServlet extends HttpServlet {

	/**
	 * for serialization.
	 */
	private static final long serialVersionUID = -1800369820706563913L;

	@Override
	public void init() throws ServletException {

		super.init();
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

		response.setCharacterEncoding(UTF_8);
		response.setContentType("text/plain");

		final PrintWriter out = response.getWriter();

		out.println(new DateTime());

		out.println("-- request.attributes (introspection) --");

		for (final Enumeration<?> e = request.getAttributeNames(); e
				.hasMoreElements();) {

			final String name = e.nextElement().toString();

			printlnAttribute(out, request, name);
		}

		out.println("-- request.attributes (forced) --");

		printlnAttribute(out, request, "REMOTE_USER");
		printlnAttribute(out, request, "cn");
		printlnAttribute(out, request, "sn");
		printlnAttribute(out, request, "uid");
		printlnAttribute(out, request, "Shib_Identity_Provider");
		printlnAttribute(out, request, "persistent_id");
		printlnAttribute(out, request, "eppn");

		out.println("-- request.cookies --");

		final Cookie[] cookies = request.getCookies();

		if (cookies != null) {

			for (final Cookie cookie : cookies) {

				out.println(cookie.getName() + ":");
				out.println("  domain: " + cookie.getDomain());
				out.println("  path: " + cookie.getPath());
				out.println("  value: " + cookie.getValue());
				out.println("  comment: " + cookie.getComment());
				out.println("  maxAge: " + cookie.getMaxAge());
				out.println("  version: " + cookie.getVersion());
				out.println("  secure: " + cookie.getSecure());
			}
		}

		out.println("-- request.headers --");

		for (final Enumeration<?> e = request.getHeaderNames(); //
		e.hasMoreElements();) {

			final String name = e.nextElement().toString();

			out.println(name + ":");

			for (final Enumeration<?> h = request.getHeaders(name); //
			h.hasMoreElements();) {

				final String value = h.nextElement().toString();

				out.println("  " + value);
			}
		}

		out.println("-- request.properties --");

		printlnProperty(out, request, "authType");
		printlnProperty(out, request, "characterEncoding");
		printlnProperty(out, request, "contentLength");
		printlnProperty(out, request, "contentType");
		printlnProperty(out, request, "contextPath");
		printlnProperty(out, request, "locale");
		printlnProperty(out, request, "localAddr");
		printlnProperty(out, request, "localName");
		printlnProperty(out, request, "localPort");
		printlnProperty(out, request, "method");
		printlnProperty(out, request, "pathInfo");
		printlnProperty(out, request, "pathTranslated");
		printlnProperty(out, request, "protocol");
		printlnProperty(out, request, "remoteAddr");
		printlnProperty(out, request, "remoteHost");
		printlnProperty(out, request, "remotePort");
		printlnProperty(out, request, "remoteUser");
		printlnProperty(out, request, "requestURI");
		printlnProperty(out, request, "scheme");
		printlnProperty(out, request, "serverName");
		printlnProperty(out, request, "serverPort");
		printlnProperty(out, request, "servletPath");
		printlnProperty(out, request, "userPrincipal");

		out.println("-- request.parameters --");

		for (final Enumeration<?> e = request.getParameterNames(); //
		e.hasMoreElements();) {

			final String name = e.nextElement().toString();

			out.println(name + ":");

			for (final String value : request.getParameterValues(name)) {

				out.println("  " + value);
			}
		}

		// END

		out.flush();

		out.close();
	}

	private static void printlnAttribute(final PrintWriter out,
			final HttpServletRequest request, final String name) {

		out.print(name + ": ");

		final Object attribute = request.getAttribute(name);

		if (attribute == null) {

			out.println("(null)"	);

			return;
		}

		final String value = attribute.toString();

		if (!String.class.equals(attribute.getClass())) {

			out.print("(" + attribute.getClass().getName() + ") ");
		}

		out.println(value);
	}

	private static void printlnProperty(final PrintWriter out,
			final HttpServletRequest request, final String propertyName) {

		final Object value;

		try {

			value = HttpServletRequest.class.getMethod(
					"get" + capitalize(propertyName)).invoke(request);

		} catch (final InvocationTargetException e) {

			throw new RuntimeException(e.getTargetException());

		} catch (final IllegalAccessException e) {

			throw new RuntimeException(e);

		} catch (final NoSuchMethodException e) {

			throw new RuntimeException(e);
		}

		out.println(propertyName + ": " + value);
	}
}
