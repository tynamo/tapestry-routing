package org.tynamo.routing.modules;

import org.apache.tapestry5.ioc.Configuration;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.SubModule;
import org.apache.tapestry5.ioc.services.ApplicationDefaults;
import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.tynamo.routing.RoutingSymbols;
import org.tynamo.routing.pages.SimplePage;
import org.tynamo.routing.services.AnnotatedPagesManager;

@SubModule(TestsModule.class)
public class AutoDiscoveryDisabledModule {

	@Contribute(SymbolProvider.class)
	@ApplicationDefaults
	public static void provideApplicationDefaults(MappedConfiguration<String, String> configuration) {
		configuration.add(RoutingSymbols.DISABLE_AUTODISCOVERY, "true");
	}

	@Contribute(AnnotatedPagesManager.class)
	public static void annotatedPagesManager(Configuration<Class> configuration) {
		configuration.add(SimplePage.class);
	}

}