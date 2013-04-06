package org.tynamo.routing.services;

import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.PlasticClass;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;
import org.apache.tapestry5.services.transform.TransformationSupport;
import org.tynamo.routing.Route;
import org.tynamo.routing.annotations.At;

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
		if (plasticClass.hasAnnotation(At.class)) {
			At ann = plasticClass.getAnnotation(At.class);
			String pathExpression = ann.value();

			String pageName = componentClassResolver.resolvePageClassNameToPageName(plasticClass.getClassName());
			String canonicalized = componentClassResolver.canonicalizePageName(pageName);

			Route route = routeFactory.create(pathExpression, canonicalized);

			annotatedPagesManager.add(route);
		}
	}
}
