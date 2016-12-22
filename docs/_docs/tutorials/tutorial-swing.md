---
tags: [ "eclipse" , "che" ]
title: Java Swing in Che
excerpt: ""
layout: tutorials
permalink: /:categories/swing/
---
Swing is a GUI widget toolkit for Java. It is part of Oracle's Java Foundation Classes (JFC) – an API for providing a graphical user interface (GUI) for Java programs.
```text  
#Import a project from source:
https://github.com/codenvy-templates/desktop-swing-java-basic

#Select `Custom stack > Write your own stack` option. The recipe goes as follows
FROM codenvy/ubuntu_jdk8_x11\
```

```text  
#In the IDE create a Maven command with the following syntax to build your project:
Title:    build
Working directory: ${current.project.path}
Command:  clean install
Preview:  http://${server.port.6080}\
```

```text  
#In the noVNC window right mouse click to call the Terminal. Go to `projects/{your-project-name}/target` directory and start your project:

java -jar {your-artifact-name}.jar\
```

```text  
# Test your application
1. Click Get Greeting tab to call the info box.
2. Exit.
3. Go to the IDE and make some changes to the app.
4. Rerun `build` command.
5. Click Preview URL, cd /projects/{your-project-name}/target directory again and run `java -jar {your-project-name}.jar` command to see changes.\
```
