package org.tynamo.routing.services;

import java.util.Collection;

public class AnnotatedPagesManagerImpl implements AnnotatedPagesManager {

	Collection<Class> pages;

	public AnnotatedPagesManagerImpl(Collection<Class> pages) {
		this.pages = pages;
	}

	public Collection<Class> getPages() {
		return pages;
	}

}
