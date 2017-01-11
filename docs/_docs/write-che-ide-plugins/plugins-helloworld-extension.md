---
tags: [ "eclipse" , "che" ]
title: HelloWorld Extension
excerpt: "Create your first extension"
layout: docs
permalink: /:categories/helloworld-extension/
---
{% include base.html %}
This documentation is a step-by-step guide to build your first HelloWorld extension for Eclipse Che.

In order to follow this guide, we consider you have successfully configured Eclipse Che sources inside of Eclipse IDE and have been able to execute your manually built assembly. If not, please refer to the following [documentation](https://eclipse-che.readme.io/docs/setup-che-workspace).


## 1- Create a new maven project

Create a new maven project.
![ScreenShot2016-10-13at16.28.28.png]({{ base }}/assets/imgs/ScreenShot2016-10-13at16.28.28.png)
Skip the archetype selection.
![ScreenShot2016-10-13at14.15.20.png]({{ base }}/assets/imgs/ScreenShot2016-10-13at14.15.20.png)
Provide basic information about the Maven project.
![ScreenShot2016-10-13at14.13.02.png]({{ base }}/assets/imgs/ScreenShot2016-10-13at14.13.02.png)
Provide asked information (we will update them in the next step)

## 2- Add dependencies and parent definition to the pom.xml

In order to be properly defined, we will rely on `che-parent` for the hello world extension. We will inherit from the global che definition.

We add the dependencies for:
- Guice
- Che Core IDE API
- GWT

We also add the Maven repository used to retrieve artifacts and the Build configuration (refer to [https://eclipse-che.readme.io/v5.0/docs/create-and-build-extensions#section-pom-xml](https://eclipse-che.readme.io/v5.0/docs/create-and-build-extensions#section-pom-xml))  .
```xml  
<?xml version="1.0" encoding="UTF-8"?>
<!--

    Copyright (c) 2012-2016 Codenvy, S.A.
    All rights reserved. This program and the accompanying materials
    are made available under the terms of the Eclipse Public License v1.0
    which accompanies this distribution, and is available at
    http://www.eclipse.org/legal/epl-v10.html

    Contributors:
      Codenvy, S.A. - initial API and implementation

-->
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <artifactId>che-parent</artifactId>
        <groupId>org.eclipse.che</groupId>
        <version>5.0.0-M6-SNAPSHOT</version>
    </parent>
    <artifactId>che-helloworld-sample</artifactId>
    <packaging>jar</packaging>
    <name>Che Sample :: Hello World</name>
    <dependencies>
        <dependency>
            <groupId>com.google.inject</groupId>
            <artifactId>guice</artifactId>
        </dependency>
        <dependency>
            <groupId>org.eclipse.che.core</groupId>
            <artifactId>che-core-ide-api</artifactId>
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
    </repositories>
    <build>
        <sourceDirectory>src/main/java</sourceDirectory>
        <outputDirectory>target/classes</outputDirectory>
        <resources>
            <resource>
                <directory>src/main/java</directory>
            </resource>
            <resource>
                <directory>src/main/resources</directory>
            </resource>
        </resources>
        <plugins>
            <plugin>
                <groupId>com.google.code.sortpom</groupId>
                <artifactId>maven-sortpom-plugin</artifactId>
                <configuration>
                    <verifyFail>Warn</verifyFail>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```
Once your pom.xml file is edited, save it.
You will see that Eclipse is displaying an error on the `che-helloworld-extension` module. Fix this error by doing an "Update Project" with the Eclipse's Maven plugin:
![Screen_Shot_2016-10-13_at_14_17_47.png]({{ base }}/assets/imgs/Screen_Shot_2016-10-13_at_14_17_47.png)
## 3- Create HelloWorldExtension class

Create the package `org.eclipse.che.ide.ext.helloworld` in `src/main/java`:
![ScreenShot2016-10-13at14.24.09.png]({{ base }}/assets/imgs/ScreenShot2016-10-13at14.24.09.png)
Add `HelloWorldExtension` java class in the package `org.eclipse.che.ide.ext.helloworld`:
![ScreenShot2016-10-13at14.27.40.png]({{ base }}/assets/imgs/ScreenShot2016-10-13at14.27.40.png)
In this extension we'll need to talk to Parts and Action API. Gin and Singleton imports are obligatory as well for any extension. Add the following import:
```java  
...
import org.eclipse.che.ide.api.extension.Extension;
import com.google.inject.Inject;
import com.google.inject.Singleton;
...\
```
We add the following annotations:
```java  
/**
 * @Singleton is required in case the instance is triggered several times this extension will be initialized several times as well.
 * @Extension lets us know this is an extension and code injected in it will be executed when launched
 */
@Singleton
@Extension(title = "Hello world\ version = "1.0.0")
public class HelloWorldExtension
{
}
```
In the constructor, we want our HelloWorld extension to display an "Hello World" message in the Events Panel. In order to do that, we will use the notification manager.
```java  
{
    @Inject
    public HelloWorldExtension(NotificationManager notificationManager) {
        notificationManager.notify("Hello World");
    }
}
```
Finally, your class should be like this:
```java  
/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.helloworld;

import org.eclipse.che.ide.api.extension.Extension;

/**
 * Che API imports. In this extension we'll need
 * to talk to Parts and Action API. Gin and Singleton
 * imports are obligatory as well for any extension
 */

import org.eclipse.che.ide.api.notification.NotificationManager;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @Singleton is required in case the instance is triggered several times this extension will be initialized several times as well.
 * @Extension lets us know this is an extension and code injected in it will be executed when launched
 */
@Singleton
@Extension(title = "Hello world\ version = "1.0.0")
public class HelloWorldExtension
{
    @Inject
    public HelloWorldExtension(NotificationManager notificationManager) {
        notificationManager.notify("Hello World");
    }
}
```
## 4- Create HelloWorldExtension GWT module

Create the package `org.eclipse.che.ide.ext.helloworld` in `src/main/resources`:
![ScreenShot2016-10-13at14.57.02.png]({{ base }}/assets/imgs/ScreenShot2016-10-13at14.57.02.png)
Add `HelloWorldExtension` GWT module by creating the file `HelloWorldExtension.gwt.xml`:
![ScreenShot2016-10-13at14.59.27.png]({{ base }}/assets/imgs/ScreenShot2016-10-13at14.59.27.png)
Inherits from the GWT modules: User, Inject. We will also inherits from the IDE GWT API.
```xml  
<!--

    Copyright (c) 2012-2016 Codenvy, S.A.
    All rights reserved. This program and the accompanying materials
    are made available under the terms of the Eclipse Public License v1.0
    which accompanies this distribution, and is available at
    http://www.eclipse.org/legal/epl-v10.html
    Contributors:
      Codenvy, S.A. - initial API and implementation

-->
<module>
    <inherits name="com.google.gwt.user.User"/>
    <inherits name="org.eclipse.che.ide.Api"/>
    <inherits name="com.google.gwt.inject.Inject"/>
    <source path=""/>
</module>
```
## 5- Build the Extension

Create the following `Run Configuration`:
![ScreenShot2016-10-13at15.05.00.png]({{ base }}/assets/imgs/ScreenShot2016-10-13at15.05.00.png)
Or you can also open a terminal where you create your HelloWorld Extension and run a `mvn clean install`.


If everything goes well, you should have:
```shell  
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 8.673 s
[INFO] Finished at: 2016-10-13T15:05:21+02:00
[INFO] Final Memory: 47M/648M
[INFO] ------------------------------------------------------------------------
\
```
## 6- Add extension to root pom of Che

In order to allow your extension to be visible from the root level of Che, add your extension as a dependency in the list of `<dependencies>` from the `<dependencyManagement>` block. Edit the `pom.xml` from `che-parent`
```xml  
...
<dependencyManagement>
  <dependencies>
    ...
    <dependency>
      <groupId>org.eclipse.che</groupId>
      <artifactId>che-helloworld-sample</artifactId>
      <version>${che.version}</version>
    </dependency>
    ...
  </dependencies>
</dependencyManagement>
...\
```

![ScreenShot2016-10-13at15.30.00.png]({{ base }}/assets/imgs/ScreenShot2016-10-13at15.30.00.png)
You can insert the dependency anywhere in the list. After you have inserted it, run `mvn sortpom:sort` and maven will order the pom.xml for you.
![ScreenShot2016-10-13at15.34.00.png]({{ base }}/assets/imgs/ScreenShot2016-10-13at15.34.00.png)
## 7- Link to IDE Assembly

The HelloWorld extension is only a client-side (IDE) extension. You have to introduce your extension as a dependency in /che/assembly/assembly-ide-war/pom.xml and also have it added as a dependency to the GWT application.

First add the dependency:
```xml  
...
<dependencies>
	...
  <dependency>
     <groupId>org.eclipse.che</groupId>
     <artifactId>che-helloworld-sample</artifactId>
  </dependency>
  ...
</dependencies>\
```
You can insert the dependency anywhere in the list. After you have inserted it, run `mvn sortpom:sort` and maven will order the pom.xml for you.
![ScreenShot2016-10-13at16.35.54.png]({{ base }}/assets/imgs/ScreenShot2016-10-13at16.35.54.png)
Second, link your GUI extension into the GWT app. You will add an `<inherits>` tag to the module definition. The name of the GWT extension is derived from the direction + package structure given to the GWT module defined in our HelloWorld extension.

In `assembly-ide-war/src/main/resources/org/eclipse/che/ide/IDE.gwt.xml` add:
```xml  
...
<inherits name='org.eclipse.che.ide.ext.helloworld.HelloWorldExtension'/>
...\
```
This means that in our embed sample, there is a file with a *.gwt.xml extension in a folder structure identical to the name above.

![ScreenShot2016-10-13at16.35.54.png]({{ base }}/assets/imgs/ScreenShot2016-10-13at16.35.54.png)

![ScreenShot2016-10-13at16.34.24.png]({{ base }}/assets/imgs/ScreenShot2016-10-13at16.34.24.png)
## 8- Build Che with your extension.

First, we need to rebuild the assembly-ide-war:
![ScreenShot2016-10-13at16.40.08.png]({{ base }}/assets/imgs/ScreenShot2016-10-13at16.40.08.png)
Or you can also do it in a terminal:
```shell  
# Build a new IDE.war
# This IDE web app will be bundled into the assembly
cd che/assembly/assembly-ide-war
mvn clean install\
```
Second, we need to rebuild the whole Eclipse Che assembly:

Or you can also do it in a terminal:
```shell  
# Create a new Che assembly that includes all new client-side extensions
cd che/assembly/assembly-main
mvn clean install\
```
## 9- Start your custom assembly

To start Che from the custom assembly you just built, you can refer to this [Usage: Docker Launcher](doc:usage-docker#local-eclipse-che-binaries). Remind your custom assembly is located in {workspace-path}\che\assembly\assembly-main\target\eclipse-che-<version>\eclipse-che-<version>

## 10- Test your extension

First create a new workspace and open it in the IDE.
Second open the "Events" panel.
You'll see the "Hello World" notification displayed in the list of events.
![ScreenShot2016-10-13at16.55.05.png]({{ base }}/assets/imgs/ScreenShot2016-10-13at16.55.05.png)
