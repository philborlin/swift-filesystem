package com.risertech.swift;

import java.io.IOError;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;

import org.javaswift.joss.model.Container;

public abstract class AbstractSwiftContainerPath extends AbstractSwiftPath {
	private Container container;

	protected AbstractSwiftContainerPath(SwiftFileSystem fileSystem, Container container) {
		super(fileSystem);
		this.container = container;
	}
	
	protected Container getContainer() {
		return container;
	}
	
	@Override
	public AbstractSwiftPath getRoot() {
		return new SwiftContainerPath(getFileSystem(), getContainer());
	}
	
	@Override
	public Path getParent() {
		String pathName = getPath();
		
		int lastIndex = pathName.lastIndexOf('/');
		if (lastIndex > -1) {
			return new SwiftDirectoryPath(getFileSystem(), getContainer(), pathName.substring(0, lastIndex));
		} else {
			return getRoot();			
		}
	}
	
	@Override
	public URI toUri() {
		try {
			// TODO How do we pass credentials?
			return new URI("swift://host:port/" + container.getName() + "/" + getPath());
		} catch (URISyntaxException exception) {
			throw new IOError(exception);
		}
	}
}
