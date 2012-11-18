package org.tynamo.routing.services;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.LocalizationSetter;
import org.tynamo.routing.Route;

public class RouteFactoryImpl implements RouteFactory {

	private final LocalizationSetter localizationSetter;
	private final ComponentClassResolver componentClassResolver;
	private final String applicationFolder;
	private final boolean encodeLocaleIntoPath;

	public RouteFactoryImpl(LocalizationSetter localizationSetter,
	                        ComponentClassResolver componentClassResolver,
	                        @Symbol(SymbolConstants.APPLICATION_FOLDER) String applicationFolder,
	                        @Symbol(SymbolConstants.ENCODE_LOCALE_INTO_PATH) boolean encodeLocaleIntoPath) {
		this.localizationSetter = localizationSetter;
		this.componentClassResolver = componentClassResolver;
		this.applicationFolder = applicationFolder;
		this.encodeLocaleIntoPath = encodeLocaleIntoPath;
	}

	@Override
	public Route create(String pathExpression, String canonicalized) {
		return new Route(pathExpression, canonicalized, localizationSetter, encodeLocaleIntoPath, applicationFolder);
	}

	@Override
	public Route create(String pathExpression, Class page) {
		String pageName = componentClassResolver.resolvePageClassNameToPageName(page.getName());
		String canonicalized = componentClassResolver.canonicalizePageName(pageName);
		return create(pathExpression, canonicalized);
	}
}
