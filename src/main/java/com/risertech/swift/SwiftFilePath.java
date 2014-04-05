package com.risertech.swift;

import org.javaswift.joss.model.Container;
import org.javaswift.joss.model.StoredObject;

public class SwiftFilePath extends AbstractSwiftContainerPath {
	private StoredObject storedObject;

	public SwiftFilePath(SwiftFileSystem fileSystem, Container container, StoredObject storedObject) {
		super(fileSystem, container);
		this.storedObject = storedObject;
	}
	
	protected String getPath() {
		return storedObject.getName();
	}
	
	protected StoredObject getStoredObject() {
		return storedObject;
	}
}
