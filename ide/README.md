# Che IDE

- [Making a GWT library for the IDE GWT app](#making-a-gwt-library-for-the-ide-gwt-app)
  * [pom.xml](#pomxml)
  * [*.gwt.xml](#gwtxml)
  * [Consuming the shared libraries](#consuming-the-shared-libraries)
- [Including an IDE plugin to the IDE GWT app](#including-an-ide-plugin-to-the-ide-gwt-app)
- [GWT Super DevMode](#gwt-super-devmode)

## Making a GWT library for the IDE GWT app
GWT library it's a JAR that contains compiled classes, project's (re-)sources, GWT module descriptor (*.gwt.xml) and possibly other GWT-specific files.

### pom.xml
To make a GWT library (e.g., IDE plugin) for using it in IDE GWT app just do the two steps in your pom.xml:
- add the `gwt-maven-plugin` configuring GWT module name:
  ```xml
  <plugin>
     <groupId>net.ltgt.gwt.maven</groupId>
     <artifactId>gwt-maven-plugin</artifactId>
     <extensions>true</extensions>
     <configuration>
        <moduleName>org.eclipse.che.plugin.python.Python</moduleName>
     </configuration>
  </plugin>
  ```
- set packaging to `gwt-lib` which triggers a Maven lifecycle that will build a `gwt-lib` artifact.

### *.gwt.xml
Project's `*.gwt.xml` file is generated within the `gwt-lib` Maven lifecycle and contains:
- the declarations for the default source folders:
  ```xml
  <source path="client"/>
  <source path="shared"/>
  <super-source path="super"/>
  ```
- `<inherits/>` directives for the project's *direct* dependencies which were packaged as a `gwt-lib`.

*Optional* template may be provided in `src/main/module.gwt.xml` for generating project's `*.gwt.xml` file.
The most common cases when you may require a template:
- need to override the default source folders, like [here](https://github.com/eclipse/che/blob/f15fbf1cb1248d18acc3ee6fdc41766946ea4a3b/plugins/plugin-java/che-plugin-java-ext-lang-client/src/main/module.gwt.xml#L18);
- need to add `<inherits/>` directive for a GWT lib that isn't packaged as a `gwt-lib` artifact (doesn't contain GWT-specific meta information).

### Consuming the shared libraries
The shared libraries don't require any GWT-specific files or configuration in pom.xml to be consumed by a GWT library.

To use shared code in a GWT library:
- declare a dependency on the "normal" artifact (JAR with compiled classes);
- declare a dependency on the "sources" artifact (with `<classifier>sources</classifier>`).

See an example [here](https://github.com/eclipse/che/blob/19f5fd1f5ae8f165b7306e71cb0d58c2082fafab/plugins/plugin-python/che-plugin-python-lang-ide/pom.xml#L49-L57).

## Including an IDE plugin to the IDE GWT app
Just add a Maven dependency on the appropriate artifact (gwt-lib) to the `che-ide-gwt-app`'s pom.xml.

In case the added artifact represents Che's sub-project, dependency should be declared with `<type>gwt-lib</type>` or `<classifier>sources</classifier>` to be able to use it with Super DevMode.

## GWT Super DevMode
To launch GWT Super DevMode run the command `mvn gwt:codeserver -pl :che-ide-gwt-app -am -Pfast` from the root folder of the Che project.
