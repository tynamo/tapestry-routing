package org.tynamo.routing;

import org.apache.tapestry5.internal.EmptyEventContext;
import org.apache.tapestry5.internal.services.LinkSecurity;
import org.apache.tapestry5.internal.services.RequestSecurityManager;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.apache.tapestry5.services.*;
import org.apache.tapestry5.test.TapestryTestCase;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.*;
import org.tynamo.routing.pages.Home;
import org.tynamo.routing.pages.SimplePage;
import org.tynamo.routing.pages.SubFolderHome;
import org.tynamo.routing.pages.subpackage.SubPackageMain;
import org.tynamo.routing.pages.subpackage.SubPage;
import org.tynamo.routing.pages.subpackage.SubPageFirst;
import org.tynamo.routing.services.RouterDispatcher;
import org.tynamo.routing.services.RouterLinkTransformer;
import org.tynamo.routing.services.RoutingModule;
import org.tynamo.routing.services.TestModule;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RouteTest extends TapestryTestCase {

	private static Registry registry;
	private URLEncoder urlEncoder;
	private ContextValueEncoder valueEncoder;
	private ContextPathEncoder contextPathEncoder;

	@BeforeSuite
	public final void setup_registry() {
		RegistryBuilder builder = new RegistryBuilder();

		builder.add(TapestryModule.class);
		builder.add(RoutingModule.class);
		builder.add(TestModule.class);

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

	@BeforeClass
	public void setup() {
		urlEncoder = getService(URLEncoder.class);
		valueEncoder = getService(ContextValueEncoder.class);
		contextPathEncoder = getService(ContextPathEncoder.class);
	}

	@Test
	public void auto_discovery_disbled_only_one_contributed_service() {
		RouterDispatcher dispatcher = getService(RouterDispatcher.class);
		Assert.assertEquals(dispatcher.getRouteMap().size(), 1, "there is only one contributed service, autodiscovery is disabled");
	}

	@Test
	public void regular_expressions() {

		String path = "/foo/52";

		String routeExpression = Route.buildExpression("/foo/{0}");

		Assert.assertEquals(routeExpression, "\\Q/foo/\\E([^/]+)");

		Pattern p = Pattern.compile(routeExpression);
		Matcher m = p.matcher(path);

		Assert.assertTrue(m.matches());
		Assert.assertEquals(m.group(1), "52");

		p = Pattern.compile(Route.buildExpression("/blah/{0}/{1}/bar"));
		m = p.matcher("/blah/54/foo/bar");

		Assert.assertTrue(m.matches());
		Assert.assertEquals(m.group(1), "54");
		Assert.assertEquals(m.group(2), "foo");
	}

	@Test
	public void decode_page_render_request() {
		Route route = new Route(SimplePage.class, SimplePage.class.getSimpleName());
		Request request = mockRequest();

		expect(request.getPath()).andReturn("/foo/45/bar/24").atLeastOnce();

		replay();

		PageRenderRequestParameters parameters = route.decodePageRenderRequest(request, urlEncoder, valueEncoder);

		Assert.assertEquals(parameters.getLogicalPageName(), "SimplePage");
		Assert.assertEquals(parameters.getActivationContext().getCount(), 2);
		Assert.assertEquals(parameters.getActivationContext().get(Integer.class, 0).intValue(), 45);
		Assert.assertEquals(parameters.getActivationContext().get(Integer.class, 1).intValue(), 24);

	}

	@Test
	public void home() {
		testPageRenderLinkGeneration("/", Home.class, "/", "", 0);
	}

	@Test
	public void home_with_context() {
		testPageRenderLinkGeneration("/myapp/", Home.class, "/", "/myapp", 0);
	}

	@Test
	public void subfolder_listing() {
		testPageRenderLinkGeneration("/subfolder/", SubFolderHome.class, "/subfolder/", "", 0);
	}

	@Test
	public void subfolder_listing_with_context() {
		testPageRenderLinkGeneration("/myapp/subfolder/", SubFolderHome.class, "/subfolder/", "/myapp", 0);
	}

	@Test
	public void simplepage() {
		testPageRenderLinkGeneration("/foo/45/bar/24", SimplePage.class, "/foo/45/bar/24", "", 2);

	}

	@Test
	public void simplepage_with_context() {
		testPageRenderLinkGeneration("/myapp/foo/45/bar/24", SimplePage.class, "/foo/45/bar/24", "/myapp", 2);
	}

	@Test
	public void subpackage() {
		testPageRenderLinkGeneration("/subpackage/inventedpath", SubPage.class, "/subpackage/inventedpath", "", 0);
	}

	@Test
	public void subpackage_with_package_prefix() {
		testPageRenderLinkGeneration("/subpackage", SubPackageMain.class, "/subpackage", "", 0);
	}

	@Test
	public void order() throws IOException {

		Class[] processOrder = {SubPage.class, SubPageFirst.class};
		Class first = SubPageFirst.class;
		String requestPath = "/subpackage/inventedpath";

		ComponentClassResolver classResolver = getService(ComponentClassResolver.class);
		String logical = classResolver.resolvePageClassNameToPageName(first.getName());
		String canonicalized = classResolver.canonicalizePageName(logical);

		Request request = mockRequest();
		expect(request.getPath()).andReturn(requestPath).atLeastOnce();

		PageRenderRequestParameters expectedParameters = new PageRenderRequestParameters(canonicalized, new EmptyEventContext(), false);

		ComponentRequestHandler requestHandler = mockComponentRequestHandler();

		requestHandler.handlePageRender(expectedParameters);

		RouterDispatcher routerDispatcher = new RouterDispatcher(requestHandler,
		                                                         null,
		                                                         null,
		                                                         classResolver,
		                                                         LoggerFactory.getLogger(RouteTest.class),
		                                                         Arrays.asList(processOrder));

		replay();

		routerDispatcher.dispatch(request, null);

		verify();
	}

	private void testPageRenderLinkGeneration(String expectedURI,
	                                          Class pageClass,
	                                          String requestPath,
	                                          String contextPath,
	                                          int activationContextCount) {

		ComponentClassResolver classResolver = getService(ComponentClassResolver.class);
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

		Route route = new Route(pageClass, canonicalized);
		PageRenderRequestParameters parameters = route.decodePageRenderRequest(request, urlEncoder, valueEncoder);

		Assert.assertEquals(parameters.getLogicalPageName(), logical);
		Assert.assertEquals(parameters.getActivationContext().getCount(), activationContextCount);

		RouterDispatcher routerDispatcher =
				new RouterDispatcher(null, null, null, classResolver, LoggerFactory.getLogger(RouteTest.class), Arrays.asList(pageClass));
		RouterLinkTransformer linkTransformer =
				new RouterLinkTransformer(routerDispatcher, request, securityManager, response, contextPathEncoder,
				                          null);

		Assert.assertEquals(linkTransformer.transformPageRenderLink(null, parameters).toURI(), expectedURI);
	}
}