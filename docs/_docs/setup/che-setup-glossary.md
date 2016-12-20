---
tags: [ "eclipse" , "che" ]
title: Glossary
excerpt: ""
layout: docs
permalink: /:categories/glossary/
---
**Artifacts**: The packaged results of build processes executed by a command. Build artifacts are stored in your project tree, can be downloaded, or injected into other machines where they are used during process execution.

**Command **: A configuration that defines a process that will execute within a machine. Commands can be used to launch builds, runs, unit tests or any process that is needed to execute against the files of a project. A command defines how a process will start and stop. Commands have scope and can be made reusable to different users depending upon where it is saved. Commands are injected into any machine controlled by Che, whether that machine is local or remote.

**Compose Recipe**: A Docker syntax for defining the configuration and startup sequence of a set of containers.

**Machine**: A virtual environment that contains a stack of software to be used when executing your project source code and artifacts. Project source code and build artifacts are stored within the machine and synchronized with your central project storage. A machine accepts commands that can start, modify or stop processes. A machine can be active (running) or suspended (saved snapshot) or destroyed (shutdown). The lifecycle of a machine such as booting or shutdown is triggered on various user events such as opening the IDE. Machines are granted an allocation of workspace resources for its execution. Machines can have type where different machines of the same type can be used interchangeably within the same project. Eclipse Che provides a native CLI and Docker implementation of Che server and machines.

**Modules**: A module is a directory in a project that can be independently built and run. Modules have their own project type and attributes, which can affect how the command behavior works for that directory apart from others or the project as a whole. Modules can be nested.

**Multi-Machine**: Much like a single machine definition but contains multiple machine definitions. Each machine within a virtual environment can accept commands that can start, modify or stop processes. A specially named "dev-machine" must be included with each virtual environment which contains the required dependencies/agents and project code. Each machine is granted an allocation of workspace resources for its own execution. Eclipse Che provides multiple machine through a compose recipe.

**Permission**: An authorization granted to access resources. Permissions can be access permissions, which control specific access to resources such as file and project visibility. Permissions can also be behavior permissions, which control access to functionality in the system, such as rights to use the services of a plug-in.

**Preview**: Preview objects are URLs that reference specific files or endpoints activated by your project when you run a command. For example, you may have a command to start a Spring application server and deploy your project's artifacts into the application server. A preview object can be used to present the user with the URL of the deployed application.

**Private Workspace**: A workspace whose permissions are only available to an explicit set of users.

**Process**: A result of a command. Processes run natively within a machine and are the result of either executing a terminal command or having a command injected into a machine.

**Project**: A bundle of source code and configuration files. A project has access to facilities to edit, build, run and debug its contents. A project may be associated with a single source code repository. Projects have a type, which define default behaviors within the IDE and also affects the plug-ins that are injected into the machine that is powering the workspace hosting this project.  Projects can reside in parent-child relationships, for which children projects are called modules.

**Public Workspace**: A workspace whose configuration and runtime that can be accessed by anyone.

**Recipe**: A definition or script that can be used to construct a machine. The default machine implementation within Che is Docker allowing Dockerfiles or Docker compose syntax to be use to define the recipe content.

**Runtime**: Instances of machines that provide an environment for agents and projects to run within the workspace.​

**Resource**: Physical asset consumed by a workspace.  Resources can be defined differently for each workspace.

**Snapshot**: A machine Instance saved to disk with its state preserved. Snapshots of machines are saved as Docker images. Machines and snapshots are bound to Workspaces.

**Stack**: A stack is the configuration of a runtime that can be used to power a workspace. Users choose the stack that powers a workspace within the user dashboard. Stacks have a recipe that defines how the container should be created and also meta data that defines the tags associated with the stack.

**Template**: A template is a packaged set of sample code that is launched in the workspace when a user creates a new project. Users can select from a template while using the user dashboard. Templates have both sample code and a default set of commands associated with them. Templates are loaded based upon the type of stack selected. You can add your own templates to the default Che distribution.

**User**: Anyone interacting with the system.
