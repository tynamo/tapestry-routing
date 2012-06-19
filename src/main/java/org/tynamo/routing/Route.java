package org.tynamo.routing;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.internal.EmptyEventContext;
import org.apache.tapestry5.internal.URLEventContext;
import org.apache.tapestry5.services.ContextValueEncoder;
import org.apache.tapestry5.services.PageRenderRequestParameters;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.URLEncoder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Route {

	private static final String URI_PARAM_NAME_REGEX = "\\w[\\w\\.-]*";
	private static final String URI_PARAM_REGEX_REGEX = "[^{}][^{}]*";
	private static final String URI_PARAM_REGEX = "\\{\\s*(" + URI_PARAM_NAME_REGEX + ")\\s*(:\\s*(" + URI_PARAM_REGEX_REGEX + "))?\\}";
	private static final Pattern URI_PARAM_PATTERN = Pattern.compile(URI_PARAM_REGEX);

	private String canonicalizedPageName;
	private String pathExpression;
	private Pattern pattern;

	public Route(String pathExpression, String canonicalizedPageName) {

		this.canonicalizedPageName = canonicalizedPageName;

		// remove ending slash unless it's the root path
		this.pathExpression = pathExpression.length() > 1 && pathExpression.charAt(pathExpression.length()-1) == '/' ? pathExpression.substring(0, pathExpression.length()-1) : pathExpression;
		
		if (!this.pathExpression.startsWith("/")) {
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

	public PageRenderRequestParameters decodePageRenderRequest(final Request request,
	                                                           final URLEncoder urlEncoder,
	                                                           final ContextValueEncoder valueEncoder) {

		// remove ending slash unless it's the root path
		Matcher matcher = pattern.matcher(request.getPath().length() > 1 && request.getPath().charAt(request.getPath().length()-1) == '/' ? request.getPath().substring(0, request.getPath().length()-1) : request.getPath());
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
