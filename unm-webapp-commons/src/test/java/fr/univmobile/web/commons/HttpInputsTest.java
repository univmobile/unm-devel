package fr.univmobile.web.commons;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;

public class HttpInputsTest {

	@Test
	public void testHttpInputs_String() throws Exception {

		final HttpInputs_String[] httpInputsHolder = new HttpInputs_String[1];

		final AbstractJspController controller = new AbstractJspController() {

			@Override
			public View action() throws IOException {

				httpInputsHolder[0] = getHttpInputs(HttpInputs_String.class);

				return null;
			}
		};

		final HttpServletRequest request = mock(HttpServletRequest.class);

		controller.setThreadLocalRequest(request);

		when(request.getMethod()).thenReturn("GET");
		when(request.getParameter("first_name")).thenReturn("Toto");

		controller.action();

		verify(request).getMethod();
		verify(request).getParameter("first_name");

		final HttpInputs_String httpInputs = httpInputsHolder[0];

		assertTrue(httpInputs.isHttpValid());

		assertEquals("Toto", httpInputs.first_name());
	}

	@Test
	public void testHttpInputs_int() throws Exception {

		final HttpInputs_int[] httpInputsHolder = new HttpInputs_int[1];

		final AbstractJspController controller = new AbstractJspController() {

			@Override
			public View action() throws IOException {

				httpInputsHolder[0] = getHttpInputs(HttpInputs_int.class);

				return null;
			}
		};

		final HttpServletRequest request = mock(HttpServletRequest.class);

		controller.setThreadLocalRequest(request);

		when(request.getMethod()).thenReturn("POST");
		when(request.getParameter("age")).thenReturn("  80  ");

		controller.action();

		final HttpInputs_int httpInputs = httpInputsHolder[0];

		assertTrue(httpInputs.isHttpValid());

		assertEquals(80, httpInputs.age());
	}

	@HttpMethods("GET")
	private interface HttpInputs_String extends HttpInputs {

		@HttpParameter
		@HttpRequired
		String first_name();
	}

	@HttpMethods("POST")
	private interface HttpInputs_int extends HttpInputs {

		@HttpParameter(trim = true)
		@HttpRequired
		int age();
	}
}
