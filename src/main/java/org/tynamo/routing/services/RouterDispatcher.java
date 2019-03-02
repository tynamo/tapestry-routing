package org.tynamo.routing.services;

import org.apache.tapestry5.annotations.Log;
import org.apache.tapestry5.ioc.annotations.Primary;
import org.apache.tapestry5.services.*;

import java.io.IOException;

import org.tynamo.routing.Route;

/**
 * The router dispatcher recognizes incoming requests and transforms them into page render requests.
 */
public class RouterDispatcher implements Dispatcher {

	private final ComponentRequestHandler componentRequestHandler;
	private final ComponentEventLinkEncoder linkEncoder;
	private final RouteSource routeSource;

	public RouterDispatcher(ComponentRequestHandler componentRequestHandler, ComponentEventLinkEncoder linkEncoder, RouteSource routeSource) {
		this.componentRequestHandler = componentRequestHandler;
		this.linkEncoder = linkEncoder;
		this.routeSource = routeSource;
	}

	@Log
	public boolean dispatch(Request request, final Response response) throws IOException {
		PageRenderRequestParameters parameters = routeSource.decodePageRenderRequest(request);
		if (parameters != null) {
			componentRequestHandler.handlePageRender(parameters);
			return true;
		}

		parameters = linkEncoder.decodePageRenderRequest(request);
		if (parameters == null) return false;

		Route route = routeSource.getRoute(parameters.getLogicalPageName());
		if (route == null) return false;

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