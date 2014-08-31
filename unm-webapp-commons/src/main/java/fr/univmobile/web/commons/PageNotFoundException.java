package fr.univmobile.web.commons;

import javax.annotation.Nullable;

public class PageNotFoundException extends Exception {

	/**
	 * for serialization.
	 */
	private static final long serialVersionUID = 6360378870132935048L;

	public PageNotFoundException() {

		super();
	}

	public PageNotFoundException(@Nullable final String message) {

		super(message);
	}
}
