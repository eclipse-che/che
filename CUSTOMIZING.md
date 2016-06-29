Eclipse Che is a platform for creating distributed developer tooling. There are numerous ways to extend, modify, and customize Eclipse Che. This document itemizes all of the resources available for customizing Che. We have documentation pages, tutorials, and specifications.

#### Stacks
A stack is the configuration of a runtime that can be used to power a workspace. Users choose the stack that powers a workspace within the user dashboard. Stacks have a recipe that defines how the container should be created and also meta data that defines the tags associated with the stack. 
* [Add Stacks](https://eclipse-che.readme.io/docs/stacks#custom-stacks-for-che)
* [Stack JSON Object](https://eclipse-che.readme.io/docs/stack)
* [Che Stacks](https://eclipse-che.readme.io/docs/stacks)
* [Che Included Stacks](https://eclipse-che.readme.io/docs/stacks#section-ready-to-go-stacks)
* [Add Your Stack to Default Che Assembly](https://eclipse-che.readme.io/v4.0/docs/stacks#adding-stacks-to-the-che-default-assembly)
* * TODO: Update this link to point to wiki page that can be updated by community

#### Recipes
A recipe defines the runtime of a workspace environment.  Workspaces can have multiple environments, each with their own runtime.  Che supports different kinds of runtimes, but the default is Docker letting us make use of [Dockerfiles](https://docs.docker.com/engine/reference/builder/) as recipes. 
* [Writing Workspace Recipes](https://eclipse-che.readme.io/v4.3/docs/recipes#section-authoring-custom-recipes)
* [Che Recipes](https://eclipse-che.readme.io/v4.3/docs/recipes)
* [Che Included Recipes](https://github.com/codenvy/dockerfiles)
* [Che Recipes Requirements](https://eclipse-che.readme.io/v4.3/docs/recipes#section-inherit-from-non-eclipse-che-base-images)

#### Templates
A template is a packaged set of sample code that is launched in the workspace when a user creates a new project. Users can select from a template while using the user dashboard. Templates have both sample code and a default set of commands associated with them. Templates are loaded based upon the type of stack selected. You can add your own templates to the default Che distribution.
* [Add Templates](https://eclipse-che.readme.io/docs/templates#register-new-project-templates)
* [Template JSON Object](https://eclipse-che.readme.io/docs/template)
* [Che Templates](https://eclipse-che.readme.io/docs/templates)
* [Add Your Template to Default Che Assembly](https://eclipse-che.readme.io/docs/templates#section-add-your-template-to-default-che-assembly)

#### Commands
A command is a process that is injected into your workspace. It's outputs are streamed into the console. Commands have type and you can create, save, update and delete commands from within the IDE. You can create templates that have sample projects with default commands that are pre-populated in the IDE. Commands can reference macros that intelligently navigate the project tree or current selections.
* [Create Commands](https://eclipse-che.readme.io/docs/commands)
* [Command Macros](https://eclipse-che.readme.io/docs/commands#macros)
* [Command JSON Object](https://eclipse-che.readme.io/docs/command)

#### Extension Development
An extension is a set of code and resources that are packaged into a plugin that alter the behavior of the system. Extensions can be IDE extensions, workspace extensions (they are injected into the workspace agent running within each workspace), or Che extensions (injected into the Che server). Extensions are authored in Java and maven, and then packaged into JAR / ZIP files for deployment into Eclipse Che.
* [Che Extensions Introduction](https://dash.readme.io/project/eclipse-che/v4.3/docs/introduction-1)
* [Developing Extensions](https://eclipse-che.readme.io/docs/create-and-build-extensions)
* [JSON Extension Tutorial](https://eclipse-che.readme.io/v4.3/docs/introduction-1#section-the-json-example)
* [Authoring Extensions in Che](https://eclipse-che.readme.io/docs/setup-che-workspace#author-extension-using-the-che-ide)
* [Authoring Extensions in Eclipse](https://eclipse-che.readme.io/docs/setup-che-workspace#author-extension-using-the-eclipse-ide)
* [Authoring Extensions in IntelliJ](https://eclipse-che.readme.io/docs/setup-che-workspace#author-extensions-using-intellij-ide)
* [Dependency Injection](https://eclipse-che.readme.io/docs/dependency-injection-basics)
* [Extensions Packaged With Che](https://github.com/eclipse/che/tree/master/plugins)
* [Extension SDK JavaDoc](https://eclipse-che.readme.io/v4.0/docs/java-class-reference)

#### IDE Extensions
IDE extensions are compiled into JavaScript with other extensions to create a single, JavaScript application. You can package many extensions together into a single JavaScript application. The JavaScript application is cross-browser optimized. You can debug extensions and perform execution traces from within the browser of extension code. IDE extensions can invoke REST services that are running within the Che server or within a workspace. Che provides default workspace REST APIs or you can provide your own with workspace extensions.
* [Invoking Workspace REST APIs](https://eclipse-che.readme.io/v4.0/docs/calling-workspace-apis)
* [Editors](https://eclipse-che.readme.io/docs/code-editors)
* [Project Type](https://eclipse-che.readme.io/docs/project-types)
* [Actions](https://eclipse-che.readme.io/docs/actions)
* [Services](https://eclipse-che.readme.io/docs/serverworkspace-access)
* [Parts](https://eclipse-che.readme.io/docs/parts)
* [Commands](https://github.com/benoitf/ide-plugin-demo)
* [Events](https://eclipse-che.readme.io/docs/events)
* * TODO: Panels 
* * TODO: Popups
* * TODO: Wizards

#### Server-Side Extensions
Server-side extensions are libraries that are deployed into the workspace agent when the workspace is activated. Che deploys some standard server-side extensions that expose the Workspace REST API to the outside world. You can author extensions that modify or extend this API with your own services. New workspace APIs are exposed as JAX-RS services and you use dependency injection to define the API interfaces.
* * TODO: JAX-RS Conventions
* * TODO: Adding Custom Services into Workspace Agent

#### Workspace REST API
IDE extensions have access to a default set of workspace APIs that are deployed within each workspace. These APIs are available through a Swagger configuration. 
* [Swagger Configuration of APIs](https://eclipse-che.readme.io/docs/rest-api)
* [Authentication](https://eclipse-che.readme.io/docs/authentication)
* [Workspaces](https://eclipse-che.readme.io/docs/create-workspaces-and-projects)
* [Projects](https://eclipse-che.readme.io/docs/api-projects)
* [Project Types](https://eclipse-che.readme.io/docs/custom-project-types)
* [File Acces](https://eclipse-che.readme.io/docs/edit-build-and-run)
* [Events](https://eclipse-che.readme.io/docs/events)

#### Plug-Ins and Assemblies
A plugin is a set of extensions (both IDE and workspace extensions) along with their collective reosurces that are packaged into a single deployable unit, usually as a JAR or ZIP file. An assembly is a set of plug-ins combined with the Eclipse Che core that is assembled into a re-distributable set of binaries. A new assembly can fundamentally alter the Che branding. Che can create assemblies packaged as a desktop IDE or as a new Che server.
* [Plugin Development](https://eclipse-che.readme.io/docs/plug-ins)
* [Drag and Drop Plugins](https://eclipse-che.readme.io/v1.0/docs/developing-plugins) FYI - Not yet updated for 4.x
* [Assemblies](https://eclipse-che.readme.io/docs/assemblies)
