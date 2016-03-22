Eclipse Che is a platform for creating distributed developer tooling. There are numerous ways to extend, modify, and customize Eclipse Che. This document itemizes all of the resources available for customizing Che. We have documentation pages, tutorials, and specifications.

#### Commands
A command is a process that is injected into your workspace. It's outputs are streamed into the console. Commands have type and you can create, save, update and delete commands from within the IDE. You can create templates that have sample projects with default commands that are pre-populated in the IDE. Commands can reference macros that intelligently navigate the project tree or current selections.
* [Create Commands](https://eclipse-che.readme.io/docs/commands)
* [Command Macros](https://eclipse-che.readme.io/docs/commands#macros)

#### Templates
A template is a packaged set of sample code that is launched in the workspace when a user creates a new project. Users can select from a template while using the user dashboard. Templates have both sample code and a default set of commands associated with them. Templates are loaded based upon the type of stack selected. You can add your own templates to the default Che distribution.
* TODO: Reference Template Docs

#### Stacks
A stack is the configuration of a runtime that can be used to power a workspace. Users choose the stack that powers a workspace within the user dashboard. Stacks have a recipe that defines how the container should be created and also meta data that defines the tags associated with the stack. Tags define how the stack is presented in the stack library and also filters out the available project templates that a user sees when creating a project.
* [Custom Stacks](https://eclipse-che.readme.io/docs/environments#custom-stacks)
* * TODO: Remove custom stacks from environments page and create dedicated stacks page.
* * TODO: Include adding custom stacks into the dedicated stacks docs page.
* [Che's Default Stacks](https://github.com/codenvy/dockerfiles)
* [Stacks Created by Community](https://github.com/eclipse/che/pull/570)
* * TODO: Update this link to point to wiki page that can be updated by community

#### Server-Side Extensions
1. Documentation

>COMING SOON

2. REST API
 * Access the Workspace Master APIs [Docs](https://eclipse-che.readme.io/docs/rest-api)
 * Authentication [Docs](https://eclipse-che.readme.io/docs/authentication)
 * Workspaces [Docs](https://eclipse-che.readme.io/docs/create-workspaces-and-projects)
 * Projects [Docs](https://eclipse-che.readme.io/docs/api-projects)
 * Project Types [Docs](https://eclipse-che.readme.io/docs/custom-project-types)
 * Files [Docs](https://eclipse-che.readme.io/docs/edit-build-and-run)
 * Events [Docs](https://eclipse-che.readme.io/docs/events)

3. Add New REST API
  * Create a Server-Side Extension Example [Docs](https://eclipse-che.readme.io/docs/developing-extensions#server-side-extension-example)

4. Workspace Agents

>COMING SOON

#### Create IDE Extensions
**1. Documentation**
  * Extension Directory Structure [Docs](https://eclipse-che.readme.io/docs/developing-extensions#extension-directory-structure)
  * Extension Loading Sequence [Docs](https://eclipse-che.readme.io/docs/developing-extensions#loading-sequence)
  * Dependency Injection [Docs](https://eclipse-che.readme.io/docs/developing-extensions#dependency-injection)
  * Author Extensions Using the Che IDE [Docs](https://eclipse-che.readme.io/docs/developing-extensions#author-extensions-using-the-che-ide)
  * Author Extensions Using the Eclipse IDE [Docs](https://eclipse-che.readme.io/docs/developing-extensions#author-extensions-using-the-eclipse-ide)
  * Author Extension Using IntelliJ IDE [Docs](https://eclipse-che.readme.io/docs/developing-extensions#author-extensions-using-intellij-ide)

**2. Existing Extensions**
  * Extensions packaged with Che [Repository](https://github.com/eclipse/che/tree/master/plugins)
  * Simple Client Extension Example [Docs](https://eclipse-che.readme.io/docs/developing-extensions#ide-extension-example)

**3. JavaDoc**

* How to get the JavaDoc for Che & GWT classes [Docs](https://eclipse-che.readme.io/v4.0/docs/java-class-reference)

**4. Registering Project Type**
>COMING SOON

**5. Menus**
>COMING SOON

**6. Events**
 * Events [Docs](https://eclipse-che.readme.io/docs/events)

**7. Actions & Commands**
 * Sample Extension for Jetty [Repository](https://github.com/benoitf/ide-plugin-demo)
 
**8. Panels**
>COMING SOON

**9. Popups**
>COMING SOON

**10. Wizards**
>COMING SOON

#### Packaging Plug-Ins Into Assemblies
1. Documentation
  * Package Extensions and Build Assemblies [Docs](https://eclipse-che.readme.io/docs/developing-extensions#package-extensions)
