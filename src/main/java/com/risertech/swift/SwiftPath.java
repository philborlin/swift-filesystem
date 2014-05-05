package com.risertech.swift;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.javaswift.joss.model.Container;
import org.javaswift.joss.model.StoredObject;

public class SwiftPath extends AbstractSwiftPath {
	private static final Integer MAX_PAGE_SIZE = 9999;
	
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
		return path;
	}
	
	@Override
	public AbstractSwiftPath resolve(String other) {
		return new SwiftPath(getFileSystem(), getPath() + (getPath().endsWith("/") ? "" : "/") + other);
	}
	
	DirectoryStream<Path> list(Filter<? super Path> filter) {
		SwiftUri uri = getSwiftUri();
		return new StoredObjectsStream(getContainer(uri), uri.getPath(), filter);
	}
	
	@Override
	protected void delete() throws IOException {
		if (path.endsWith("/")) {
			DirectoryStream<Path> directoryStream = list(new Filter<Path>() {
				public boolean accept(Path entry) throws IOException {
					return true;
				}
			});
			for (Path path : directoryStream) {
				try {
					((SwiftPath)path).delete();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		} else {
			super.delete();
		}
	}
	
	@Override
	public AbstractSwiftPath getRoot() {
		return new SwiftPath(getFileSystem(), "/");
	}
	
	public class StoredObjectsStream implements DirectoryStream<Path> {
		private Container container;
		private Collection<StoredObject> storedObjects;
		private String pathWithoutContainer;
		private Filter<? super Path> filter;

		public StoredObjectsStream(Container container, String pathWithoutContainer, Filter<? super Path> filter) {
			this.container = container;
			this.storedObjects = container.list(pathWithoutContainer, null, MAX_PAGE_SIZE);
			this.pathWithoutContainer = pathWithoutContainer;
			this.filter = filter;
		}

		@Override
		public void close() throws IOException {
		}

		@Override
		public Iterator<Path> iterator() {
			Pattern pattern = Pattern.compile("(.*/).*");
			
			// TODO Handle pagination if there are more than 9999
			
			Set<Path> paths = new HashSet<Path>();
			for (StoredObject storedObject: storedObjects) {
				String containerName = container != null ? "/" + container.getName() : "";
				SwiftPath potentialPath = new SwiftPath(getFileSystem(), containerName + storedObject.getName());
				if (!potentialPath.equals(SwiftPath.this)) {
					try {
						if (filter == null || filter.accept(potentialPath)) {
							String prefix = containerName + pathWithoutContainer;
							String restOfPath = potentialPath.path.substring(prefix.length());
							if (!restOfPath.contains("/")) {
								paths.add(potentialPath);
							}
							Matcher matcher = pattern.matcher(restOfPath);
							if (matcher.matches()) {
								paths.add(new SwiftPath(getFileSystem(), prefix + matcher.group(1)));
							}
						}
					} catch (IOException ioe) {
						throw new RuntimeException(ioe);
					}
				}
			}
			
			return paths.iterator();
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SwiftPath other = (SwiftPath) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}
}
