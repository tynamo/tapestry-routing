package org.tynamo.routing.modules;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.InternalSymbols;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.Primary;
import org.apache.tapestry5.ioc.services.ApplicationDefaults;
import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.tynamo.routing.Route;
import org.tynamo.routing.services.RouteFactory;
import org.tynamo.routing.services.RouteProvider;

public class TestsModule {

	@Contribute(SymbolProvider.class)
	@ApplicationDefaults
	public static void provideApplicationDefaults(MappedConfiguration<String, Object> configuration) {
		configuration.add(InternalConstants.TAPESTRY_APP_PACKAGE_PARAM, "org.tynamo.routing");
		configuration.add(InternalSymbols.APP_NAME, "app");
		configuration.add(InternalSymbols.APP_PACKAGE_PATH, "org/tynamo/routing");
		configuration.add(SymbolConstants.SUPPORTED_LOCALES, "en_US,fi");
	}

	@Primary @Contribute(RouteProvider.class)
	public static void addRoutes(OrderedConfiguration<Route> configuration, RouteFactory routeFactory) {

		String canonicalized = "subpackage/UnannotatedPage";
		configuration.add(canonicalized.toLowerCase(), routeFactory.create("/not/annotated/{0}", canonicalized));

	}

}