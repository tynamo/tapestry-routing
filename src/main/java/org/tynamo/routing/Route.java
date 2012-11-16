package org.tynamo.routing;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.internal.EmptyEventContext;
import org.apache.tapestry5.internal.URLEventContext;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.ContextValueEncoder;
import org.apache.tapestry5.services.LocalizationSetter;
import org.apache.tapestry5.services.PageRenderRequestParameters;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.URLEncoder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Route {

	private static final char SLASH = '/';

	private static final String URI_PARAM_NAME_REGEX = "\\w[\\w\\.-]*";
	private static final String URI_PARAM_REGEX_REGEX = "[^{}][^{}]*";
	private static final String URI_PARAM_REGEX = "\\{\\s*(" + URI_PARAM_NAME_REGEX + ")\\s*(:\\s*(" + URI_PARAM_REGEX_REGEX + "))?\\}";
	private static final Pattern URI_PARAM_PATTERN = Pattern.compile(URI_PARAM_REGEX);

	private final String canonicalizedPageName;
	private final String pathExpression;
	private final Pattern pattern;
	private final LocalizationSetter localizationSetter;
	private final boolean encodeLocaleIntoPath;
	private final String applicationFolder;

	public Route(final String pathExpression,
	             final String canonicalizedPageName,
	             final LocalizationSetter localizationSetter,
	             @Symbol(SymbolConstants.ENCODE_LOCALE_INTO_PATH) boolean encodeLocaleIntoPath,
	             @Symbol(SymbolConstants.APPLICATION_FOLDER) final String applicationFolder) {

		this.localizationSetter = localizationSetter;
		this.canonicalizedPageName = canonicalizedPageName;
		this.encodeLocaleIntoPath = encodeLocaleIntoPath;

		this.applicationFolder = applicationFolder.equals("") ? "" : SLASH + applicationFolder;

		// remove ending slash unless it's the root path
		this.pathExpression = pathExpression.length() > 1 && pathExpression.charAt(pathExpression.length() - 1) == SLASH ? pathExpression.substring(0, pathExpression.length() - 1) : pathExpression;

		if (!(this.pathExpression.charAt(0) == SLASH)) {
			throw new RuntimeException(
					"ERROR: Expression: \"" + this.pathExpression + "\" in: \"" + canonicalizedPageName +
							"\" page should start with a \"/\"");
		}

		String regex = buildExpression(this.pathExpression);
		pattern = Pattern.compile(regex);

	}

	static String buildExpression(String expression) {

		String[] split = URI_PARAM_PATTERN.split(expression);
		Matcher withPathParam = URI_PARAM_PATTERN.matcher(expression);
		int i = 0;
		StringBuilder builder = new StringBuilder();
		if (i < split.length) builder.append(Pattern.quote(split[i++]));

		while (withPathParam.find()) {
			String expr = withPathParam.group(3);
			builder.append("(");
			if (expr == null) {
				builder.append("[^/]+");
			} else {
				throw new RuntimeException("regular expression mappings are not yet supported");
			}

			builder.append(")");
			if (i < split.length) builder.append(Pattern.quote(split[i++]));
		}

		return builder.toString();
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

	public String removeAppFolderAndLocaleFromPath(final Request request) {

		String path = request.getPath();

		if (this.applicationFolder.length() > 0)
		{
			path = path.substring(applicationFolder.length());
		}

		String locale = getLocaleFromPath(path);

		if (locale != null) {
			localizationSetter.setLocaleFromLocaleName(locale);
			path = path.substring(locale.length() + 1);
		}
		return path.length() > 1 && path.charAt(path.length() - 1) == '/' ? path.substring(0, path.length() - 1) : path;
	}

	public PageRenderRequestParameters decodePageRenderRequest(final Request request,
	                                                           final URLEncoder urlEncoder,
	                                                           final ContextValueEncoder valueEncoder) {

		// remove ending slash unless it's the root path
		Matcher matcher = pattern.matcher(removeAppFolderAndLocaleFromPath(request));
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

		return new PageRenderRequestParameters(canonicalizedPageName, context, false);

	}

	public String getPathExpression() {
		return pathExpression;
	}

	public String getCanonicalizedPageName() {
		return canonicalizedPageName;
	}
}
