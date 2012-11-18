package org.tynamo.routing.services;

import org.apache.tapestry5.ioc.annotations.UsesConfiguration;
import org.tynamo.routing.Route;

import java.util.List;

@UsesConfiguration(Route.class)
public interface RouteSource {

	Route getRoute(String canonicalizedPageName);
	List<Route> getRoutes();

}
