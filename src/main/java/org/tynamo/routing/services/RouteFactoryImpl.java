package org.tynamo.routing.services;

import org.apache.tapestry5.services.ComponentClassResolver;
import org.tynamo.routing.Route;

public class RouteFactoryImpl implements RouteFactory {

	private final ComponentClassResolver componentClassResolver;

	public RouteFactoryImpl(ComponentClassResolver componentClassResolver) {
		this.componentClassResolver = componentClassResolver;
	}

	@Override
	public Route create(String pathExpression, String canonicalized) {
		return new Route(pathExpression, canonicalized);
	}

	@Override
	public Route create(String pathExpression, Class page) {
		String pageName = componentClassResolver.resolvePageClassNameToPageName(page.getName());
		String canonicalized = componentClassResolver.canonicalizePageName(pageName);
		return create(pathExpression, canonicalized);
	}
}
