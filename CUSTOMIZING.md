Eclipse Che is a platform for creating distributed developer tooling. There are numerous ways to extend, modify, and customize Eclipse Che. This document itemizes all of the resources available for customizing Che. We have documentation pages, tutorials, and specifications.

#### Stacks
A stack is the configuration of a runtime that can be used to power a workspace. Users choose the stack that powers a workspace within the user dashboard. Stacks have a recipe that defines how the container should be created and also meta data that defines the tags associated with the stack. 
* [Add Your Stacks to Che](https://www.eclipse.org/che/docs/workspace/stacks/#custom-stacks-for-che)
* [Stack Data Model](https://www.eclipse.org/che/docs/workspace/stacks)
* [Che Included Stacks](https://www.eclipse.org/che/docs/workspace/stacks/#section-ready-to-go-stacks)
* [Add Your Stack to Default Che Assembly](https://www.eclipse.org/che/docs/workspace/stacks#adding-stacks-to-the-che-default-assembly)
* * TODO: Update this link to point to wiki page that can be updated by community

#### Recipes
A recipe defines the runtime of a workspace environment.  Workspaces can have multiple environments, each with their own runtime.  Che supports different kinds of runtimes, but the default is Docker letting us make use of [Dockerfiles](https://docs.docker.com/engine/reference/builder/) as recipes. 
* [Recipes](https://www.eclipse.org/che/docs/workspace/recipes)
* [Writing Custom Recipes](https://www.eclipse.org/che/docs/workspace/recipes/#section-authoring-custom-recipes)
* [Che Included Recipes](https://github.com/eclipse/che-dockerfiles)

#### Project Samples
A project sample is a packaged set of sample code that is launched in the workspace when a user creates a new project. Users can select from a sample while using the user dashboard. Samples have both sample code and a default set of commands associated with them. Samples are loaded based upon the type of stack selected. You can add your own samples to the default Che distribution.
* [Samples](https://www.eclipse.org/che/docs/workspace/samples)
* [Add Samples](https://www.eclipse.org/che/docs/workspace/samples/#register-new-project-templates)
* [Samples Data Model](https://www.eclipse.org/che/docs/workspace/samples)
* [Add Your Samples to Default Che Assembly](https://www.eclipse.org/che/docs/workspace/samples/#add-your-template-to-default-che-assembly)

#### Commands
A command is a process that is injected into your workspace. It's outputs are streamed into the console. Commands have type and you can create, save, update and delete commands from within the IDE. You can create templates that have sample projects with default commands that are pre-populated in the IDE. Commands can reference macros that intelligently navigate the project tree or current selections.
* [Commands](https://www.eclipse.org/che/docs/ide/commands/)
* [Macros](https://www.eclipse.org/che/docs/ide/commands/#macros)
* [Command Data Model](https://www.eclipse.org/che/docs/workspace/data-model-samples/#commands)

#### Extension Development
An extension is a set of code and resources that are packaged into a plugin that alter the behavior of the system. Extensions can be IDE extensions, workspace extensions (they are injected into the workspace agent running within each workspace), or Che extensions (injected into the Che server). Extensions are authored in Java and maven, and then packaged into JAR / ZIP files for deployment into Eclipse Che.
* [Extension Intro](https://www.eclipse.org/che/docs/plugins/introduction/)
* [Developing Extensions](https://www.eclipse.org/che/docs/plugins/create-and-build-extensions/)
* [JSON Extension Tutorial](https://www.eclipse.org/che/docs/plugins/introduction/#the-json-example)
* [Authoring Extensions in Che](https://www.eclipse.org/che/docs/plugins/setup-che-workspace/#setup-the-che-ide)
* [Authoring Extensions in Eclipse](https://www.eclipse.org/che/docs/plugins/setup-che-workspace/#eclipse-ide---yatta-installer)
* [Authoring Extensions in IntelliJ](https://www.eclipse.org/che/docs/plugins/setup-che-workspace/#gwt-super-dev-mode-for-intellij)
* [Dependency Injection](https://www.eclipse.org/che/docs/plugins/dependency-injection-basics/)
* [Extensions Packaged With Che](https://github.com/eclipse/che/tree/master/plugins)
* [Extension SDK JavaDoc](https://www.eclipse.org/che/docs/plugins/java-class-reference/)

#### IDE Extensions
IDE extensions are compiled into JavaScript with other extensions to create a single, JavaScript application. You can package many extensions together into a single JavaScript application. The JavaScript application is cross-browser optimized. You can debug extensions and perform execution traces from within the browser of extension code. IDE extensions can invoke REST services that are running within the Che server or within a workspace. Che provides default workspace REST APIs or you can provide your own with workspace extensions.
* [Invoking Workspace REST APIs](https://www.eclipse.org/che/docs/plugins/calling-workspace-apis)
* [Editors](https://www.eclipse.org/che/docs/plugins/code-editors)
* [Project Type](https://www.eclipse.org/che/docs/plugins/project-types)
* [Actions](https://www.eclipse.org/che/docs/plugins/actions)
* [Services](https://www.eclipse.org/che/docs/plugins/serverworkspace-access)
* [Parts](https://www.eclipse.org/che/docs/plugins/parts)
* [Commands](https://www.eclipse.org/che/docs/plugins/helloworld-extension)
* [Events](https://www.eclipse.org/che/docs/plugins/introduction/#actions)
* * TODO: Panels 
* * TODO: Popups
* * TODO: Wizards

#### Server-Side Extensions
Server-side extensions are libraries that are deployed into the workspace agent when the workspace is activated. Che deploys some standard server-side extensions that expose the Workspace REST API to the outside world. You can author extensions that modify or extend this API with your own services. New workspace APIs are exposed as JAX-RS services and you use dependency injection to define the API interfaces.
* * TODO: JAX-RS Conventions
* * TODO: Adding Custom Services into Workspace Agent

#### Workspace REST API
IDE extensions have access to a default set of workspace APIs that are deployed within each workspace. These APIs are available through a Swagger configuration. 
* [Swagger](https://www.eclipse.org/che/docs/server/rest-api)
* [Authentication](https://www.eclipse.org/che/docs/setup/managing/#authenticated-access)
* [Workspaces](https://www.eclipse.org/che/docs/server/create-workspaces/)
* [Projects](https://www.eclipse.org/che/docs/server/api-projects/)
* [Project Types](https://www.eclipse.org/che/docs/server/project-types/)
* [File Access](https://www.eclipse.org/che/docs/server/build-run/)
* [Events](https://www.eclipse.org/che/docs/server/events/)

#### Plug-Ins and Assemblies
A plugin is a set of extensions (both IDE and workspace extensions) along with their collective reosurces that are packaged into a single deployable unit, usually as a JAR or ZIP file. An assembly is a set of plug-ins combined with the Eclipse Che core that is assembled into a re-distributable set of binaries. A new assembly can fundamentally alter the Che branding. Che can create assemblies packaged as a desktop IDE or as a new Che server.
* [Plugin Development](https://www.eclipse.org/che/docs/plugins/introduction/)
* [Drag and Drop](https://www.eclipse.org/che/docs/plugins/developing-plugins) - Not yet updated for 5.x
* [Assemblies](https://www.eclipse.org/che/docs/plugins/assemblies)
