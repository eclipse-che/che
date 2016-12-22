---
tags: [ "eclipse" , "che" ]
title: Projects
excerpt: ""
layout: docs
permalink: /:categories/projects/
---
You can place any number of projects into a workspace.

Projects are combinations of modules, folders, and files. Projects can be mapped 1:1 to a source code repository. If a project is given a type, then Che will active plug-ins related to that type. For example, projects with the maven project type will automatically get the Java plug-in, which provides a variety of intellisense for Java projects.
# Modules  
A module is a portion of a project that can have sets of commands run against it where the sub-directory is treated as the root working directory. Modules make it possible to organize a single repository into multiple, independently buildable and runnable units. To create a module, right click on a folder in the IDE explorer tree and select `Create Module`.  You can then execute commands directly against this module.
# Into  
You can step into or out of the project tree. If you step into a folder, that folder will be set as the project tree root and the explorer will redraw itself. All commands are then executed against this folder root.
# Project Type Definition  
Plug-in developers can define their own project types. Since project types trigger certain behaviors within the IDE, the construction of the projects is important to understand.

1. **A project has type.** Project type is defined as one primary type and zero or more mixin types. A primary project type is one where the project is editable, buildable and runnable. A mixin project type defines additional restrictions and behaviors of the project, but by itself cannot be a primary project type. The collection of primary and mixin types for a single project define the aggregate set of attributes that will be stored as meta data within the project.
2. **Project types describe different aspects of a project** such as types of source files inside, the structure of the explorer tree, the way in which a command will be executed, associated workflows, and which plug-ins must be installed.
3. ** A project type defines a set of attributes.** The attributes of a project can be mandatory or optional. Attributes that are optional can be dynamically set at runtime or during configuration.
4. **The attributes for a project type can come from multiple locations.** They can be stored within the server’s repository for the project, or sourced from different locations such as source files, build files (pom.xml), or meta-tags. Attribute value providers are abstractions for sourcing these type attributes from different locations.
5. **Attribute value providers allow auto-detection of project type.** They are used during any import to attempt project type auto-detection based upon analysis of the contents of incoming source code. If the filter provided by an Attribute Value Provider generates attributes from source files that match a registered project type, then that type will automatically be assigned to the incoming project.
6. **Project types support multiple inheritance.** A child project type may extend the attribute set of a parent type.
7. **Projects may have parent-child relationships with other projects** in a workspace. Child projects are called modules.
8. **Modules may have different project types than their parents.** Modules may physically exist within the tree structure of the parent (as its subfolders) or outside (the parent is a soft link to the module project).
