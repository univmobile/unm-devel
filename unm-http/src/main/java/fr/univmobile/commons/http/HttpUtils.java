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

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class HttpUtils {

	private static final Log log = LogFactory.getLog(HttpUtils.class);

	public static String wget(final String url) throws IOException {

		final URL u = new URL(url);

		final HttpURLConnection cxn = (HttpURLConnection) u.openConnection();
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

	private static final byte[] EMPTY_BYTES = new byte[0];

	private static byte[] getParamBytes(final String[] params)
			throws UnsupportedEncodingException {

		if (params == null || params.length == 0) {
			return EMPTY_BYTES;
		}

		final StringBuilder sb = new StringBuilder();

		for (int i = 0; i < params.length; ++i) {

			if (i != 0) {
				sb.append('&');
			}

			sb.append(params[i]);

			++i;

			if (i < params.length) {

				sb.append('=').append(urlEncode(params[i]));

			} else {

				break;
			}
		}

		return sb.toString().getBytes(UTF_8);
	}

	public static String wpost(final String url, final String... params)
			throws IOException {

		final byte[] bytes = getParamBytes(params);

		final URL u = new URL(url);

		final HttpURLConnection cxn = (HttpURLConnection) u.openConnection();
		try {

			log.debug("openConnection() OK");

			cxn.setRequestMethod("POST");
			cxn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
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
