package org.tynamo.routing.services;

import org.tynamo.routing.Route;

public interface RouteFactory {

	Route create(String pathExpression, String canonicalized);

	@Deprecated
	Route create(String pathExpression, Class page);

}
