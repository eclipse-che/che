---
tags: [ "eclipse" , "che" ]
title: Usage&#58 Native Server
excerpt: "Running Che server natively provides the best performance"
layout: docs
permalink: /:categories/native/
---
This is advanced syntax intended for developers that are building extensions for Che. The environmental setup requires Java and Docker - please see the pre-reqs for different operating systems at the end of this page.

If you are behind a proxy see our [Configuration: Proxies](doc:configuration-proxies) docs.
# Start  

#### UID=1000
On Linux systems Che must run under a user with UID=1000 to properly run the workspace containers. Run `id` command to get your UID settings.\n\nIf you user is not UID=1000 you can skip the check by using -`-skip:uid` on the command line or create a new user with the proper uid and add it to docker group.\n\nIn Ubuntu this can be achieved by running `adduser mynewuser` followed by adding the user to the `docker` group with `usermod -aG docker mynewuser` and then changing to the new user with `su - newuser`.  


```shell  
# In a terminal in the Che home directory.
# Windows:
bin/che run

# Mac / Linux:
bin/che.sh run\
```
After starting, Che is available at:
```http  
http://localhost:8080\
```

```http  
/                   # Loads dashboard or last opened workspace, based upon user config
/dashboard          # Loads dashboard for managing projects and workspaces
/che/<wsname>       # Loads specific workspace\
```

# Command Line  

```text  
Usage:
  che [OPTIONS] [COMMAND]
     -m:name,   --machine:name       For Win & Mac, sets the docker-machine VM name; default=default
     -p:port,   --port:port          Port that Che server will use for HTTP requests; default=8080
     -r:ip,     --remote:ip          If Che clients are not localhost, set to IP address of Che server
     -h,        --help               Show this help
     -d,        --debug              Use debug mode (prints command line options + app server debug)

  Options when running Che natively:
     -b,        --blocking-entropy   Security: https://wiki.apache.org/tomcat/HowTo/FasterStartUp
     -g,        --registry           Launch Docker registry as a container (used for ws snapshots)
     -s:client, --skip:client        Do not print browser client connection information
     -s:java,   --skip:java          Do not enforce Java version checks
     -s:uid,    --skip:uid           Do not enforce UID=1000 for Docker

  Commands:
     run                             (Default) Starts Che server with logging in current console
     start                           Starts Che server in new console
     stop                            Stops Che server\
```
`che.bat` checks for the installation of `bash` and executes `che.sh`. The default parameters for `che.sh` include: running Che as a native server (not as a Docker image), launching on port 8080, and executing the `run` server action.

Using `-i` will cause the Che server to launch itself in a Docker container. Otherwise, Che will start itself on the native operating system. Either way, once the Che server has been started, that server also needs access to create its own Docker containers.  The native Che server will create containers using the avialable Docker daemon.  The Che docker image will create its dependent Docker containers using the same daemon.

If you launch Che as a Docker image, you can choose specific version of Che by passing in the version using the `tag` attribute. We support `latest`, `nightly`, and `version`.
# Stop  

```shell  
bin/che stop\
```

# Troubleshoot  
Errors will appear as exceptions in `stdout` of Tomcat when you run it. Che saves standard and error output to `/logs/catalina.out`.

### Too Many Files Opened
This means that Docker is not properly configured.  For MacOS or Windows, [start a Docker machine](doc:install-pre-reqs#docker) instance and apply its environment variables to your shell before starting Che.

### Port Already In Use
Che runs on a server that expects port 8080 to be configured. If you get this error, the port that Che is connecting to is already being used. Use the `-p:port` option when starting Che to change the port being used.

### Cannot Create Projects
On Linux, Che must be launched with user ID=1000. The workspaces created by Che are launched in Docker containers that create an internal container user with ID=1000. If the Che server is launched with a user that does not have ID=1000 then your Che users will not have write permissions to the projects running within the workspace. This issue appears if you run Che as a sudo user or if you have set up special user accounts.  You can launch Che with `--skip:uid` to ignore this check. However, you will need to create custom stacks that have their internal container users match the UID of your Linux installation.

### Che Server Takes Long Time to Load
If you are running on Digital Ocean or other providers, there are additional security configurations which [may be necessary.](https://www.digitalocean.com/community/questions/fresh-tomcat-takes-loong-time-to-start-up)

# Pre-reqs: Windows  
###Java
Java is used to run Che's server and the SDK tools which are used to create and package plug-ins. Install [Java 1.8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).  Java JRE is enough to run Che. You need the Java JDK if you want to build Che from source.

The `JAVA_HOME` environment variable should be exported in the system and point to a valid Java installation.

Windows users can either set these variables for a current terminal session or export them system wide. See: [Setting JAVA_HOME on Windows](https://confluence.atlassian.com/doc/setting-the-java_home-variable-in-windows-8895.html).

###Docker
[Docker](https://get.docker.com/) will install `docker`, `docker-machine`, Git bash, and other utilities required to operating Docker.

#### BIOS Virtualization Enabled
}  

Che depends upon Docker and will not start if it cannot verify Docker running. If you have Docker Toolbox installed, Che will create a VM using VirtualBox that is running Docker (this is unusualy now that Docker has released Docker for Windows). The script is performing the equivalent of the following. You can verify that VirtualBox is working properly if the following commands complete execution without an error.
```shell  
# Start the Docker Machine
docker-machine start default

# If you get an error, you may need to create a machine named 'default'
docker-machine create --driver virtualbox default

# After the machine is created, add ENV variables to your shell
# This command will give you instructions of what to do
# MacOS
docker-machine env default

# Windows
docker-machine env --shell cmd default\
```
###Git Bash
On Windows, `bash` is required to launch the Che server. This is typically installed with Git Windows, which is installed by Docker Toolbox for Windows, a requirement for using Che.
# Pre-reqs: Mac / Linux  
###Java
Set `$JAVA_HOME` variable in the `~/.bash_profile` or `~/.profile` files.
```text  
export JAVA_HOME=/path/to/Java
export PATH=$JAVA_HOME/bin:$PATH\
```

# Networking  
This page provides a how-to and important configuration properties that you must review to successfully set up Eclipse Che as a hosted server for remote clients.

This page is a companion to the [Configuration: Networking](doc:configuration-networking) page which specifies all of the properties and workflow that Che provides related to networking.  
![che_networking.png]({{ base }}/assets/imgs/che_networking.png)
This graphic provides a logical framework of the various components that have to be configured to be reachable to one another in a server scenarios. This document reviews the connection points between each component.

## DNS
If your Che server has a configured DNS, you need to ensure that this DNS is reachable from the browser client and from within the workspace container. If the workspace container is running within a VM, then the DNS entry must also be reachable from within the VM.

## Che Server => Workspace Agent
Che has an algorithm for retrieving the workspace agent URL that includes properties, environment variables and default values.

If Che is launched in a VM and `DOCKER_HOST` or `CHE_DOCKER_MACHINE_HOST` variables have not been exported, the Che server will try to reach workspace agent at `localhost:32768-65535`. In this case, make sure `iptables` allows local connections for this port range.

If `DOCKER_HOST` or `CHE_DOCKER_MACHINE_HOST` are exported and point to an external IP, make sure `32768-65535` port range is open for inbound connections since Che server will use this IP (or DNS) to reach the workspace agent.

## Local Clients (Browser) => Workspace Agent
After the Che server has successfully pinged a workspace agent, the client will use the same endpoint to establish its connection with a workspace agent as well over `http` and `ws`.

If you have customized `docker.client.daemon_url` or `machine.docker.local_node_host`, or you have exported `DOCKER_HOST` or `CHE_DOCKER_MACHINE_HOST`, the browser will use those provided values to connect to a workspace agent.

## Workspace Agent => Che Server
When a workspace is coming online, it must verify that it can connect back to the Che server. The `che.properties` file has the property `machine.docker.che_api.endpoint` that identifies the location of the Che server APIs. The default value for this property uses a `che-host` alias. When a workspace container starts, Che writes an entry into the container's `/etc/hosts` file to map `che-host` to the IP address of the Che server.  For example:
```text  
172.17.0.1	che-host\
```
In most cases, `che-host` maps to the IP address of the `docker0` network interface. If Che fails to retrieve the IP address for this interface, then IP defaults that vary by operating system type are used.  

Make sure to check that the Che server's port is opened on the server's `iptables`. It is common that these ports would be closed and this would prevent the workspace agent from connecting back to the Che server, even though your browser clients can connect to the Che server.

###Docker
