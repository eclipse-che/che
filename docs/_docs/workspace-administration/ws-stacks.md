---
tags: [ "eclipse" , "che" ]
title: Runtime Stacks
excerpt: "Stacks define the workspace runtime, commands, and configuration."
layout: docs
permalink: /:categories/stacks/
---
{% include base.html %}
A stack is a runtime configuration for a workspace. It contains a [runtime recipe](doc:recipes), meta information like tags, description, environment name, and security policies. Since Che supports different kinds of runtimes, there are different stack recipe formats.

Stacks are displayed within the user dashboard and stack tags are used to filter the [project code samples](doc:templates) that are available. It is possible to have a single stack with a variety of different project samples, each of which are filtered differently.

You can use Che's [built-in stacks](https://eclipse-che.readme.io/docs/stacks#using-stacks) or [author your own custom stacks](https://eclipse-che.readme.io/docs/stacks#custom-stacks-for-che).

A stack is different from a [recipe](doc:recipes). A stack is a JSON definition with meta information which includes a reference to a recipe. A recipe is either a [Dockerfile](https://docs.docker.com/engine/reference/builder/) or a [Docker compose file](https://docs.docker.com/compose/) used by Che to create a runtime that will be embedded within the workspace.  It is also possible to write a custom plug-in that replaces the default Docker machine implementation within Che with another one. For details on this, see [Building Extensions](doc:create-and-build-extensions) and / or start a dialog with the core Che engineers at `che-dev@eclipse.org`.
# Using Stacks To Create a New Workspace  
To create a new workspace in the user dashboard:
- Click `Dashboard` > `Create Workspace`
- Click `Workspaces` > `Add Workspace`
- Hit the “+” next to `Recent Workspaces`
![che-stacks1.jpg]({{ base }}/assets/imgs/che-stacks1.jpg)
The stack selection form is available in the “Select Workspace” section, it allows you to choose stacks provided with Che or create and edit your own stack.

## Ready-To-Go Stacks
Che provides ready-to-go stacks for various technologies. These stacks provide a default set of tools and commands that are related to a particular technology.

## Stack Library
Che provides a wider range of stacks which you can browse from the “Stack library” tab. This list contains advanced stacks which can also be used as runtime configuration for your workspaces.
![che-stacks2.jpg]({{ base }}/assets/imgs/che-stacks2.jpg)
## Custom Stack
User can create their own stack from the "Custom stack" tab. Using Che's interface the user can provide a [runtime recipe](doc:recipes) from an existing external recipe file or by writing a recipe directly.

Che provides a form that can be used to write a recipe directly or copied/pasted from an existing location. A recipe can be written directly as a Dockerfile or a Docker compose file and Che will detect which one it is automatically based on syntax. Refer to [Runtime Recipes](doc:recipes) documentation for additional information.
# Stack Administration  
## Stack Loading
In 5.x, we introduced an underlying database for storing product configuration state, including the state of stacks and templates. In previous versions, we primarily allowed stack configuration through a `stacks.json` object file that was in the base of a Che assembly. The `stacks.json` object file is still there, and if you provide any stack definitions within it, they will be loaded (and override!) any stacks in the database whenever Che starts. We will be removing support for the JSON configuration approach in upcoming releases as it is error prone.

## Configuring Stacks
In the user dashboard, click the `Stacks` to view all the available stacks. New stacks can be created and existing stacks can be modified/searched.
```json  
{
  // Tags describes components that make up the stack such as Tomcat, PHP, etc.
  // Tags are listed on stacks when creating a workspace.
  "tags": [
    "Java\n    "JDK\n    "Maven\n    "Tomcat\n    "Subversion\n    "Ubuntu\n    "Git"
  ],
  // Creator is the name of the person or organization that wrote the stack.
  "creator": "ide\n  // Workspace configuration defines environments, commands, and project info.
  "workspaceConfig": {
    "defaultEnv": "default\n    "commands": [
      {
  			// Commands will be pre-loaded in the workspace. They use bash syntax.
        "commandLine": "mvn clean install -f ${current.project.path}\n        "name": "build\n        "type": "mvn\n        "attributes": {}
      }
    ],
		// Projects can be pre-loaded into the workspace.
    "projects": [],
		// Name of the workspace as it appears in the IDE.
    "name": "default\n    "environments": {
      "default": {
        "recipe": {
          "location": "codenvy/ubuntu_jdk8\n          "type": "dockerimage"
        },
        "machines": {
          "dev-machine": {
            "servers": {},
            // Agents are injected into the workspace to provide special funtions.
            "agents": [
              "org.eclipse.che.terminal\n              "org.eclipse.che.ws-agent\n              "org.eclipse.che.ssh"
            ],
            // Sets the RAM allocated to the machine.
            "attributes": {
              "memoryLimitBytes": "2147483648"
            }
          }
        }
      }
    },
    "links": []
  },
  // Name field is used in the "Components" column of the Stack table in Codenvy.
  "components": [
    {
      "version": "1.8.0_45\n      "name": "JDK"
    },
    {
      "version": "3.2.2\n      "name": "Maven"
    },
    {
      "version": "8.0.24\n      "name": "Tomcat"
    }
  ],
  // Description appears at the bottom of the Stack's "tile" in the dashboard.
  "description": "Default Java Stack with JDK 8, Maven and Tomcat.\n  "scope": "general\n  "source": {
    "origin": "codenvy/ubuntu_jdk8\n    "type": "image"
  },
  // Unique name and ID for the stack.
  "name": "Java\n  "id": "java-default"
}
```
## Create a Stack
A stack can be created from scratch using a skeleton template or duplicated from an existing stack.

To create a stack from scratch click the `Add Stacks` button at the top left of the page. This will load a skeleton template that can be edited. After editing the template configuration and changing the stack name, clicking the save button to add the new stack to the available stacks.
![che-add-stack.gif]({{ base }}/assets/imgs/che-add-stack.gif)
## Duplicate a Stack
Duplicating an existing stack is often a good way to create your own. Click the duplicate icon on the right of the stack item you want. This will create a new stack name `<original name> - Copy` which can then be renamed and configuration edited.
![Che-Stack-Duplicate.jpg]({{ base }}/assets/imgs/Che-Stack-Duplicate.jpg)
## Edit a Stack
Stacks name and configuration can be edited by clicking on the stack item name which will bring up the stack editing interface. The stack can be renamed at the top of the stack editing interface. The stack configuration can be changed using the provided forms. After editing is complete, the stack can be saved by clicking the save button.
![che-edit-stack.gif]({{ base }}/assets/imgs/che-edit-stack.gif)
## Delete a Stack
Stacks can be deleted by clicking the checkbox on the left then the delete button that appear on the top right of the page or by clicking the trash bin icon on the right side of the stack item.
![Che-Stack-Delete.jpg]({{ base }}/assets/imgs/Che-Stack-Delete.jpg)
## Register a Custom Stack
Che has a stack API that you can call to manage your custom stacks. See the [Stacks](https://eclipse-che.readme.io/docs/stacks-1) page in section _Use Che as a workspace server_ section.
# Adding Stacks to the Che Default Assembly  

#### Double check this - I think the location of stacks has changed in 5
}  
