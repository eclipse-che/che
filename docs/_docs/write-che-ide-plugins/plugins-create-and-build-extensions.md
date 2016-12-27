---
tags: [ "eclipse" , "che" ]
title: Building Extensions
excerpt: ""
layout: docs
permalink: /:categories/create-and-build-extensions/
---
{% include base.html %}
This section describes, how to create, build and run a new extension to Che. It is focused on the infrastructural part only. To set-up a workspace, you should first refer to the [Contributor Setup: Che Workspace](doc:setup-che-workspace)

# Build and Run Che  
We document how to build and run Che from source in the [GitHub repository wiki](https://github.com/eclipse/che/wiki/Development-Workflow). You should get familiar with this development workflow and be able to compile and run Che before you start to build an extension.

In order to build Che, you need to properly set the dependencies, they are listed in the [GitHub repository wiki](https://github.com/eclipse/che/wiki/Development-Workflow#dependencies).
Similarly, to build the Dashboard submodule, you'll find the dependencies and instructions in the following wiki `readme.md` of the [GitHub repository](https://github.com/eclipse/che/tree/master/dashboard).

To simplify this we build a Docker image with all the dependencies needed to build Che and all its submodules. Please follow theses instructions build [Che using the Docker image](https://github.com/eclipse/che/wiki/Development-Workflow#build-che-using-docker).


If you want to work with an IDE, please follow this guideline: [IDE Setup](doc:setup-che-workspace).
# Sample Extensions  
There are a set of sample extensions that are included in the Che source code. They are in the `/samples` directory. The examples below reference once of those samples, named `sample-plugin-embedjs`.

The Che sample plugins are not packaged with Che. You need to add each one to Che to test its capabilities. Most of the documentation provides instructions on how to create, build and link extensions manually.

Usually, most developers will copy an existing extension into a new directory and then build that. The linking process is one-time and can be cumbersome the first time that you do it.  Che provides a utility in the source code (in a built assembly) called `bin/che-install-plugin` that automates the linking of your extensions into Che and the rebuilding of an assembly. While this utility is helpful, it's best if you go through the linking exercise once manually to understand the various relationships of what is linked where and why.
# Extension Structure  
The typical structure of a Che extension is composed of the following:

| `/extension/pom.xml`\n`/extension/src/`\n`/extension/target/`   | File   
| --- | ---
| Details   | Your extension source and build files. The `pom.xml` is a build file that compiles your extension and creates a JAR packaging of it. Your JAR is placed in the `/target` directory and installed into your local maven repository.   

Depending on the complexity of your extension, you might build the structure of your plugin with multiple modules. Each module is independently buildable, and they each would have their own `pom.xml`, `src`, and `target` entries.

| File   | Details   
| --- | ---
| `/extension/extension-ide`   | Your extension module for the IDE client.   
| `/extension/extension-server`   | Your extension module for the server part.   
| `/extension/extension-shared`   | Your extension module for the shared code between the server and the client.   

Each module has a directory structure that is based upon maven and will include source code and a target where artifacts are placed.
```shell  
pom.xml
src/main/java/{package-name}/{extensionName}.java\
```
Depending on the extension, you may also need to include:
```text  
# Required for client-side extensions
src/main/resources/{package-name}/{extensionName}.gwt.xml

# Optional, required if you use GIN injection (in client-side extensions)
# GIN injection causes configuration & invocation during IDE activation
src/main/java/{package-name}/{extensionName}GinModule.java

# Optional, required if you use Guice injection (in server-side extensions)
# Guice injection causes configuration & invocation during Che server activation
src/main/java/{package-name}/{extensionName}GuiceModule.java

# Optional, required if you want to include static JavaScript or CSS files (in client-side extensions)
src/main/resources/{full-qualified-extension-name}/your-javascript.js\
```
Generally you can give Gin and Guice modules any file name, but for the sake of simplicity we suggest sticking to the naming convention above.

See the following [example](https://github.com/vparfonov/plugin-sample-wizard) for an advanced sample extension.

# Compiling Extensions  
## pom.xml
Each extension has a root `pom.xml` file. When configuring an extension `pom.xml` file, you must define an `<artifactId>` tag which will be the unique name identifier given to this extension. This identifier tag will be referenced by the Che assembly to declare that your extension is part of Che. You can override the `groupId` and `version` parameters from the parent, or if not specified, it will inherit the values set by the `<parent>` tag reference.
```xml  
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <artifactId>che-parent</artifactId>
        <groupId>org.eclipse.che</groupId>
        <version>!!! REPLACE_WITH_CHE_VERSION !!!</version>
    </parent>
    <artifactId>che-examples-service</artifactId>
    <packaging>jar</packaging>
    <name>Che :: Examples :: Service</name>
    <dependencies>
        <dependency>
            <groupId>com.google.inject</groupId>
            <artifactId>guice</artifactId>
        </dependency>
        <dependency>
            <groupId>javax.ws.rs</groupId>
            <artifactId>javax.ws.rs-api</artifactId>
        </dependency>
        <dependency>
            <groupId>org.eclipse.che.core</groupId>
            <artifactId>che-core-commons-inject</artifactId>
        </dependency>
        <dependency>
            <groupId>com.google.gwt</groupId>
            <artifactId>gwt-user</artifactId>
            <scope>provided</scope>
        </dependency>
    </dependencies>
    <repositories>
        <repository>
            <id>codenvy-public-repo</id>
            <name>codenvy public</name>
            <url>https://maven.codenvycorp.com/content/groups/public/</url>
        </repository>
        <repository>
            <id>codenvy-public-snapshots-repo</id>
            <name>codenvy public snapshots</name>
            <url>https://maven.codenvycorp.com/content/repositories/codenvy-public-snapshots/</url>
        </repository>
    </repositories>
    <pluginRepositories>
        <pluginRepository>
            <id>codenvy-public-repo</id>
            <name>codenvy public</name>
            <url>https://maven.codenvycorp.com/content/groups/public/</url>
        </pluginRepository>
        <pluginRepository>
            <id>codenvy-public-snapshots-repo</id>
            <name>codenvy public snapshots</name>
            <url>https://maven.codenvycorp.com/content/repositories/codenvy-public-snapshots/</url>
        </pluginRepository>
    </pluginRepositories>
    <build>
        <resources>
            <resource>
                <directory>src/main/java</directory>
            </resource>
        </resources>
    </build>
</project>
```
Extensions can choose to reference the [Che maven dependency management `pom.xml`](https://github.com/codenvy/che-parent) which enforces coding standards incorporated throughout Che. It will also set dependencies version automatically. The `<repositories>` tag provides a reference to Che's maven repository hosted by Codenvy.

Please notice the `<version>` tag. Make sure you alter the version reference so it points to the correct version of Che that you installed (or merely the Git tag/branch you checked out). You can find out the correct Che version by looking at the che parent pom `/che/pom.xml`.

We do not require you to use this parent configuration file. Therefore you can bypass the default Che parent configuration and use a custom maven configuration according to your needs.

## Licensing
Referencing the Che parent `pom.xml` enforces the Eclipse Che license header to be in place for all source files. You can execute `mvn license:format` to add license headers to your files.  Or, to skip the license check add:
```xml  
<plugin>
  <groupId>com.mycila</groupId>
  <artifactId>license-maven-plugin</artifactId>
  <configuration>
    <skip>true</skip>
	</configuration>
</plugin>\
```
If you modify the `pom.xml` it needs to be sorted. Run `mvn sortpom:sort` to sort the `pom.xml`.

## Build Your Extension
In your extension directory, run `mvn clean install`. This will build JAR files that bundle your extension.
```text  
/target
  {your-extension-name}-{version}-sources.jar
  {your-extension-name}-{version}.jar\
```

# Linking Extensions  
After you compile your extension, they will be packaged as JAR files. Those JAR files will need to be included into Che and then you can create a custom assembly of Che that includes the JAR files of your extensions.

## Che Files For Linking

| File   | Details   
| --- | ---
| `/che/assembly/pom.xml`   | **Both server-side and client-side extensions.** Due to a temporary limitation in version management, your dependency must also be added to the root Che build artifact.   
| `/che/assembly/assembly-ide-war/pom.xml`   | **Client-side (IDE) extensions. ** Build file for main Che assembly.client side components, extension dependency should be added to this `pom.xml`.   
| `/che/assembly/assembly-ide-war/src/main/resources/org/eclipse/che/ide/IDE.gwt.xml`   | **Client-side (IDE) extensions.** Client-side extensions are authored in GWT. Add your extension GWT module as an inheritance to this IDE GWT module.   
| `/che/assembly/assembly-wsagent-war/pom.xml`   | **Server-side workspace extensions.** Build file that generates the Che web application agent that is deployed inside of a running workspace. You can add your extension to be included with this agent by adding it as a dependency in this file. Update this if your extension brings a new server-side service or component, or extends an existing API deployed with a workspace agent.   
| `/che/assembly/assembly-wsmaster-war/pom.xml`   | **Server-side Che server extensions.** Add your extension as a dependency here if you want your server-side APIs to be accessible as part of the Che server. We call the workspace master (Che server) the location where master functions are provide like add / remove workspace. But if you just want server-side functionality that is available to the IDE, it must go into the workspace, which we call a workspace agent.   
| `che/assembly/assembly-wsagent-server/pom.xml`   | **Optional**  This assembly constructs all of the pieces to create a Che agent server that will be packaged and deployed into any running workspace that has the dev-agent deployed into it. This Che agent server runs a Tomcat server that deploys any of your server-side workspace extensions along with other APIs required by Che to manage the workspace.   

## Your Extension Identifier
Every extension has a unique maven identifier. You will need to reference this identifier as a dependency. You can define your own identifiers for extensions or pull the identifier out of the `pom.xml` of the extension you are working with.  For example, in the samples that are provided with che the `samples/sample-plugin-embedjs/` has a single module for the IDE, and the identifier for the IDE extension is located in `sample/sample-plugin-embedjs/che-sample-plugin-embedjs-ide/pom.xml`.
```xml  
<parent>
  <artifactId>che-sample-plugin-embedjs-parent</artifactId>
  <groupId>org.eclipse.che.sample</groupId>
  <version>5.0.0-M6-SNAPSHOT</version>
</parent>
<artifactId>che-sample-plugin-embedjs-ide</artifactId>\
```
And the identifier of this extension is the `artifactId`, `groupId`, and `version` tags combined together. Tags can inherit values from parents if they are not explicitly defined in the `pom.xml`. So this extension has the identifier of.
```xml  
<artifactId>che-sample-plugin-embedjs-ide</artifactId>
<groupId>org.eclipse.che.sample</groupId>
<version>5.0.0-M6-SNAPSHOT</version>\
```
## Add Extension To Root Che POM
In order to allow your extension to be visible from the root level of Che, add your extension as a dependency in the list of `<dependencies>` from the `<dependencyManagement>`block.  There are a lot of them in the root `pom.xml`. To avoid transitive dependencies, we require every dependency to be explicitly listed and added. While this may seem like a pain in the arse, this will save you a lot of pain in the future from having circular references.
```xml  
<dependencyManagement>
  <dependencies>
    ...
		<dependency>
  		<groupId>org.eclipse.che.sample</groupId>
  		<artifactId>che-sample-plugin-embedjs-ide</artifactId>
  		<version>${che.version}</version>
		</dependency>
    ...
  </dependencies>
</dependencyManagement>\
```
You can insert the dependency anywhere in the list. After you have inserted it, run `mvn sortpom:sort` and maven will order the `pom.xml` for you.

#### Optional: Skip Enforcement
By default, Che has the Maven enforcer plug-in activated. When this plugin is activated, your dependency must be declared in the root `pom.xml`. You can skip enforcement, which will not require your extension to be in the root `pom.xml`. You skip enforcement by building `-Denforcer.skip=true`.

## IDE Extension: Link To Assembly
To include your jar files within the Che assemblies you have to introduce your extension as a dependency in `/che/assembly/assembly-ide-war/pom.xml` and also have it added as a dependency to the GWT application.  First add the dependency:
```xml  
<dependency>
  <groupId>org.eclipse.che.sample</groupId>
  <artifactId>che-sample-plugin-embedjs-ide</artifactId>
</dependency>\
```
You can insert the dependency anywhere in the list. After you have inserted it, run `mvn sortpom:sort` and maven will order the `pom.xml` for you.

Second, link your GUI extension into the GWT app. You will add an `<inherits>` tag to the module definition. The name of the extension is derived from the direction + package structure that you have given your extension.  For example:
```xml  
<inherits name='org.eclipse.che.plugin.embedjsexample.EmbedJSExample'/>\
```
And this means that in our embed sample, there is a file with a `*.gwt.xml` extension in a folder structure identical to the name above.
```shell  
# This name was derived from the package structure in your sample:
/che/samples/sample-plugin-embedjs/che-sample-plugin-embedjs-ide/src/main/resources/org/eclipse/che/plugin/embedjsexample/EmbedJSExample.gwt.xml\
```
Once you have added the IDE extension to both locations, you need to rebuild the IDE.
```shell  
# Build a new IDE.war
# This IDE web app will be bundled into the assembly
cd che/assembly/assembly-ide-war
mvn clean install

# Create a new Che assembly that includes all new server- and client-side extensions
cd assembly/assembly-main
mvn clean install

# Start Che natively
cd che/assembly/assembly-main/target/eclipse-*/eclipse-*/bin
che run

# Or start Che using the CLI with your new assembly
# Replace <version> with the actual directory name
export CHE_LOCAL_BINARY=path_to_che_sources/assembly/assembly-main/target/eclipse-che-<version>/eclipse-che-<version>
che start\
```
## Server Side Workspace Extensions: Link To Assembly
To include your jar files within the Che assemblies you have to introduce your extension as a dependency in `/che/assembly/assembly-wsagent-war/pom.xml` and then rebuild the agent server.  
```xml  
<dependency>
  <groupId>org.eclipse.che</groupId>
  <artifactId>che-examples-service</artifactId>
</dependency>\
```
Once you have added the server-side extension as a dependency, you need to rebuild the agent that is deployed into the workspace.
```shell  
# Create a new web-app that includes your server-side extension
cd che/assembly/assembly-wsagent-war
mvn clean install

# Creates a new agent that includes your server web app that will deploy into workspace
cd che/assembly/assembly-wsagent-server
mvn clean install

# Create a new Che assembly that includes all new server- and client-side extensions
cd assembly/assembly-main
mvn clean install

# Start Che natively
cd che/assembly/assembly-main/target/eclipse-*/eclipse-*/bin
che run

# Or start Che using the CLI with your new assembly
# Replace <version> with the actual directory name
export CHE_LOCAL_BINARY=/assembly/assembly-main/target/eclipse-che-<version>/eclipse-che-<version>
che start\
```
## Server Side Che Server Extensions: Link To Assembly
To include your jar files within the Che assemblies you have to introduce your extension as a dependency in `/che/assembly/assembly-wsmaster-war/pom.xml` and then rebuild the Che server, which we call master.  
```xml  
<dependency>
  <groupId>org.eclipse.che</groupId>
  <artifactId>che-examples-service</artifactId>
</dependency>\
```
Once you have added the extension to the Che server, you need to rebuild the Che server.
```shell  
# Create a new Che server web app that includes your Che server extension
cd che/assembly/assembly-wsmaster-war
mvn clean install

# Create a new Che assembly that includes all new server- and client-side extensions
cd assembly/assembly-main
mvn clean install

# Start Che natively
cd che/assembly/assembly-main/target/eclipse-*/eclipse-*/bin
che run

# Or start Che using the CLI with your new assembly
# Replace <version> with the actual directory name
export CHE_LOCAL_BINARY=/assembly/assembly-main/target/eclipse-che-<version>/eclipse-che-<version>
che start\
```

# Loading Sequence  
There are three ways your extension code is invoked:
1. Compiled into the Che IDE application.
2. When the Che IDE application is activated your GIN modules are invoked.
3. When the Che server boots your Guice modules are invoked.
```text  
CHE ASSEMBLY                              YOUR PLUGIN (title: YourExtension)
------------                              ----------------------------------
IDE.gwt.xml  ------>  references  ----->  YourExtension.gwt.xml
pom.xml  ---------->  builds  --------->  YourExtension.java
  |
  | builds
  |
  🔻
  Che IDE  -------->  injects  -------->  YourGinModule.java      (optional)


CHE RUNTIME                              
-----------                              
@boot  ------------>  injects  -------->  YourGuiceModule.java    (optional)

\
```
The Che assembly is the root Che project that builds a number of assemblies from a set of system plug-ins together with your custom plug-in. The Che assembly has a master configuration file, `/assembly/assembly-ide-war/src/main/resources/org/eclipse/che/ide/IDE.gwt.xml` which defines the modules to compile into the application.

To manually add your plug-in to the Che assembly, you update the `IDE.gwt.xml` file and the assembly `pom.xml` with information about your plug-in. When the Che assembly is built, it will download your extension as a dependency and compile it into the Che IDE application. The Che IDE application will use dependency injection to load any Gin modules.  When you boot Che within tomcat or another application server, Che uses Guice to load any server-side modules for dependency injection.

Your extension may require access to types provides by Che or the Che API, i.e. if you are implemenenting a custom project type or wizard. Che objects can be injected with Gin and Guice which enables your extension to make use of them.
# Improving Incremental Builds  
There are a number of tweaks you can use to speed up the various stages of development. One possibility to speed up the initial build and packaging process is by disabling tests and skipping certain maven plugins.
```shell  
# Add flags to skip the testing and code analysis phases of maven
mvn clean install -DskipTests -Dskip-validate-sources -Dgwt.compiler.localWorkers=4 -Dfindbugs.skip=true\
```

```xml  
<!-- In /assembly/assembly-ide-war/src/main/resources/org/eclipse/che/ide/IDE.gwt.xml -->

<!-- Tell Che to only build one type of browser JavaScript -->
<!-- Values can be 'safari' or 'firefox'.  Safari builds for chrome -->
<set-property name="user.agent" value="safari"/>

<!-- Reduces compiler permutations through some GWT magic. -->
<!-- See https://code.google.com/p/google-web-toolkit/wiki/SoftPermutations. -->
<collapse-all-properties />
```
