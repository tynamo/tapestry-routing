package org.tynamo.routing.services;

import org.apache.tapestry5.annotations.Log;
import org.apache.tapestry5.ioc.annotations.Primary;
import org.apache.tapestry5.services.*;

import java.io.IOException;

/**
 * The router dispatcher recognizes incoming requests and transforms them into page render requests.
 */
public class RouterDispatcher implements Dispatcher {

	private final ComponentRequestHandler componentRequestHandler;
	private final RouteSource routeSource;

	public RouterDispatcher(ComponentRequestHandler componentRequestHandler, @Primary RouteSource routeSource) {
		this.componentRequestHandler = componentRequestHandler;
		this.routeSource = routeSource;
	}

	@Log
	public boolean dispatch(Request request, final Response response) throws IOException {
		PageRenderRequestParameters parameters = routeSource.decodePageRenderRequest(request);
		if (parameters != null) {
			componentRequestHandler.handlePageRender(parameters);
			return true;
		}
		return false;
	}
}