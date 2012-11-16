package org.tynamo.routing;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.internal.services.LinkSecurity;
import org.apache.tapestry5.internal.services.RequestSecurityManager;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.services.*;
import org.apache.tapestry5.test.TapestryTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.tynamo.routing.Route;
import org.tynamo.routing.services.RouterDispatcher;
import org.tynamo.routing.services.RouterLinkTransformer;
import org.tynamo.routing.services.RoutingModule;

public abstract class RoutingTestCase extends TapestryTestCase {

	protected Registry registry;
	protected URLEncoder urlEncoder;
	protected ContextValueEncoder valueEncoder;
	protected ContextPathEncoder contextPathEncoder;
	protected LocalizationSetter localizationSetter;
	protected PersistentLocale persistentLocale;
	protected SymbolSource symbolSource;
	protected ComponentClassResolver classResolver;
	protected RouterDispatcher routerDispatcher;

	protected abstract void addAdditionalModules(RegistryBuilder builder);

	@BeforeClass
	public final void setup_registry() {
		RegistryBuilder builder = new RegistryBuilder();

		builder.add(TapestryModule.class);
		builder.add(RoutingModule.class);

		addAdditionalModules(builder);

		registry = builder.build();
		registry.performRegistryStartup();

		urlEncoder = getService(URLEncoder.class);
		valueEncoder = getService(ContextValueEncoder.class);
		contextPathEncoder = getService(ContextPathEncoder.class);
		localizationSetter = getService(LocalizationSetter.class);
		persistentLocale = getService(PersistentLocale.class);
		symbolSource = registry.getService(SymbolSource.class);
		classResolver = registry.getService(ComponentClassResolver.class);
		routerDispatcher = registry.getService(RouterDispatcher.class);
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


		boolean encodeLocaleIntoPath = Boolean.parseBoolean(symbolSource.valueForSymbol(SymbolConstants.ENCODE_LOCALE_INTO_PATH));
		String applicationFolder = symbolSource.valueForSymbol(SymbolConstants.APPLICATION_FOLDER);

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

		Route route = routerDispatcher.getRoute(canonicalized);

		PageRenderRequestParameters parameters = route.decodePageRenderRequest(request, urlEncoder, valueEncoder);

		Assert.assertEquals(parameters.getLogicalPageName(), logical);
		Assert.assertEquals(parameters.getActivationContext().getCount(), activationContextCount);

		RouterLinkTransformer linkTransformer = new RouterLinkTransformer(routerDispatcher, request, securityManager,
				response, contextPathEncoder, null, persistentLocale, encodeLocaleIntoPath, applicationFolder);

		Assert.assertEquals(linkTransformer.transformPageRenderLink(null, parameters).toURI(), expectedURI);
	}
}
