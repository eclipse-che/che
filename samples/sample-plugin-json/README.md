## Description

The **JSON Example** is a providing a plugin sample which is use as a continuous example in the plugin documentation. You can learn more about it at: https://www.eclipse.org/che/docs/plugins/introduction/index.html#the-json-example

This sample demonstrate how to extend the Eclipse Che in various ways:
- How to register a new file type and add code completion (Read the tutorial at:https://www.eclipse.org/che/docs/plugins/code-editors/index.html#code-completion)  

- How to define a custom project type with project creation wizard and register project-specific actions (Read the tutorial at: https://www.eclipse.org/che/docs/plugins/project-types/index.html)


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

# Creates a new workspace agent that includes new web app w/ your extension
cd assembly/assembly-wsagent-server
mvn clean install

# Create a new Che assembly that includes all new server- and client-side extensions
cd assembly/assembly-main
mvn clean install
```

### 4- Run Eclipse Che

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
