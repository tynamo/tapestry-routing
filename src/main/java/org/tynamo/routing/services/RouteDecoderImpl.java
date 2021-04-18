package org.tynamo.routing.services;

import java.util.regex.Matcher;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.internal.EmptyEventContext;
import org.apache.tapestry5.internal.URLEventContext;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.ContextValueEncoder;
import org.apache.tapestry5.services.LocalizationSetter;
import org.apache.tapestry5.services.PageRenderRequestParameters;
import org.apache.tapestry5.services.URLEncoder;
import org.slf4j.Logger;
import org.tynamo.routing.Route;

public class RouteDecoderImpl implements RouteDecoder {

	private static final char SLASH = '/';

	private final LocalizationSetter localizationSetter;
	private final boolean encodeLocaleIntoPath;
	private final String applicationFolder;

	private final URLEncoder urlEncoder;
	private final ContextValueEncoder valueEncoder;
	private final Logger logger;

	public RouteDecoderImpl(LocalizationSetter localizationSetter,
	                        @Symbol(SymbolConstants.ENCODE_LOCALE_INTO_PATH) boolean encodeLocaleIntoPath,
	                        @Symbol(SymbolConstants.APPLICATION_FOLDER) final String applicationFolder,
	                        URLEncoder urlEncoder,
	                        ContextValueEncoder valueEncoder,
	                        Logger logger) {


		this.localizationSetter = localizationSetter;
		this.encodeLocaleIntoPath = encodeLocaleIntoPath;

		this.applicationFolder = applicationFolder.equals("") ? "" : SLASH + applicationFolder;

		this.urlEncoder = urlEncoder;
		this.valueEncoder = valueEncoder;
		this.logger = logger;
	}

	private String getLocaleFromPath(String path) {
		if (!encodeLocaleIntoPath) return null;
		// we have to get the possibly encoded locale from the request
		// the following was copied and modified from AppPageRenderLinkTransformer.decodePageRenderRequest(...)
		String[] split = path.substring(1).split("/");
		if (split.length > 0 && !"".equals(split[0])) {
			String possibleLocaleName = split[0];
			// Might be just the page activation context, or it might be locale then page
			// activation context
			return localizationSetter.isSupportedLocaleName(possibleLocaleName) ? possibleLocaleName : null;
		}
		return null;
	}

	private String removeAppFolderAndLocaleFromPath(final Request request) {

		String path = request.getPath();

		if (this.applicationFolder.length() > 0) {
			path = path.substring(applicationFolder.length());
		}

		String locale = getLocaleFromPath(path);

		if (locale != null) {
			localizationSetter.setLocaleFromLocaleName(locale);
			path = path.substring(locale.length() + 1);
		}
		return path.length() > 1 && path.charAt(path.length() - 1) == '/' ? path.substring(0, path.length() - 1) : path;
	}

	@Override
	public PageRenderRequestParameters decodePageRenderRequest(final Route route, final Request request) {

		Matcher matcher = route.getPattern().matcher(removeAppFolderAndLocaleFromPath(request));
		if (!matcher.matches()) return null;

		EventContext context;

		int groupsSize = matcher.groupCount();

		if (groupsSize < 1) {
			context = new EmptyEventContext();
		} else {

			String[] split = new String[groupsSize];

			for (int i = 0; i < groupsSize; i++) {
				String value = matcher.group(i + 1);
				split[i] = urlEncoder.decode(value);
			}

			context = new URLEventContext(valueEncoder, split);
		}

		if (logger.isDebugEnabled())
			logger.debug("decoding render request using route: \"" + route.getPathExpression() + "\" for page: " + route.getCanonicalizedPageName());

		return new PageRenderRequestParameters(route.getCanonicalizedPageName(), context, false);

	}
}
