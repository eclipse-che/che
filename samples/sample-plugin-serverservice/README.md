## Description

This sample plugin  show how to implement server service and how they are consumed from the client IDE. 

Read the tutorial at: https://eclipse-che.readme.io/v5.0/docs/serverworkspace-access


## How to test sample-serverservice plugin

### 1- Link to IDE assembly

The plugin-serverservice has both part, an IDE extension and a server extension. You have to introduce the extension as a dependency in `/che/assembly/assembly-ide-war/pom.xml`. 

Add: 
```XML
...
<dependency>
  <groupId>org.eclipse.che.sample</groupId>
  <artifactId>che-sample-plugin-serverservice-ide</artifactId>
</dependency>
...
```
You can insert the dependency anywhere in the list. After you have inserted it, run `mvn sortpom:sort` and maven will order the `pom.xml` for you.

### 2- Link to WS-Master assembly

The plugin-serverservice has a server side part. You have to introduce the extension as a dependency in `/che/assembly/assembly-wsmaster-war/pom.xml`. 

Add: 
```XML
...
<dependency>
  <groupId>org.eclipse.che.sample</groupId>
  <artifactId>che-sample-plugin-serverservice-server</artifactId>
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
<inherits name='org.eclipse.che.plugin.serverservice.ServerService'/>
...
```


### 4- Rebuild Eclipse Che


```Shell
# Create a new Che server web app that includes your Che server extension
cd che/assembly/assembly-wsmaster-war
mvn clean install

# Build a new IDE.war
# This IDE web app will be bundled into the assembly
cd che/assembly/assembly-ide-war
mvn clean install

# Create a new Che assembly that includes all new server- and client-side extensions
cd assembly/assembly-main
mvn clean install
```

### 5- Run Eclipse Che

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
