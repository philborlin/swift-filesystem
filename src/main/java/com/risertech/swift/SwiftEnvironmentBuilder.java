package com.risertech.swift;

import java.util.Map;
import java.util.HashMap;

public class SwiftEnvironmentBuilder {
	private String password;
	private String authUrl;
	private int socketTimeout;
	private String preferredRegion;
	private String hashPassword;
	private AuthenticationMethod authenticationMethod;

	public SwiftEnvironmentBuilder setPassword(String password) {
		this.password = password;
		return this;
	}

	public SwiftEnvironmentBuilder setAuthUrl(String authUrl) {
		this.authUrl = authUrl;
		return this;
	}

	public SwiftEnvironmentBuilder setSocketTimeout(int socketTimeout) {
		this.socketTimeout = socketTimeout;
		return this;
	}

	public SwiftEnvironmentBuilder setPreferredRegion(String preferredRegion) {
		this.preferredRegion = preferredRegion;
		return this;
	}

	public SwiftEnvironmentBuilder setHashPassword(String hashPassword) {
		this.hashPassword = hashPassword;
		return this;
	}

	public SwiftEnvironmentBuilder setAuthenticationMethod(AuthenticationMethod authenticationMethod) {
		this.authenticationMethod = authenticationMethod;
		return this;
	}
	
	public Map<String, Object> build() {
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put(SwiftFileSystemProvider.PASSWORD, password);
		map.put(SwiftFileSystemProvider.AUTH_URL, authUrl);
		map.put(SwiftFileSystemProvider.SOCKET_TIMEOUT, socketTimeout);
		map.put(SwiftFileSystemProvider.PREFERRED_REGION, preferredRegion);
		map.put(SwiftFileSystemProvider.HASH_PASSWORD, hashPassword);
		map.put(SwiftFileSystemProvider.AUTHENTICATION_METHOD, authenticationMethod);
		
		return map;
	}
	
	public enum AuthenticationMethod {
    BASIC,
    KEYSTONE,
    TEMPAUTH
	}
}
