package com.risertech.swift;

public class SwiftPath extends AbstractSwiftPath {
	private final String path;

	protected SwiftPath(SwiftFileSystem fileSystem, String path) {
		super(fileSystem);
		this.path = path;
	}
	
	@Override
	public boolean isAbsolute() {
		return path.startsWith("/");
	}

	@Override
	protected String getPath() {
		return path;
	}
	
	@Override
	public String toString() {
		return path.toString();
	}
}
