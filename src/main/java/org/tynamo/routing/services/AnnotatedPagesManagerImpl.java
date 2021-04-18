package org.tynamo.routing.services;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.tapestry5.commons.services.InvalidationEventHub;
import org.apache.tapestry5.commons.services.InvalidationListener;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.services.PageSource;
import org.apache.tapestry5.ioc.annotations.ComponentClasses;
import org.apache.tapestry5.ioc.annotations.PostInjection;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.Orderer;
import org.apache.tapestry5.ioc.services.ClassNameLocator;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.slf4j.Logger;
import org.tynamo.routing.Route;
import org.tynamo.routing.RoutingSymbols;

public class AnnotatedPagesManagerImpl implements AnnotatedPagesManager, InvalidationListener {

	private final Collection<Class> contributedClasses;

	private final Logger logger;
	private final ClassNameLocator classNameLocator;
	private final ComponentClassResolver componentClassResolver;
	private final PageSource pageSource;
	private final String appPackage;
	private final Boolean preventScan;

	private Orderer<Route> routeOrderer;
	private List<Route> routes;
	private Map<String, Route> routeMap;

	private boolean objectWasInvalidated;

	public AnnotatedPagesManagerImpl(Collection<Class> pages,
	                                 Logger logger,
	                                 ClassNameLocator classNameLocator,
	                                 ComponentClassResolver componentClassResolver,
	                                 PageSource pageSource,
	                                 @Symbol(InternalConstants.TAPESTRY_APP_PACKAGE_PARAM) String appPackage,
	                                 @Symbol(RoutingSymbols.DISABLE_AUTODISCOVERY) Boolean preventScan) {

		this.contributedClasses = pages;

		this.logger = logger;
		this.classNameLocator = classNameLocator;
		this.componentClassResolver = componentClassResolver;
		this.pageSource = pageSource;

		this.appPackage = appPackage;
		this.preventScan = preventScan;

		this.objectWasInvalidated = true;

		routeOrderer = new Orderer<Route>(logger);
		routeMap = CollectionFactory.newConcurrentMap();
	}

	@Override
	public void add(Route route, String... order) {
		routeOrderer.add(route.getCanonicalizedPageName(), route, order);
		routeMap.put(route.getCanonicalizedPageName(), route);
	}

	private void eagerLoadPages() {

		if (!preventScan) {
			for (String className : classNameLocator.locateClassNames(appPackage + "." + InternalConstants.PAGES_SUBPACKAGE)) {
				String pageName = componentClassResolver.resolvePageClassNameToPageName(className);
				try {
					pageSource.getPage(componentClassResolver.canonicalizePageName(pageName));
				} catch (Exception e) {
					// TYNAMO-222
					logger.error(e.getMessage());
				}
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
		routeOrderer = new Orderer<Route>(logger);
		routeMap.clear();
		routes = null;
	}

	@Override
	public Route getRoute(String canonicalizedPageName) {
		if (objectWasInvalidated) eagerLoadPages();
		return routeMap.get(canonicalizedPageName);
	}

	@Override
	public List<Route> getRoutes() {
		if (objectWasInvalidated) eagerLoadPages();
		if (routes == null) {
			routes = Collections.unmodifiableList(routeOrderer.getOrdered());
		}
		return routes;
	}
}
