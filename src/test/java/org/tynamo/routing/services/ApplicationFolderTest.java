package org.tynamo.routing.services;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.internal.services.LinkSecurity;
import org.apache.tapestry5.internal.services.RequestSecurityManager;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.ioc.services.ThreadLocale;
import org.apache.tapestry5.services.*;
import org.apache.tapestry5.test.TapestryTestCase;
import org.testng.Assert;
import org.testng.annotations.*;
import org.tynamo.routing.Route;
import org.tynamo.routing.pages.Home;
import org.tynamo.routing.pages.SubFolderHome;

public class ApplicationFolderTest extends TapestryTestCase {

	private static Registry registry;

	@BeforeSuite
	public final void setup_registry() {
		RegistryBuilder builder = new RegistryBuilder();

		builder.add(TapestryModule.class);
		builder.add(RoutingModule.class);
		builder.add(ApplicationFolderModule.class);

		registry = builder.build();

		registry.performRegistryStartup();

	}

	@AfterSuite
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

	@Test
	public void auto_discovery_enabled() {
		testPageRenderLinkGeneration(registry, "/myapp/t5", Home.class, "/t5/", "/myapp", 0);
	}

	public void testPageRenderLinkGeneration(Registry registry,
	                                         String expectedURI,
	                                         Class pageClass,
	                                         String requestPath,
	                                         String contextPath,
	                                         int activationContextCount) {

		SymbolSource symbolSource = registry.getService(SymbolSource.class);

		boolean encodeLocaleIntoPath = Boolean.parseBoolean(symbolSource.valueForSymbol(SymbolConstants.ENCODE_LOCALE_INTO_PATH));
		String applicationFolder = symbolSource.valueForSymbol(SymbolConstants.APPLICATION_FOLDER);

		URLEncoder urlEncoder = registry.getService(URLEncoder.class);
		ContextValueEncoder valueEncoder = registry.getService(ContextValueEncoder.class);
		ContextPathEncoder contextPathEncoder = registry.getService(ContextPathEncoder.class);
		PersistentLocale persistentLocale = registry.getService(PersistentLocale.class);

		ComponentClassResolver classResolver = registry.getService(ComponentClassResolver.class);
		String logical = classResolver.resolvePageClassNameToPageName(pageClass.getName());
		String canonicalized = classResolver.canonicalizePageName(logical);

		Request request = mockRequest();
		expect(request.getPath()).andReturn(requestPath).atLeastOnce();
		expect(request.getContextPath()).andReturn(contextPath).atLeastOnce();

		Response response = mockResponse();
		train_encodeURL(response, expectedURI, expectedURI);

		RequestSecurityManager securityManager = newMock(RequestSecurityManager.class);
		expect(securityManager.checkPageSecurity(logical)).andReturn(LinkSecurity.INSECURE);

		replay();

		RouterDispatcher routerDispatcher = registry.getService(RouterDispatcher.class);
		Route route = routerDispatcher.getRoute(canonicalized);

		PageRenderRequestParameters parameters = route.decodePageRenderRequest(request, urlEncoder, valueEncoder);

		Assert.assertEquals(parameters.getLogicalPageName(), logical);
		Assert.assertEquals(parameters.getActivationContext().getCount(), activationContextCount);

		RouterLinkTransformer linkTransformer = new RouterLinkTransformer(routerDispatcher, request, securityManager,
				response, contextPathEncoder, null, persistentLocale, encodeLocaleIntoPath, applicationFolder);

		Assert.assertEquals(linkTransformer.transformPageRenderLink(null, parameters).toURI(), expectedURI);
	}
}
