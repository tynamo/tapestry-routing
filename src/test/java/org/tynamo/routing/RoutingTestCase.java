package org.tynamo.routing;

import org.apache.tapestry5.LinkSecurity;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.internal.services.RequestSecurityManager;
import org.apache.tapestry5.internal.test.PageTesterContext;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.modules.TapestryModule;
import org.apache.tapestry5.services.*;
import org.apache.tapestry5.test.TapestryTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.tynamo.routing.services.*;

import java.util.Locale;

public abstract class RoutingTestCase extends TapestryTestCase {

	protected Registry registry;
	protected URLEncoder urlEncoder;
	protected ContextValueEncoder valueEncoder;
	protected ContextPathEncoder contextPathEncoder;
	protected PersistentLocale persistentLocale;
	protected SymbolSource symbolSource;
	protected ComponentClassResolver classResolver;
	protected RouteSource routeSource;
	protected RouteFactory routeFactory;
	protected RouteDecoder routeDecoder;

	protected abstract void addAdditionalModules(RegistryBuilder builder);

	@BeforeClass
	public final void setup_registry() {
		RegistryBuilder builder = new RegistryBuilder();

		builder.add(TapestryModule.class);
		builder.add(RoutingModule.class);

		addAdditionalModules(builder);

		registry = builder.build();

		ApplicationGlobals globals = registry.getObject(ApplicationGlobals.class, null);
		globals.storeContext(new PageTesterContext("src/test/webapp"));

		registry.performRegistryStartup();

		urlEncoder = getService(URLEncoder.class);
		valueEncoder = getService(ContextValueEncoder.class);
		contextPathEncoder = getService(ContextPathEncoder.class);
		routeFactory = getService(RouteFactory.class);
		persistentLocale = getService(PersistentLocale.class);
		symbolSource = registry.getService(SymbolSource.class);
		classResolver = registry.getService(ComponentClassResolver.class);
		routeSource = registry.getService(RouteSource.class);
		routeDecoder = getService(RouteDecoder.class);

	}

	@AfterClass
	public final void shutdown_registry() {
		registry.shutdown();
		registry = null;
	}

	@AfterMethod
	public final void cleanupThread() {
		registry.cleanupThread();
	}

	public final <T> T getService(Class<T> serviceInterface) {
		return registry.getService(serviceInterface);
	}

	public void testPageRenderLinkGeneration(String expectedURI,
	                                         Class pageClass,
	                                         String requestPath,
	                                         String contextPath,
	                                         int activationContextCount) {

		testPageRenderLinkGeneration(expectedURI, pageClass, requestPath, contextPath, activationContextCount, null);
	}

	public void testPageRenderLinkGeneration(String expectedURI,
	                                         Class pageClass,
	                                         String requestPath,
	                                         String contextPath,
	                                         int activationContextCount,
	                                         Locale locale) {

		boolean encodeLocaleIntoPath = Boolean.parseBoolean(symbolSource.valueForSymbol(SymbolConstants.ENCODE_LOCALE_INTO_PATH));
		String applicationFolder = symbolSource.valueForSymbol(SymbolConstants.APPLICATION_FOLDER);

		testPageRenderLinkGeneration(expectedURI, pageClass, requestPath, contextPath, activationContextCount, locale, encodeLocaleIntoPath, applicationFolder);
	}


	public void testPageRenderLinkGeneration(String expectedURI,
	                                         Class pageClass,
	                                         String requestPath,
	                                         String contextPath,
	                                         int activationContextCount,
	                                         Locale locale,
	                                         boolean encodeLocaleIntoPath,
	                                         String applicationFolder) {
		if (locale != null) persistentLocale.set(locale);

		String logical = classResolver.resolvePageClassNameToPageName(pageClass.getName());
		String canonicalized = classResolver.canonicalizePageName(logical);

		Request request = mockRequest();
		expect(request.getPath()).andReturn(requestPath).atLeastOnce();

		Response response = mockResponse();
		train_encodeURL(response, expectedURI, expectedURI);

		RequestSecurityManager securityManager = newMock(RequestSecurityManager.class);
		expect(securityManager.checkPageSecurity(logical)).andReturn(LinkSecurity.INSECURE);

		replay();

		Route route = routeSource.getRoute(canonicalized);

		PageRenderRequestParameters parameters = routeDecoder.decodePageRenderRequest(route, request);

		Assert.assertEquals(parameters.getLogicalPageName(), logical);
		Assert.assertEquals(parameters.getActivationContext().getCount(), activationContextCount);

		RouterLinkTransformer linkTransformer = new RouterLinkTransformer(routeSource, request, securityManager,
				response, contextPathEncoder, null, persistentLocale, contextPath, encodeLocaleIntoPath, applicationFolder);

		Assert.assertEquals(linkTransformer.transformPageRenderLink(null, parameters).toURI(), expectedURI);
	}
}
