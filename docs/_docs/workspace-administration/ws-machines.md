---
tags: [ "eclipse" , "che" ]
title: Runtime Machines
excerpt: ""
layout: docs
permalink: /:categories/machines/
---
A machine is part of an environment, which are in turn part of a Che workspace. The [workspace administration introduction](https://eclipse-che.readme.io/docs/workspace-admin-intro) explains the details of this relationship.

A machine is created from a [runtime stack](doc:stacks). Che supports both single-machine environments and multi-machine environments. The Che server manages the lifecycle of environments and the machines inside, including creating snapshots of machines.  Additionally, the Che server can inject [workspace agents](doc:workspace-agents) into a machine to provide additional capabilities inside the machine.

## Add / Remove Libraries and Tools To Runtime Machines
You can use the terminal and command line to install additional libraries and tools. After you have created a workspace, open a terminal in the IDE.  You can then perform commands like `npm` or `yum install` to add software into your workspace.  These changes will only exist for the lifespan of this single workspace. You can capture these changes permanently by creating a snapshot (which can then be used as a [recipe](https://eclipse-che.readme.io/docs/recipes)), or writing a custom stack that includes these commands in a Dockerfile (this method will make your workspace shareable with others).
![install-jetty8.png]({{ base }}/assets/imgs/install-jetty8.png)
The following example takes a Java ready-to-go stack and adds Jetty8 in the workspace runtime configuration.
```shell  
# apt-get update
sudo apt-get update

# Upgrade existing tools
sudo apt-get upgrade

# Install Jett8
sudo apt-get install jetty8

# Jetty8 installed at path /usr/share/jetty8\
```
## Machine Snapshot
Machines can have their internal state saved into a Docker image with a snapshot.

Snapshots are important to preserve the internal state of a machine that is not defined by the recipe. For example, you may define a recipe that includes maven, but your project may require numerous dependencies that are downloaded and locally installed into the internal maven repository of the machine. If you stop and restart the machine without a snapshot, that internal state will be lost.

Snapshots image a machine and then it is committed, tagged, and optionally pushed into a Docker registry. You can use a local Docker registry or a remote one. See [Configuration](https://eclipse-che.readme.io/docs/configuration#section-workspace-snapshots) for information on how to setup a docker registry.

To snapshot a machine go to the Operations Perspective by clicking the button on the far right of the menu bar. Choose Machines > Snapshot from the top menu bar. See our other docs [for details on setting up a local or remote Docker Registry](https://eclipse-che.readme.io/docs/configuration#section-workspace-snapshots).
![che-create-snapshot.jpg]({{ base }}/assets/imgs/che-create-snapshot.jpg)
By default, Che does not need a local/remote Docker registry to create snapshots. If no registry is used, a container is committed into an image which is then tagged, so that next time a workspace is started with this image. The behavior is regulated with the following environment variables:
```shell  
# Windows use `set` in lieu of `export`

# If or not to use a Docker registry to push and pull snapshots
export CHE_PROPERTY_machine_docker_snapshot__use__registry=false

# enable/disable auto snapshotting and auto restoring from a snapshot
export CHE_PROPERTY_workspace_runtime_auto__snapshot=true
export CHE_PROPERTY_workspace_runtime_auto__restore=true\
```
## Machine Information
Information on each machine is provided by Eclipse Che server.

### Operations Perspective Machine Information
Information on each machine can be viewed in the operations perspective in the IDE. You can toggle between the code perspective and operations perspective by clicking the pair of buttons in the top-right of the menu bar.

The operations perspective provides general meta information such as name, status, id, etc. Also, there is externally exposed machine port information in the server tab section. Exposed ports are used in in various ways such as [priview urls](https://eclipse-che.readme.io/docs/run#section-preview-url), allowing SSH into a machine, debug server ports, and various ports used by Eclipse Che server to interact with the machine.
![Che-machine-information.jpg]({{ base }}/assets/imgs/Che-machine-information.jpg)
### Dashboard Machine Information

Information on each machine can be viewed in the in the `Dashboard`. Information on each machine in a workspace can be viewed after clicking on the workspace name, selecting the `Runtime` tab, and clicking the expand button located next to each machine. Machine information includes source, ram, [agents](doc:workspace-agents), exposed ports, and environment variables. All of these configuration items can be changed on a stopped workspace/machine. If the workspace/machine is running, the workspace will be stopped automatically. The saved configuration items will be used the next time the work space is started and will supersede any values given in the original workspace stack. Changes made to the runtime configuration will only effect the workspace and will not be saved to original stack to be create other workspaces from.
![Che-machine-information-edit.jpg]({{ base }}/assets/imgs/Che-machine-information-edit.jpg)
