package com.risertech.swift;


public class SwiftRelativePath extends AbstractSwiftPath {
	private String path;

	protected SwiftRelativePath(SwiftFileSystem fileSystem, String path) {
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
