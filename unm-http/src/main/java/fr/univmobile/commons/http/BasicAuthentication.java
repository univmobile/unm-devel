package fr.univmobile.commons.http;

import static com.google.common.base.Preconditions.checkNotNull;
import static fr.univmobile.commons.http.HttpUtils.base64Encode;

public final class BasicAuthentication extends Authorization {

	public BasicAuthentication(final String username, final String password) {

		this.username = checkNotNull(username, "username");
		this.password = checkNotNull(password, "password");
	}

	private final String username;
	private final String password;

	@Override
	public String getAuthorizationRequestProperty() {

		return "Basic " + base64Encode(username + ":" + password);
	}
}
