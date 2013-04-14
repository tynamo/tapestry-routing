package org.tynamo.routing.services;

import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.PlasticClass;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;
import org.apache.tapestry5.services.transform.TransformationSupport;
import org.tynamo.routing.annotations.At;
import org.tynamo.routing.annotations.Route;

public class RouteWorker implements ComponentClassTransformWorker2 {

	private final AnnotatedPagesManager annotatedPagesManager;
	private final ComponentClassResolver componentClassResolver;
	private final RouteFactory routeFactory;

	public RouteWorker(AnnotatedPagesManager annotatedPagesManager, ComponentClassResolver componentClassResolver, RouteFactory routeFactory) {
		this.annotatedPagesManager = annotatedPagesManager;
		this.componentClassResolver = componentClassResolver;
		this.routeFactory = routeFactory;
	}

	@Override
	public void transform(PlasticClass plasticClass, TransformationSupport support, MutableComponentModel model) {

		String pathExpression = null;
		String[] order = {};

		if (plasticClass.hasAnnotation(At.class)) {
			At ann = plasticClass.getAnnotation(At.class);
			pathExpression = ann.value();
			order = ann.order();
		} else if (plasticClass.hasAnnotation(Route.class)) {
			Route ann = plasticClass.getAnnotation(Route.class);
			pathExpression = ann.value();
			order = ann.order();
		}

		if (pathExpression != null) {
			String pageName = componentClassResolver.resolvePageClassNameToPageName(plasticClass.getClassName());
			String canonicalized = componentClassResolver.canonicalizePageName(pageName);

			org.tynamo.routing.Route route = routeFactory.create(pathExpression, canonicalized);

			annotatedPagesManager.add(route, order);
		}
	}
}
