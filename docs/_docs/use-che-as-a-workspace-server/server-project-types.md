---
tags: [ "eclipse" , "che" ]
title: Project Types
excerpt: ""
layout: docs
permalink: /:categories/project-types/
---
A **Project Type** is a behavior that apples a named group of attribute definitions, pre-defined attributes, and an associated set of machine images.

For example, within Che, maven is a Project Type. Each Project Type can have a different set of attributes associated with it as defined by the author of the Project Type. Project Types can have both mandatory and optional attributes. Some attributes are pre-defined and cannot be changed. Some attributes can be derived by the extension at runtime and others must be defined by the Developer User when constructing a new Project.

Each Project Type has an associated set of machine images that are instantiated at runtime to execute Builders and Runners.

A single Project Type can have many machines associated with it that can be interchangeably used by developers. For example, a maven project can have multiple machines with different JVM, application server types, and versions, with the Project successfully executing within each machine.
```shell  
curl http://localhost:8080/api/ext/project/workspacee1p60f8ge4vukxxz/estimate/my-first-sample?type=maven\
```
