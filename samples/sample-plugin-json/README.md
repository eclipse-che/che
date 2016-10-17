## Description

The **JSON Example** is a providing a plugin sample which is use as a continuous example in the plugin documentation. You can learn more about it at: https://eclipse-che.readme.io/docs/introduction-1#section-the-json-example

This sample demonstrate how to extend the Eclipse Che in various ways:
- How to register a new file type and add code completion (Read the tutorial at: https://eclipse-che.readme.io/docs/code-editors#section-code-completion)  

- How to define a custom project type with project creation wizard and register project-specific actions (Read the tutorial at: https://eclipse-che.readme.io/docs/project-types)


## How to build sample-plugin-json plugin

### 1- Link to IDE assembly

The plugin-json extension has a client-side (IDE) part and an server part. It also includes some code shared between the IDE and the server. You have to introduce the extension as a dependency in `/che/assembly/assembly-ide-war/pom.xml`. 

Add: 
```XML
...
<dependency>
  <groupId>org.eclipse.che.sample</groupId>
  <artifactId>che-sample-plugin-json-ide</artifactId>
</dependency>
<dependency>
  <groupId>org.eclipse.che.sample</groupId>
  <artifactId>che-sample-plugin-json-shared</artifactId>
</dependency>
...
```

You can insert the dependency anywhere in the list. After you have inserted it, run `mvn sortpom:sort` and maven will order the `pom.xml` for you.


### 2- Link to WS-Agent assembly

Introduce the server part of the extension as a dependency in `/che/assembly/assembly-wsagent-war`. 

Add: 
```XML
...
<dependency>
  <groupId>org.eclipse.che.sample</groupId>
  <artifactId>che-sample-plugin-json-server</artifactId>
</dependency>
<dependency>
  <groupId>org.eclipse.che.sample</groupId>
  <artifactId>che-sample-plugin-json-shared</artifactId>
</dependency>
...
```

You can insert the dependency anywhere in the list. After you have inserted it, run `mvn sortpom:sort` and maven will order the pom.xml for you.

### 3- Register dependency to the GWT application

Link the GUI extension into the GWT app. You will add an `<inherits>` tag to the module definition. The name of the GWT extension is derived from the direction + package structure given to the GWT module defined in our extension.

In: `assembly-ide-war/src/main/resources/org/eclipse/che/ide/IDE.gwt.xml`

Add:
```XML
...
<inherits name='org.eclipse.che.plugin.jsonexample.JSONExample'/>
...
```

### 3- Rebuild Eclipse Che


```Shell
# Build a new IDE.war
# This IDE web app will be bundled into the assembly
cd che/assembly/assembly-ide-war
mvn clean install

# Create a new web-app that includes the server-side extension
cd che/assembly/assembly-wsagent-war
mvn clean install

# Create a new Che assembly that includes all new server- and client-side extensions
cd assembly/assembly-main
mvn clean install
```

### 4- Run Eclipse Che

```Shell
# Start Che using the CLI with your new assembly
# Replace <version> with the actual directory name
export CHE_LOCAL_BINARY=path_to_che_sources/assembly/assembly-main/target/eclipse-che-<version>/eclipse-che-<version>
che start
```


### Documentation resources

- IDE Setup: https://eclipse-che.readme.io/v5.0/docs/setup-che-workspace  
- Building Extensions: https://eclipse-che.readme.io/v5.0/docs/create-and-build-extensions
- Run local Eclipse Che binaries: https://eclipse-che.readme.io/v5.0/docs/usage-docker#local-eclipse-che-binaries
