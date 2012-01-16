package org.tynamo.routing;

import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.apache.tapestry5.services.TapestryModule;
import org.apache.tapestry5.test.TapestryTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.tynamo.routing.services.AutoDiscoveryModule;
import org.tynamo.routing.services.RouterDispatcher;
import org.tynamo.routing.services.RoutingModule;


public class AutoDiscoveryTest extends TapestryTestCase {

	private static Registry registry;

	@BeforeSuite
	public final void setup_registry() {
		RegistryBuilder builder = new RegistryBuilder();

		builder.add(TapestryModule.class);
		builder.add(RoutingModule.class);
		builder.add(AutoDiscoveryModule.class);

		registry = builder.build();

		registry.performRegistryStartup();

	}

	@AfterSuite
	public final void shutdown_registry() {
		registry.shutdown();
		registry = null;
	}

	@AfterMethod
	public final void cleanupThread() {
		registry.cleanupThread();
	}

	public final <T> T getService(Class<T> serviceInterface) {
		return registry.getService(serviceInterface);
	}

	@Test
	public void auto_discovery_enabled() {
		RouterDispatcher dispatcher = getService(RouterDispatcher.class);
		Assert.assertEquals(dispatcher.getRouteMap().size(), 6, "there are six pages with Routes in org/tynamo/routing");
	}
}