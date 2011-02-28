package org.tynamo.routing.services;

import org.apache.tapestry5.ioc.internal.util.Orderer;
import org.apache.tapestry5.services.*;
import org.slf4j.Logger;
import org.tynamo.routing.Route;
import org.tynamo.routing.annotations.At;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The router dispatcher recognizes URLs and dispatches them to a controllers action.
 * It can also generate paths and URLs, avoiding the need to hardcode strings in your views.
 */
public class RouterDispatcher implements Dispatcher {

	private final ComponentRequestHandler componentRequestHandler;
	private final ContextValueEncoder valueEncoder;
	private final URLEncoder urlEncoder;
	private final ComponentClassResolver componentClassResolver;
	private final Logger logger;

	private List<Class> pages;

	private List<Route> routes;
	private Map<String, Route> routeMap;

	public RouterDispatcher(ComponentRequestHandler componentRequestHandler, ContextValueEncoder valueEncoder,
	                        URLEncoder urlEncoder, ComponentClassResolver componentClassResolver, Logger logger,
	                        List<Class> pages)
	{
		this.componentRequestHandler = componentRequestHandler;
		this.valueEncoder = valueEncoder;
		this.urlEncoder = urlEncoder;
		this.pages = pages;
		this.componentClassResolver = componentClassResolver;
		this.logger = logger;

		loadRoutes(pages);

	}

	private void loadRoutes(List<Class> pages) {

		Orderer<Route> orderer = new Orderer<Route>(logger);
		routeMap = new HashMap<String, Route>();

		for (Class clazz : pages) {
			if (clazz.isAnnotationPresent(At.class)) {
				At ann = (At) clazz.getAnnotation(At.class);
				if (ann != null) {
					String canonicalized = componentClassResolver.canonicalizePageName(
							componentClassResolver.resolvePageClassNameToPageName(clazz.getName()));
					Route route = new Route(clazz, canonicalized);
					orderer.add(clazz.getSimpleName().toLowerCase(), route, ann.order());
					routeMap.put(canonicalized, route);
				}
			}
		}
		routes = orderer.getOrdered();
	}

	public boolean dispatch(Request request, final Response response) throws IOException {

		for (Route route : routes) {
			PageRenderRequestParameters parameters = route.decodePageRenderRequest(request, urlEncoder, valueEncoder);
			if (parameters != null) {
				componentRequestHandler.handlePageRender(parameters);
				return true;
			}
		}

		return false;
	}

	public Map<String, Route> getRouteMap() {
		return routeMap;
	}
}