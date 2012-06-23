package org.tynamo.routing.services;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.InternalSymbols;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.services.ApplicationDefaults;
import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.apache.tapestry5.services.LocalizationSetter;
import org.tynamo.routing.Route;

public class TestsModule {

	@Contribute(SymbolProvider.class)
	@ApplicationDefaults
	public static void provideApplicationDefaults(MappedConfiguration<String, String> configuration) {
		configuration.add(InternalConstants.TAPESTRY_APP_PACKAGE_PARAM, "org.tynamo.routing");
		configuration.add(InternalSymbols.APP_PACKAGE_PATH, "org/tynamo/routing");
		configuration.add(SymbolConstants.SUPPORTED_LOCALES, "en_US,fi");
	}

	@Contribute(RouterDispatcher.class)
	public static void addRoutes(OrderedConfiguration<Route> configuration, LocalizationSetter localizationSetter) {
		String canonicalized = "subpackage/UnannotatedPage";
		configuration.add(canonicalized.toLowerCase(), new Route("/not/annotated/{0}", canonicalized, localizationSetter));
	}
	
}