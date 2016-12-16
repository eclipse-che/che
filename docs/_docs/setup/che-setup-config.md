---
tags: [ "eclipse" , "che" ]
title: Configuration
excerpt: "Configure Che to bend to your will."
layout: docs
permalink: /:categories/config/
---
There is a lot you can do with Eclipse Che by changing its configuration. You can do this in three ways depending on how you interact with Che:
1. [Che CLI](https://eclipse-che.readme.io/docs/configuration-bdm-alternative#configure-che-using-the-che-cli): The CLI makes it easy to change any property.
2. [Che Launcher Environment Variables](https://eclipse-che.readme.io/docs/usage-docker-server#section-pass-che-command-line-options): You can change properties in Che by passing environment variables into the commands in the che launcher.
3. [Che Server Properties File](https://eclipse-che.readme.io/docs/configuration-bdm-alternative#configure-che-using-the-che-server): Che uses a properties file that can be altered directly if you're not using the CLI or launcher. This method is not recommended as it's the most error prone and complicated.
#### Eclipse Che Properties File
Throughout this document we reference the embedded `che.properties` file. You can [view this file and its defaults](https://github.com/eclipse/che/blob/08344fe62ebedfaa199e3258279b29acec7c88f8/assembly/assembly-wsmaster-war/src/main/webapp/WEB-INF/classes/codenvy/che.properties) in our repo.  


# Configure Che Using the CLI  
If you are using the [Che CLI](https://eclipse-che.readme.io/docs/che-cli) to start / stop Che, you can configure the `che-launcher` and `che-server` through environment variables. This is the preferred and simplest way to configure Che.

Some values configure the CLI, some the `che-launcher`, and others the Che server. The CLI will also generate a one-time use `che.properties` from any parameters provided as environment variables.

## Environment Variables
You can set environment variables that will affect the behavior of the CLI. These values are also used to configure how the `che-launcher` will be called and passed along as an input into the `che-launcher`.  You can also [set a Che CLI profile](https://eclipse-che.readme.io/docs/che-cli#profiles) which will store a set of environment variables for use during other calls so that you can have multiple sets of configurations.


| Variable   | Description   | Defaults>>>>>>>>>>>   
| --- | --- | ---
| `CHE_PORT`   | External port of Che server.   | `8080`   
| `CHE_HOSTNAME`   | `CHE_CONF_FOLDER`   | `CHE_RESTART_POLICY`   
| `CHE_USER`   | External hostname of Che server. Only need to set if admin is managing over hostname. Changing this should be rare.   | Varies by OS, but is typically `localhost`.   
| Folder where custom `che.properties` located.   | `null`   | Che server restart policy if exited. See [Docker restart policies](https://docs.docker.com/engine/reference/commandline/run/#/restart-policies-restart) for all options.   
| `no`   | User ID of the Che server within its container.   | `root`   
| `CHE_LOCAL_BINARY`   | The path to a Che assembly that is on your host to be used instead of the binary contained within the `che-server` image.   | `/home/user/che`   
| `CHE_VERSION`   | Che server version to boot. Can be any valid [Che tag](https://github.com/eclipse/che/tags). Set to `nightly` to get the nightly images and builds of Che.   | `latest`   
| `CHE_SERVER_CONTAINER_NAME`   | Changes the pretty-name of the container used to start the Che server. Change the name if you want to have multiple Che servers running as containers at the same time.   | `che-server`   
| `CHE_DATA_FOLDER`   | Folder where user workspaces and Che preferences are saved.   | `/home/user/che`   
| `CHE_LOG_LEVEL`   | Logging level of output for Che server. Can be `debug` or `info`.   | `info`   
| `CHE_LAUNCHER_IMAGE_NAME`\n`CHE_SERVER_IMAGE_NAME`\n`CHE_FILE_IMAGE_NAME`\n`CHE_MOUNT_IMAGE_NAME`\n`CHE_TEST_IMAGE_NAME`   | Che uses various Docker images as utilities. Each image provides a different capability and is when launched as a container, given a container name. You can change the image names if you want to use one that you have built locally   | `eclipse/che`\n`eclipse/che-server`\n`eclipse/che-file`\n`eclipse/che-mount`\n`eclipse/che-test`   
| `CHE_IS_INTERACTIVE`   | Set to `false` if you want the CLI to work in environments where there is no interactivivity available, such as part of a provisioner within Vagrant.   | `true`   
| `CHE_PRODUCT_NAME`\n`CHE_MINI_PRODUCT_NAME`   | Sets the miniature and full product names used by the utilities in their output to the console.   | `ECLIPSE CHE`\n`che`   
| `CHE_HOST_IP`   | IP address Che server will bind to. Used by browsers to contact workspaces. You must set this IP address if you want to bind the Che server to an external IP address that is not the same as Docker's.   | The IP address set to the Docker host.   
| `CHE_IS_PSEUDO_TTY`   | Set to 'false' if you want the CLI to work without a PSEUDO_TTY, necessary for products like Jenkins.   | `true`   
| `CHE_DEBUG_SERVER`   | If `true`, then will launch the Che server with JPDA activated so that you a Java debugger can attach to the Che server for debugging plugins, extensions, and core libraries.   | `false`   
| `CHE_DEBUG_SERVER_PORT`   | The port that the JPDA debugger will listen.   | `8000`   
| `CHE_DEBUG_SERVER_SUSPEND`   | If `true`, then activates `JPDA_SUSPEND` flag for Tomcat running the Che server. Used for advanced internal debugging of extensions.   | `false`   
| `CHE_DOCKER_MACHINE_HOST_EXTERNAL`   | The hostname a browser should use to ping a workspace, if that workspace is not directly pingable by the browser. The default configuration will have the browser use the same hostname and IP address to reach the Che server and each workspace. In some cases, like Docker for Mac which is in a VM, the Che server can use an internal IP to reach the workspace, while the browser needs to use a separate, external hostname. It would be rare to change this.   | `localhost` if on Docker for Windows or Mac. Otherwise, `\\`.   
| `CHE_EXTRA_VOLUME_MOUNT`   | Folder(s) to mount from your host into the workspace. Each mount is seperated by a semicolon `;`. \nExample:\n`~/.ssh:/home/user/.ssh;~/.m2:/home/user/.m2`   | `null`   
| `CHE_UTILITY_VERSION`   | `null`   | The version of the che utility to use.   

## One Time Use Properties
There are a number of internal properties that define how the Che server will operate. These are typically provided in a `che.properties` file.  If you are using the CLI, you can provide a one-time use `che.properties` file by defining additional environment variables. The CLI will create this file and pass it along to the launcher and server.

Any variable with the format `CHE_PROPERTY_<name>=<value>` will be configured into property value. A single underscore `_` will be converted into a period `.`.  A double underscore `__` will be converted into a single underscore `_`.  For example:
```
# This environment variable:
export CHE_PROPERTY_machine_ws__agent_max__start__time__ms=1000

# Is placed into the temporary che.properties as:
machine.ws_agent.max_start_time_ms=1000
```

## Custom Properties File
Eclipse Che has an embedded `che.properties` file [which provides a set of defaults](https://github.com/eclipse/che/blob/08344fe62ebedfaa199e3258279b29acec7c88f8/assembly/assembly-wsmaster-war/src/main/webapp/WEB-INF/classes/codenvy/che.properties).

If you set the `CHE_LOCAL_CONF` environment variable, it will point to a directory name that can have any number of `*.properties` files. The values in these files will be passed along to the server instead of any values passed as environment variables.

## Other Customizations
Additionally, there are a number of other areas you can customize inside Che through [System properties](https://eclipse-che.readme.io/docs/configuration#configure-che-using-the-che-server)
# Configure Che Using the Che Launcher  
If you are using our `docker run eclipse/che start` or `docker run eclipse/che-launcher start` syntax, there is a smaller set of environment variables that are available and all `che.properties` must be provided in a custom file.  See [Usage: Docker Launcher](https://eclipse-che.readme.io/docs/usage-docker) for specifics.

Additionally, there are a number of other areas you can customize inside Che through [System properties](https://eclipse-che.readme.io/docs/configuration#configure-che-using-the-che-server)
# Configure Che Using the Che Server  
This method is not recommended as it is the most error prone and complex.

If you are using our `docker run eclipse/che-server` syntax, you can customize the Che server by passing in a `che.properties` file and with some limited command line options. See [Usage: Docker Server](https://eclipse-che.readme.io/docs/usage-docker-server#section-pass-che-command-line-options) for specifics.

Additionally, there are a number of other areas you can customize inside Che through [System properties](https://eclipse-che.readme.io/docs/configuration#configure-che-using-the-che-server)
# Custom Workspace Properties  
Each workspace is a separate runtime, and has at least one development agent that runs as a miniaturized Che server within the workspace. That agent has its own properties that can be configured as well. If you are authoring custom workspace extensions that are deployed within Che's agent in the workspace, you can customize.

Properties saved in `CHE_CONF_FOLDER\plugin-conf\` folder will be mounted into the runtime for each workspace created by Che. The Che development agent that runs within the workspace will load these properties when it boots.
# System Properties  
Whether you are using the CLI, Che launcher, or Che server, most internal Che configuration is done with a property, usually loaded by the server as a `che.properties` file. Che has an embedded `che.properties` file [which provides a set of defaults](https://github.com/eclipse/che/blob/08344fe62ebedfaa199e3258279b29acec7c88f8/assembly/assembly-wsmaster-war/src/main/webapp/WEB-INF/classes/codenvy/che.properties).

You can customize the properties by:
1: If using the Che CLI, by providing a custom `che.properties` by setting the `CHE_CONF_FOLDER` variable or by providing individual environment variables in the form of `CHE_PROPERTY_<name>=<value>`. [See more](https://eclipse-che.readme.io/docs/configuration#section-one-use-properties).
2: If using the Che launcher, [set an environment variable](https://eclipse-che.readme.io/docs/usage-docker#environment-variables) or by providing a [`che.properties` file with custom properties](https://eclipse-che.readme.io/docs/usage-docker#add-custom-eclipse-che-properties).
3: If using the Che server from our ZIP, edit `/conf/che.properties` before starting Tomcat or passing a `-D<prop>` option to Che's JVM when starting Che.

## Server Configuration

Che stores internal configuration and metadata in storage. You can change the default location where this information is saved. Note that if you are using our Docker launcher, the Che server is running in a container, and this storage is within that container.
```json  
# Folder where Che will store internal data objects
che.conf.storage=${che.home}/storage\
```
If you are using the Che CLI or Che launcher, setting `CHE_DATA_FOLDER` or `CHE_PROPERTY_che_conf_storage` will also update this property.

## Workspace Configuration

### Storage
Che stores each workspace as a separate directory on a file system.
```json  
# Path to workspace and project storage.
che.user.workspaces.storage=${che.home}/workspaces \
```
If you are using the Che CLI or Che launcher, setting `CHE_DATA_FOLDER` or `CHE_PROPERTY_che_user_workspaces_storage` will also update this property.

### Other Workspace Options
```json  
# Your projects are synchronized from the Che server into the machine running each
# workspace. This is the directory in the ws runtime where your projects are mounted.
che.machine.projects.internal.storage=/projects

# Configures proxies used by runtimes powering workspaces
http.proxy=
https.proxy=
no_proxy=

# Java command line options when starting our Che agent in workspace runtime
che.machine.java_opts=-Xms256m -Xmx2048m -Djava.security.egd=file:/dev/./urandom

# Folder to mount from your host into the workspace.
machine.server.extra.volume

# Workspace SSH connection timeouts.
machine.ssh.connection_timeout_ms=3000

# Pather for workspace logs form Che agents and other runtimes
machine.logs.location=${che.logs.dir}/machine/logs

# RAM default for new workspace runtimes.
machine.default_mem_size_mb=1024\
```
## Stacks
[Stacks](https://eclipse-che.readme.io/docs/stacks) define the recipes used to create workspace runtimes. They appear in the stack library of the dashboard. You can create your own.
```json  
# File name containing default stacks definitions
che.stacks.default=${che.home}/stacks/stacks.json

# Folder name where stack images are stored
che.stacks.images.storage=${che.home}/stacks/images\
```
## Templates
Code [templates](doc:templates) allow you to define sample projects that are cloned into a workspace if the user chooses it when creating a new project. You can add your own.
```json  
# Folder that contains JSON files with code templates and samples
project.template_description.location_dir=${che.home}/templates\
```
## GitHub oAuth
Refer to [GitHub using OAuth](https://eclipse-che.readme.io/docs/git#section-github-oauth) for configuration information.

## GitLab oAuth
Refer to [GitHub using OAuth](https://eclipse-che.readme.io/docs/git#section-gitlab-oauth) for configuration information.

## Docker in Docker
By default, Che has Docker privileged mode disabled. This prevents your workspace (which is a Docker container) from launching their own Docker containers. Activating privileged mode will allow you to do Docker-in-Docker.
```json  
# If true, then all docker machines will start in privilege mode.
machine.docker.privilege_mode=false\
```
## Workspace Snapshots
In the IDE, it is possible to snapshot workspace runtimes. Your projects are saved outside the snapshot and then re-mounted into the runtime after it is reactivated from the snapshot.

Che stores snapshots as Docker images. These images can be saved to disk (default) or into a Docker registry. Workspaces that are restarted will automatically use a snapshot as the base image instead of the originating image used to create the runtime. Snapshots let you store internal state, such as contents of a database, which are not part of your project tree.

```json  
# If false, snapshots are saved to disk.
# If true, snapshots are saved in a Docker registry
machine.docker.snapshot_use_registry=false

# Automatically creates a snapshot when workspace stopped if the value is {true},
# Otherwise, just stops the workspace.
workspace.runtime.auto_snapshot=true

# Automatically restore workspace from snapshot if {true},
# Otherwise, create a workspace from base image.
workspace.runtime.auto_restore=true\
```

### Start A Docker Registry
You can launch a private Docker registry with Docker.
```shell  
# Launches a docker registry instance on port 5000
docker run -d -p 5000:5000 --restart=always --name registry registry:2\
```
The `- -restart=always` policy causes this container to be started any time a Docker daemon starts. You can change the location of where this registry will save the snapshots. By default images are stored in a registry container (`/var/lib/registry`). It is possible to mount this directory when [starting Docker Registry](https://docs.docker.com/registry/deploying/).

### Add Your Registry To Che
```json  
# Docker registry example. Uncomment to add a registry configuration.
# You can configure multiple registries with different names.
docker.registry.auth.<insert-name>.url=https://index.docker.io/v1/
docker.registry.auth.<insert-name>.username=<username>
docker.registry.auth.<insert-name>.password=<password>

# Example
docker.registry.auth.url=http://localhost:5000
docker.registry.auth.username=user1
docker.registry.auth.password=pass
docker.registry.auth.email=user1@email.com

# Or, save your snapshots to Docker Hub:
machine.docker.registry={namespace/repository}  
docker.registry.auth.url=https://index.docker.io/v1
docker.registry.auth.username={username}
docker.registry.auth.password={password}
docker.registry.auth.email=user1@email.com\
```
