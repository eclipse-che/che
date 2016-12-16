---
tags: [ "eclipse" , "che" ]
title: Usage&#58 Docker Launcher
excerpt: "Run Che inside a Docker container with workspaces saved on your host."
layout: docs
permalink: /:categories/docker/
---
This syntax is the preferred way to start and stop Eclipse Che. It follows the principles of 12 factor apps and has a format that works consistently on every operating system. This technique uses a Docker "launcher" - a Docker container that starts the Che server launched in a second container. This creates a simpler syntax reusable on different operating systems.
# Pre-reqs  
* Eclipse Che 4.6+: [Docker 1.8+](https://www.docker.com/products/docker).
* Eclipse Che 4.5-: see the pre-reqs in running Che as a [Server](doc:usage-docker-server).
# Usage  

```shell  
docker run --rm -t -v /var/run/docker.sock:/var/run/docker.sock eclipse/che:5.0.0-latest [COMMAND]

# WHERE COMMAND IS:
  start     # Starts Che server
  stop      # Stops Che server
  restart   # Restarts Che server
  update    # Pulls latest version of Che image (upgrade)
  info      # Print debugging information\
```

# Start  

```shell  
docker run --rm -t -v /var/run/docker.sock:/var/run/docker.sock eclipse/che:5.0.0-latest start

# YOUR OUTPUT
INFO: ------------------------------------
INFO: ECLIPSE CHE: CONTAINER STARTING
INFO: ECLIPSE CHE: SERVER BOOTING
INFO: ECLIPSE CHE: See logs at "docker logs -f che"
INFO: ECLIPSE CHE: BOOTED AND REACHABLE
INFO: ECLIPSE CHE: http://<your-che-host>:8080
INFO: ------------------------------------
```
The `-v /var/run/docker.sock:/var/run/docker.sock` syntax is needed when starting Eclipse Che so that the Che server (running inside of a container) can communicate to the Docker daemon and ask it to launch workspace containers as peers to the Che server container.

You can monitor the progress of how the server is booting or check for errors in the Eclipse Che server logs. You access these logs in another terminal with `docker logs -f che`. If the server has booted successfully, you should see something similar to the following at the end of the logs.
```shell  
2016-07-15 20:46:24,204[main] [INFO ] [o.a.catalina.startup.Catalina 642] - Server startup in 9052 ms\
```

# Workspace Storage  
Che saves your workspaces, projects and Che internal configuration in `/home/user/che`. This location must be changed if you are on Microsoft Windows by setting the `CHE_DATA_FOLDER` environment variable.
```shell  
docker run --rm -t -v /var/run/docker.sock:/var/run/docker.sock
                   -e CHE_DATA=<path-to-location>
                   eclipse/che:5.0.0-latest start\
```

#### Microsoft Windows Nuances
1. Your storage location needs to be provided in a Docker format. So use `/c/Users/tyler` but not `c:\\Users\\tyler` or `c:\\\\Users\\\\Tyler`.\n2. Windows is case sensitive, so `/c/Users`, but not `/c/users`. \n3. You *must* provide a path that has no spaces. Docker does not handle directories with spaces well. \n4. If you are using boot2docker, then you *must* choose a folder that is a subdirectory of `%userprofile%` which is usually `/c/Users`.  However, if you are using Docker for Windows on Windows 10+, then you can select any directory.  


# Diagnose Boot Problems  
Generally, if Che fails to boot smoothly, then it is most commonly a configuration issue related to how Docker is setup on your operating system. While we try to address all possible configurations within our system, sometimes networking, firewalls, and other items can interfere.

There are [configuration items that you can set to alter how Che](doc:networking) communicates with its workspaces, the browser, and the Docker daemon. There are some simple tests that you can run to diagnose if one of the connections is failing you.

The Che CLI has a number of debugging assistants built into it to provide helpful values during the process of configuring your Che system.
```shell  
# Print CLI debugging info
$ che info
DEBUG: ---------------------------------------
DEBUG: ---------  CHE CLI DEBUG INFO  --------
DEBUG: ---------------------------------------
DEBUG:
DEBUG: ---------  PLATFORM INFO  -------------
DEBUG: DOCKER_INSTALL_TYPE       = boot2docker
DEBUG: DOCKER_HOST_IP            = 192.168.99.100
DEBUG: IS_DOCKER_FOR_WINDOWS     = NO
DEBUG: IS_DOCKER_FOR_MAC         = NO
DEBUG: IS_BOOT2DOCKER            = YES
DEBUG: IS_NATIVE                 = NO
DEBUG: HAS_DOCKER_FOR_WINDOWS_IP = NO
DEBUG: IS_MOBY_VM                = NO
DEBUG:
DEBUG: ---------------------------------------
DEBUG: ---------------------------------------
DEBUG: ---------------------------------------

# Print the Che launcher and server debugging info
DEBUG: ---------------------------------------
DEBUG: ---------  CHE DEBUG INFO  ------------
DEBUG: ---------------------------------------
DEBUG:
DEBUG: ---------  PLATFORM INFO  -------------
DEBUG: DOCKER_INSTALL_TYPE       = boot2docker
DEBUG: DOCKER_HOST_OS            = Boot2Docker 1.12.0 (TCL 7.2); HEAD:e030bab - Fri Jul 29 00:29:14 UTC 2016
DEBUG: DOCKER_HOST_IP            = 192.168.99.100
DEBUG: DOCKER_DAEMON_VERSION     = 1.12.0
DEBUG:
DEBUG:
DEBUG: --------- CHE INSTANCE INFO  ----------
DEBUG: CHE CONTAINER EXISTS      = YES
DEBUG: CHE CONTAINER STATUS      = running
DEBUG: CHE SERVER STATUS         = running
DEBUG: CHE IMAGE                 = eclipse/che-server:nightly
DEBUG: CHE SERVER CONTAINER ID   = 09bde1bdbaec
DEBUG: CHE CONF FOLDER           = not set
DEBUG: CHE DATA FOLDER           = /home/user/che/workspaces
DEBUG: CHE DASHBOARD URL         = http://192.168.99.100:8080
DEBUG: CHE API URL               = http://192.168.99.100:8080/api
DEBUG: CHE LOGS                  = run `docker logs -f che-server`
DEBUG:
DEBUG:
DEBUG: ----  CURRENT COMMAND LINE OPTIONS  ---
DEBUG: CHE_PORT                  = 8080
DEBUG: CHE_VERSION               = nightly
DEBUG: CHE_RESTART_POLICY        = no
DEBUG: CHE_USER                  = root
DEBUG: CHE_HOST_IP               = 192.168.99.100
DEBUG: CHE_LOG_LEVEL             = info
DEBUG: CHE_HOSTNAME              = 192.168.99.100
DEBUG: CHE_DATA_FOLDER           = /home/user/che
DEBUG: CHE_CONF_FOLDER           = not set
DEBUG: CHE_LOCAL_BINARY          = not set
DEBUG: CHE_SERVER_CONTAINER_NAME = che-server
DEBUG: CHE_SERVER_IMAGE_NAME     = eclipse/che-server
DEBUG:
DEBUG: ---------------------------------------
DEBUG: ---------------------------------------
DEBUG: ---------------------------------------

# Run a connectivity test
$ che info --networking
DEBUG:
DEBUG: ---------------------------------------
DEBUG: -------- CHE CONNECTIVITY TEST --------
DEBUG: ---------------------------------------
DEBUG: Browser             => Workspace Agent              : Connection succeeded
DEBUG: Browser (websocket) => Workspace Agent              : Connection succeeded
DEBUG: Che Server          => Workspace Agent (External IP): Connection succeeded
DEBUG: Che Server          => Workspace Agent (Internal IP): Connection succeeded\
```

# Environment Variables  
We provide a range of environment variables that you can set that alters how Che launches from the local host. You can provide as many of these variables as you need to the launcher using the `-e VAR=<value>` syntax.

| Variable   | Description   | Default Values>>>>>>>>>>>   
| --- | --- | ---
| `CHE_DATA`   | Folder where user workspaces and Che preferences are saved.   | `/home/user/che`   
| `CHE_PORT`   | `CHE_VERSION`   | `CHE_RESTART_POLICY`   
| `CHE_USER`   | `CHE_CONF_FOLDER`   | `CHE_LOG_LEVEL`   
| `CHE_HOST_IP`   | External port of Che server.   | `8080`   
| Che server version to boot. Set to `nightly` to get the nightly images and builds of Che.   | `latest`   | Che server restart policy if exited. See [Docker restart policies](https://docs.docker.com/engine/reference/commandline/run/#/restart-policies-restart) for all options.   
| `no`   | User ID of the Che server within its container.   | `root`   
| Folder where custom `che.properties` located.   | null   | Logging level of output for Che server. Can be `debug` or `info`.   
| `info`   | IP address Che server will bind to. Used by browsers to contact workspaces. You must set this IP address if you want to bind the Che server to an external IP address that is not the same as Docker's.   | The IP address set to the Docker host.   
| `CHE_ASSEMBLY`   | The path to a Che assembly that is on your host to be used instead of the binary contained within the `che-server` image.   | `/home/user/che`   
| `CHE_HOSTNAME`   | External hostname of Che server. Only need to set if admin is managing over hostname.   | Varies by OS, but is typically `localhost`.   
| `CHE_LAUNCHER_IMAGE_NAME`\n`CHE_SERVER_IMAGE_NAME`\n`CHE_FILE_IMAGE_NAME`\n`CHE_MOUNT_IMAGE_NAME`\n`CHE_TEST_IMAGE_NAME`\n `CHE_SERVER_CONTAINER_NAME`   | Che uses various Docker images as utilities. Each image provides a different capability and is when launched as a container, given a container name. You can change the image names if you want to use one that you have built locally, or change the name of containers in case you want to launch multiple instances of the same image using different container names. Also consider using the Che CLI profile capability to manage different sets of environment values to run multiple instances of Che with different containers and images.   | `eclipse/che`\n`eclipse/che-server`\n`eclipse/che-file`\n`eclipse/che-mount`\n`eclipse/che-test`\n`che-server`   

Starting in 4.7, you can also define any property typically loaded from `che.properties` using an environment variable. `che.properties` is an internal configuration file that defines advanced ways that Che will configure itself. If an override is not provided by a user, [then Che loads the default file](https://raw.githubusercontent.com/eclipse/che/master/assembly/assembly-wsmaster-war/src/main/webapp/WEB-INF/classes/codenvy/che.properties). There are dozens of additional configuration parameters available. You can change the default value of a property by passing in a variable that matches the name of the property replacing `.` with `_`.  For example, you can pass `-e CHE_WORKSPACE_STORAGE=<value>` to override the location where workspaces are stored.
# Versions and Nightly Builds  
You can specify the version of Eclipse Che you want launched by providing a tag to the `che-launcher` image name. We parse that tag and will invoke a matching `che-server` image with the same label. If you would like the Che server image to be a different version than the launcher, use the `CHE_VERSION` environment variable.
```text  
# Run the nightly version of the launcher and Che server
docker run --rm -t -v /var/run/docker.sock:/var/run/docker.sock \
           eclipse/che-launcher:nightly start

# Run the latest launcher and the 4.7.2 Che server
docker run --rm -t -v /var/run/docker.sock:/var/run/docker.sock \
           -e CHE_VERSION=4.7.2 \
           eclipse/che start\
```

# Local Eclipse Che Binaries  

You can have the launcher use Eclipse Che binaries that you have on your local host instead of those within the Che image that you select. This is useful for extension developers that are building local assemblies and would like to use the launcher to execute their custom Che build.

Inform Che of the local directory where your binaries are stored and the Che image will use those.
```text  
# If your local assembly is located at /home/assembly:
docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \
           -e CHE_ASSEMBLY=path_to_che_sources/assembly/assembly-main/target/eclipse-che-<version>/eclipse-che-<version> \          
           eclipse/che start\
```

# Add Custom Eclipse Che Properties  
There are many customizations you can make to the Che server by altering the `che.properties` file.
```shell  
# Save your custom che.properties in any local folder
/etc/che/che.properties

# Start che notifying the launcher of the custom properties
docker run --rm -t -v /var/run/docker.sock:/var/run/docker.sock \
           -e CHE_CONF_FOLDER=/etc/che \
           eclipse/che:5.0.0-latest start\
```
You can get a template of the latest version of the `che.properties` [file from here](https://github.com/eclipse/che/blob/master/assembly/assembly-wsmaster-war/src/main/webapp/WEB-INF/classes/codenvy/che.properties). You can get the default `che.properties` file for older versions by changing the tag selector in GitHub.

In the `che-server` container launched by the `che-launcher`, the Eclipse Che server loads default properties from `/tomcat/webapps/wsmaster/WEB-INF/classes/codenvy/che.properties`. The Che server overrides those values with `.properties` files that you provide.

System properties and environment variables may be used in `.properties` files in the form `${name}`. For example:
```json  
# If java.io.tmpdir is Java property with value "/tmp" then "/tmp/my-index" is result
index.dir=${java.io.tmpdir}/my-index

# che.home is set as a system property when Che starts
data.dir=${che.home}/conf
```

# Under The Hood  
The Eclipse Che launcher is a Docker container which launches the Che server container. When you run the launcher container, the container executes a script that gathers information about your environment and then uses that information to launch the Che server container.

You can bypass the Che launcher and run the Che server container directly. Note that to run this container directly you must replace the environment variables with your own values. For additional information on running Che server container directly refer to [Usage: Docker Server](https://eclipse-che.readme.io/docs/usage-docker-server).
```shell  
docker run -d --name "${CHE_SERVER_CONTAINER_NAME}" \
    -v /var/run/docker.sock:/var/run/docker.sock \
    -v /home/user/che/lib:/home/user/che/lib-copy \
    ${CHE_LOCAL_BINARY_ARGS} \
    -p "${CHE_PORT}":8080 \
    --restart="${CHE_RESTART_POLICY}" \
    --user="${CHE_USER}" \
    ${CHE_CONF_ARGS} \
    ${CHE_STORAGE_ARGS} \
    "${CHE_SERVER_IMAGE_NAME}":"${CHE_VERSION}" \
                --remote:"${CHE_HOST_IP}" \
                -s:uid \
                -s:client \
                ${CHE_DEBUG_OPTION} \
                run > /dev/null
```

# Docker Unix Socket Mounting vs TCP Mode  
The `-v /var/run/docker.sock:/var/run/docker.sock` syntax is for mounting a Unix socket to allow processes within the container to reach the Docker daemon.

However, peculiarities of file systems and permissions may make it impossible to invoke Docker processes from inside a container. If this happens, the Che startup scripts will print an error about not being able to reach the Docker daemon with guidance on how to resolve the issue.

An alternative solution is to have Docker listen to both TCP and Unix sockets. On the host running the Docker daemon:
```text  
# Set this environment variable and restart the Docker daemon
DOCKER_OPTS=" -H tcp://0.0.0.0:2375 -H unix:///var/run/docker.sock"

# Verify that the Docker API is responding at:
http://<docker-ip>:2375/containers/json
```
Having verified that your Docker daemon is listening, run the Che container with the with `DOCKER_HOST` environment variable set to the IP address of `docker0` or `eth0` network interface. If `docker0` is running on 1.1.1.1 then:
```shell  
docker run --rm -t -v /var/run/docker.sock:/var/run/docker.sock \
           -e DOCKER_HOST=tcp://1.1.1.1:2375 \
          eclipse/che:5.0.0-latest start\
```

# Websockets  
Eclipse Che heavily relies on WebSocket communications. When a browser client connects to a workspace, it connects to it through WebSockets. Inside the workspace, the workspace agent also uses WebSockets to connect back to the Che server.
