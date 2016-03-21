Eclipse Che is both an IDE and a platform for creating distributed developer tooling. There are numerous ways to extend, modify, and customize Eclipse Che. This document itemizes all of the resources available for customizing Che. We have documentation pages, tutorials, and specifications.

#### Add New Commands

* Register new commands in workspace [Docs](https://eclipse-che.readme.io/docs/commands)
* Commands macro [Docs](https://eclipse-che.readme.io/docs/commands#macros)


#### Create Custom Stacks

* Guide to create a custom stack [Docs](https://eclipse-che.readme.io/docs/environments#custom-stacks)
* List of default Che's stacks [Repository](https://github.com/codenvy/dockerfiles)
* File to update to register custom stacks in Che [Repository](https://github.com/eclipse/che/blob/master/core/ide/che-core-ide-stacks/src/main/resources/predefined-stacks.json)

**Existing contributions:**
* Sample TomEE's stacks with custom commands [Pull Request](https://github.com/eclipse/che/pull/570)

#### Install Software Stacks

>COMING SOON


#### Create Server-Side Extensions
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
