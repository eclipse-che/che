---
tags: [ "eclipse" , "che" ]
title: Java+Gradle in Che
excerpt: ""
layout: tutorials
permalink: /:categories/gradle/
---
Gradle is an open source build automation system that builds upon the concepts of Apache Ant and Apache Maven and introduces a Groovy-based domain-specific language (DSL) instead of the XML form used by Apache Maven of declaring the project configuration.
```text  
#Import a project from source:
https://github.com/che-samples/console-java-gradle

#Select Custom stack > Write your own stack option. The recipe goes as follows
FROM eclipse/ubuntu_gradle\
```

```text  
#In the IDE create a custom command with the following syntax to build your project:
Title:    build
Command:  cd ${current.project.path} && gradle build
Preview:  <empty>

#Create a new custom command to run your application. In this case the command syntax will be:
Title:   run
Command: java -jar ${current.project.path}/build/libs/*.jar
Preview: <empty>

#Run commands may vary depending on the application type (console, webapp etc.)\
```

```text  
# Test your application
1. Execute `build` command.
2. After a successful project build run the `run` command.
3. See the output on the Consoles panel.\
```
