package org.tynamo.routing.services;

import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.apache.tapestry5.services.TapestryModule;
import org.apache.tapestry5.test.TapestryTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;


public class AutoDiscoveryTest extends TapestryTestCase {

	@Test
	public void auto_discovery_enabled() {

		RegistryBuilder builder = new RegistryBuilder();

		builder.add(TapestryModule.class);
		builder.add(RoutingModule.class);
		builder.add(TestsModule.class);

		Registry registry = builder.build();

		registry.performRegistryStartup();

		RouterDispatcher dispatcher = registry.getService(RouterDispatcher.class);
		Assert.assertEquals(dispatcher.count(), 7, "there are six pages with Routes in org/tynamo/routing and one extra Routed contributed manualy");

		registry.cleanupThread();
		registry.shutdown();

	}

	@Test
	public void auto_discovery_disbled_only_one_contributed_service() {

		RegistryBuilder builder = new RegistryBuilder();

		builder.add(TapestryModule.class);
		builder.add(RoutingModule.class);
		builder.add(AutoDiscoveryDisabledModule.class);

		Registry registry = builder.build();

		registry.performRegistryStartup();

		RouterDispatcher dispatcher = registry.getService(RouterDispatcher.class);
		Assert.assertEquals(dispatcher.count(), 2, "there is one contributed page, one contributed route and autodiscovery is disabled");

		registry.cleanupThread();
		registry.shutdown();

	}

}