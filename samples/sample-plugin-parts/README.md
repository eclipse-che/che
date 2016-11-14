## Description

This sample plugin  show how to implement a custom view with Che. It also shows how to configure the workbench and open/hides certain views. 

Read the tutorial at: https://eclipse-che.readme.io/v5.0/docs/parts


## How to test sample-plugin-parts plugin

### 1- Link to IDE Assembly

The plugin-parts extension is only a client-side (IDE) extension. You have to introduce the extension as a dependency in `/che/assembly/assembly-ide-war/pom.xml`. 

Add: 
```XML
...
<dependency>
  <groupId>org.eclipse.che.sample</groupId>
  <artifactId>che-sample-plugin-parts-ide</artifactId>
</dependency>
...
```
You can insert the dependency anywhere in the list. After you have inserted it, run `mvn sortpom:sort` and maven will order the `pom.xml`  for you.


### 2- Register dependency to the GWT application

Link the GUI extension into the GWT app. You will add an `<inherits>` tag to the module definition. The name of the GWT extension is derived from the direction + package structure given to the GWT module defined in our extension.

In: `assembly-ide-war/src/main/resources/org/eclipse/che/ide/IDE.gwt.xml`

Add:
```XML
...
<inherits name='org.eclipse.che.plugin.parts.SampleParts'/>
...
```

### 3- Rebuild Eclipse Che


```Shell
# Build a new IDE.war
# This IDE web app will be bundled into the assembly
cd che/assembly/assembly-ide-war
mvn clean install

# Create a new Che assembly that includes all new server- and client-side extensions
cd assembly/assembly-main
mvn clean install
```

### 4- Run Eclipse Che

```Shell
# Start Che using the CLI with your new assembly
# Replace <version> with the actual directory name
export CHE_ASSEMBLY=path_to_che_sources/assembly/assembly-main/target/eclipse-che-<version>/eclipse-che-<version>
che start
```


### Documentation resources

- IDE Setup: https://eclipse-che.readme.io/v5.0/docs/setup-che-workspace  
- Building Extensions: https://eclipse-che.readme.io/v5.0/docs/create-and-build-extensions
- Run local Eclipse Che binaries: https://eclipse-che.readme.io/v5.0/docs/usage-docker#local-eclipse-che-binaries
