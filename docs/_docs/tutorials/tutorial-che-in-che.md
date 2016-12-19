---
tags: [ "eclipse" , "che" ]
title: Che in Che
excerpt: "There is nothing like a little Che inception to brighten your day."
layout: tutorials
permalink: /:categories/che-in-che/
---
You can build and run Che using Che!  Since Che runs within a Docker container and its workspaces are generated as Docker containers, there is extra configuration that is needed to enable the Che that you build in Che to generate its own workspaces!
# Concepts  
We are going to setup Che to have the Che launcher, the Che server, your primary development workspace, and the new Che you will compile be Docker containers. All of these Docker containers will be launched and managed by a shared Docker daemon, which is running on your host system.
![Capture.PNG]({{ base }}/assets/imgs/Capture.PNG)
* **Native**: The CLI that launches your main instance of Che with the `che-launcher`.
* **Che Launcher**: A Docker container, which provides cross-platform management of your Che server.
* **Che Server**: A Docker container running your primary Che server.
* **Che Workspace**: A Docker container which containers your development workspace. The Che source code is cloned into this workspace, compiled here, and acts as the launch point. It includes the CLI that launches the launcher (creating a type of recursive behavior). The new inner Che will be able to launch its own workspaces.

All of these containers share a common Docker daemon that is running on the host operating system. This means that even though we are doing Che-in-Che (or why not Che-in-Che-in-Che), all of the containers created are siblings of one another managed by the same daemon.
# Step By Step Guide  
### Configure Che
There are two values that you must set as environment variables. This requires the 4.7 CLI - previous versions of the CLI do not support converting environment variables into Che properties.
```text  
# This will create a che.properties value
export CHE_PROPERTY_machine_server_extra_volume=/var/run/docker.sock:/var/run/docker.sock

# Set CHE_DATA_FOLDER to a directory that you will remember.
# This value will be needed inside of the workspace
export CHE_DATA_FOLDER=/Users/tyler/data\
```
### Start Che
```shell  
# Start Che
che start\
```
Now, all workspaces started in this Che server will have access to the host's Docker daemon. Because workspaces will share access to the host daemon, be careful with sharing workspaces in this configuration. It's possible that workspaces can send commands to the daemon that gain privileges to the host that you may not want to give.

### Get a Che Workspace
1: Create a workspace using the Eclipse Che ready-to-go stack. It will have the Che logo next to it. This stack is based upon Alpine and [includes Java 8, maven, and the Docker client](https://github.com/eclipse/che-dockerfiles/tree/master/recipes/alpine_jdk8).

2: Choose the Eclipse Che template. This will clone source from `http://github.com/eclipse/che` and give the resulting project a `maven` project type. Setting the type to maven will trigger dependencies being downloaded. It's a big project - so it will take a moment to finish the clone and create the workspace.

4: Project > Set Configuration.  Set the project configuration to be `maven`. This will add Java intellisense and the maven plugin so that your dependencies are managed properly.

5:  Compile Che by adding a maven command.  In the toolbar, choose "Edit Commands...".  Create a new maven command.  Set the working directory to be /che/assembly/assembly-main.  The command should be `clean install`.

### Run Che-in-Che
Now that you have a compiled Che binary, you need to run it.  We will use the Che Launcher Docker container to run the binary. Your workspace project has all of its files mounted onto the host. So while you see the files inside your workspace, they are also running on the host - where the Docker daemon is.

We will launch the Che Launcher from inside the workspace, but pass environment variables that allow the launcher to create a new Che server on the host, and that new Che server will be started with the binaries that you just compiled (also on the host).
```shell  
# First, find the location where Che built itself in your workspace.
cd /projects

# Replace <proj-name> and <version> with your setup
set MY_CHE_BINARY="/<proj-name>/assembly/assembly-main/target/<version>/<version>"

# Your che-in-che will launch itself with the name "che-server"
# Temporary fix - to avoid collisions, rename your dev server container.
docker rename che-server che-primary-server

# Create a new data directory.
mkdir /home/user/che-did

# Inside your Che workspace, launch Che-in-Che with:
sudo docker run --rm -t -v /var/run/docker.sock:/var/run/docker.sock
                --env CHE_LOCAL_BINARY=/home/user/che/workspaces/<ws-name>$MY_CHE_BINARY
                --env CHE_DATA_FOLDER=/home/user/che-did
                --env CHE_HOST_IP=$(curl -s https://4.ifcfg.me/)
                --env CHE_PORT=50000
                   eclipse/che start

# NOTE: Set the CHE_PORT to any value >33,000
# NOTE: Set the CHE_PORT to a value not used by your primary Che server or workspace
# NOTE: CHE_HOST_IP only necessary if you are running Che as a shared server

# CHE-IN-CHE will be running at http://$CHE_HOST_IP:50000
```
