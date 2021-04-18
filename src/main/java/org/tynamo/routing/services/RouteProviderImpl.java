package org.tynamo.routing.services;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.tapestry5.commons.util.CollectionFactory;
import org.tynamo.routing.Route;

public class RouteProviderImpl implements RouteProvider {

	private final List<Route> routes;
	private final Map<String, Route> routeMap = CollectionFactory.newConcurrentMap();

	public RouteProviderImpl(List<Route> routes) {
		this.routes = Collections.unmodifiableList(routes);
		for (Route route : routes) {
			routeMap.put(route.getCanonicalizedPageName(), route);
		}
	}

	@Override
	public Route getRoute(String canonicalizedPageName) {
		return routeMap.get(canonicalizedPageName);
	}

	@Override
	public List<Route> getRoutes() {
		return routes;
	}
}
