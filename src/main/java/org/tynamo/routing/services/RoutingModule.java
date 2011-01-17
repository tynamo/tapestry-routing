package org.tynamo.routing.services;

import org.apache.tapestry5.internal.InternalSymbols;
import org.apache.tapestry5.ioc.Configuration;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Primary;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.ClassNameLocator;
import org.apache.tapestry5.services.Dispatcher;
import org.apache.tapestry5.services.linktransform.PageRenderLinkTransformer;
import org.tynamo.routing.annotations.At;

public class RoutingModule {

	public static void bind(ServiceBinder binder) {

		binder.bind(RouterDispatcher.class);
	}

	@Contribute(PageRenderLinkTransformer.class)
	@Primary
	public static void provideURLRewriting(OrderedConfiguration<PageRenderLinkTransformer> configuration) {
		configuration.addInstance("RouterLinkTransformer", RouterLinkTransformer.class);
	}

	public static void contributeMasterDispatcher(OrderedConfiguration<Dispatcher> configuration, RouterDispatcher dispatcher) {
		configuration.add(RouterDispatcher.class.getSimpleName(), dispatcher, "after:PageRender");
	}

	@Contribute(RouterDispatcher.class)
	public static void contributeRouterDispatcher(OrderedConfiguration<Class> configuration,
	                                              @Inject @Symbol(InternalSymbols.APP_PACKAGE_PATH) String appPackagePath,
	                                              ClassNameLocator classNameLocator) {

		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

		for (String className : classNameLocator.locateClassNames(appPackagePath)) {
			try {
				Class entityClass = contextClassLoader.loadClass(className);

				if (entityClass.isAnnotationPresent(At.class)) {
					configuration.add(entityClass.getSimpleName(), entityClass);
				}
			}

			catch (ClassNotFoundException ex) {
				throw new RuntimeException(ex);
			}
		}
	}

}
