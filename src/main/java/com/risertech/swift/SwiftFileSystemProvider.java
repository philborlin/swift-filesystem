package com.risertech.swift;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileStore;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.javaswift.joss.client.factory.AccountFactory;
import org.javaswift.joss.model.Container;
import org.javaswift.joss.model.StoredObject;

import com.risertech.swift.SwiftEnvironmentBuilder.AuthenticationMethod;

/**
 * Represents a Swift ring
 * @author phil
 */
public class SwiftFileSystemProvider extends FileSystemProvider {
	@SuppressWarnings("unused")
	private static ServiceLoader<FileSystemProvider> fileSystemProviderLoader = ServiceLoader.load(FileSystemProvider.class);
	
	static final String PASSWORD = "password";
	static final String AUTH_URL = "authUrl";
	static final String SOCKET_TIMEOUT = "socketTimeout";
	static final String PREFERRED_REGION = "preferredRegion";
	static final String HASH_PASSWORD = "hashPassword";
	static final String AUTHENTICATION_METHOD = "authenticationMethod";
	static final String MOCK = "mock";
	
	private Map<URI, SwiftFileSystem> cache = new HashMap<URI, SwiftFileSystem>();
	
	void flushCache() {
		cache.clear();
	}
	
	@Override
	public String getScheme() {
		return "swift";
	}

	/**
	 * A normal swift url looks like: http://server:port/v1/account/container/object
	 * In this case we substitute the http for swift to make it swift://username@server:port/v1/account/container/object
	 * 
	 * The username is put in the userinfo portion of the URI so that each user gets their own FileSystem instance.
	 * Since FileSystem caching is done by URI the username must be in the URI. DO NOT put the password in the URI.
	 * Not only is it assumed the entire userinfo portion is the username but it would be a serious security risk
	 * to put the password in userinfo since the URI may be cached for the life of the JRE leaving the password in
	 * memory for an extended period of time. Use the env map to pass in either the password or the hashed password.
	 * 
	 * For creating a new file system we are only interested in the version and the account parts of the path.
	 * The rest of the path will be ignored.
	 */
	@Override
	public SwiftFileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
		if (cache.containsKey(uri)) throw new FileSystemAlreadyExistsException();
		
		SwiftUri swiftUri = new SwiftUri(uri);
		if (!swiftUri.hasVersion() || !swiftUri.hasTenant()) {
			throw new IllegalArgumentException("URI path needs both a version and a tenant");
		}
		if (swiftUri.hasContainer()) {
			throw new IllegalArgumentException("URI path should only have a version and tenant.");
		}
		if (!swiftUri.getVersion().equals("v1")) throw new IllegalArgumentException("This provider only supports version 1");
		
		AccountFactory accountFactory = new AccountFactory();
		accountFactory.setTenantName(swiftUri.getTenant());
		accountFactory.setUsername(uri.getUserInfo());
		accountFactory.setPassword((String) env.get(PASSWORD));
		accountFactory.setAuthUrl((String) env.get(AUTH_URL));
		accountFactory.setSocketTimeout((Integer) env.get(SOCKET_TIMEOUT));
		accountFactory.setPreferredRegion((String) env.get(PREFERRED_REGION));
		accountFactory.setHashPassword((String) env.get(HASH_PASSWORD));
		accountFactory.setAuthenticationMethod(convert((AuthenticationMethod) env.get(AUTHENTICATION_METHOD)));
		accountFactory.setMock((Boolean) env.get(MOCK));
		
		SwiftFileSystem fileSystem = new SwiftFileSystem(this, uri, accountFactory.createAccount());
		cache.put(uri, fileSystem);
		
		return fileSystem;
	}
	
	private org.javaswift.joss.client.factory.AuthenticationMethod convert(AuthenticationMethod authenticationMethod) {
		if (authenticationMethod == AuthenticationMethod.BASIC) {
			return org.javaswift.joss.client.factory.AuthenticationMethod.BASIC;
		} else if (authenticationMethod == AuthenticationMethod.KEYSTONE) {
			return org.javaswift.joss.client.factory.AuthenticationMethod.KEYSTONE;
		} else if (authenticationMethod == AuthenticationMethod.TEMPAUTH) {
			return org.javaswift.joss.client.factory.AuthenticationMethod.TEMPAUTH;
		} else {
			return org.javaswift.joss.client.factory.AuthenticationMethod.BASIC;
		}
	}

	@Override
	public SwiftFileSystem getFileSystem(URI uri) {
		return cache.get(uri);
	}

	/**
	 * 
	 */
	@Override
	public AbstractSwiftPath getPath(URI uri) {
		SwiftUri swiftUri = new SwiftUri(uri);
		if (!swiftUri.hasContainer()) {
			throw new IllegalArgumentException("URI path needs at least a version, account, and container");
		}
		
		SwiftFileSystem fileSystem = cache.get(swiftUri.getFileSystemUri());
		return fileSystem.getPath("/" + swiftUri.getContainer(), swiftUri.getPath());
	}

	@Override
	public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs)
			throws IOException {
		
		if (!path.isAbsolute()) throw new IllegalStateException("Cannot create a byte channel on a non absolute path");
		
		return new SwiftSeekableByteChannel((AbstractSwiftPath) path);
	}

	@Override
	public DirectoryStream<Path> newDirectoryStream(Path dir, Filter<? super Path> filter) throws IOException {
		if (dir instanceof SwiftContainerPath) {
			return new ContainerStream((SwiftContainerPath) dir, filter);
		} else if (dir instanceof SwiftDirectoryPath) {
			// TODO Handle this
			return null;
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
		// TODO Use Swift copying if they are in the same container.
		// TODO What about copying directories?
		
		ReadableByteChannel sourceChannel = Files.newByteChannel(source);
		WritableByteChannel targetChannel = Files.newByteChannel(target);
		
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		int readSize = 0;
		while ((readSize = sourceChannel.read(buffer)) > 0) {
			buffer.flip();
			if (buffer.capacity() > readSize) {
				targetChannel.write(ByteBuffer.wrap(buffer.array(), 0, readSize));
			} else {
				targetChannel.write(buffer);
			}
			buffer.clear();
		}
		
		sourceChannel.close();
		targetChannel.close();
	}

	@Override
	public void move(Path source, Path target, CopyOption... options) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isSameFile(Path path, Path path2) throws IOException {
		return ((AbstractSwiftPath) path).getPath().equals(((AbstractSwiftPath) path2).getPath());
	}

	@Override
	public boolean isHidden(Path path) throws IOException {
		return false;
	}

	@Override
	public FileStore getFileStore(Path path) throws IOException {
		return ((AbstractSwiftPath) path).getFileSystem().getFileStore();
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
	
	class SwiftSeekableByteChannel implements SeekableByteChannel {
		private AbstractSwiftPath path;
		private StoredObject object;
		private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		private ReadableByteChannel readableChannel;
		private final WritableByteChannel writableChannel;
		private boolean open = false;
		private long position = 0;

		public SwiftSeekableByteChannel(AbstractSwiftPath path) {
			this.path = path;
			this.object = path.getStoredObject();
			writableChannel = Channels.newChannel(byteArrayOutputStream);
		}
		
		@Override
		public boolean isOpen() {
			synchronized (this) {
				return open;
			}
		}

		@Override
		public void close() throws IOException {
			synchronized (this) {
				open = false;
			}
			
			try { readableChannel.close(); } catch (Exception e) {}
			try { writableChannel.close(); } catch (Exception e) {}
			Container container = path.getContainer();
			if (!container.exists()) container.create();
			object.uploadObject(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
		}

		@Override
		public int read(ByteBuffer dst) throws IOException {
			synchronized (this) {
				if (readableChannel == null) readableChannel = Channels.newChannel(object.downloadObjectAsInputStream());
			}
			
			int readSize = readableChannel.read(dst);
			position = position + readSize;
			return readSize;
		}

		@Override
		public int write(ByteBuffer src) throws IOException {
			return writableChannel.write(src);
		}

		@Override
		public long position() throws IOException {
			return position;
		}

		@Override
		public SeekableByteChannel position(long newPosition) throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		public long size() throws IOException {
			return object.getContentLength();
		}

		@Override
		public SeekableByteChannel truncate(long size) throws IOException {
			throw new UnsupportedOperationException();
		}
	}
}
