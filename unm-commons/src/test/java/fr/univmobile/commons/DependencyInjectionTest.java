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

// TODO many tests (with factory:, class:, duplicate injectClass, etc.)
// TODO are actually in the unm-backend-client-json project.
// TODO Import some of those tests here.
public class DependencyInjectionTest {

	@Test
	public void testInjectFromInitParams001() throws Exception {

		final Map<String, String> initParams = DomBinderUtils.xmlContentToJava(
				new File("src/test/inject/001-inject.xml"), InitParams.class)
				.getInitParams();

		final MyApi api = new DependencyInjection(initParams).getInject(
				MyApi.class).into(DependencyInjectionTest.class);

		assertEquals("HELLO WORLD!", api.doSomething());

	}

	@Test
	public void testInjectImplClass() throws Exception {

		final Map<String, String> initParams = DomBinderUtils.xmlContentToJava(
				new File("src/test/inject/001-inject.xml"), InitParams.class)
				.getInitParams();

		final MyApi api = new DependencyInjection(initParams).getInject(
				MyApi.class).into(DependencyInjectionTest.class);

		assertEquals("HELLO WORLD!", api.doSomething());

	}

	@Test
	public void testInjectFactory() throws Exception {

		final Map<String, String> initParams = DomBinderUtils.xmlContentToJava(
				new File("src/test/inject/002-inject-factory.xml"),
				InitParams.class).getInitParams();

		final MyApi api = new DependencyInjection(initParams).getInject(
				MyApi.class).into(DependencyInjectionTest.class);

		assertEquals("Hxexlxlxox xWxoxrxlxdx!x", api.doSomething());

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

abstract class MyHandlerFactory {

	public static MyHandler newHandler(final String x) {
		
		return new MyHandler() {
			
			@Override
			public String handleSomething(final String s) {
				
				final StringBuilder sb = new StringBuilder();
				
				for (final char c: s.toCharArray()) {
					
					sb.append(c).append(x);
				}
				
				return sb.toString();
			}
		};
	}
}
