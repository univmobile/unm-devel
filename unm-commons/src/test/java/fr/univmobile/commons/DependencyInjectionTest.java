package fr.univmobile.commons;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import net.avcompris.binding.annotation.XPath;
import net.avcompris.binding.dom.helper.DomBinderUtils;

import org.junit.Test;

public class DependencyInjectionTest {

	@Test
	public void testInjectFromInitParams() throws Exception {

		final Map<String, String> initParams = DomBinderUtils.xmlContentToJava(
				new File("src/test/inject/001-inject.xml"), InitParams.class)
				.getInitParams();

		final MyApi api = new DependencyInjection(initParams).getInject(
				MyApi.class).into(DependencyInjectionTest.class);
		
		assertEquals("HELLO WORLD!", api.doSomething());

	}

	@XPath("/init-params")
	public interface InitParams {

		@XPath(value = "init-param", //
		mapKeysType = String.class, mapValuesType = String.class, //
		mapKeysXPath = "param-name", mapValuesXPath = "param-value")
		Map<String, String> getInitParams();
	}
}

interface MyApi {

	String doSomething();
}

class MyApiImpl implements MyApi {

	@Inject
	public MyApiImpl(final MyHandler handler) {

		this.handler = checkNotNull(handler, "handler");
	}

	private final MyHandler handler;

	@Override
	public String doSomething() {

		return handler.handleSomething("Hello World!");
	}
}

interface MyHandler {

	String handleSomething(String s);
}

class MyHandlerImpl implements MyHandler {

	@Override
	public String handleSomething(final String s) {

		return s.toUpperCase(Locale.ENGLISH);
	}
}
