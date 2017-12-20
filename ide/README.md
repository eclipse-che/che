# Che IDE

- [Making a GWT library for the IDE GWT app](#making-a-gwt-library-for-the-ide-gwt-app)
  * [pom.xml](#pomxml)
  * [*.gwt.xml](#gwtxml)
  * [Consuming the shared libraries](#consuming-the-shared-libraries)
- [Including an IDE plugin to the IDE GWT app](#including-an-ide-plugin-to-the-ide-gwt-app)
- [GWT Super DevMode](#gwt-super-devmode)
- [Extending IDE GWT app](#extending-ide-gwt-app)

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
There are two options available to launch GWT Super DevMode, depending on the state of the Che sources: whether it's built or not since a lot of sources are generated during the Maven build.
- Case 1: Che sources have been already built. Use the following command:

`mvn gwt:codeserver -pl :che-ide-gwt-app -am -Dmaven.main.skip -Dmaven.resources.skip -Dche.dto.skip -Dskip-enforce -Dskip-validate-sources`

- Case 2: Che sources haven't been built, e.g. freshly cloned or after executing `mvn clean` or you just don't need to build the whole project. Use the following command:

`mvn gwt:codeserver -pl :che-ide-gwt-app -am -Dskip-enforce -Dskip-validate-sources`

The second one requires *more time* to launch GWT CodeServer since the second one it executes `process-classes` build phase for each maven module. So using the first command is preferable.

**Note**, both commands have to be performed in the root folder of the Che project.

## Extending IDE GWT app
There're two GWT libraries provided which allows you to easily extend IDE GWT app: Basic IDE and Full IDE.

Basic IDE represents IDE without any plugins. It allows you to compile IDE GWT app with your own IDE plugins only, e.g.:
  ```xml
  <dependencies>
     <dependency>
        <groupId>org.eclipse.che.core</groupId>
        <artifactId>che-ide-core</artifactId>
    </dependency>
    <dependency>
        <groupId>my.ide.plugin</groupId>
        <artifactId>my-ide-plugin</artifactId>
    </dependency>
  </dependencies>
  ```
Full IDE represents IDE with full set of the standard plugins. It allows you to compile IDE GWT app excluding some of the standard plugins and/or including your own IDE plugins, e.g.:
  ```xml
  <dependencies>
     <dependency>
        <groupId>org.eclipse.che.core</groupId>
        <artifactId>che-ide-full</artifactId>
        <exclusions>
           <exclusion>
              <artifactId>che-plugin-product-info</artifactId>
              <groupId>org.eclipse.che.plugin</groupId>
           </exclusion>
        </exclusions>
    </dependency>
    <dependency>
        <groupId>my.ide.plugin</groupId>
        <artifactId>my-ide-plugin</artifactId>
    </dependency>
  </dependencies>
  <build>
     <plugins>
        <plugin>
           <groupId>org.eclipse.che.core</groupId>
           <artifactId>che-core-gwt-maven-plugin</artifactId>
           <version>${project.version}</version>
           <executions>
              <execution>
                 <goals>
                    <goal>process-excludes</goal>
                 </goals>
              </execution>
           </executions>
        </plugin>
     </plugins>
  </build>
  ```
Note that `che-core-gwt-maven-plugin` have to be added in order to correctly process the IDE plugins exclusions.
