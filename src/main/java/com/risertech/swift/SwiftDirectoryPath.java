package com.risertech.swift;

import java.nio.file.FileSystem;

import org.javaswift.joss.model.Container;

public class SwiftDirectoryPath extends AbstractSwiftContainerPath {
	private String path;

	public SwiftDirectoryPath(FileSystem fileSystem, Container container, String path) {
		super(fileSystem, container);
		this.path = path;
	}
	
	protected String getPath() {
		return path;
	}
}
