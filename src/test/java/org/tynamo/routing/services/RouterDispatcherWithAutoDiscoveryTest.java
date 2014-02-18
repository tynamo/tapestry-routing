package org.tynamo.routing.services;

import org.apache.tapestry5.internal.test.PageTesterContext;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.apache.tapestry5.modules.TapestryModule;
import org.apache.tapestry5.services.ApplicationGlobals;
import org.apache.tapestry5.test.TapestryTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.tynamo.routing.modules.AutoDiscoveryDisabledModule;
import org.tynamo.routing.modules.TestsModule;


public class RouterDispatcherWithAutoDiscoveryTest extends TapestryTestCase {

	@Test
	public void auto_discovery_enabled() {

		Registry registry = getRegistry(TapestryModule.class, RoutingModule.class, TestsModule.class);

		AnnotatedPagesManager provider = registry.getService(AnnotatedPagesManager.class);
		Assert.assertEquals(provider.getRoutes().size(), 7, "there are seven pages with Routes in org/tynamo/routing");

		registry.cleanupThread();
		registry.shutdown();

	}

	@Test
	public void auto_discovery_disabled_only_one_contributed_service() {

		Registry registry = getRegistry(TapestryModule.class, RoutingModule.class, AutoDiscoveryDisabledModule.class);

		AnnotatedPagesManager provider = registry.getService(AnnotatedPagesManager.class);
		Assert.assertEquals(provider.getRoutes().size(), 1, "there is one contributed page and autodiscovery is disabled");

		registry.cleanupThread();
		registry.shutdown();

	}

	private Registry getRegistry(Class... moduleClasses) {

		RegistryBuilder builder = new RegistryBuilder();
		builder.add(moduleClasses);
		Registry registry = builder.build();

		ApplicationGlobals globals = registry.getObject(ApplicationGlobals.class, null);
		globals.storeContext(new PageTesterContext("src/test/webapp"));

		registry.performRegistryStartup();
		return registry;
	}


}