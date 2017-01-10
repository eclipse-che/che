---
tags: [ "eclipse" , "che" ]
title: Introduction
excerpt: ""
layout: docs
permalink: /:categories/introduction/
---
{% include base.html %}
This part of the documentation describes how to extend Eclipse Che with custom plugins, e.g. to provide support for new languages. It covers various aspects from adding new file types, extending the code editor, adding intellisense features, defining a specific project-type, and accessing the workspace. Before you start to extend Che, we recommend to get a general technical overview as provided [here](https://eclipse-che.readme.io/docs/).

In general, Che can be extended in its three different components, the IDE running in the Browser, the Che Server, and the Workspace (see diagram below).
  * First, the IDE running in the Browser can be extended by new local features, such as simple actions, new editors, and views, or immediate syntax highlighting. Some of these extensions can run completely local, some will use the Che server API.

  * Second, the Che Server can be extended by new plugins. Those extensions will also affect the IDE, e.g. defining a new project type in a plugin, which is done on the server, will create a menu entry in the “New” dialog of the IDE. Server plugins might provide new APIs to be consumed by IDE plugins, e.g. to provide new IntelliSense features. Finally, server plugins can also access the current workspace, e.g. to access files, projects or even the current target machine. Again, there is existing API provided by the workspace agent to be used.

  * Third, the Workspace can also be extended to provide new API to be consumed by the Che server.
![image05.png]({{ base }}/assets/imgs/image05.png)
Therefore, typical extensions of Che are deployed to up to three different components, depending on the use case. However, as many extensions include plugin parts for the IDE, the server and even the Workspace, the extension documentation is not organized by Che components, but by extension use cases (e.g. adding a new file type or implementing a client server communication accessing the workspace).

Technically, client and server extensions are different components, however, as they conceptually belong together, they are organized in one plugin containing several sub-components.

This tutorial starts with an introduction on the following prerequisites to build a custom extensions. If you want to follow the tutorial hands-on, you should read those sections, if you are just interested in learning about a specific extension use case, you might skip them for now.

*  How to [set-up a workspace](doc:setup-che-workspace) for developing a Che extension.
*  How to [create and build extensions](https://eclipse-che.readme.io/docs/create-and-build-extensions) including a description of the general plugin structure used in Che
* A brief introduction on [dependency injection](doc:dependency-injection-basics) and its usage in Che, on the client and on the server side. (If you are already familiar with Guice and Gin, you might want to skip this part)

The remaining tutorial is structured along extension use cases. For every extension capability, we provide a general introduction containing code examples.

The tutorial often refers to a continuous example: The implementation of simple JSON support in Eclipse Che. If you are interested in learning about a certain extension use case, please directly navigate to the respective part of the tutorial. If you are interested in getting an overview about the most important extension features, please have a look at [the introduction of the JSON example in the following section](https://eclipse-che.readme.io/docs/introduction-1#section-the-json-example). It provides a use case oriented overview of the contents of this tutorial and contains links to the detailed parts.


##The JSON Example

In this section, we give a functional overview of the continuous example that we use in most parts of this tutorial, the “**JSON Example**”. It provides simple support for creating, modifying and validating JSON files within Eclipse Che. Please note that the JSON example is not designed to provide perfect JSON support in Che. In fact, it is designed to cover most aspects of providing support for a custom language in Eclipse Che while remaining as simple as possible. This section shall provide an overview about the example and at the same time about Che’s various extension use cases.

The source code for the JSON example is part of the Eclipse Che project itself, you can find it here:
[https://github.com/eclipse/che/tree/master/samples/sample-plugin-json](https://github.com/eclipse/che/tree/master/samples/sample-plugin-json)

The example includes the following parts, which can be found in the respective parts of this tutorial.

###File Type and Code Editor

The example provides [a custom file type](code-editors#section-file-types) for JSON including a custom icon. For this file type it includes [a custom code editor ](code-editors) for JSON files. The code editor provides [syntax highlighting](code-editors#section-syntax-highlighting) for JSON files.
![image08.png]({{ base }}/assets/imgs/image08.png)
Additionally, the example implements [code completion](code-editors) based on a list of suggestions. There are two sources for the list of suggestions, the first and simple one is [directly calculated on the client,](code-editors) it can therefore only operate on information available in the context (e.g. the current file opened). The second one is [calculated on the server](serverworkspace-access#section-server-services) and can therefore access the complete workspace to calculate the suggestions.


![image13.png]({{ base }}/assets/imgs/image13.png)
###Project Type

There is a [custom project type](project-types) for working with JSON files. The example provides[ a custom project creation wizard](project-types#section-project-creation-wizard) which allows to enter project specific data. In case of the JSON example, it allows to specify a URL pointing to a JSON schema, which is later used to validate the JSON files within the project. The project wizard is available in the standard “Create New Project” dialog of Che:


![image03.png]({{ base }}/assets/imgs/image03.png)
The JSON example wizard will initialize a new project with two existing JSON files and a directory to contain custom ones:
![image08.png]({{ base }}/assets/imgs/image08.png)
###Actions

The JSON example register two [project-specific actions](docs:section-project-perspective-specific-actions-json-example-) for the custom project type. The first one implements [a simple “Hello World”](docs:section-project-perspective-specific-actions-json-example-).[ The second one ](serverworkspace-access#section-workspace-services)calls a custom service on the server which will access the workspace and count the number of lines of all JSON files within the project. It thereby also includes a template for client/server communication and for accessing source files (e.g. for validation or compilation).

![image00.png]({{ base }}/assets/imgs/image00.png)
