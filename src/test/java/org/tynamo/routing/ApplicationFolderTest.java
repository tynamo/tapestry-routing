package org.tynamo.routing;

import org.apache.tapestry5.ioc.RegistryBuilder;
import org.testng.annotations.Test;
import org.tynamo.routing.pages.Home;
import org.tynamo.routing.modules.ApplicationFolderModule;

public class ApplicationFolderTest extends RoutingTestCase {

	@Override
	protected void addAdditionalModules(RegistryBuilder builder) {
		builder.add(ApplicationFolderModule.class);
	}

	@Test
	public void auto_discovery_enabled() {
		testPageRenderLinkGeneration("/myapp/t5", Home.class, "/t5/", "/myapp", 0);
	}

}
