package org.tynamo.routing;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.internal.services.LinkSecurity;
import org.apache.tapestry5.internal.services.RequestSecurityManager;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.apache.tapestry5.services.*;
import org.apache.tapestry5.test.TapestryTestCase;
import org.testng.Assert;
import org.testng.annotations.*;
import org.tynamo.routing.annotations.At;
import org.tynamo.routing.services.RouterDispatcher;
import org.tynamo.routing.services.RouterLinkTransformer;

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
	public void testRegularExpressions() {

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
	public void decodePageRenderRequestTest() {
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
	public void homeWithContext() {

		testLinkGeneration("/myapp/", Home.class, "/", "/myapp", 0);
	}

	@Test
	public void subfolderListingWithContext() {
		testLinkGeneration("/myapp/subfolder/", SubFolderHome.class, "/subfolder/", "/myapp", 0);
	}

	@Test
	public void homeWithoutContext() {

		testLinkGeneration("/", Home.class, "/", "", 0);
	}

	@Test
	public void subfolderListingWithoutContext() {

		testLinkGeneration("/subfolder/", SubFolderHome.class, "/subfolder/", "", 0);

	}

	@Test
	public void simplePageWithContext() {

		testLinkGeneration("/myapp/foo/45/bar/24", SimplePage.class, "/foo/45/bar/24", "/myapp", 2);

	}

	@Test
	public void simplePageWithoutContext() {
		testLinkGeneration("/foo/45/bar/24", SimplePage.class, "/foo/45/bar/24", "", 2);

	}

	private void testLinkGeneration(String expectedURI, Class pageClass, String requestPath, String contextPath,
	                                int activationContextCount) {
		String simpleName = pageClass.getSimpleName();

		Request request = mockRequest();
		expect(request.getPath()).andReturn(requestPath).atLeastOnce();
		expect(request.getContextPath()).andReturn(contextPath).atLeastOnce();

		Response response = mockResponse();
		train_encodeURL(response, expectedURI, expectedURI);

		RequestSecurityManager securityManager = newMock(RequestSecurityManager.class);
		expect(securityManager.checkPageSecurity(simpleName)).andReturn(LinkSecurity.INSECURE);

		ComponentClassResolver classResolver = mockComponentClassResolver();
		expect(classResolver.resolvePageClassNameToPageName(pageClass.getName())).andReturn(simpleName.toLowerCase());
		expect(classResolver.canonicalizePageName(simpleName.toLowerCase())).andReturn(simpleName);

		replay();

		Route route = new Route(pageClass, pageClass.getSimpleName());
		PageRenderRequestParameters parameters = route.decodePageRenderRequest(request, urlEncoder, valueEncoder);

		Assert.assertEquals(parameters.getLogicalPageName(), simpleName);
		Assert.assertEquals(parameters.getActivationContext().getCount(), activationContextCount);

		RouterDispatcher routerDispatcher =
				new RouterDispatcher(null, null, null, classResolver, Arrays.asList((Class) pageClass));
		RouterLinkTransformer linkTransformer =
				new RouterLinkTransformer(routerDispatcher, request, securityManager, response, contextPathEncoder,
				                          null);

		Assert.assertEquals(linkTransformer.transformPageRenderLink(null, parameters).toURI(), expectedURI);
	}


	@At("/foo/{0}/bar/{1}")
	class SimplePage {

		@Property
		private String message;

		protected void onActivate(String message0, String message1) throws Exception {
			this.message = message0 + " - " + message1;
		}
	}

	@At("/")
	class Home {

		protected void onActivate() {
		}
	}

	@At("/subfolder/")
	class SubFolderHome {

		protected void onActivate() {
		}
	}

}
