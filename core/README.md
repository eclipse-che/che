## About Eclipse Che
Eclipse Che is a next generation Eclipse IDE and open source alternative to IntelliJ. This repository is licensed under the Eclipse Public License 1.0. Visit [Eclipse Che's Web site](http://eclipse.org/che) for feature information or the main [Che assembly repository](http://github.com/codenvy/che) for a description of all participating repositories.

## Build
```sh
cd che-core
mvn clean install
```

## What's Inside?

#### che-core-test-framework
Framework used to test plugins.

#### che-core-vfs-impl
Implementation of VirtualFileSystemProvider for a plain file system.

#### commons
Commons classes used by components and sub-modules.

#### ide
The skeleton of an IDE as a web application that includes UI components, client side API, editors abstractions, wizards, panels, debugger, etc.

#### platform-api-client-gwt
Clients for platform API (server side REST services).

#### platform-api
Che API, including models and REST services.
