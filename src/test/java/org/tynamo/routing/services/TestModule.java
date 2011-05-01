package org.tynamo.routing.services;

import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.ioc.MappedConfiguration;

public class TestModule {
	public static void contributeApplicationDefaults(MappedConfiguration<String, String> configuration) {
		configuration.add(InternalConstants.TAPESTRY_APP_PACKAGE_PARAM, "org.tynamo.routing");
	}
}