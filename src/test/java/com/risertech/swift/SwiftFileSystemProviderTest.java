package com.risertech.swift;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.javaswift.joss.client.factory.AccountFactory;
import org.javaswift.joss.model.Account;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SwiftFileSystemProviderTest {
	String basicFileySystemUriString = "swift://localhost/v1/tentant";
	byte[] expectedBytes = new byte[] { 1, 2, 3, 4 };
	Map<String, Object> env;
	Account account;
	SwiftFileSystemProvider provider;
	SwiftFileSystem basicFileSystem;
	
	@Before
	public void setup() throws IOException, URISyntaxException {
		// TODO Test the SwiftEnvironmentBuilder
		account = new AccountFactory().setMock(true).createAccount();
		env = new SwiftEnvironmentBuilder().build();
		env.put(SwiftFileSystemProvider.ACCOUNT, account);
		provider = new SwiftFileSystemProvider();
		basicFileSystem = provider.newFileSystem(new URI(basicFileySystemUriString), env);
	}
	
	@After
	public void tearDown() {
		provider.flushCache();
	}
	
	@Test
	public void newFileSystemByURI() throws IOException, URISyntaxException {
		Assert.assertTrue(basicFileSystem instanceof SwiftFileSystem);
	}
	
	@Test
	public void getFileSystemByURI() throws IOException, URISyntaxException {
		Assert.assertEquals(basicFileSystem, provider.getFileSystem(new URI(basicFileySystemUriString)));
	}
	
	@Test
	public void getPathByUri() throws URISyntaxException {
		String pathString = "/container/test1";
		AbstractSwiftPath path = provider.getPath(new URI(basicFileySystemUriString + pathString));
		Assert.assertEquals(pathString, path.getPath());
	}
	
	@Test
	public void getPathFromFileSystem() {
		AbstractSwiftPath path = basicFileSystem.getPath("/container", "test1");
		Assert.assertEquals("/container/test1", path.getPath());
	}
	
	private AbstractSwiftPath createPathWithDefaultContent(String first, String... more) throws IOException {
		AbstractSwiftPath path = basicFileSystem.getPath(first, more);
		
		WritableByteChannel channel = Files.newByteChannel(path);
		channel.write(ByteBuffer.wrap(expectedBytes));
		channel.close();
		
		return path;
	}
	
	private byte[] getBytesFromPath(AbstractSwiftPath path) throws IOException {
		try (ReadableByteChannel channel = Files.newByteChannel(path)) {
			return IOUtils.read(channel);
		}
	}
	
	@Test
	public void saveFileWhenContainerDoesNotExist() throws IOException {
		AbstractSwiftPath path = createPathWithDefaultContent("/container", "test1");
		Assert.assertArrayEquals(expectedBytes, getBytesFromPath(path));
	}
	
	@Test
	public void copyFileWithNoOptions() throws IOException {
		AbstractSwiftPath source = createPathWithDefaultContent("/container", "test1");
		AbstractSwiftPath target = basicFileSystem.getPath("/container", "test2");
		
		provider.copy(source, target);
		
		Assert.assertArrayEquals(expectedBytes, getBytesFromPath(target));
	}
	
	@Test
	public void service() throws IOException, URISyntaxException {
		FileSystem newFileSystem = FileSystems.newFileSystem(new URI(basicFileySystemUriString), env);
		Assert.assertTrue(newFileSystem instanceof SwiftFileSystem);
		
		FileSystem getFileSystem = FileSystems.getFileSystem(new URI(basicFileySystemUriString));
		Assert.assertEquals(newFileSystem, getFileSystem);
	}
	
	@Test
	public void resolve() throws IOException {
		AbstractSwiftPath path = createPathWithDefaultContent("/container", "test1").resolve("resolved");
		Assert.assertEquals("/container/test1/resolved", path.getPath());
	}
	
	@Test
	public void exists() throws IOException {
		AbstractSwiftPath existingPath = createPathWithDefaultContent("/container", "test1");
		AbstractSwiftPath nonExistingPath = existingPath.resolve("doesNotExist");
		// TODO Test directory exists
		
		Assert.assertTrue(Files.exists(existingPath));
		Assert.assertFalse(Files.exists(nonExistingPath));
	}
	
	@Test
	public void deleteFileThatExists() throws IOException {
		AbstractSwiftPath path = createPathWithDefaultContent("/container", "test1");
		Files.delete(path);
		Assert.assertFalse(Files.exists(path));
	}
	
	@Test
	public void deleteFileThatDoesNotExists() throws IOException {
		AbstractSwiftPath path = basicFileSystem.getPath("/container", "test1");
		try {
			Files.delete(path);
			Assert.fail("Should throw an exception when deleting a file that does not exist");
		} catch (IOException e) {
		}
	}
	
	@Test
	public void createContainer() throws IOException {
		Assert.assertFalse(account.getContainer("container").exists());
		Files.createFile(basicFileSystem.getPath("/container"));
		Assert.assertTrue(account.getContainer("container").exists());
	}
	
	@Test
	public void deleteContainerThatExists() throws IOException {
		account.getContainer("container").create();
		Assert.assertTrue(account.getContainer("container").exists());
		Files.delete(basicFileSystem.getPath("/container"));
		Assert.assertFalse(account.getContainer("container").exists());
	}
	
	@Test
	public void deleteContainerThatDoesNotExists() throws IOException {
		AbstractSwiftPath path = basicFileSystem.getPath("/container");
		Assert.assertFalse(account.getContainer("container").exists());
		
		try {
			Files.delete(path);
			Assert.fail("Should throw an exception when deleting a file that does not exist");
		} catch (IOException e) {
		}
	}
	
	@Test
	public void listDirectory() throws IOException {
		AbstractSwiftPath path1 = createPathWithDefaultContent("/container", "dir/test1");
		AbstractSwiftPath path2 = createPathWithDefaultContent("/container", "dir/test2");
		
		AbstractSwiftPath dir = basicFileSystem.getPath("/container", "dir/");
		
		List<SwiftPath> files = directoryStreamToList(Files.newDirectoryStream(dir));
		Assert.assertEquals(2, files.size());
		Assert.assertTrue(files.contains(path1));
		Assert.assertTrue(files.contains(path2));
	}
	
	@Test
	public void deleteDirectory() throws IOException {
		AbstractSwiftPath path1 = createPathWithDefaultContent("/container", "dir/test1");
		Assert.assertTrue(Files.exists(path1));
		AbstractSwiftPath path2 = createPathWithDefaultContent("/container", "dir/test2");
		Assert.assertTrue(Files.exists(path2));
		
		AbstractSwiftPath dir = basicFileSystem.getPath("/container", "dir/");
		Files.delete(dir);
		
		Assert.assertFalse(Files.exists(path1));
		Assert.assertFalse(Files.exists(path2));
	}
	
	private List<SwiftPath> directoryStreamToList(DirectoryStream<Path> directoryStream) {
		List<SwiftPath> list = new ArrayList<SwiftPath>();
		for (Path path : directoryStream) {
			list.add((SwiftPath) path);
		}
		return list;
	}
	
	@Test
	public void directoryEndsWithASlash() throws IOException {
		AbstractSwiftPath path = createPathWithDefaultContent("/container", "dir/test1");
		Assert.assertTrue(Files.exists(path));
		
		AbstractSwiftPath dir = basicFileSystem.getPath("/container", "/");
		List<SwiftPath> files = directoryStreamToList(Files.newDirectoryStream(dir));
		Assert.assertEquals(1, files.size());
		Assert.assertTrue(files.get(0).getPath().endsWith("/"));
	}
	
	@Test
	public void directoryIsADirectory() throws IOException {
		AbstractSwiftPath path = createPathWithDefaultContent("/container", "dir/test1");
		Assert.assertTrue(Files.exists(path));
		
		AbstractSwiftPath dir = basicFileSystem.getPath("/container", "/");
		List<SwiftPath> files = directoryStreamToList(Files.newDirectoryStream(dir));
		Assert.assertEquals(1, files.size());
		Assert.assertTrue(Files.isDirectory(files.get(0)));
	}
	
	@Test
	public void listDirectoryReturnsSubdirectories() throws IOException {
		AbstractSwiftPath path1 = createPathWithDefaultContent("/container", "dir1/test1");
		Assert.assertTrue(Files.exists(path1));
		AbstractSwiftPath path2 = createPathWithDefaultContent("/container", "dir2/test1");
		Assert.assertTrue(Files.exists(path2));
		
		AbstractSwiftPath dir = basicFileSystem.getPath("/container", "/");
		AbstractSwiftPath dir1 = basicFileSystem.getPath("/container", "dir1/");
		AbstractSwiftPath dir2 = basicFileSystem.getPath("/container", "dir2/");
		
		List<SwiftPath> files = directoryStreamToList(Files.newDirectoryStream(dir));
		Assert.assertEquals(2, files.size());
		Assert.assertTrue(files.contains(dir1));
		Assert.assertTrue(files.contains(dir2));
		
//		for (Path path : Files.newDirectoryStream(dir)) {
//			Assert.assertTrue(path.equals(dir1) || path.equals(dir2));
//		}
	}
	
	@Test
	public void listDirectoryDoesNotReturnFilesTwoDirectoriesDeep() throws IOException {
		AbstractSwiftPath path1 = createPathWithDefaultContent("/container", "dir1/test1");
		Assert.assertTrue(Files.exists(path1));
		
		AbstractSwiftPath dir = basicFileSystem.getPath("/container", "/");
		AbstractSwiftPath dir1 = basicFileSystem.getPath("/container", "dir1/");
		
		List<SwiftPath> files = directoryStreamToList(Files.newDirectoryStream(dir));
		Assert.assertEquals(1, files.size());
		Assert.assertTrue(files.get(0).equals(dir1));
	}
	
	// TODO Create, delete, and list directories
}
