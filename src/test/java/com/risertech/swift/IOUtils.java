package com.risertech.swift;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class IOUtils {
	public static byte[] read(ReadableByteChannel channel) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		int readSize = 0;
		while ((readSize = channel.read(buffer)) > 0) {
			buffer.flip();
			outputStream.write(buffer.array(), 0, readSize);
			buffer.clear();
		}

		return outputStream.toByteArray();
	}
	
	public static byte[] read(InputStream inputStream) throws IOException {
		return read(Channels.newChannel(inputStream));
	}
}
