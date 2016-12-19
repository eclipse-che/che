---
tags: [ "eclipse" , "che" ]
title: Vaadin in Che
excerpt: ""
layout: tutorials
permalink: /:categories/vaadin/
---
Vaadin is a set of user interface components for Web applications. Vaadin projects work within Che. This page provides a quick configuration guide to get started.
```text  
# In the dashboard, create a new project and import from source:
https://github.com/mstahv/framework-example

# Choose the Java stack.
# Create the workspace.\
```

```text  
# In the IDE, create a new command. Give it the syntax:
Title:    run
Command:  jetty:run
Preview:  http://${server.port.8080}/${current.project.relpath}

# You can set up live reload and auto-compilation.
# Add a second command of type maven:
Title:    compile
Command:  compile
Preview:  <empty>\
```

```text  
# Test your application
1. Open src/main/java/org/vaadin/samples/helloworld/*.java
2. Make some edits
3. Run the `run` command.
4. Run the `compile` command.
5. You can refresh the web app in the preview URL to see your changes.\
```
