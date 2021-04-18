package org.tynamo.routing.services;

import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.ioc.annotations.UsesConfiguration;
import org.apache.tapestry5.services.PageRenderRequestParameters;
import org.tynamo.routing.Route;

@UsesConfiguration(Route.class)
public interface RouteSource {

	Route getRoute(String canonicalizedPageName);
	PageRenderRequestParameters decodePageRenderRequest(Request request);

}
