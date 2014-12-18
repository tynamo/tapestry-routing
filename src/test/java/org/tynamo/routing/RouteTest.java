package org.tynamo.routing;

import org.apache.tapestry5.internal.EmptyEventContext;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.services.PageRenderDispatcher;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.apache.tapestry5.services.*;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.tynamo.routing.annotations.At;
import org.tynamo.routing.modules.TestsModule;
import org.tynamo.routing.pages.Home;
import org.tynamo.routing.pages.SimplePage;
import org.tynamo.routing.pages.SubFolderHome;
import org.tynamo.routing.pages.subpackage.SubPackageMain;
import org.tynamo.routing.pages.subpackage.SubPage;
import org.tynamo.routing.pages.subpackage.UnannotatedPage;
import org.tynamo.routing.services.RouterDispatcher;

import java.io.IOException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RouteTest extends RoutingTestCase {

	private static final String EMPTY_PATH = "";
	private static final int NO_CONTEXT = 0;
	private static final String APPLICATION_FOLDER = "";
	private static final Locale FI = new Locale("fi");

	@Override
	protected void addAdditionalModules(RegistryBuilder builder) {
		builder.add(TestsModule.class);
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
		Route route = routeFactory.create(SimplePage.class.getAnnotation(At.class).value(), SimplePage.class.getSimpleName());
		Request request = mockRequest();

		expect(request.getPath()).andReturn("/foo/45/bar/24").atLeastOnce();

		replay();

		PageRenderRequestParameters parameters = routeDecoder.decodePageRenderRequest(route, request);

		Assert.assertEquals(parameters.getLogicalPageName(), "SimplePage");
		Assert.assertEquals(parameters.getActivationContext().getCount(), 2);
		Assert.assertEquals(parameters.getActivationContext().get(Integer.class, 0).intValue(), 45);
		Assert.assertEquals(parameters.getActivationContext().get(Integer.class, 1).intValue(), 24);

	}

	@Test
	public void home() {
		testPageRenderLinkGeneration("/", Home.class, "/", EMPTY_PATH, NO_CONTEXT);
	}

	@Test
	public void home_with_locale() {
		testPageRenderLinkGeneration("/fi/", Home.class, "/", EMPTY_PATH, NO_CONTEXT, FI);
	}

	@Test
	public void home_with_explicit_locale() {
		testPageRenderLinkGeneration("/fi/", Home.class, "/fi/", EMPTY_PATH, NO_CONTEXT, FI);
	}

	@Test
	public void home_with_context() {
		testPageRenderLinkGeneration("/myapp/", Home.class, "/", "/myapp", NO_CONTEXT);
	}

	@Test
	public void home_with_context_and_locale() {
		testPageRenderLinkGeneration("/myapp/fi/", Home.class, "/", "/myapp", NO_CONTEXT, FI);
	}

	@Test
	public void home_with_context_and_explicit_locale() {
		testPageRenderLinkGeneration("/myapp/fi/", Home.class, "/fi/", "/myapp", NO_CONTEXT, FI);
	}

	@Test
	public void subfolder_listing() {
		testPageRenderLinkGeneration("/subfolder", SubFolderHome.class, "/subfolder/", EMPTY_PATH, NO_CONTEXT);
	}

	@Test
	public void subfolder_listing_with_locale() {
		testPageRenderLinkGeneration("/fi/subfolder", SubFolderHome.class, "/subfolder/", EMPTY_PATH, NO_CONTEXT, FI);
	}

	@Test
	public void subfolder_listing_with_explicit_locale() {
		testPageRenderLinkGeneration("/fi/subfolder", SubFolderHome.class, "/fi/subfolder/", EMPTY_PATH, NO_CONTEXT, FI);
	}

	@Test
	public void subfolder_listing_with_locale_path_encoding_off() {
		testPageRenderLinkGeneration("/subfolder", SubFolderHome.class, "/subfolder", EMPTY_PATH, NO_CONTEXT, FI, false, APPLICATION_FOLDER);
	}

	@Test
	public void subfolder_listing_without_last_slash() {
		testPageRenderLinkGeneration("/subfolder", SubFolderHome.class, "/subfolder", EMPTY_PATH, NO_CONTEXT);
	}

	@Test
	public void subfolder_listing_with_context() {
		testPageRenderLinkGeneration("/myapp/subfolder", SubFolderHome.class, "/subfolder/", "/myapp", NO_CONTEXT);
	}

	@Test
	public void simplepage() {
		testPageRenderLinkGeneration("/foo/45/bar/24", SimplePage.class, "/foo/45/bar/24", EMPTY_PATH, 2);
	}

	@Test
	public void simplepage_with_context() {
		testPageRenderLinkGeneration("/myapp/foo/45/bar/24", SimplePage.class, "/foo/45/bar/24", "/myapp", 2);
	}

	@Test
	public void subpackage() {
		testPageRenderLinkGeneration("/subpackage/inventedpath", SubPage.class, "/subpackage/inventedpath", EMPTY_PATH, NO_CONTEXT);
	}

	@Test
	public void subpackage_with_package_prefix() {
		testPageRenderLinkGeneration("/subpackage", SubPackageMain.class, "/subpackage", EMPTY_PATH, NO_CONTEXT);
	}

	@Test
	public void link_to_unannotatedpage() {
		testPageRenderLinkGeneration("/not/annotated/parameter", UnannotatedPage.class, "/not/annotated/parameter", EMPTY_PATH, 1);
	}

	@Test
	public void order() throws IOException {

		Request request = mockRequest();
		expect(request.getPath()).andReturn("/subpackage/inventedpath").atLeastOnce();

		PageRenderRequestParameters expectedParameters = new PageRenderRequestParameters("subpackage/SubPageFirst", new EmptyEventContext(), false);
		ComponentRequestHandler requestHandler = mockComponentRequestHandler();
		requestHandler.handlePageRender(expectedParameters);

		RouterDispatcher routerDispatcher = new RouterDispatcher(requestHandler, routeSource);

		replay();

		routerDispatcher.dispatch(request, null);

		verify();
	}

	@Test
	public void PageRender_precedence_over_RouterDispatcher() throws IOException {

		ComponentEventLinkEncoder linkEncoder = getService(ComponentEventLinkEncoder.class);
		ComponentRequestHandler handler = mockComponentRequestHandler();

		Dispatcher dispatcher = new PageRenderDispatcher(handler, linkEncoder);

		Request request = mockRequest();
		Response response = null;

		expect(request.getPath()).andReturn("/home").atLeastOnce();
		expect(request.getParameter("t:lb")).andReturn(null).atLeastOnce();
		expect(request.getLocale()).andReturn(FI).atLeastOnce();
		expect(request.getAttribute(InternalConstants.REFERENCED_COMPONENT_NOT_FOUND)).andReturn(null).once();

		Capture<PageRenderRequestParameters> parameters = newCapture();

		handler.handlePageRender(EasyMock.capture(parameters)); EasyMock.expectLastCall();

		replay();

		RequestGlobals globals = registry.getService(RequestGlobals.class);
		globals.storeRequestResponse(request, response);

		Assert.assertTrue(dispatcher.dispatch(request, response));
		Assert.assertEquals(parameters.getValue().getLogicalPageName(), "Home", "Home page should take precedence over the '/home' route in the Inaccessible page");
		Assert.assertEquals(parameters.getValue().getActivationContext().getCount(), 0);

		verify();
	}
}