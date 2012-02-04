package org.tynamo.routing.services;

import org.apache.tapestry5.ioc.annotations.UsesConfiguration;

import java.util.Collection;

/**
 * Contains a set of contributed page classes from which to load routes.
 */
@UsesConfiguration(Class.class)
public interface AnnotatedPagesManager {

	Collection<Class> getPages();

}
