package org.tynamo.routing.pages.subpackage;

import org.tynamo.routing.annotations.At;

@At(value = "/subpackage/inventedpath", order = "before:subpackage/SubPage1")
public class SubPageFirst {
	protected void onActivate() {}
}