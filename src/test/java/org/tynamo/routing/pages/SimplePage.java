package org.tynamo.routing.pages;

import org.apache.tapestry5.annotations.Property;
import org.tynamo.routing.annotations.At;

@At("/foo/{0}/bar/{1}")
public class SimplePage {

	@Property
	private String message;

	protected void onActivate(String message0, String message1) throws Exception {
		this.message = message0 + " - " + message1;
	}
}