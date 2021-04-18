package org.tynamo.routing.services;

import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.services.PageRenderRequestParameters;
import org.tynamo.routing.Route;

public interface RouteDecoder {

	PageRenderRequestParameters decodePageRenderRequest(Route route, Request request);

}
