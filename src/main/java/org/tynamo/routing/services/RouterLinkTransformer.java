package org.tynamo.routing.services;

import org.apache.tapestry5.Link;
import org.apache.tapestry5.TapestryConstants;
import org.apache.tapestry5.internal.services.LinkImpl;
import org.apache.tapestry5.internal.services.RequestSecurityManager;
import org.apache.tapestry5.services.*;
import org.apache.tapestry5.services.linktransform.PageRenderLinkTransformer;
import org.tynamo.routing.Route;

import java.text.MessageFormat;

public class RouterLinkTransformer implements PageRenderLinkTransformer {

	private RouterDispatcher routerDispatcher;
	private final Request request;
	private final Response response;
	private final RequestSecurityManager requestSecurityManager;
	private final BaseURLSource baseURLSource;
	private final ContextPathEncoder contextPathEncoder;
	private static final int BUFFER_SIZE = 100;
	private static final char SLASH = '/';

	public RouterLinkTransformer(RouterDispatcher routerDispatcher, Request request, Response response, RequestSecurityManager requestSecurityManager, BaseURLSource baseURLSource, ContextPathEncoder contextPathEncoder) {
		this.routerDispatcher = routerDispatcher;
		this.request = request;
		this.response = response;
		this.requestSecurityManager = requestSecurityManager;
		this.baseURLSource = baseURLSource;
		this.contextPathEncoder = contextPathEncoder;
	}

	public PageRenderRequestParameters decodePageRenderRequest(Request request) {
		return null;
	}

	public Link transformPageRenderLink(Link defaultLink, PageRenderRequestParameters parameters) {

		String activePageName = parameters.getLogicalPageName();

		Route route = routerDispatcher.getRouteMap().get(activePageName);

		if (route != null) {
			StringBuilder builder = new StringBuilder(BUFFER_SIZE);

			if (!"".equals(request.getContextPath())) {
				// Build up the absolute URI.
				builder.append(request.getContextPath());
				builder.append(SLASH);
			}

			builder.append(MessageFormat.format(route.getPathExpression(), parameters.getActivationContext().toStrings()));

			Link link = new LinkImpl(builder.toString(), false, requestSecurityManager.checkPageSecurity(activePageName), response, contextPathEncoder, baseURLSource);

			if (parameters.isLoopback())
				link.addParameter(TapestryConstants.PAGE_LOOPBACK_PARAMETER_NAME, "t");

			return link;
		}

		return null;
	}
}
