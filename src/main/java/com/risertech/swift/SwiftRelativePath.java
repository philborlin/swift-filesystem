package com.risertech.swift;

import java.nio.file.FileSystem;

public class SwiftRelativePath extends AbstractSwiftPath {
	private String path;

	protected SwiftRelativePath(FileSystem fileSystem, String path) {
		super(fileSystem);
		this.path = path;
	}
	
	@Override
	public boolean isAbsolute() {
		return false;
	}

	@Override
	protected String getPath() {
		return path;
	}
}
