package com.risertech.swift;

import java.nio.file.Path;

import org.javaswift.joss.model.Container;

public class SwiftContainerPath extends AbstractSwiftContainerPath {
	protected SwiftContainerPath(SwiftFileSystem fileSystem, Container container) {
		super(fileSystem, container);
	}
	
	protected String getPath() {
		return "";
	}

	@Override
	public AbstractSwiftPath getRoot() {
		return this;
	}

	@Override
	public Path getParent() {
		return null;
	}

	@Override
	public int getNameCount() {
		return 1;
	}
}
