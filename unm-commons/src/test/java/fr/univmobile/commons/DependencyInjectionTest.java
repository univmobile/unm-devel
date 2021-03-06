package fr.univmobile.commons;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

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
	public void testInjectClass() throws Exception {

		final Map<String, String> initParams = DomBinderUtils.xmlContentToJava(
				new File("src/test/inject/009-inject-class.xml"), InitParams.class)
				.getInitParams();

		final Class<?> clazz = new DependencyInjection(initParams).getInject(
				Class.class).into(DependencyInjectionTest.class);

		assertEquals(MyHandlerImpl.class, clazz);
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

	@Test
	public void testInjectFactoryWithRef() throws Exception {

		final Map<String, String> initParams = DomBinderUtils.xmlContentToJava(
				new File("src/test/inject/003-inject-factoryWithRef.xml"),
				InitParams.class).getInitParams();

		final MyApi api = new DependencyInjection(initParams).getInject(
				MyApi.class).into(DependencyInjectionTest.class);

		assertEquals("Hzezlzlzoz zWzozrzlzdz!z", api.doSomething());
	}

	@Test
	public void testInjectFactoryWithFileRef() throws Exception {

		final Map<String, String> initParams = DomBinderUtils.xmlContentToJava(
				new File("src/test/inject/005-inject-factoryWithFileRef.xml"),
				InitParams.class).getInitParams();

		final MyApi api = new DependencyInjection(initParams).getInject(
				MyApi.class).into(DependencyInjectionTest.class);

		assertEquals("Hello World!(pom.xml)", api.doSomething());
	}

	@Test
	public void testInjectFactoryWithFilteredFileRef() throws Exception {

		final Map<String, String> initParams = DomBinderUtils
				.xmlContentToJava(
						new File(
								"src/test/inject/008-inject-factoryWithFilteredFileRef.xml"),
						InitParams.class).getInitParams();

		final MyApi api = new DependencyInjection(initParams).getInject(
				MyApi.class).into(DependencyInjectionTest.class);

		assertEquals("Hello World!(pom.xml)", api.doSomething());
	}

	@Test
	public void testInjectRef() throws Exception {

		final Map<String, String> initParams = DomBinderUtils.xmlContentToJava(
				new File("src/test/inject/004-inject-ref.xml"),
				InitParams.class).getInitParams();

		assertEquals("z",
				new DependencyInjection(initParams).getInject(String.class)
						.ref("toto"));
	}

	@Test
	public void testInjectConstructorWithRef() throws Exception {

		final Map<String, String> initParams = DomBinderUtils.xmlContentToJava(
				new File("src/test/inject/006-inject-constructorWithRef.xml"),
				InitParams.class).getInitParams();

		final MyApi api = new DependencyInjection(initParams).getInject(
				MyApi.class).into(DependencyInjectionTest.class);

		assertEquals(MyApiImplWithString.class, api.getClass());

		assertEquals("zHello World!z", api.doSomething());
	}

	@Test
	public void testInjectConstructorWithString() throws Exception {

		final Map<String, String> initParams = DomBinderUtils.xmlContentToJava(
				new File("src/test/inject/010-inject-constructorWithString.xml"),
				InitParams.class).getInitParams();

		final MyApi api = new DependencyInjection(initParams).getInject(
				MyApi.class).into(DependencyInjectionTest.class);

		assertEquals(MyApiImplWithString.class, api.getClass());

		assertEquals("yHello World!y", api.doSomething());
	}

	@Test
	public void testInjectConstructorWithFilteredRef() throws Exception {

		final Map<String, String> initParams = DomBinderUtils.xmlContentToJava(
				new File("src/test/inject/007-inject-filteredRef.xml"),
				InitParams.class).getInitParams();

		final MyApi api = new DependencyInjection(initParams).getInject(
				MyApi.class).into(DependencyInjectionTest.class);

		assertEquals(MyApiImplWithString.class, api.getClass());

		assertEquals("zazHello World!zaz", api.doSomething());
	}

	@Test
	public void testInjectFilteredRef() throws Exception {

		final Map<String, String> initParams = DomBinderUtils.xmlContentToJava(
				new File("src/test/inject/007-inject-filteredRef.xml"),
				InitParams.class).getInitParams();

		assertEquals("a",
				new DependencyInjection(initParams).getInject(String.class)
						.ref("hello"));

		assertEquals("zaz",
				new DependencyInjection(initParams).getInject(String.class)
						.ref("toto"));
	}

	@XPath("/init-params")
	private interface InitParams {

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

				for (final char c : s.toCharArray()) {

					sb.append(c).append(x);
				}

				return sb.toString();
			}
		};
	}
}

abstract class MyFileHandlerFactory {

	public static MyHandler newHandler(final File x) {

		return new MyHandler() {

			@Override
			public String handleSomething(final String s) {

				return s + "(" + x.getName() + ")";
			}
		};
	}
}

class MyApiImplWithString implements MyApi {

	@Inject
	public MyApiImplWithString(@Named final String toto) {

		this.toto = checkNotNull(toto, "toto");
	}

	private final String toto;

	@Override
	public String doSomething() {

		return toto + "Hello World!" + toto;
	}
}
