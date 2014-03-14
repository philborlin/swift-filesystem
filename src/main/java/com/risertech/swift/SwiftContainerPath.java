package com.risertech.swift;

import java.nio.file.FileSystem;
import java.nio.file.Path;

import org.javaswift.joss.model.Container;

public class SwiftContainerPath extends AbstractSwiftContainerPath {
	protected SwiftContainerPath(FileSystem fileSystem, Container container) {
		super(fileSystem, container);
	}
	
	protected String getPath() {
		return "";
	}

	@Override
	public Path getRoot() {
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
