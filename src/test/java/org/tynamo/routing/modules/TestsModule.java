package org.tynamo.routing.modules;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.commons.MappedConfiguration;
import org.apache.tapestry5.commons.OrderedConfiguration;
import org.apache.tapestry5.http.internal.TapestryHttpInternalConstants;
import org.apache.tapestry5.http.internal.TapestryHttpInternalSymbols;
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
		configuration.add(TapestryHttpInternalConstants.TAPESTRY_APP_PACKAGE_PARAM, "org.tynamo.routing");
		configuration.add(TapestryHttpInternalSymbols.APP_NAME, "app");
		configuration.add(TapestryHttpInternalSymbols.APP_PACKAGE_PATH, "org/tynamo/routing");
		configuration.add(SymbolConstants.SUPPORTED_LOCALES, "en_US,fi");
		configuration.add(SymbolConstants.PRODUCTION_MODE, false);
	}

	@Primary @Contribute(RouteProvider.class)
	public static void addRoutes(OrderedConfiguration<Route> configuration, RouteFactory routeFactory) {

		String canonicalized = "subpackage/Unannotated";
		configuration.add(canonicalized.toLowerCase(), routeFactory.create("/not/annotated/{0}", canonicalized));

	}

}