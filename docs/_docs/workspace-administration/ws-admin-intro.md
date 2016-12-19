---
tags: [ "eclipse" , "che" ]
title: Workspaces
excerpt: ""
layout: docs
permalink: /:categories/admin-intro/
---
A Che workspace is composed of projects (source files) and environments (runtimes). A workspace can contain one or more environments (e.g. hack environment, populated database environment, etc...) but only one environment at a time can be running in the workspace. An environment is composed of one or more machines. The default machine in an environment is called the "dev-machine" and your projects are mounted or synchronized into that machine so that the software running in the machine can gain access to the source code.  
![WorksapceBasicArchitecture.png]({{ base }}/assets/imgs/WorksapceBasicArchitecture.png)

# Machines  
Environments contain one or more machines. A machine is an abstraction for a single run time, defined by a [recipe](docs:recipes) or [stack](docs:stacks). These docs use the terms "machine" and "workspace runtime" interchangeably.

A machine can have different implementations and our default implementation is to have one Docker container for each machine. Different Che deployments can have different implementations for machines such as localhost or a remote VM.

Machines are instantiated by Che with the stack of software defined by the recipe. Dockerfiles or Docker Compose files, for example, define utilities, frameworks, compilers, and debuggers that should be installed, configured and / or running in the machine(s).  Che then syncs the workspace's projects into the machine so that the software running in the machine can gain access to the source code.  

Any changes made to the project files within the machine is synchronized back to the workspace master out of the machine. This happens through a simple mount.

### Types of Machines
There are two types of machines:
1. A workspace "dev-machine" that is the runtime for the workspace's projects. Che automatically boots and suspends the dev-machine as the workspace is started and stopped.
2. Ancillary machines that provide additional runtime environments for use by projects in the workspace. For example, you may need to launch a database server that is used by projects in the workspace machine. Che will launch additional ancillary machines defined by each user.  The user is responsible for defining, starting and stopping ancillary machines.

A workspace always has one workspace machine, and it can have zero or more ancillary machines.

### Agents for Machine Superpowers
Additionally, Che can inject additional software into a machine through [workspace agents](doc:workspace-agents). Agents can provide machines with special services for the source code. For example, a Java agent runs JDT intellisense inside the machine against the project files and then makes the results available to browsers through REST and websocket communications.
# How It Works  
Docker is the default machine implementation in Che. Each machine is created from a [runtime recipe](doc:recipes) that defines a [runtime stack](doc:stacks). Che provides a variety of ready-to-go stacks that contain common combinations of frameworks needed to develop software projects of various languages. However, the user can create their own [runtime stack](doc:stacks) by defining a Dockerfile or Docker Compose file when creating a new workspace or editing the Che server `stacks.json`.

Che builds a Docker image from the recipe, if an image is not already available. The image can be stored locally on your host, loaded from a private Docker registry, or downloaded from DockerHub. By default, the [provided stacks within Che](https://eclipse-che.readme.io/docs/stacks) will cause Che to download pre-built images from Che's DockerHub repository.

After the image is downloaded, Che runs a container from that image. This running container represents a machine. If it is a workspace dev-machine, a workspace agent (packaged as a Tomcat server) is injected and started.

When a machine is started, Che volume mounts the projects folder on the host, specified by the `CHE_DATA_FOLDER `(default `/home/user/che`) environment variable, into the machine located at `CHE_PROPERTY_che_machine_projects_internal_storage`(default `/projects`). Your workspace projects will be in both the machine and host.
![machine.png]({{ base }}/assets/imgs/machine.png)

# Where To Go From Here  
Che provides a lot of flexibility to administrators and users. You can start by choosing from our stack library that is within the user dashboard.

Or, if you want to distribute your own workspace configuration, study how to add a [Runtime Stack](doc:stacks) or a [Project Sample](doc:templates).

In the [Stacks](doc:stacks) section, we also provide instructions for how to write custom Dockerfiles or Docker Compose files that will act as a workspace recipes.
