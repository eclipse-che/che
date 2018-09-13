Eclipse Che is a platform for creating distributed developer tooling. There are numerous ways to extend, modify, and customize Eclipse Che. This document itemizes all of the resources available for customizing Che. We have documentation pages, tutorials, and specifications.

#### Stacks
A stack is the configuration of a runtime that can be used to power a workspace. Users choose the stack that powers a workspace within the user dashboard. Stacks have a recipe that defines how the container should be created and also meta data that defines the tags associated with the stack.
* [How to Use and Modify Stacks](https://www.eclipse.org/che/docs/stacks.html)

#### Recipes
A recipe defines the runtime of a workspace environment.  Workspaces can have multiple environments, each with their own runtime.  Che supports different kinds of runtimes, but the default is Docker letting us make use of [Dockerfiles](https://docs.docker.com/engine/reference/builder/) or [Compose](https://docs.docker.com/compose/compose-file/#build) as recipes.
* [Recipes](https://www.eclipse.org/che/docs/recipes.html)
* [Che Included Recipes](https://github.com/eclipse/che-dockerfiles)

#### Project Samples
A project sample is a code template cloned into the workspace when a user creates a new project. Users can select from a sample while using the user dashboard. Samples have both sample code and a default set of commands associated with them. Samples are loaded based upon the type of stack selected. You can add your own samples to the default Che distribution.
* [Samples](https://github.com/eclipse/che/blob/master/ide/che-core-ide-templates/src/main/resources/samples.json)


#### Commands
A command is a process that is injected into your workspace. It's outputs are streamed into the console. Commands have type and you can create, save, update and delete commands from within the IDE. You can create templates that have sample projects with default commands that are pre-populated in the IDE. Commands can reference macros that intelligently navigate the project tree or current selections.
* [Commands](https://www.eclipse.org/che/docs/commands-ide-macro.html)


#### Extension Development
An extension is a set of code and resources that are packaged into a plugin that alter the behavior of the system. Extensions can be IDE extensions, workspace extensions (they are injected into the workspace agent running within each workspace), or Che extensions (injected into the Che server). Extensions are authored in Java and maven, and then packaged into JAR / ZIP files for deployment into Eclipse Che.
* [Extension Intro](https://www.eclipse.org/che/docs/framework-overview.html)


#### IDE Extensions
IDE extensions are compiled into JavaScript with other extensions to create a single, JavaScript application. You can package many extensions together into a single JavaScript application. The JavaScript application is cross-browser optimized. You can debug extensions and perform execution traces from within the browser of extension code. IDE extensions can invoke REST services that are running within the Che server or within a workspace. Che provides default workspace REST APIs or you can provide your own with workspace extensions.
* [Invoking Workspace REST APIs](https://www.eclipse.org/che/docs/rest-api.html)
* [Project Type](https://www.eclipse.org/che/docs/project-types.html)
* [Actions](https://www.eclipse.org/che/docs/actions.html)
* [Parts](https://www.eclipse.org/che/docs/parts.html)


#### Server-Side Extensions and Agents
Server-side extensions are libraries that are deployed into the workspace agent when the workspace is activated. Che deploys some standard server-side extensions that expose the Workspace REST API to the outside world. You can author extensions that modify or extend this API with your own services. New workspace APIs are exposed as JAX-RS services and you use dependency injection to define the API interfaces.

Agents are scripts that are executed after a [runtime machine](https://www.eclipse.org/che/docs/devops/runtime-machines/index.html) is created. They add additional capabilities to the machines they’re injected in - for example to allow terminal access or enhanced language services (using the Language Server Protocol). Agents allow these services to be injected into machines built from stock Dockerfiles or Compose files.
* [Language Server Protocol](https://www.eclipse.org/che/docs/language-servers.html)
* [Agents Packaged With Che](https://github.com/eclipse/che/tree/master/agents)

#### Workspace REST API
IDE extensions have access to a default set of workspace APIs that are deployed within each workspace. These APIs are available through a Swagger configuration.
* [Swagger](https://www.eclipse.org/che/docs/rest-api.html)
* [Security Model](https://www.eclipse.org/che/docs/authentication.html)
* [Workspaces](https://www.eclipse.org/che/docs/workspace-data-model.html)
