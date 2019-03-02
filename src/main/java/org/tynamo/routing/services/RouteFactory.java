package org.tynamo.routing.services;

import org.tynamo.routing.Route;
import org.tynamo.routing.Behavior;

public interface RouteFactory {

	Route create(String pathExpression, String canonicalized, Behavior behavior);

	@Deprecated
	Route create(String pathExpression, Class page, Behavior behavior);

}
