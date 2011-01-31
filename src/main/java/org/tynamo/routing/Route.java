package org.tynamo.routing;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.internal.EmptyEventContext;
import org.apache.tapestry5.internal.URLEventContext;
import org.apache.tapestry5.services.ContextValueEncoder;
import org.apache.tapestry5.services.PageRenderRequestParameters;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.URLEncoder;
import org.tynamo.routing.annotations.At;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Route {

	private static final String URI_PARAM_NAME_REGEX = "\\w[\\w\\.-]*";
	private static final String URI_PARAM_REGEX_REGEX = "[^{}][^{}]*";
		private static final String URI_PARAM_REGEX = "\\{\\s*(" + URI_PARAM_NAME_REGEX + ")\\s*(:\\s*(" + URI_PARAM_REGEX_REGEX + "))?\\}";
	private static final Pattern URI_PARAM_PATTERN = Pattern.compile(URI_PARAM_REGEX);

	private Class pageClass;
	private String pathExpression;
	private String regex;
	private Pattern pattern;

	public Route(Class pageClass) {
		this.pageClass = pageClass;

		At ann = (At) pageClass.getAnnotation(At.class);
		if (ann != null) {
			this.pathExpression = ann.value();

			if (!this.pathExpression.startsWith("/")) {
				throw new RuntimeException(
						"ERROR: Expression: \"" + this.pathExpression + "\" in: \"" + this.pageClass.getSimpleName() +
						"\" page should start with a \"/\"");
			}

			regex = buildExpression(this.pathExpression);
			pattern = Pattern.compile(regex);
		} else {
			throw new RuntimeException("Something went wrong!. Where is the @At annotation?");
		}

	}

	static String buildExpression(String expression) {

		String[] split = URI_PARAM_PATTERN.split(expression);
		Matcher withPathParam = URI_PARAM_PATTERN.matcher(expression);
		int i = 0;
		StringBuffer buffer = new StringBuffer();
		if (i < split.length) buffer.append(Pattern.quote(split[i++]));

		while (withPathParam.find()) {
			String expr = withPathParam.group(3);
			buffer.append("(");
			if (expr == null) {
				buffer.append("[^/]+");
			} else {
				throw new RuntimeException("regular expression mappings are not yet supported");
			}

			buffer.append(")");
			if (i < split.length) buffer.append(Pattern.quote(split[i++]));
		}

		return buffer.toString();
	}

	public PageRenderRequestParameters decodePageRenderRequest(Request request, URLEncoder urlEncoder, ContextValueEncoder valueEncoder) {

		Matcher matcher = pattern.matcher(request.getPath());
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

		return new PageRenderRequestParameters(pageClass.getSimpleName(), context, false);

	}

	public String getPathExpression() {
		return pathExpression;
	}
}
