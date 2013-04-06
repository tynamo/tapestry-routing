package org.tynamo.routing.services;

import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.services.PageSource;
import org.apache.tapestry5.ioc.annotations.PostInjection;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.services.ClassNameLocator;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.ComponentClasses;
import org.apache.tapestry5.services.InvalidationEventHub;
import org.apache.tapestry5.services.InvalidationListener;
import org.tynamo.routing.Route;
import org.tynamo.routing.RoutingSymbols;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class AnnotatedPagesManagerImpl implements AnnotatedPagesManager, InvalidationListener {

	private final Collection<Class> contributedClasses;

	private final ClassNameLocator classNameLocator;
	private final ComponentClassResolver componentClassResolver;
	private final PageSource pageSource;
	private final String appPackage;
	private final Boolean preventScan;

	private final List<Route> routes = CollectionFactory.newList();
	private final Map<String, Route> routeMap = CollectionFactory.newConcurrentMap();

	private boolean objectWasInvalidated;

	public AnnotatedPagesManagerImpl(Collection<Class> pages,
	                                 ClassNameLocator classNameLocator,
	                                 ComponentClassResolver componentClassResolver,
	                                 PageSource pageSource,
	                                 @Symbol(InternalConstants.TAPESTRY_APP_PACKAGE_PARAM) String appPackage,
	                                 @Symbol(RoutingSymbols.DISABLE_AUTODISCOVERY) Boolean preventScan) {
		this.contributedClasses = pages;
		this.classNameLocator = classNameLocator;
		this.componentClassResolver = componentClassResolver;
		this.pageSource = pageSource;

		this.appPackage = appPackage;
		this.preventScan = preventScan;
		this.objectWasInvalidated = true;
	}

	@Override
	public void add(Route route) {
		routes.add(route);
		routeMap.put(route.getCanonicalizedPageName(), route);
	}

	private void eagerLoadPages() {

		if (!preventScan) {
			for (String className : classNameLocator.locateClassNames(appPackage + "." + InternalConstants.PAGES_SUBPACKAGE)) {
				String pageName = componentClassResolver.resolvePageClassNameToPageName(className);
				pageSource.getPage(componentClassResolver.canonicalizePageName(pageName));
			}
		}

		for (Class page : contributedClasses) {
			String pageName = componentClassResolver.resolvePageClassNameToPageName(page.getName());
			pageSource.getPage(componentClassResolver.canonicalizePageName(pageName));
		}

		objectWasInvalidated = false;
	}

	@PostInjection
	public void listenForInvalidations(@ComponentClasses InvalidationEventHub hub) {
		hub.addInvalidationListener(this);
	}

	@Override
	public void objectWasInvalidated() {
		objectWasInvalidated = true;
		routes.clear();
	}

	@Override
	public Route getRoute(String canonicalizedPageName) {
		if (objectWasInvalidated) eagerLoadPages();
		return routeMap.get(canonicalizedPageName);
	}

	@Override
	public List<Route> getRoutes() {
		if (objectWasInvalidated) eagerLoadPages();
		return routes;
	}
}
