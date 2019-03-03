package org.tynamo.routing.pages;

import org.tynamo.routing.annotations.At;
import org.tynamo.routing.Behavior;

@At(value = "/not-found-behavior/", behavior = Behavior.NOT_FOUND)
public class NotFoundBehavior {
	protected void onActivate() {}
}