---
tags: [ "eclipse" , "che" ]
title: Project Samples
excerpt: "Sample code that can be used to bootstrap the content of a new project."
layout: docs
permalink: /:categories/samples/
---
A template is a packaged set of sample code that is launched in the workspace when a user creates a new project. Users can select from a template while using the user dashboard. Templates have both sample code and a default set of commands associated with them. Templates are filtered based upon the type of stack selected. You can add your own templates to Che.
# Project Templates  
Che provides stacks with project templates (code samples).  It is possible to have a single stack with a variety of different project templates, which provide sample code to get started with or wizard-driven templates.

In the user dashboard and after a stack is selected, Che propose a list of project templates that you can import in your workspace. You can either start with the ready-to-run project samples or use the wizard-driven templates.
![templates.png]({{ base }}/assets/imgs/templates.png)
Each ready-to-run project sample registers their set of commands in the workspace which allow the code to build and run without additional configuration.

### Wizard-driven templates
Wizard-driven templates allow the user to create a project and configure the code it uses within the IDE. The project structure will be created in the workspace, then a wizard will be displayed in the IDE for configuring project metadata.
![maven.png]({{ base }}/assets/imgs/maven.png)

# Register New Project Templates  

#### Template Object
To register your a new project template to Che, you first need to create the corresponding Template Object. Please read the following documentation for the [Template Object](https://dash.readme.io/project/eclipse-che/docs/code-template).  

A code template can be used to instantiate a set of files into the project tree. The code template will appear in the user dashboard when a user attempts to create a new project.
Those projects templates can be registered for existing Che stacks, or it can be for a [custom stack that you author](https://eclipse-che.readme.io/docs/stacks#custom-stacks-for-che).
Different templates will be shown to the user depending upon the stack that they select using embedded filters. The template lets the initial code tree to be instantiated from a git / URL or from a hosted ZIP archive.
```shell  
# Location of Che template definitions
${che.home}/templates

# How Maven grabs templates and packages them into a binary distribution
/assembly-main/src/assembly/assembly.xml\
```
When `assembly-main` module is compiled, it unpacks `che-core-ide-templates-${CHE_VERSION}.jar` into `/templates` directory that you will find in `${CHE_HOME}` after compilation is completed.

Templates JSON file is packed into  `che-core-ide-templates-${CHE_VERSION}.jar` which is part of [che-core](https://github.com/eclipse/che/tree/master/core/ide/che-core-ide-templates/src/main/resources).

When Che is up, it looks for `project.template_description.location_dir` property and attempts to load all `.json` files with template descriptors:
```json  
# path to templates descriptor location
project.template_description.location_dir=${che.home}/templates\
```

# Add Your Template to Default Che Assembly  
To add a template to the default Che assembly issue a pull request against the `samples.json` at https://github.com/eclipse/che/blob/master/ide/che-core-ide-templates/src/main/resources/samples.json.
