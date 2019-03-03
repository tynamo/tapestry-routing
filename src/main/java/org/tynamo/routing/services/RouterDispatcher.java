package org.tynamo.routing.services;

import org.apache.tapestry5.annotations.Log;
import org.apache.tapestry5.services.ComponentEventLinkEncoder;
import org.apache.tapestry5.services.ComponentRequestHandler;
import org.apache.tapestry5.services.Dispatcher;
import org.apache.tapestry5.services.PageRenderRequestParameters;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Response;
import org.tynamo.routing.Route;

import java.io.IOException;

/**
 * The router dispatcher recognizes incoming requests and transforms them into page render requests.
 */
public class RouterDispatcher implements Dispatcher {

	private final ComponentRequestHandler componentRequestHandler;
	private final ComponentEventLinkEncoder linkEncoder;
	private final RouteSource routeSource;

	public RouterDispatcher(final ComponentRequestHandler componentRequestHandler, final ComponentEventLinkEncoder linkEncoder, final RouteSource routeSource) {
		this.componentRequestHandler = componentRequestHandler;
		this.linkEncoder = linkEncoder;
		this.routeSource = routeSource;
	}

	@Log
	public boolean dispatch(final Request request, final Response response) throws IOException {
		PageRenderRequestParameters pageParameters = linkEncoder.decodePageRenderRequest(request);
		PageRenderRequestParameters routeParameters = routeSource.decodePageRenderRequest(request);
		Route pageParametersRoute = pageParameters == null ? null : routeSource.getRoute(pageParameters.getLogicalPageName());
		Route routeParametersRoute = routeParameters == null ? null : routeSource.getRoute(routeParameters.getLogicalPageName());

		if (pageParameters != null && pageParametersRoute == null) {
			return false;
		}
		if (pageParameters != null && pageParametersRoute != null && !pageParameters.getLogicalPageName().endsWith("Index")) {
			return handleRoute(pageParametersRoute, pageParameters, request, response);
		}
		if (routeParametersRoute != null) {
			componentRequestHandler.handlePageRender(routeParameters);
			return true;
		}
		if (pageParametersRoute != null) {
			boolean handled = handleRoute(pageParametersRoute, pageParameters, request, response);
			if (handled) return true;
		}

		return false;
	}

	private boolean handleRoute(Route route, PageRenderRequestParameters parameters, Request request, Response response) throws IOException {
		switch (route.getBehavior()) {
			case REDIRECT:
				response.sendRedirect(linkEncoder.createPageRenderLink(parameters).toAbsoluteURI());
				return true;
			case NOT_FOUND:
				response.sendError(404, "Not Found");
				return true;
			case DEFAULT:
			default:
				return false;
		}
	}
}