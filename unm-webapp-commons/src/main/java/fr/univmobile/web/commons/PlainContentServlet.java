package fr.univmobile.web.commons;

import static org.apache.commons.lang3.CharEncoding.UTF_8;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.avcompris.lang.NotImplementedException;

public class PlainContentServlet extends HttpServlet {

	private static final Log log = LogFactory.getLog(PlainContentServlet.class);

	/**
	 * for serialization.
	 */
	private static final long serialVersionUID = 1727933268611720071L;

	@Override
	public void service(final HttpServletRequest request,
			final HttpServletResponse response) throws IOException,
			ServletException {

		// 1. REQUEST

		request.setCharacterEncoding(UTF_8);

		final String uriPath = UnivMobileHttpUtils.extractUriPath(request);

		// 2. VALIDATION

		if (!uriPath.startsWith("css/") //
				&& !uriPath.equals("css") //
				&& !uriPath.startsWith("js/") //
				&& !uriPath.equals("js") //
				&& !uriPath.startsWith("img/") //
				&& !uriPath.equals("img") //
		) {
			log.error("Unknown scheme for uriPath: " + uriPath);

			UnivMobileHttpUtils.sendError404(request, response, uriPath);

			return;
		}

		// 3. FILE TO SERVE

		final ServletContext servletContext = getServletContext();

		final InputStream is = servletContext.getResourceAsStream(uriPath);
		if (is != null) {
			try {

				final String contentType;

				if (uriPath.startsWith("img/")
						|| uriPath.startsWith("css/images/")
						|| uriPath.endsWith(".png")) {

					if (uriPath.endsWith(".png")) {

						contentType = "image/png";

					} else {

						log.error("Unknown img file extension in uriPath: "
								+ uriPath);

						UnivMobileHttpUtils.sendError404(request, response,
								uriPath);

						return;
					}

				} else if (// uriPath.startsWith("css/") ||
				uriPath.endsWith(".css")) {

					contentType = "text/css";

				} else if (// uriPath.startsWith("js/") ||
				uriPath.endsWith(".js")) {

					contentType = "application/javascript";

				} else {

					contentType = "text/plain";
				}

				response.setContentType(contentType);

				if (contentType.startsWith("text/")) {
					response.setCharacterEncoding(UTF_8);
				}

				final OutputStream os = response.getOutputStream();
				try {

					IOUtils.copy(is, os);

					return;

				} finally {
					os.close();
				}

			} finally {
				is.close();
			}
		}

		final String realPath = servletContext.getRealPath(uriPath);

		if (realPath == null) {

			UnivMobileHttpUtils.sendError404(request, response, uriPath);

			return;
		}

		final File dir = new File(realPath);

		if (!dir.isDirectory()) {

			UnivMobileHttpUtils.sendError404(request, response, uriPath);

			return;
		}

		throw new NotImplementedException("Directory: " + uriPath);
	}
}