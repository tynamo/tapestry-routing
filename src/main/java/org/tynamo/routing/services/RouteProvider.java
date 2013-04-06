package org.tynamo.routing.services;

import org.tynamo.routing.Route;

import java.util.List;

public interface RouteProvider {

	Route getRoute(String canonicalizedPageName);

	List<Route> getRoutes();

}
