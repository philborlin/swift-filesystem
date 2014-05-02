package com.risertech.swift;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

public class SwiftFileAttributes implements BasicFileAttributes {
	private SwiftPath path;

	public SwiftFileAttributes(SwiftPath path) {
		this.path = path;
	}

	@Override
	public FileTime lastModifiedTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FileTime lastAccessTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FileTime creationTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isRegularFile() {
		return path.isRegularFile();
	}

	@Override
	public boolean isDirectory() {
		return path.isDirectory();
	}

	@Override
	public boolean isSymbolicLink() {
		return false;
	}

	@Override
	public boolean isOther() {
		return false;
	}

	@Override
	public long size() {
		if (isRegularFile()) return path.getStoredObject().getContentLength();

		return 0;
	}

	@Override
	public Object fileKey() {
		return null;
	}
}
