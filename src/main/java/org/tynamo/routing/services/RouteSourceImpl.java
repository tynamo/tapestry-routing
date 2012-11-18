package org.tynamo.routing.services;

import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.tynamo.routing.Route;

import java.util.List;
import java.util.Map;

public class RouteSourceImpl implements RouteSource {

	private final List<Route> routes;
	private final Map<String, Route> routeMap = CollectionFactory.newConcurrentMap();

	public RouteSourceImpl(List<Route> routes) {
		this.routes = routes;
		buildMap();
	}

	private void buildMap() {
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
