swift-filesystem
================

A Java 7 File System Provider for Swift

This provider is in heavy development. Usage details will appear as the project progresses.

To create a new FileSystem

    URI uri = new URI("swift://hostname:port/version/tenant");
    Map<String, Object> env = new SwiftEnvironmentBuilder().
        setHashPassword(hashedPassword).
        setAuthenticationMethod(SwiftEnvironmentBuilder.AuthenticationMethod.BASIC).
        build()
    FileSystem fileSystem = FileSystems.newFileSystem(uri, env);
  
To get access to the same FileSystem later

    URI uri = new URI("swift://hostname:port/version/tenant");
    FileSystem fileSystem = FileSystems.getFileSystem(uri);
  
A FileSystem represents a tenant and contains a single root. Creating a directory stream from the root of the file
system will give you a list of containers.


