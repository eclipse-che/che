Eclipse Che is a platform for creating distributed developer tooling. There are numerous ways to extend, modify, and customize Eclipse Che. This document itemizes all of the resources available for customizing Che. We have documentation pages, tutorials, and specifications.

#### Stacks
A stack is the configuration of a runtime that can be used to power a workspace. Users choose the stack that powers a workspace within the user dashboard. Stacks have a recipe that defines how the container should be created and also meta data that defines the tags associated with the stack.
* [Add Your Stacks to Che](https://www.eclipse.org/che/docs/devops/runtime-stacks/index.html#stack-administration)
* [Stack Data Model](https://www.eclipse.org/che/docs/devops/runtime-stacks-data-model/index.html)
* [Che Included Stacks](https://www.eclipse.org/che/docs/devops/runtime-stacks/index.html#stack-library)
* [Add Your Stack to Default Che Assembly](https://www.eclipse.org/che/docs/devops/runtime-stacks/index.html#adding-stacks-to-the-che-default-assembly)

#### Recipes
A recipe defines the runtime of a workspace environment.  Workspaces can have multiple environments, each with their own runtime.  Che supports different kinds of runtimes, but the default is Docker letting us make use of [Dockerfiles](https://docs.docker.com/engine/reference/builder/) or [Compose](https://docs.docker.com/compose/compose-file/#build) as recipes.
* [Recipes](https://www.eclipse.org/che/docs/devops/runtime-recipes/index.html)
* [Writing Single-Container Recipes](https://www.eclipse.org/che/docs/devops/runtime-recipes/index.html#single-container-recipes)
* [Writing Multi-Container Recipes](https://www.eclipse.org/che/docs/devops/runtime-recipes/index.html#multi-container-recipes)
* [Che Included Recipes](https://github.com/eclipse/che-dockerfiles)

#### Project Samples
A project sample is a code template cloned into the workspace when a user creates a new project. Users can select from a sample while using the user dashboard. Samples have both sample code and a default set of commands associated with them. Samples are loaded based upon the type of stack selected. You can add your own samples to the default Che distribution.
* [Samples](https://www.eclipse.org/che/docs/devops/project-samples/index.html)
* [Add Samples](https://www.eclipse.org/che/docs/devops/project-samples/index.html#register-new-project-templates)
* [Samples Data Model](https://www.eclipse.org/che/docs/devops/project-samples-data-model/index.html)
* [Add Your Samples to Default Che Assembly](https://www.eclipse.org/che/docs/devops/project-samples/index.html#add-your-template-to-default-che-assembly)

#### Commands
A command is a process that is injected into your workspace. It's outputs are streamed into the console. Commands have type and you can create, save, update and delete commands from within the IDE. You can create templates that have sample projects with default commands that are pre-populated in the IDE. Commands can reference macros that intelligently navigate the project tree or current selections.
* [Commands](https://www.eclipse.org/che/docs/ide/commands/index.html)
* [Authoring Command Instructions](https://www.eclipse.org/che/docs/ide/commands/index.html#authoring-command-instructions)
* [Macros](https://www.eclipse.org/che/docs/ide/commands/index.html#macros)
* [Command Data Model](https://www.eclipse.org/che/docs/devops/runtime-stacks-data-model/index.html#commands)

#### Extension Development
An extension is a set of code and resources that are packaged into a plugin that alter the behavior of the system. Extensions can be IDE extensions, workspace extensions (they are injected into the workspace agent running within each workspace), or Che extensions (injected into the Che server). Extensions are authored in Java and maven, and then packaged into JAR / ZIP files for deployment into Eclipse Che.
* [Extension Intro](https://www.eclipse.org/che/docs/assemblies/intro/index.html)
* [Assembly Lifecycle](https://www.eclipse.org/che/docs/assemblies/assembly-lifecycle/index.html)
* [Plugin Lifecycle](https://www.eclipse.org/che/docs/assemblies/plugin-lifecycle/index.html)
* [Development Workflow](https://github.com/eclipse/che/wiki/Development-Workflow)
* [Dependency Injection](https://www.eclipse.org/che/docs/plugins/dependency-injection-basics/)
* [Extensions Packaged With Che](https://github.com/eclipse/che/tree/master/plugins)
* [Extensions Samples](https://github.com/eclipse/che/tree/master/samples)
* [Extension SDK JavaDoc](https://www.eclipse.org/che/docs/assemblies/sdk-class-reference/index.html)

#### IDE Extensions
IDE extensions are compiled into JavaScript with other extensions to create a single, JavaScript application. You can package many extensions together into a single JavaScript application. The JavaScript application is cross-browser optimized. You can debug extensions and perform execution traces from within the browser of extension code. IDE extensions can invoke REST services that are running within the Che server or within a workspace. Che provides default workspace REST APIs or you can provide your own with workspace extensions.
* [Extensions Samples](https://github.com/eclipse/che/tree/master/samples)
* [Invoking Workspace REST APIs](https://www.eclipse.org/che/docs/assemblies/sdk-rest-apis/index.html)
* [Editors](https://www.eclipse.org/che/docs/assemblies/sdk-code-editors/index.html)
* [Project Type](https://www.eclipse.org/che/docs/assemblies/sdk-project-types/index.html)
* [Actions](https://www.eclipse.org/che/docs/assemblies/sdk-actions/index.html)
* [Services](https://www.eclipse.org/che/docs/assemblies/sdk-services/index.html)
* [Parts](https://www.eclipse.org/che/docs/assemblies/sdk-parts/index.html)
* [Themes](https://www.eclipse.org/che/docs/assemblies/sdk-themes/index.html)
Che IDE is developed based on GWT, but it is also possible to embed native web components:
* [Embedded Native JavaScript](https://www.eclipse.org/che/docs/assemblies/sdk-embed-htmljs/index.html)

#### Server-Side Extensions and Agents
Server-side extensions are libraries that are deployed into the workspace agent when the workspace is activated. Che deploys some standard server-side extensions that expose the Workspace REST API to the outside world. You can author extensions that modify or extend this API with your own services. New workspace APIs are exposed as JAX-RS services and you use dependency injection to define the API interfaces.
* [Services](https://www.eclipse.org/che/docs/assemblies/sdk-services/index.html)
Agents are scripts that are executed after a [runtime machine](https://www.eclipse.org/che/docs/devops/runtime-machines/index.html) is created. They add additional capabilities to the machines they’re injected in - for example to allow terminal access or enhanced language services (using the Language Server Protocol). Agents allow these services to be injected into machines built from stock Dockerfiles or Compose files.
* [Agents](https://www.eclipse.org/che/docs/assemblies/sdk-custom-agents/index.html)
* [Language Server Protocol](https://www.eclipse.org/che/docs/assemblies/sdk-language-server-protocol/index.html)
* [Agents Packaged With Che](https://github.com/eclipse/che/tree/master/agents)

#### Workspace REST API
IDE extensions have access to a default set of workspace APIs that are deployed within each workspace. These APIs are available through a Swagger configuration.
* [Swagger](https://www.eclipse.org/che/docs/assemblies/sdk-rest-apis/index.html#browsing-rest-apis)
* [Authentication](https://www.eclipse.org/che/docs/setup/managing/index.html#authenticated-access)
* [Workspaces](https://www.eclipse.org/che/docs/assemblies/sdk-workspace/index.html)
