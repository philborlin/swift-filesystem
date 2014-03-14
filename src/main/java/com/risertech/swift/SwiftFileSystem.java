package com.risertech.swift;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.javaswift.joss.model.Account;
import org.javaswift.joss.model.Container;

/**
 * Represents an account on a Swift server. The containers
 * are the root directories
 * @author phil
 */
public class SwiftFileSystem extends FileSystem {
	private final FileSystemProvider fileSystemProvider;
	private final Account account;
	private final SwiftFileStore fileStore;
	private Boolean closed = false;

	protected SwiftFileSystem(FileSystemProvider fileSystemProvider, Account account) {
		this.fileSystemProvider = fileSystemProvider;
		this.account = account;
		fileStore = new SwiftFileStore(account);
	}
	
	@Override
	public FileSystemProvider provider() {
		return fileSystemProvider;
	}

	@Override
	public void close() throws IOException {
		closed = true;
	}

	@Override
	public boolean isOpen() {
		return closed;
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public String getSeparator() {
		return "/";
	}

	@Override
	public Iterable<Path> getRootDirectories() {
		Collection<Container> containers = account.list();
		
		ArrayList<Path> paths = new ArrayList<Path>();
		for (Container container: containers) {
			paths.add(new SwiftContainerPath(this, container));
		}
		
		return paths;
	}

	@Override
	public Iterable<FileStore> getFileStores() {
		ArrayList<FileStore> fileStores = new ArrayList<FileStore>();
		fileStores.add(fileStore);
		return fileStores;
	}

	@Override
	public Set<String> supportedFileAttributeViews() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Path getPath(String first, String... more) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PathMatcher getPathMatcher(String syntaxAndPattern) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserPrincipalLookupService getUserPrincipalLookupService() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WatchService newWatchService() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
}
