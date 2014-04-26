package com.risertech.swift;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.javaswift.joss.model.Container;
import org.javaswift.joss.model.StoredObject;

public abstract class AbstractSwiftPath implements Path {
	private SwiftFileSystem fileSystem;

	protected AbstractSwiftPath(SwiftFileSystem fileSystem) {
		this.fileSystem = fileSystem;
	}
	
	StoredObject getStoredObject() {
		SwiftUri uri = new SwiftUri(toUri());
		Container container = getFileSystem().getAccount().getContainer(uri.getContainer());
		return container.getObject(uri.getPath());
	}
	
	Container getContainer() {
		SwiftUri uri = new SwiftUri(toUri());
		return getFileSystem().getAccount().getContainer(uri.getContainer());
	}
	
	protected void delete() throws IOException {
		SwiftUri uri = new SwiftUri(toUri());
		
		try {
			if (uri.hasPath()) {
				getStoredObject().delete();
			} else {
				getContainer().delete();
			}
		} catch (Throwable t) {
			throw new IOException("File does not exist", t);
		}
	}
	
	protected boolean exists() {
		return getStoredObject().exists();
	}

	@Override
	public SwiftFileSystem getFileSystem() {
		return fileSystem;
	}

	@Override
	public boolean isAbsolute() {
		return true;
	}

	@Override
	public AbstractSwiftPath getRoot() {
		return null;
	}

	@Override
	public Path getFileName() {
		return getName(getNameCount() - 1);
	}
	
	@Override
	public Path getParent() {
		return subpath(0, getNameCount() - 1);
	}
	
	protected abstract String getPath();

	public int getNameCount() {
		String path = getPath();
		int containerCount = 1;
		int count = StringUtils.countMatches(path, ".");
		if (path.startsWith("/")) {
			return count + containerCount;
		} else {
			return count + containerCount + 1;
		}
	}

	public AbstractSwiftPath getName(int index) {
		return subpath(index, index);
	}

	@Override
	public AbstractSwiftPath subpath(int beginIndex, int endIndex) {
		String path = getPath();
		int nameCount = getNameCount();
		
		if (beginIndex < 0 || beginIndex > endIndex) {
			throw new IllegalArgumentException("beginIndex " + beginIndex + "is out of bounds.");
		}
		
		if (endIndex < beginIndex || endIndex >= nameCount) {
			throw new IllegalArgumentException("endIndex " + endIndex + "is out of bounds.");
		}
		
		if (beginIndex == endIndex && beginIndex == 0) {
			return getRoot();
		} else {
			// TODO Test and Fix the boundaries
			List<String> paths = Arrays.asList(StringUtils.split(path, '/')).subList(beginIndex, endIndex);
			return new SwiftRelativePath(getFileSystem(), StringUtils.join(paths, '/'));
		}
	}

	@Override
	public boolean startsWith(Path other) {
		if (other instanceof AbstractSwiftPath) {
			return startsWith(((AbstractSwiftPath) other).getPath());
		}
		
		return false;
	}

	@Override
	public boolean startsWith(String other) {
		String[] otherPath = StringUtils.split(other, '/');
		
		if (otherPath.length > getNameCount()) {
			return false;
		}
		
		for (int index = 0; index < otherPath.length; index++) {
			if (getName(index).getPath() != otherPath[index]) {
				return false;
			}
		}
		
		return true;
	}

	@Override
	public boolean endsWith(Path other) {
		if (other instanceof AbstractSwiftPath) {
			return endsWith(((AbstractSwiftPath) other).getPath());
		}
		
		return false;
	}

	@Override
	public boolean endsWith(String other) {
		String[] otherPath = StringUtils.split(other, '/');
		
		if (otherPath.length > getNameCount()) {
			return false;
		}
		
		int lastIndex = getNameCount() - 1;
		int lastOtherIndex = otherPath.length - 1;
		
		for (int index = 0; index < otherPath.length; index++) {
			if (getName(lastIndex - index).getPath() != otherPath[lastOtherIndex - index]) {
				return false;
			}
		}
		
		return false;
	}

	@Override
	public AbstractSwiftPath normalize() {
		// TODO Support /../path notation
		return this;
	}

	@Override
	public AbstractSwiftPath resolve(Path other) {
		if (other instanceof AbstractSwiftPath) {
			return resolve(((AbstractSwiftPath) other).getPath());
		}
		
		return this;
	}

	@Override
	public AbstractSwiftPath resolve(String other) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractSwiftPath resolveSibling(Path other) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractSwiftPath resolveSibling(String other) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractSwiftPath relativize(Path other) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URI toUri() {
		if (!isAbsolute()) throw new IllegalStateException("Non absolute paths cannot be resolved to URIs");
		
		try {
			return new URI(getFileSystem().getUri().toString() + normalize().getPath());
		} catch (URISyntaxException e) {
			throw new IOError(e);
		}
	}

	@Override
	public AbstractSwiftPath toAbsolutePath() {
		throw new UnsupportedOperationException("There is no default container to resolve this against.");
	}

	@Override
	public Path toRealPath(LinkOption... options) throws IOException {
		return toAbsolutePath();
	}

	@Override
	public File toFile() {
		throw new UnsupportedOperationException();
	}

	@Override
	public WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... modifiers) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public WatchKey register(WatchService watcher, Kind<?>... events) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<Path> iterator() {
		List<Path> paths = new ArrayList<Path>();
		for (int x = 0; x < getNameCount(); x++) {
			paths.add(subpath(x, x));
		}
		return paths.iterator();
	}

	@Override
	public int compareTo(Path other) {
		if (other instanceof AbstractSwiftPath) {
			return getPath().compareTo(((AbstractSwiftPath) other).getPath());
		}
		
		throw new ClassCastException();
	}
}
