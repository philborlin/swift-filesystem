package com.risertech.swift;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SwiftFileSystemProviderTest {
	String basicFileySystemUriString = "swift://localhost/v1/tentant";
	byte[] expectedBytes = new byte[] { 1, 2, 3, 4 };
	Map<String, Object> env;
	SwiftFileSystemProvider provider;
	SwiftFileSystem basicFileSystem;
	
	@Before
	public void setup() throws IOException, URISyntaxException {
		env = new SwiftEnvironmentBuilder().build();
		env.put(SwiftFileSystemProvider.MOCK, true);
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
}
