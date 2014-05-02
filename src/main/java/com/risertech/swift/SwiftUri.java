package com.risertech.swift;

import java.net.URI;
import java.net.URISyntaxException;

public class SwiftUri {
	private final URI fileSystemUri;
	private final String version;
	private final String tenant;
	private final String container;
	private final String path;
	
	SwiftUri(URI fileSystemUri, String version, String tenant, String container, String path) {
		this.fileSystemUri = fileSystemUri;
		this.version = version;
		this.tenant = tenant;
		this.container = container;
		this.path = path;
	}
	
	public SwiftUri(URI uri) {
		if (uri.getPath() != null) {
			String[] pathParts = uri.getPath().substring(1).split("/");
			
			version = pathParts.length >= 1 ? pathParts[0] : "";
			tenant = pathParts.length >= 2 ? pathParts[1] : "";
			container = pathParts.length >= 3 ? pathParts[2] : "";
			
			String potentialPath;
			if (pathParts.length >= 4) {
				StringBuffer buffer = new StringBuffer();
				for (int index = 3; index < pathParts.length; index++) {
					buffer.append("/");
					buffer.append(pathParts[index]);
				}
				potentialPath = buffer.toString();
			} else {
				potentialPath = "";
			}
			
			path = potentialPath + (uri.getPath().endsWith("/") ? "/" : "");
		} else {
			version = tenant = container = path = "";
		}
		
		try {
			if (container == "") {
				fileSystemUri = uri;
			} else {
				String uriString = uri.toString();
				fileSystemUri = new URI(uriString.substring(0, uriString.indexOf("/" + container + path)));
			}
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	
	public URI getFileSystemUri() {
		return fileSystemUri;
	}
	
	public String getVersion() {
		return version;
	}
	
	public boolean hasVersion() {
		return version != "";
	}
	
	public String getTenant() {
		return tenant;
	}
	
	public boolean hasTenant() {
		return tenant != "";
	}
	
	public String getContainer() {
		return container;
	}
	
	public boolean hasContainer() {
		return container != "";
	}
	
	public String getPath() {
		return path;
	}
	
	public boolean hasPath() {
		return !path.equals("");
	}
	
	public SwiftUri copy(String container, String path) {
		return new SwiftUri(fileSystemUri, version, tenant, container, path);
	}
	
	public SwiftUri copy(String path) {
		return copy(container, path);
	}
	
	@Override
	public String toString() {
		return fileSystemUri.toString() + "/" + container + path;
	}
}
