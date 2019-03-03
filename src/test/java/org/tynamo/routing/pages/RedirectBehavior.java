package org.tynamo.routing.pages;

import org.tynamo.routing.annotations.At;
import org.tynamo.routing.Behavior;

@At(value = "/redirect-behavior", behavior = Behavior.REDIRECT)
public class RedirectBehavior {
	protected void onActivate() {}
}