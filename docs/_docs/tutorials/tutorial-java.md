---
tags: [ "eclipse" , "che" ]
title: Java Console Apps in Che
excerpt: ""
layout: tutorials
permalink: /:categories/java/
---
# 1. Start any Java stack  
Go to Workspaces tab in User Dashboard, pick any Java stack, create a workspace and run it.
# 2. Create a Java project  
When in the IDE, go to Workspace > Create Project > Java. A wizard will ask for a project name and sources directory location.
# 3. Compile and run  
When a project shows up in project explorer, open `src/Main.java`. Hit Ctrl+Space ti check code auto-completion, see error marking in action etc.

To compile and run the project, go to `Edit Commands`, choose Java type command, press `+` button to add a new command.

By default, `Main.java` is a main class and the command syntax uses this name. However, it is possible to choose another main class and the command will adjust itself accordingly.
# 4. Add a library to classpath  
If your project uses 3rd party libraries, they should be added to classpath at `Project > Configure Classpath`. Before adding a jar to project classpath, it should be uploaded to the project at `Project > Upload File`.
