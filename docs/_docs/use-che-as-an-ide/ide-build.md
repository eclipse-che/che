---
tags: [ "eclipse" , "che" ]
title: Build
excerpt: "Building and compiling projects with commands"
layout: docs
permalink: /:categories/build/
---
If your project has a built-in project type, Che will install a series of type-specific commands that provide utilities for building projects. For example, Che has a built-in `maven` project type that will install the Maven plug-in whenever one or more projects in your workspace are set with the `maven` project type.

Plug-in developers can optionally provide typed commands, which will appear in the [Commands](doc:commands) editor to simplify the creation of commands to perform compiling tasks.  With Maven, this includes dependencies update, project tree view for external libraries, and maven flag interpolation.

For all other projects, you can write custom [Commands](doc:commands). Or, you can run command-line processes through the terminal.
# Maven Commands  
You can create maven-specific commands in the command editor. The maven plug-in installs a Maven section to the editor. This command type will add in the flags and path to executing the maven command so that you just provide the lifecycle and goal within the editor.

The commands are context sensitive. For example, if you create a `build` command where the body is `clean install`, this command will get executed against the current selection in the project tree. If you change the selection in the project tree the maven goal will be executed specifically against the targeted node.

You can reference the `${current.project.path}` macro within your command. Its value is the path to the `pom.xml` from the current selection in the project tree. You can then target a single command to work for modules or projects.

It is also possible to use custom commands to build and package your projects (`Run > Edit Commands > Custom`). `${current.project.path}` will provide you with the path to the currently selected project/module.  
```shell  
# cd to project directory using macro and run Maven build command
cd ${current.project.path}
mvn clean install

# execute Maven command with an argument as a macro
mvn -f ${current.project.path} clean install

# execute Maven command with an argument as an absolute path
mvn -f /projects/awesomeproject clean install\
```
For Maven projects, Eclipse Che automatically discovers and downloads dependencies into the local Maven repository stored within your workspace. This is triggered when the project is opened. You can update dependencies manually at `Code > Update Dependencies`.
# Build in Terminal  
All projects are created and stored in `/projects` directory in a machine that runs a workspace. You can navigate to project directory through the Terminal and run Maven commands in a tried and true way:
```shell  
$ cd /projects/awesomeproject
$ mvn clean install\
```
