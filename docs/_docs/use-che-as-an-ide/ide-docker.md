---
tags: [ "eclipse" , "che" ]
title: Docker
excerpt: "Best practices for using Docker with Che"
layout: docs
permalink: /:categories/docker/
---
{% include base.html %}
Eclipse Che is intimately tied to Docker. The Che server runs as a Docker container and each workspace has a set of environments which may be powered by containers.  Also, our utilities for launching Che, mounting local IDEs, running smoke tests, and compiling Che are also built in Docker.

There are numerous best practices that you can follow to get the most out of Docker when using Che. Also, if your projects depend upon Docker themeslves, there are common questions about Docker in Docker or other similar tactics.
# Using Docker In Your Workspaces  
You may want to use Docker in your workspaces if:
1. You want to build Dockerfiles using commands or in the terminal.
2. You want to launch or manage Docker containers from your workspace.
3. You want to run Che in Che, which allows you to build and run Che from source code using Che.

You can make use of Docker from within your workspaces. You need to have a workspace stack that contains a Docker client and you must configure Che to allow your workspaces to access a Docker daemon,which is usually the one that is already running on your host.

## Docker Daemon Access
There are two ways to configure this.

### Mount Docker Socket
In `che.properties`, add `machine.server.extra.volume=/var/run/docker.sock:/var/run/docker.sock`. This will have your workspace containers perform a volume mount of the Docker socket so that the Docker client in your workspace will use the parent daemon for any Docker CLI commands. If you are using the Che CLI you can set `CHE_PROPERTY_machine_server_extra_volume=/var/run/docker.sock:/var/run/docker.sock` to achieve the same behavior.  You may need to set up permissions for `/var/run/docker.sock` on the host with `sudo chmod 777 /var/run/docker.sock`.

### Configure Your Docker Daemon
Configure your Docker daemon to listen on TCP.  First, add the following to your Docker configuration file (on Ubuntu it's `/etc/default/docker` - see the Docker docs for the location for your OS):
```
DOCKER_OPTS=" -H tcp://0.0.0.0:2375 -H unix:///var/run/docker.sock"
Verify that the Docker API is responding at: http://$IP:2375/containers/json
```
Second, export `DOCKER_HOST` variable in your workspace. You can do this in the terminal or make it permanent by adding `ENV DOCKER_HOST=tcp://$IP:2375` to a workspace recipe, where `$IP` is your local machine IP.   

## Add Docker CLI to Your Workspace
There are many ways to do this. If you are in the terminal, you can follow the instructions to install Docker on the command line at [Docker's web site](https://docs.docker.com/engine/installation/).  You can also have your workspace be created from a stack that already includes the Docker CLI.

The Eclipse Che stack [that ships with Che is based upon the `docker 1.12.0` image](https://github.com/eclipse/che-dockerfiles/blob/master/recipes/alpine_jdk8/Dockerfile#L9) which includes the latest Docker client within the workspace. You can then make use of docker commands in your workspace by using `sudo docker <command>`.

### Get SSH Workspace Public Key
If you are working with a source code versioning system such as github.com or Gitlab you may need to create a private and public keypair in order to clone your repositories within your workspaces.

To achieve this you may use the following command substituting the [CONTAINER ID] for your Eclipse Che workspace instance ID.

First within the Terminal pane of your workspace generate a keypair if one doesn't already exists using the 'ssh-keygen' command.

Once the keypair is created run this command from your docker host.

`$ docker exec [CONTAINER ID] cat /home/user/.ssh/id_rsa.pub`
# Mount Host Volumes Into Workspaces  
You can have folders on your host system mounted and available for access within your workspaces. In `che.properties`, add `machine.server.extra.volume=<host-mount-path>:<ws-mount-path>`. You can have multiple volumes mounted by seaparting them with semicolons.

When your workspace containers are created, they will volume mount this location from the host.

You can also set this using the Che CLI with an environment variable.
`CHE_PROPERTY_machine_server_extra_volume=/var/run/docker.sock:/var/run/docker.sock` to achieve the same behavior.  You may need to set up permissions for `/var/run/docker.sock` on the host with `sudo chmod 777 /var/run/docker.sock`.

```text  
CHE_PROPERTY_machine_server_extra_volume=/c/Users/tyler:/projects/tyler;/c/Users/allsyon:/projects/allyson\
```
