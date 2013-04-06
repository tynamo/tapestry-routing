package org.tynamo.routing.services;

import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.apache.tapestry5.services.TapestryModule;
import org.apache.tapestry5.test.TapestryTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.tynamo.routing.modules.AutoDiscoveryDisabledModule;
import org.tynamo.routing.modules.TestsModule;


public class RouterDispatcherWithAutoDiscoveryTest extends TapestryTestCase {

	@Test
	public void auto_discovery_enabled() {

		RegistryBuilder builder = new RegistryBuilder();

		builder.add(TapestryModule.class);
		builder.add(RoutingModule.class);
		builder.add(TestsModule.class);

		Registry registry = builder.build();

		registry.performRegistryStartup();

//		RouteSource source = registry.getService(RouteSource.class);
//		Assert.assertEquals(source.getRoutes().size(), 7, "there are six pages with Routes in org/tynamo/routing and one extra Routed contributed manualy");

		registry.cleanupThread();
		registry.shutdown();

	}

	@Test
	public void auto_discovery_disabled_only_one_contributed_service() {

		RegistryBuilder builder = new RegistryBuilder();

		builder.add(TapestryModule.class);
		builder.add(RoutingModule.class);
		builder.add(AutoDiscoveryDisabledModule.class);

		Registry registry = builder.build();

		registry.performRegistryStartup();

//		RouteSource source = registry.getService(RouteSource.class);
//		Assert.assertEquals(source.getRoutes().size(), 2, "there is one contributed page, one contributed route and autodiscovery is disabled");

		registry.cleanupThread();
		registry.shutdown();

	}

}