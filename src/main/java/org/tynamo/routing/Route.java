package org.tynamo.routing;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Route {

	public static final char SLASH = '/';

	private static final String URI_PARAM_NAME_REGEX = "\\w[\\w\\.-]*";
	private static final String URI_PARAM_REGEX_REGEX = "[^{}][^{}]*";
	private static final String URI_PARAM_REGEX = "\\{\\s*(" + URI_PARAM_NAME_REGEX + ")\\s*(:\\s*(" + URI_PARAM_REGEX_REGEX + "))?\\}";
	private static final Pattern URI_PARAM_PATTERN = Pattern.compile(URI_PARAM_REGEX);

	private final String canonicalizedPageName;
	private final String pathExpression;
	private final Pattern pattern;

	public Route(final String pathExpression, final String canonicalizedPageName) {

		this.canonicalizedPageName = canonicalizedPageName;

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

	public String getPathExpression() {
		return pathExpression;
	}

	public String getCanonicalizedPageName() {
		return canonicalizedPageName;
	}

	public Pattern getPattern() {
		return pattern;
	}
}
