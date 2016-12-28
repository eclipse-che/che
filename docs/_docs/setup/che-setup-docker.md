---
tags: [ "eclipse" , "che" ]
title: Docker Installation
excerpt: "Run the Che server with Docker bypassing the CLI with workspaces mounted on your host."
layout: docs
permalink: /:categories/docker/
---
{% include base.html %}
You can run the Che server directly by launching a Docker image. This approach bypasses the CLI, which has additional utilities to simplify administration and operation. The `eclipse/che-server` Docker image is appropriate for running Che within clusters, orchestrators, or by third party tools with automation.

# Run the Image  
```shell  
# Run the latest released version of Che
# Replace <path-for-data> with any host folder
# Che will place backup files there - configurable properties, workspaces, lib, storage
docker run -p 8080:8080 \
           --name che \
           --rm \
           -v /var/run/docker.sock:/var/run/docker.sock \
           -v <path-for-data>:/data \
           eclipse/che-server:5.0.0-latest

# To run the nightly version of Che, replace eclipse/che-server:5.0.0-latest with
eclipse/che-server:nightly

# To run a specific tagged version of Che, replace eclipse/che-server:5.0.0-latest with
eclipse/che-server:<version>

# Stop the container running Che
docker stop che

# Restart the container running Che and restart the Che server
docker restart che

# Upgrade to a newer version
docker pull eclipse/che-server:5.0.0-latest
docker restart che\
```
Che has started when you see `Server startup in ##### ms`.  After starting, Che is available at `localhost:8080` or a remote IP if Che has been started remotely.  The examples on this page assume that I am running this container on a Windows machine and I would like my workspaces to be saved in `C:\tmp`.  Note that on Windows, volume mounts require Linux-formatted syntax, for which they are here.

## SELinux
If you are on SELinux then run this instead:
```shell  
# Run the latest released version of Che
docker run -p 8080:8080 \
           --name che \
           -v /var/run/docker.sock:/var/run/docker.sock \
           -v /C/tmp:/data:Z \
           eclipse/che-server:5.0.0-latest\
```
## Ports
Tomcat inside the container will bind itself to port 8080 by default. You must map this port to be exposed in your container using `-p 8080:8080`.  If you want to change the port at which your browsers connect, then change the first value, such as `p 9000:8080`.  This will route requests from port 9000 to the internal Tomcat bound to port 8080.  If you want to change the internal port that Tomcat is bound, you must update the port binding and set `CHE_PORT` to the new value.
```text  
docker run -p 9000:9500 \
           --name che \
           -e CHE_PORT=9500 \
           -v /var/run/docker.sock:/var/run/docker.sock \
           -v /C/tmp:/data \
           eclipse/che-server:5.0.0-latest\
```
## Configuration
Most important configuration properties are defined as environment variables that you pass into the container. You can also optionally pass in a custom `che.properties` which has internal Che configuration that is loaded when Che's tomcat is booted.  For example, to have Che listen on port 9000:
```shell  
docker run -p:9000:9000 \
           --name che \
           -e CHE_SERVER_ACTION=stop \
           -v /var/run/docker.sock:/var/run/docker.sock \
           -v /C/tmp:/data \
           eclipse/che-server:5.0.0-latest\
```
There are many variables that can be set.

| Variable   | Description   | Defaults>>>>>>>>>>>   
| --- | --- | ---
| `CHE_SERVER_ACTION`   | The command to send to Tomcat. it can be [run | start | stop].   | `run`   
| `CHE_LOCAL_CONF_DIR`   | Folder where a custom `che.properties` located. If not provided, then the container will copy the system `che.properties` into `/data/conf/che.properties`. You can reuse this or replace it  with configuration on a different folder using this variable. If you use this variable, you must also mount the same directory.   | `/data/conf`   
| `CHE_ASSEMBLY`   | The path to a Che assembly that is on your host to be used instead of the assembly packaged within the `che-server` image. If you set this variable, you must also volume mount the same directory to `/home/user/che`   | `/home/user/che`   
| `CHE_IN_VM`   | Set to 'true' if this container is running inside of a VM providing Docker such as boot2docker, Docker for Mac, or Docker for Windows. We auto-detect this for most situations, but it's not always perfect.   | auto-detection   
| `CHE_LOG_LEVEL`   | Logging level of output for Che server. Can be `debug` or `info`.   | `info`   
| `CHE_IP`   | IP address Che server will bind to. Used by browsers to contact workspaces. You must set this IP address if you want to bind the Che server to an external IP address that is not the same as Docker's.   | The IP address set to the Docker host. This does cover 99% of situations, but on rare occassions we are not able to discover this IP address and you must provide it.   
| `CHE_DEBUG_SERVER`   | If `true`, then will launch the Che server with JPDA activated so that you a Java debugger can attach to the Che server for debugging plugins, extensions, and core libraries.   | `false`   
| `CHE_DEBUG_SERVER_PORT`   | The port that the JPDA debugger will listen.   | `8000`   
| `CHE_DEBUG_SERVER_SUSPEND`   | If `true`, then activates `JPDA_SUSPEND` flag for Tomcat running the Che server. Used for advanced internal debugging of extensions.   | `false`   
| `CHE_PORT`   | The port the Che server will bind itself to within the Che container.   | '8080`   

## Run Che on Public IP Address
If you want to have remote browser clients connect to the Che server (as opposed to local browser clients) and override the defaults that we detect, set `CHE_IP` to the Docker host IP address that will have requests forwarded to the `che-server` container.

We run an auto-detection algorithm within the che-server container to determine this IP.  If Docker is running on `boot2docker` this is usually the `eth1` interface. If you are running Docker for Windows or Docker for Mac this is usually the `eth0` interface. If you are running Docker natively on Linux, this is the `docker0` interface. If your host that is running Docker has its IP at 10.0.75.4 and you wanted to allow remote clients access to this container then:
```shell  
docker run -p:8080:8080 \
           --name che \
           -e CHE_IP=10.0.75.4 \
           -v /var/run/docker.sock:/var/run/docker.sock \
           -v /C/tmp:/data \
           eclipse/che-server:5.0.0-latest\
```
## Run Che as a Daemon
Pass the `--restart always` parameter to the docker syntax to have the Docker daemon restart the container on any exit event, including when your host is initially booting. You can also run Che in the background with the `-d` option.
```shell  
docker run -p:8080:8080 \
           --name che \
           --restart always \
           -e CHE_IP=10.0.75.4 \
           -v /var/run/docker.sock:/var/run/docker.sock \
           -v /C/tmp:/data \
           eclipse/che-server:5.0.0-latest\
```
## Override Che Internals
Eclipse Che running in the container has its configuration in `/data/conf/che.properties`. This file is copied into the directory that you mount into `/data`. You can modify this file and restart Che if you want.

You can also start a shell session in the context of the running container. This will allow you to browse all directories and use your favorite text editor.

```shell  
sudo docker exec -it che /bin/bash

# You can also just edit /home/user/che/conf/che.properties`:
sudo docker exec -it che vi /data/conf/che.properties

# After you finish making changes, restart the contain
docker restart che\
```
You can save your Che configuration to reside outside of the container to be reusable between different container instances. Create a local `che.properties` file, volume mount it into the container, and also tell Che in the container where to locate this file.
```shell  
# Mount host directory with che.properties into container and inform Che where it is.
# Place the folder with che.properties as a mount to /conf
docker run -p:8080:8080 \
           --name che \
           --restart always \
           -e CHE_IP=10.0.75.4 \
           -v /var/run/docker.sock:/var/run/docker.sock \
           -v /C/tmp:/data \

           -v /C/conf:/conf \

           eclipse/che-server:5.0.0-latest\
```
## Use Local Che Assembly
You can run the Che image with a Che assembly that you have installed (or compiled) locally on your host. If you are developing a custom assembly, extension, or developing Che, you can mount your local binaries built on your host into the container by mounting the assembly to `/assembly`.
```text  
# If your local assembly is located at /home/my_assembly:
docker run -p:8080:8080 \
           --name che \
           --restart always \
           -v /var/run/docker.sock:/var/run/docker.sock \
           -v /C/tmp:/data \
           -v /C/conf:/conf \

					 -v /home/my_assembly:/assembly \

           eclipse/che-server:5.0.0-latest\
```
## Port Exposure
Docker uses the ephemeral port range from `32768-65535`.  If you do not want to expose ports `32768-65535` because of the large port range, you need to reconfigure your Docker daemon to communicate over the `docker0` interface. You need to set  `DOCKER_MACHINE_HOST` to `172.17.0.1` and modify `machine.docker.che_api.endpoint` in the  `che.properties` file.
```shell  
# Modify your local che.properties with:
machine.docker.che_api.endpoint=http://172.17.0.1:8080/wsmaster/api

# Run the Che container with:
docker run -p:8080:8080 \
           --name che \
           --restart always \
           -v /var/run/docker.sock:/var/run/docker.sock \
           -v /C/tmp:/data \
           -v /C/conf:/conf \           
					 -v /home/my_assembly:/assembly \

           -e DOCKER_MACHINE_HOST=172.17.0.1 \

           eclipse/che-server:5.0.0-latest\
```

```yaml  
che:
   image: eclipse/che-server:5.0.0-latest
   port: 8080:8080
   restart: always
   environment:
     CHE_LOCAL_CONF_DIR=/C/conf
     CHE_ASSEMBLY=/home/my_assembly
     DOCKER_MACHINE_HOST=172.17.0.1
   volumes:
     - /var/run/docker.sock:/var/run/docker.sock
     - /C/tmp:/data
     - /C/conf:/conf
		 - /home/my_assembly:/assembly
   container_name: che\
```
Save this into a file named `Composefile`. Replace the `${IP}` with the IP address of the server where you are hosting Eclipse Che.  You can then run this with Docker Compose with `docker-compose -f Composefile -d`.
# Docker Unix Socket Mounting vs TCP Mode  
The `-v /var/run/docker.sock:/var/run/docker.sock` syntax is for mounting a Unix socket so that when a process inside the container speaks to a Docker daemon, the process is redirected to the same socket on the host system.

However, peculiarities of file systems and permissions may make it impossible to invoke Docker processes from inside a container. If this happens, the Che startup scripts will print an error about not being able to reach the Docker daemon with guidance on how to resolve the issue.

An alternative solution is to run Docker daemon in TCP mode on the host and export `DOCKER_HOST` environment variable in the container.  You can tell the Docker daemon to listen on both Unix sockets and TCP.  On the host running the Docker daemon:
```text  
# Set this environment variable and restart the Docker daemon
DOCKER_OPTS=" -H tcp://0.0.0.0:2375 -H unix:///var/run/docker.sock"

# Verify that the Docker API is responding at:
http://localhost:2375/containers/json
```
Having verified that your Docker daemon is listening, run the Che container with the with `DOCKER_HOST` environment variable set to the IP address of `docker0` or `eth0` network interface. If `docker0` is running on 1.1.1.1 then:
```shell  
docker run -p:8080:8080 \
           --name che \
           --restart always \
           -v /var/run/docker.sock:/var/run/docker.sock \
           -v /C/tmp:/data \
           -v /C/conf:/conf \           
					 -v /home/my_assembly:/assembly \
           -e DOCKER_MACHINE_HOST=172.17.0.1 \

           -e DOCKER_HOST=tcp://1.1.1.1:2375 \

           eclipse/che-server:5.0.0-latest\
```

# Where Is Data Stored?  
The Che container uses host mounted volumes to store persistent data:

| Local Location   | Container Location   | Usage   
| --- | --- | ---
| `/var/run/docker.sock`   | `/var/run/docker.sock`   | This is how Che gets access to Docker daemon. This instructs the container to use your local Docker daemon when Che wants to create its own containers.   
| `/<your-path>/lib`   | `/data/lib`   | Inside the container, we make a copy of important libraries that your workspaces will need and place them into `/lib`. When Che creates a workspace container, that container will be using your local Docker daemon and the Che workspace will look for these libraries in your local `/lib`. This is a tactic we use to get files from inside the container out onto your local host.   
| `/<your-path>/workspaces`   | `/data/workspaces`   | The location of your workspace and project files.   
| `/<your-path>/storage`   | `/data/storage`   | The location where Che stores the meta information that describes the various workspaces, projects and user preferences.   


# Debugging  
Inside the Che container is a Tomcat service. The Tomcat debugger is running on port 8000. If you want to connect to that port, you need to bind it as part of the docker run command.
```shell  
docker run -p 8080:8080 \
           -p 9000:8000 \
           --name che \
           --restart always \
           -v /var/run/docker.sock:/var/run/docker.sock \
           -v /C/tmp/:/data \
           eclipse/che-server:5.0.0-latest\
```
