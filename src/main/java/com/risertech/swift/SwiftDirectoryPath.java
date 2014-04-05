package com.risertech.swift;

import org.javaswift.joss.model.Container;

public class SwiftDirectoryPath extends AbstractSwiftContainerPath {
	private String path;

	public SwiftDirectoryPath(SwiftFileSystem fileSystem, Container container, String path) {
		super(fileSystem, container);
		this.path = path;
	}
	
	protected String getPath() {
		return path;
	}
}
