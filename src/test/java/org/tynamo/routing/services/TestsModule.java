package org.tynamo.routing.services;

import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.InternalSymbols;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.services.ApplicationDefaults;
import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.tynamo.routing.Route;

public class TestsModule {

	@Contribute(SymbolProvider.class)
	@ApplicationDefaults
	public static void provideApplicationDefaults(MappedConfiguration<String, String> configuration) {
		configuration.add(InternalConstants.TAPESTRY_APP_PACKAGE_PARAM, "org.tynamo.routing");
		configuration.add(InternalSymbols.APP_PACKAGE_PATH, "org/tynamo/routing");
	}

	@Contribute(RouterDispatcher.class)
	public static void addRoutes(OrderedConfiguration<Route> configuration) {
		configuration.add("UnannotatedPage", new Route("/not/annotated/{0}", "subpackage/UnannotatedPage"));
	}
}