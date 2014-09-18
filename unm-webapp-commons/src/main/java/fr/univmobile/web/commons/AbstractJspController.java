package fr.univmobile.web.commons;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.avcompris.binding.helper.BinderUtils;

public abstract class AbstractJspController extends AbstractController {

	/**
	 * Handle a HTTP request, and return which JSP we should forward to.
	 * 
	 * @return the JSP filename, in WEB-INF/jsp/. e.g. "home.jsp"
	 */
	public abstract View action() throws Exception;

	/**
	 * Set an attribute into the underlying {@link HttpServletRequest} object.
	 */
	protected final void setAttribute(final String name, final Object value) {

		checkNotNull(name, "name");
		checkNotNull(value, "value");

		checkedRequest().setAttribute(name, value);
	}

	/**
	 * Set an attribute into the underlying {@link HttpSession} object.
	 */
	protected final void setSessionAttribute(final String name,
			final Object value) {

		checkNotNull(name, "name");
		checkNotNull(value, "value");

		checkedRequest().getSession().setAttribute(name,
				BinderUtils.detach(value));
	}

	protected final void removeSessionAttribute(final String name) {

		checkNotNull(name, "name");

		checkedRequest().getSession().removeAttribute(name);
	}

	protected final <T> T getSessionAttribute(final String name,
			final Class<T> clazz) {

		checkNotNull(name, "name");
		checkNotNull(clazz, "clazz");

		final Object value = checkedRequest().getSession().getAttribute(name);

		if (value == null) {
			throw new IllegalStateException("Session attribute is null: "
					+ name);
		}

		return clazz.cast(value);
	}

	protected final boolean hasSessionAttribute(final String name) {

		checkNotNull(name, "name");

		return checkedRequest().getSession().getAttribute(name) != null;
	}

	/**
	 * Get an attribute from the underlying {@link HttpServletRequest} object.
	 */
	protected final <T> T getAttribute(final String name, final Class<T> clazz) {

		checkNotNull(name, "name");

		final Object value = checkedRequest().getAttribute(name);

		if (value == null) {
			throw new IllegalStateException("Request attribute is null: "
					+ name);
		}

		return clazz.cast(value);
	}

	/**
	 * client-side redirect to an URL, generally on another server.
	 */
	@Nullable
	protected final View sendRedirect(final String url) throws IOException {

		checkedResponse().sendRedirect(url);

		return null;
	}
}
