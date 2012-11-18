package org.tynamo.routing.services;

import org.tynamo.routing.Route;

public interface RouteFactory {

	Route create(String pathExpression, String canonicalized);
	Route create(String pathExpression, Class page);


}
