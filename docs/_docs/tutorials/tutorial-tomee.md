---
tags: [ "eclipse" , "che" ]
title: TomEE in Che
excerpt: ""
layout: tutorials
permalink: /:categories/tomee/
---
{% include base.html %}
TomEE is a JavaEE application server. (Learn more about TomEE) This page provides a quick configuration guide to get started with TomEE within Che.  
```text  
# In the dashboard, create a new project from samples
# Choose the stack library and select TomEE stack.
# Choose the ready-to-run project:  "web-javaee-jaxrs"
# Create the workspace.
```

```text  
# In the IDE, you have a set of predefined commands:
CUSTOM
- Run TomEE
- Stop TomEE
MAVEN
- build
- build and run
- debug\
```

```text  
# Test the application
1. Run the `build and run` command.
2. See the application on the preview URL.

# Stopping the TomEE application server
1. Run the command `stop tomee`

# Debug the application
1. Run the `debug` command
2. Connect the debugger by using local machine and port 8000\
```
