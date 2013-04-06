package org.tynamo.routing.services;

import org.apache.tapestry5.ioc.annotations.UsesConfiguration;
import org.tynamo.routing.Route;

/**
 * Contains a set of contributed page classes from which to load routes.
 */
@UsesConfiguration(Class.class)
public interface AnnotatedPagesManager extends RouteProvider {

	void add(Route route);

}
