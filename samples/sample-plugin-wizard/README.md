## Description

The **Plugin Wizard** is a demo plugin showing:
- a new project type for a programming language called "x" and gives it a pretty name of "Sample Project Type"
- a wizard that adds a compiler version property.
- a new file type for X source code
- a sample action that is invoked from within the help menu




## Plugin overview

### Create a new Project Type 

- Defining new project type:
https://github.com/eclipse/che/blob/master/samples/sample-plugin-wizard/che-sample-plugin-wizard-server/src/main/java/org/eclipse/che/plugin/sample/wizard/projecttype/SampleProjectType.java

- Register the project type on the server side through Guice injection: 
https://github.com/eclipse/che/blob/master/samples/sample-plugin-wizard/che-sample-plugin-wizard-server/src/main/java/org/eclipse/che/plugin/sample/wizard/inject/SampleWizardModule.java

- Create the UI for the wizard (view and presenter), and have it registered into the wizard registry. 
https://github.com/eclipse/che/tree/master/samples/sample-plugin-wizard/che-sample-plugin-wizard-ide/src/main/java/org/eclipse/che/plugin/sample/wizard/ide/wizard

SamplePageView.java - interface of wizard page
SamplePageViewImpl.java - implementation of the wizard page 
SamplePageViewImpl.ui.xml - UI Declaration of wizard page
SamplePagePresenter.java - logic of wizard page
SampleWizardRegistrar - class that register new wizard
 
- Use GIN injection to register the new wizard
https://github.com/eclipse/che/blob/master/samples/sample-plugin-wizard/che-sample-plugin-wizard-ide/src/main/java/org/eclipse/che/plugin/sample/wizard/ide/inject/SampleWizardGinModule.java


### Create a new File Type

- Create an action which allow to create a new X source file: 
https://github.com/eclipse/che/blob/master/samples/sample-plugin-wizard/che-sample-plugin-wizard-ide/src/main/java/org/eclipse/che/plugin/sample/wizard/ide/action/NewXFileAction.java

- Register the X filetype to FileTypeRegistry action:
https://github.com/eclipse/che/blob/master/samples/sample-plugin-wizard/che-sample-plugin-wizard-ide/src/main/java/org/eclipse/che/plugin/sample/wizard/ide/SampleWizardExtension.java#L40

- Create the UI for the wizard (view and presenter), and have it registered into the wizard registry:
https://github.com/eclipse/che/tree/master/samples/sample-plugin-wizard/che-sample-plugin-wizard-ide/src/main/java/org/eclipse/che/plugin/sample/wizard/ide/file

NewXfilePresenter.java - logic of wizard page
NewXfileView.java - interface of wizard page
NewXfileViewImpl.java - implementation of the wizard page
NewXFileViewImpl.ui.xml - UI Declaration of the wizard page

### Create a new Action

- Create a simple action which uses notification API:
https://github.com/eclipse/che/blob/master/samples/sample-plugin-wizard/che-sample-plugin-wizard-ide/src/main/java/org/eclipse/che/plugin/sample/wizard/ide/action/SampleAction.java

- Register this simple action within the extension to the help menu
https://github.com/eclipse/che/blob/master/samples/sample-plugin-wizard/che-sample-plugin-wizard-ide/src/main/java/org/eclipse/che/plugin/sample/wizard/ide/SampleWizardExtension.java#L44


## How to build sample-plugin-wizard plugin

### 1- Link to IDE assembly

The plugin-wizard extension has a client-side (IDE) part and an server part. It also includes some code shared between the IDE and the server. You have to introduce the extension as a dependency in `/che/assembly/assembly-ide-war/pom.xml`. 

Add: 
```XML
...
<dependency>
  <groupId>org.eclipse.che.sample</groupId>
  <artifactId>che-sample-plugin-wizard-ide</artifactId>
</dependency>
<dependency>
  <groupId>org.eclipse.che.sample</groupId>
  <artifactId>che-sample-plugin-wizard-shared</artifactId>
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
  <artifactId>che-sample-plugin-wizard-server</artifactId>
</dependency>
<dependency>
  <groupId>org.eclipse.che.sample</groupId>
  <artifactId>che-sample-plugin-wizard-shared</artifactId>
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
<inherits name='org.eclipse.che.plugin.sample.wizard.SampleWizard'/>
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
