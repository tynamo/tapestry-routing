package org.tynamo.routing.services;

import org.apache.tapestry5.annotations.Log;
import org.apache.tapestry5.ioc.annotations.UsesConfiguration;
import org.apache.tapestry5.services.*;
import org.slf4j.Logger;
import org.tynamo.routing.Route;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The router dispatcher recognizes URLs and dispatches them to a controllers action.
 * It can also generate paths and URLs, avoiding the need to hardcode strings in your views.
 */
@UsesConfiguration(Route.class)
public class RouterDispatcher implements Dispatcher {

	private final ComponentRequestHandler componentRequestHandler;
	private final ContextValueEncoder valueEncoder;
	private final URLEncoder urlEncoder;
    private Logger logger;

	private List<Route> routes;
	private Map<String, Route> routeMap;

	public RouterDispatcher(ComponentRequestHandler componentRequestHandler, ContextValueEncoder valueEncoder,
	                        URLEncoder urlEncoder, List<Route> routes, Logger logger) {
		this.componentRequestHandler = componentRequestHandler;
		this.valueEncoder = valueEncoder;
		this.urlEncoder = urlEncoder;
		this.routes = routes;
		this.logger = logger;

		routeMap = buildMap(routes);

	}

	private static Map<String, Route> buildMap(List<Route> routes) {
		Map<String, Route> map = new HashMap<String, Route>();
		for (Route route : routes) {
			map.put(route.getCanonicalizedPageName(), route);
		}
		return map;
	}

	@Log
	public boolean dispatch(Request request, final Response response) throws IOException {

		for (Route route : routes) {
			PageRenderRequestParameters parameters = route.decodePageRenderRequest(request, urlEncoder, valueEncoder);
			if (parameters != null) {
				if (logger.isDebugEnabled()) logger.debug("routing using route: \"" + route.getPathExpression() + "\" for page: " + route.getCanonicalizedPageName());
				componentRequestHandler.handlePageRender(parameters);
				return true;
			}
		}
		return false;
	}

	public Route getRoute(String canonicalizedPageName) {
		return routeMap.get(canonicalizedPageName);
	}

	protected int count() {
		return routes.size();
	}
}