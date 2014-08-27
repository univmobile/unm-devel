package fr.univmobile.web.commons;

import javax.annotation.Nullable;

public class ControllerException extends Exception {

	/**
	 * for serialization.
	 */
	private static final long serialVersionUID = 7706842327705208375L;

	public ControllerException(@Nullable final String message) {

		super(message);
	}

	public ControllerException(@Nullable final Throwable cause) {

		super(cause);
	}
}
