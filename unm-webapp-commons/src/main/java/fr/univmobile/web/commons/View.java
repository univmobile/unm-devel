package fr.univmobile.web.commons;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.CharEncoding.UTF_8;

import java.util.Locale;

import javax.annotation.Nullable;

/**
 * holder for the JSP filename returned by subclasses of
 * {@link AbstractController}.
 */
public class View {

	public final String jspFilename;

	/**
	 * e.g. "text/html"
	 */
	public final String contentType;

	/**
	 * e.g. "UTF-8"
	 */
	@Nullable
	public final String characterEncoding;

	@Nullable
	public final Locale locale;

	/**
	 * Constructor with default values:
	 * <ul>
	 * <li>contentType = "text/html"
	 * <li>characterEncoding = "UTF-8"
	 * <li>locale = Locale.ENGLISH
	 * </ul>
	 */
	public View(final String jspFilename) {

		this("text/html", UTF_8, Locale.ENGLISH, jspFilename);
	}

	public View(final String contentType,
			@Nullable final String characterEncoding,
			@Nullable final Locale locale, final String jspFilename) {

		this.contentType = checkNotNull(contentType, "contentType");
		this.characterEncoding = characterEncoding;
		this.locale = locale;
		this.jspFilename = checkNotNull(jspFilename, "jspFilename");
	}
}
