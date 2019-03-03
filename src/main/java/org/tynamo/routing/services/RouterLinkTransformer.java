package org.tynamo.routing.services;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.TapestryConstants;
import org.apache.tapestry5.internal.services.LinkImpl;
import org.apache.tapestry5.internal.services.RequestSecurityManager;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.BaseURLSource;
import org.apache.tapestry5.services.ContextPathEncoder;
import org.apache.tapestry5.services.PageRenderRequestParameters;
import org.apache.tapestry5.services.PersistentLocale;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Response;
import org.apache.tapestry5.services.linktransform.PageRenderLinkTransformer;
import org.tynamo.routing.Route;

import java.text.MessageFormat;
import java.util.Locale;

public class RouterLinkTransformer implements PageRenderLinkTransformer {

	private static final char SLASH = '/';
	private static final int BUFFER_SIZE = 100;

	private final RouteSource routeSource;
	private final Request request;
	private final RequestSecurityManager requestSecurityManager;
	private final Response response;
	private final ContextPathEncoder contextPathEncoder;
	private final BaseURLSource baseURLSource;
	private final PersistentLocale persistentLocale;
	private final String contextPath;
	private final boolean encodeLocaleIntoPath;
	private final String applicationFolder;

	public RouterLinkTransformer(RouteSource routeSource,
	                             Request request,
	                             RequestSecurityManager requestSecurityManager, Response response,
	                             ContextPathEncoder contextPathEncoder, BaseURLSource baseURLSource,
	                             PersistentLocale persistentLocale,
	                             @Symbol(SymbolConstants.CONTEXT_PATH) final String contextPath,
	                             @Symbol(SymbolConstants.ENCODE_LOCALE_INTO_PATH) boolean encodeLocaleIntoPath,
	                             @Symbol(SymbolConstants.APPLICATION_FOLDER) final String applicationFolder) {
		this.routeSource = routeSource;
		this.request = request;
		this.requestSecurityManager = requestSecurityManager;
		this.response = response;
		this.contextPathEncoder = contextPathEncoder;
		this.baseURLSource = baseURLSource;
		this.persistentLocale = persistentLocale;
		this.contextPath = contextPath;
		this.encodeLocaleIntoPath = encodeLocaleIntoPath;
		this.applicationFolder = applicationFolder;
	}

	public PageRenderRequestParameters decodePageRenderRequest(Request request) {
		// this transformer should not intervene in the request decoding process
		return null; // return null to continue down the PageRenderLinkTransformer chain
	}

	public Link transformPageRenderLink(Link defaultLink, PageRenderRequestParameters parameters) {
		String activePageName = parameters.getLogicalPageName();

		Route route = routeSource.getRoute(activePageName);

		if (route != null) {
			StringBuilder builder = new StringBuilder(BUFFER_SIZE);

			if (!"".equals(contextPath)) {
				// Build up the absolute URI.
				builder.append(contextPath);
			}

			encodeAppFolderAndLocale(builder);

			// deal with the very special case of a simple SLASH as a path expression
			String pathExpression = route.getPathExpression();
			if (!(pathExpression.length() == 1 && pathExpression.charAt(0) == SLASH && !applicationFolder.equals(""))) {
				builder.append(MessageFormat.format(route.getPathExpression(), (Object[]) encode(parameters.getActivationContext())));
			}

			Link link = new LinkImpl(builder.toString(), false, requestSecurityManager.checkPageSecurity(activePageName), response, contextPathEncoder, baseURLSource);

			if (parameters.isLoopback())
				link.addParameter(TapestryConstants.PAGE_LOOPBACK_PARAMETER_NAME, "t");

			return link;
		}

		return null;
	}

	private void encodeAppFolderAndLocale(StringBuilder builder) {
		if (!applicationFolder.equals(""))
		{
			builder.append(SLASH).append(applicationFolder);
		}

		if (encodeLocaleIntoPath)
		{
			Locale locale = persistentLocale.get();

			if (locale != null)
			{
				builder.append(SLASH);
				builder.append(locale.toString());
			}
		}
	}

	private String[] encode(EventContext context) {
		assert context != null;
		int count = context.getCount();

		String output[] = new String[count];

		for (int i = 0; i < count; i++) {
			Object raw = context.get(Object.class, i);
			output[i] = contextPathEncoder.encodeValue(raw);
		}

		return output;
	}
}
