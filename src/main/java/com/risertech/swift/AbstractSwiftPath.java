package com.risertech.swift;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
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

public abstract class AbstractSwiftPath implements Path {
	private FileSystem fileSystem;

	protected AbstractSwiftPath(FileSystem fileSystem) {
		this.fileSystem = fileSystem;
	}

	@Override
	public FileSystem getFileSystem() {
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
	public Path normalize() {
		// TODO This does not support non-normalized paths yet
		return this;
	}

	@Override
	public Path resolve(Path other) {
		if (other instanceof AbstractSwiftPath) {
			return resolve(((AbstractSwiftPath) other).getPath());
		}
		
		return this;
	}

	@Override
	public Path resolve(String other) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Path resolveSibling(Path other) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Path resolveSibling(String other) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Path relativize(Path other) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URI toUri() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Path toAbsolutePath() {
		// TODO Auto-generated method stub
		return null;
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
