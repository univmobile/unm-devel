package fr.univmobile.web.commons;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nullable;
import javax.servlet.ServletException;

import net.avcompris.binding.annotation.Namespaces;
import net.avcompris.binding.annotation.XPath;
import net.avcompris.binding.dom.helper.DomBinderUtils;

import org.junit.Test;

import fr.univmobile.web.commons.InitParamUtils.InitParamReader;

public class InitParamUtilsTest {

	@Test
	public void testCheckedInitParam_000_noExternalConfig() throws Exception {

		final InitParamUtils utils = new InitParamUtils(
				readBackendServletConfig(new File(
						"src/test/WEB-INF/web.xml-000-noExternalConfig")));

		assertEquals("/tmp/unm-backend/dataDir",
				utils.checkedInitParameter("dataDir"));
	}

	@Test(expected = ServletException.class)
	public void testIllegalInitParam_000_noExternalConfig() throws Exception {

		final InitParamUtils utils = new InitParamUtils(
				readBackendServletConfig(new File(
						"src/test/WEB-INF/web.xml-000-noExternalConfig")));

		utils.checkedInitParameter("externalConfig");
	}

	@Test
	public void testCheckedInitParam_001_externalConfig() throws Exception {

		final InitParamUtils utils = new InitParamUtils(
				readBackendServletConfig(new File(
						"src/test/WEB-INF/web.xml-001-externalConfig")));

		assertEquals("/tmp/unm-backend/dataDir",
				utils.checkedInitParameter("dataDir"));

		assertEquals("Hello, consumerKey",
				utils.checkedInitParameter("twitter.consumerKey"));
	}

	private static InitParamReader readBackendServletConfig(
			final File webXmlFile) throws IOException {

		final BackendServletConfig config = DomBinderUtils.xmlContentToJava(
				webXmlFile, BackendServletConfig.class);

		return new InitParamReader() {

			@Override
			public String getInitParameter(final String name) {

				return config.getInitParameter(name);
			}
		};
	}

	@Namespaces("xmlns:j2ee=http://java.sun.com/xml/ns/j2ee")
	@XPath("/j2ee:web-app/j2ee:servlet[j2ee:servlet-name = 'BackendServlet']")
	private interface BackendServletConfig {

		@XPath("j2ee:init-param[j2ee:param-name = $arg0]/j2ee:param-value")
		@Nullable
		String getInitParameter(String name);
	}
}
