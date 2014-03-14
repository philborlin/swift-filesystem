package com.risertech.swift;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;

import org.javaswift.joss.model.Account;

/**
 * @author phil
 */
public class SwiftFileStore extends FileStore {
	private Account account;

	protected SwiftFileStore(Account account) {
		this.account = account;
	}
	
	@Override
	public String name() {
		return account.toString();
	}

	@Override
	public String type() {
		return "Swift";
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public long getTotalSpace() throws IOException {
		return -1;
	}

	@Override
	public long getUsableSpace() throws IOException {
		return -1;
	}

	@Override
	public long getUnallocatedSpace() throws IOException {
		return getUsableSpace();
	}

	@Override
	public boolean supportsFileAttributeView(Class<? extends FileAttributeView> type) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean supportsFileAttributeView(String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public <V extends FileStoreAttributeView> V getFileStoreAttributeView(Class<V> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getAttribute(String attribute) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
