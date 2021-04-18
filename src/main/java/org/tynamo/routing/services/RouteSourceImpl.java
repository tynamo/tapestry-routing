package org.tynamo.routing.services;

import java.util.List;

import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.services.PageRenderRequestParameters;
import org.tynamo.routing.Route;

public class RouteSourceImpl implements RouteSource {

	private final RouteDecoder routeDecoder;
	private final List<RouteProvider> providers;

	public RouteSourceImpl(List<RouteProvider> providers, RouteDecoder routeDecoder) {
		this.routeDecoder = routeDecoder;
		this.providers = providers;
	}

	@Override
	public Route getRoute(final String canonicalizedPageName) {
		for (RouteProvider routeProvider : providers) {
			Route route = routeProvider.getRoute(canonicalizedPageName);
			if (route != null) return route;
		}
		return null;
	}

	@Override
	public PageRenderRequestParameters decodePageRenderRequest(Request request) {
		for (RouteProvider routeProvider : providers) {
			for (Route route : routeProvider.getRoutes()) {
				PageRenderRequestParameters parameters = routeDecoder.decodePageRenderRequest(route, request);
				if (parameters != null) return parameters;
			}
		}
		return null;
	}
}
