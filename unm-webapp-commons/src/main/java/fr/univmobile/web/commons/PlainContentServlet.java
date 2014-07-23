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

import com.avcompris.lang.NotImplementedException;

import fr.univmobile.web.commons.UnivMobileHttpUtils;

public class PlainContentServlet extends HttpServlet {

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
		) {

			UnivMobileHttpUtils.sendError404(request, response, uriPath);

			return;
		}

		// 3. FILE TO SERVE

		final ServletContext servletContext = getServletContext();

		final InputStream is = servletContext.getResourceAsStream(uriPath);
		if (is != null) {
			try {

				final String contentType;

				if (uriPath.startsWith("css/")) {
					contentType = "text/css";
				} else {
					contentType = "text/plain";
				}

				response.setContentType(contentType);
				response.setCharacterEncoding(UTF_8);

				final OutputStream os = response.getOutputStream();
				try {

					IOUtils.copy(is, os);

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