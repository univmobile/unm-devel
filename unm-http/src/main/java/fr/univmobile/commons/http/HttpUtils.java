package fr.univmobile.commons.http;

import static org.apache.commons.lang3.CharEncoding.UTF_8;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class HttpUtils {

	private static final Log log = LogFactory.getLog(HttpUtils.class);

	static String composeURL(final String url, final Object... params)
			throws UnsupportedEncodingException {

		if (params == null || params.length == 0) {

			return url;
		}

		final String query = getParams(params);

		if (url.contains("?")) {

			return url + "&" + query;

		} else {

			return url + "?" + query;
		}
	}

	public static String wget(final Authorization authorization,
			final String url, final Object... params) throws IOException {

		final URL u = new URL(composeURL(url, params));

		if (log.isInfoEnabled()) {
			log.info("wget():"+u+"...");
		}

		final HttpURLConnection cxn = (HttpURLConnection) u.openConnection();

		cxn.setRequestProperty("Authorization",
				authorization.getAuthorizationRequestProperty());

		return wget(cxn, params);

	}

	public static String wget(final String url, final Object... params)
			throws IOException {
		
		final URL u = new URL(composeURL(url, params));

		if (log.isInfoEnabled()) {
			log.info("wget():"+u+"...");
		}

		final HttpURLConnection cxn = (HttpURLConnection) u.openConnection();

		return wget(cxn, params);
	}

	private static String wget(final HttpURLConnection cxn,
			final Object[] params) throws IOException {

		try {

			log.debug("openConnection() OK");

			final InputStream is = new BufferedInputStream(cxn.getInputStream());
			try {

				log.debug("getInputStream() OK");

				return IOUtils.toString(is, UTF_8);

			} finally {

				log.debug("is.close()...");

				is.close();
			}

		} finally {

			log.debug("cxn.disconnect()...");

			cxn.disconnect();
		}
	}

	private static String getParams(final Object[] params)
			throws UnsupportedEncodingException {

		final StringBuilder sb = new StringBuilder();

		for (int i = 0; i < params.length; ++i) {

			if (i != 0) {
				sb.append('&');
			}

			final Object name = params[i];
			
			if (name==null || !String.class.equals(name.getClass())) {
				throw new IllegalArgumentException("paramName: "+name);
			}
			
			sb.append(name);

			++i;

			if (i < params.length) {

				sb.append('=').append(urlEncode(params[i].toString()));

			} else {

				break;
			}
		}

		return sb.toString();
	}

	private static final byte[] EMPTY_BYTES = new byte[0];

	private static byte[] getParamBytes(final String[] params)
			throws UnsupportedEncodingException {

		if (params == null || params.length == 0) {
			return EMPTY_BYTES;
		}

		return getParams(params).getBytes(UTF_8);
	}

	static String base64Encode(final String s) {

		try {

			final byte[] bytes = s.getBytes(UTF_8);

			return new String(Base64.encodeBase64(bytes), UTF_8);

		} catch (final UnsupportedEncodingException e) {

			throw new RuntimeException(e);
		}
	}

	public static String wpost(final Authorization authorization,
			final String url, final String... params) throws IOException {

		final URL u = new URL(url);

		final HttpURLConnection cxn = (HttpURLConnection) u.openConnection();

		cxn.setRequestProperty("Authorization",
				authorization.getAuthorizationRequestProperty());

		return wpost(cxn, params);
	}

	public static String wpost(final String url, final String... params)
			throws IOException {

		final URL u = new URL(url);

		final HttpURLConnection cxn = (HttpURLConnection) u.openConnection();

		return wpost(cxn, params);
	}

	private static String wpost(final HttpURLConnection cxn,
			final String[] params) throws IOException {

		final byte[] bytes = getParamBytes(params);

		try {

			log.debug("openConnection() OK");

			cxn.setRequestMethod("POST");

			cxn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded;charset=UTF-8");

			cxn.setRequestProperty("Content-Length",
					Integer.toString(bytes.length));

			// cxn.setRequestProperty("Content-Language", "en-US");

			cxn.setUseCaches(false);
			cxn.setDoInput(true);
			cxn.setDoOutput(true);

			final OutputStream os = cxn.getOutputStream();
			try {

				os.write(bytes);

				os.flush();

			} finally {

				os.close();
			}

			final InputStream is = new BufferedInputStream(cxn.getInputStream());
			try {

				log.debug("getInputStream() OK");

				return IOUtils.toString(is, UTF_8);

			} finally {

				log.debug("is.close()...");

				is.close();
			}

		} finally {

			log.debug("cxn.disconnect()...");

			cxn.disconnect();
		}
	}

	static String urlEncode(final String s) throws UnsupportedEncodingException {

		return URLEncoder.encode(s, UTF_8);
	}
}
