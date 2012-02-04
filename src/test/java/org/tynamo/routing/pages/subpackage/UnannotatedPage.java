package org.tynamo.routing.pages.subpackage;

import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.annotations.OnEvent;

public class UnannotatedPage {

	@OnEvent(EventConstants.ACTIVATE)
	void activate(String parameter) {}

}
