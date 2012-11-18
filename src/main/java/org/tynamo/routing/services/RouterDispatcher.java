package org.tynamo.routing.services;

import org.apache.tapestry5.annotations.Log;
import org.apache.tapestry5.services.*;
import org.slf4j.Logger;
import org.tynamo.routing.Route;

import java.io.IOException;

/**
 * The router dispatcher recognizes URLs and dispatches them to a controllers action.
 * It can also generate paths and URLs, avoiding the need to hardcode strings in your views.
 */
public class RouterDispatcher implements Dispatcher {

	private final ComponentRequestHandler componentRequestHandler;
	private final ContextValueEncoder valueEncoder;
	private final URLEncoder urlEncoder;
	private final RouteSource routeSource;
	private final Logger logger;

	public RouterDispatcher(ComponentRequestHandler componentRequestHandler, ContextValueEncoder valueEncoder,
	                        URLEncoder urlEncoder, RouteSource routeSource, Logger logger) {
		this.componentRequestHandler = componentRequestHandler;
		this.valueEncoder = valueEncoder;
		this.urlEncoder = urlEncoder;
		this.logger = logger;
		this.routeSource = routeSource;
	}

	@Log
	public boolean dispatch(Request request, final Response response) throws IOException {

		for (Route route : routeSource.getRoutes()) {
			PageRenderRequestParameters parameters = route.decodePageRenderRequest(request, urlEncoder, valueEncoder);
			if (parameters != null) {
				if (logger.isDebugEnabled())
					logger.debug("routing using route: \"" + route.getPathExpression() + "\" for page: " + route.getCanonicalizedPageName());
				componentRequestHandler.handlePageRender(parameters);
				return true;
			}
		}
		return false;
	}
}