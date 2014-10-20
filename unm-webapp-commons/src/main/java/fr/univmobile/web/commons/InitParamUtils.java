package fr.univmobile.web.commons;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.File;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import net.avcompris.binding.annotation.Namespaces;
import net.avcompris.binding.annotation.XPath;
import net.avcompris.binding.dom.helper.DomBinderUtils;

/**
 * Utility class to extract configuration parameters from web.xml + an
 * (optional) external configuration file.
 * <p>
 * The external configuration file location must be declared in the servlet as a
 * init-param with param-name="<code>externalConfig</code>"
 * <p>
 * Its XML syntax then follows the J2EE-servlet syntax, limited to the "
 * <code>init-param</code>" elements.
 */
final class InitParamUtils {

	public InitParamUtils(final HttpServlet servlet) {

		this(new ServletInitParamReader(servlet));
	}

	InitParamUtils(final InitParamReader reader) {

		this.reader = checkNotNull(reader, "reader");
	}

	private final InitParamReader reader;

	public interface InitParamReader {

		@Nullable
		String getInitParameter(String name);
	}

	private static final class ServletInitParamReader implements
			InitParamReader {

		public ServletInitParamReader(final HttpServlet servlet) {

			this.servlet = checkNotNull(servlet, "servlet");
		}

		private final HttpServlet servlet;

		@Override
		@Nullable
		public String getInitParameter(final String name) {

			return servlet.getInitParameter(name);
		}
	}

	public String checkedInitParameter(final String name)
			throws ServletException {

		checkNotNull(name, "name");

		final String externalConfig = reader.getInitParameter("externalConfig");

		if (externalConfig != null) {

			final File file = new File(externalConfig);

			if (!file.isFile()) {
				throw new ServletException(
						"Cannot find local file for externalConfig: "
								+ externalConfig);
			}

			final ExternalConfig c = DomBinderUtils.xmlContentToJava(file,
					ExternalConfig.class);

			final String value = c.getInitParameter(name);

			if (value != null) {

				return value;
			}
		}

		final String value = reader.getInitParameter(name);

		if (value == null) {
			throw new ServletException("Servlet init parameter \"" + name
					+ "\" cannot be found: " + value);
		}

		if (isBlank(value)) {
			throw new ServletException("Servlet init parameter \"" + name
					+ "\" is empty: " + value);
		}

		return value;
	}

	@Namespaces("xmlns:j2ee=http://java.sun.com/xml/ns/j2ee")
	@XPath("/unm-backend/j2ee:servlet[j2ee:servlet-name = 'BackendServlet']")
	private interface ExternalConfig {

		@XPath("j2ee:init-param[j2ee:param-name = $arg0]/j2ee:param-value")
		@Nullable
		String getInitParameter(String name);
	}
}
