package org.tynamo.routing.services;

import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Autobuild;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.Primary;
import org.apache.tapestry5.ioc.services.FactoryDefaults;
import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.apache.tapestry5.services.Dispatcher;
import org.apache.tapestry5.services.linktransform.PageRenderLinkTransformer;
import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;
import org.tynamo.routing.RoutingSymbols;

public class RoutingModule {

	public static void bind(ServiceBinder binder) {

		binder.bind(AnnotatedPagesManager.class);
		binder.bind(RouteFactory.class);
		binder.bind(RouteWorker.class);
		binder.bind(RouteDecoder.class);
		binder.bind(RouteSource.class);
		binder.bind(RouteProvider.class, RouteProviderImpl.class).withMarker(Primary.class);
	}

	@Contribute(PageRenderLinkTransformer.class)
	@Primary
	public static void provideURLRewriting(OrderedConfiguration<PageRenderLinkTransformer> configuration) {
		configuration.addInstance("RouterLinkTransformer", RouterLinkTransformer.class);
	}

	public static void contributeMasterDispatcher(OrderedConfiguration<Dispatcher> configuration,
	                                              @Autobuild RouterDispatcher dispatcher) {
		configuration.add(RouterDispatcher.class.getSimpleName(), dispatcher, "after:PageRender");
	}

	@Contribute(SymbolProvider.class)
	@FactoryDefaults
	public static void provideFactoryDefaults(final MappedConfiguration<String, String> configuration) {
		configuration.add(RoutingSymbols.DISABLE_AUTODISCOVERY, "false");
	}

	@Contribute(RouteSource.class)
	public static void addOtherSources(OrderedConfiguration<RouteProvider> configuration,
	                                   @Primary RouteProvider routeProvider,
	                                   AnnotatedPagesManager annotatedPagesManager) {
		configuration.add("DefaultRouteProvider", routeProvider);
		configuration.add("AnnotatedPagesManager", annotatedPagesManager);
	}

	@Contribute(ComponentClassTransformWorker2.class)
	@Primary
	public static void provideTransformWorkers(OrderedConfiguration<ComponentClassTransformWorker2> configuration,
	                                           RouteWorker routeWorker) {
		configuration.add("RouteWorker", routeWorker);
	}
}
