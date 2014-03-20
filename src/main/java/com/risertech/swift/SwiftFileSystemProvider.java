package com.risertech.swift;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.javaswift.joss.model.StoredObject;

/**
 * Represents a Swift ring
 * @author phil
 */
public class SwiftFileSystemProvider extends FileSystemProvider {
	@Override
	public String getScheme() {
		return "swift";
	}

	@Override
	public FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FileSystem getFileSystem(URI uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Path getPath(URI uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DirectoryStream<Path> newDirectoryStream(Path dir, Filter<? super Path> filter) throws IOException {
		if (dir instanceof SwiftContainerPath) {
			return new ContainerStream((SwiftContainerPath) dir, filter);
		} else {
			throw new IOException("Unsupported path type");
		}
	}

	@Override
	public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
		// This is a no-op since directories are virtual
	}

	@Override
	public void delete(Path path) throws IOException {
		if (path instanceof SwiftContainerPath) {
			((SwiftContainerPath) path).getContainer().delete();
		} else if (path instanceof SwiftFilePath) {
			((SwiftFilePath) path).getStoredObject().delete();
		} else if (path instanceof SwiftDirectoryPath) {
			// TODO Delete all paths in the directory
//			((SwiftDirectoryPath) path).getContainer().list(arg0, arg1, arg2)
		} else {
			throw new IOException("Unsupported path type");
		}
	}

	@Override
	public void copy(Path source, Path target, CopyOption... options) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void move(Path source, Path target, CopyOption... options) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isSameFile(Path path, Path path2) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isHidden(Path path) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public FileStore getFileStore(Path path) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void checkAccess(Path path, AccessMode... modes) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
		// TODO Auto-generated method stub
		
	}
	
	public class ContainerStream implements DirectoryStream<Path> {
		private SwiftContainerPath path;
		private Filter<? super Path> filter;

		public ContainerStream(SwiftContainerPath path, Filter<? super Path> filter) {
			this.path = path;
			this.filter = filter;
		}

		@Override
		public void close() throws IOException {
		}

		@Override
		public Iterator<Path> iterator() {
			// TODO Handle pagination if there are more than 9999
			//		container.getCount();
			Collection<StoredObject> storedObjects = path.getContainer().list();
			
			ArrayList<Path> paths = new ArrayList<Path>();
			for (StoredObject storedObject: storedObjects) {
				Path potentialPath = new SwiftFilePath(path.getFileSystem(), path.getContainer(), storedObject);
				try {
					if (filter.accept(potentialPath)) {
						paths.add(potentialPath);
					}
				} catch (IOException ioe) {
					throw new RuntimeException(ioe);
				}
			}
			
			return paths.iterator();
		}
		
	}
}
