package org.tynamo.routing.pages;

import org.tynamo.routing.annotations.At;

@At("/home")
public class Inaccessible {

	void onActivate() {}

}
