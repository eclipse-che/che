## Description

This sample plugin  show how to implement server service and how they are consumed from the client IDE.

Read the tutorial at: https://www.eclipse.org/che/docs/plugins/serverworkspace-access/index.html


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
# Replace <local-repo> with the path to your Che repository, to use local binaries in your local image
# Replace <version> with the actual version you are working on
docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock \
                    -v <local-path>:/data \
                    -v <local-repo>:/repo \
                       eclipse/che:<version> start --debug

```


### Documentation resources

- IDE Setup: https://www.eclipse.org/che/docs/plugins/setup-che-workspace/index.html
- Building Extensions: https://www.eclipse.org/che/docs/plugins/create-and-build-extensions/index.html
- Run local Eclipse Che binaries: https://www.eclipse.org/che/docs/setup/configuration/index.html#development-mode
