package org.tynamo.routing.services;

import org.apache.tapestry5.services.*;
import org.tynamo.routing.annotations.At;
import org.tynamo.routing.Route;

import java.io.IOException;
import java.util.ArrayList;
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
	private final ComponentClassResolver resolver;

	private List<Class> pages;

//	private Orderer<Route> routes;
	private List<Route> routes;
	private Map<String, Route> routeMap;

	public RouterDispatcher(ComponentRequestHandler componentRequestHandler, ContextValueEncoder valueEncoder, URLEncoder urlEncoder, ComponentClassResolver resolver, List<Class> pages) {
		this.componentRequestHandler = componentRequestHandler;
		this.valueEncoder = valueEncoder;
		this.urlEncoder = urlEncoder;
		this.pages = pages;
		this.resolver = resolver;

		loadRoutes(pages);

	}

	private void loadRoutes(List<Class> pages) {

		routes = new ArrayList<Route>();
		routeMap = new HashMap<String, Route>();

		for (Class clazz : pages) {
			if (clazz.isAnnotationPresent(At.class)) {
				At ann = (At) clazz.getAnnotation(At.class);
				if (ann != null) {
					Route route = new Route(clazz);
					routes.add(route);
//					routes.add(clazz.getSimpleName().toLowerCase(), new Route(clazz), ann.order());
					routeMap.put(resolver.canonicalizePageName(resolver.resolvePageClassNameToPageName(clazz.getName())), route);
				}
			}
		}
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