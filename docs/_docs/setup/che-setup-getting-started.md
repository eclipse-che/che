---
tags: [ "eclipse" , "che" ]
title: Getting Started&#58 Local
excerpt: ""
layout: docs
permalink: /:categories/getting-started/
---
{% include base.html %}
Eclipse Che is a developer workspace server and cloud IDE. You install, run, and manage Eclipse Che with Docker.

### Download
This is the administration guide for the on-premises installation of Eclipse Che. This document discusses the installation, configuration, and operation of Che that you host on your own hardware or IaaS provider.

You can get a hosted version of Eclipse Che with Codenvy at [codenvy.io](http://codenvy.io).

# How to Get Help
### Support:  
If the unthinkable happens, or you have a question, you can post [issues on our GitHub page](https://github.com/eclipse/che/issues). Please follow the [guidelines on issue reporting](https://github.com/eclipse/che/blob/master/CONTRIBUTING.md) and provide:
* your OS distribution and version
- output of `docker version` command
- output of `docker info` command
- the full `docker run ...` syntax you used on the command line
- the output of `cli.log`

### Documentation:
We put a lot of effort into our docs. Please add [suggestions on areas for improvement]() with a pull request.

# Quick Start
On any computer with Docker 1.11+ installed:
```shell
# Interactive help
docker run -it eclipse/che-cli start

# Or, full start syntax where <path> is a local directory
docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock -v <path>:/data eclipse/che-cli start
```

# Operate Che
```shell  
# Start Eclipse Che with user data saved on Windows in c:\tmp
docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock -v /c/tmp:/data eclipse/che-cli start
INFO: (che cli): Loading cli...
INFO: (che cli): Checking registry for version 'nightly' images
INFO: (che config): Generating che configuration...
INFO: (che config): Customizing docker-compose for running in a container
INFO: (che start): Preflight checks
         port 8080 (http):       [AVAILABLE]

INFO: (che start): Starting containers...
INFO: (che start): Services booting...
INFO: (che start): Server logs at "docker logs -f che"
INFO: (che start): Booted and reachable
INFO: (che start): Ver: nightly
INFO: (che start): Use: http://<your-ip>:8080
INFO: (che start): API: http://<your-ip>:8080/swagger

# Stop Che
docker run <docker-goodness> eclipse/che-cli stop

# Restart Che
docker run <docker-goodness> eclipse/che-cli restart

# Run a specific version of Che
docker run <docker-goodness> eclipse/che-cli:<version> start

# Get help
docker run eclipse/che-cli

# If boot2docker on Windows (rare), mount a subdir of `%userprofile%` to `:/data`. For example:
docker run <docker-goodness> -v /c/Users/tyler/che:/data eclipse/che-cli start

# If Che will be accessed from other machines add your server's external IP
docker run <docker-goodness> -e CHE_HOST=<your-ip> eclipse/che-cli start
```

# Develop with Che  
Now that Che is running there are a lot of fun things to try:
- Become familiar with Che through [one of our tutorials]().
- [Import a project](https://eclipse-che.readme.io/docs/import-a-project) and setup [git authentication]().
- Use [commands]() to build and run a project.
- Create a [preview URL]() to share your app.
- Setup a [debugger]().
- Create reproducible workspaces with [chedir]().
- Create a [custom runtime stack](https://eclipse-che.readme.io/docs/stacks).

# Syntax  
```
USAGE:
  docker run -it --rm <DOCKER_PARAMETERS> eclipse/che-cli:<version> [COMMAND]

MANDATORY DOCKER PARAMETERS:
  -v <LOCAL_PATH>:/data                Where user, instance, and log data saved

OPTIONAL DOCKER PARAMETERS:
  -e CHE_HOST=<YOUR_HOST>              IP address or hostname where che will serve its users
  -e CHE_PORT=<YOUR_PORT>              Port where che will bind itself to
  -v <LOCAL_PATH>:/data/instance       Where instance, user, log data will be saved
  -v <LOCAL_PATH>:/data/backup         Where backup files will be saved
  -v <LOCAL_PATH>:/repo                che git repo to activate dev mode
  -v <LOCAL_PATH>:/sync                Where remote ws files will be copied with sync command
  -v <LOCAL_PATH>:/unison              Where unison profile for optimzing sync command resides

COMMANDS:
  action <action-name>                 Start action on che instance
  backup                               Backups che configuration and data to /data/backup volume mount
  config                               Generates a che config from vars; run on any start / restart
  destroy                              Stops services, and deletes che instance data
  download                             Pulls Docker images for the current che version
  help                                 This message
  info                                 Displays info about che and the CLI
  init                                 Initializes a directory with a che install
  offline                              Saves che Docker images into TAR files for offline install
  restart                              Restart che services
  restore                              Restores che configuration and data from /data/backup mount
  rmi                                  Removes the Docker images for <version>, forcing a repull
  ssh <wksp-name> [machine-name]       SSH to a workspace if SSH agent enabled
  start                                Starts che services
  stop                                 Stops che services
  sync <wksp-name>                     Synchronize workspace with current working directory
  test <test-name>                     Start test on che instance
  upgrade                              Upgrades che from one version to another with migrations and backups
  version                              Installed version and upgrade paths
```

# Pre-Reqs  
### Hardware:
* 1 cores
* 256MB RAM
* 300MB disk space

Che requires 300 MB storage and 256MB RAM for internal services. The RAM, CPU and storage resources required for your users' workspaces are additive. Che Docker images consume ~300MB of disk and the Docker images for your workspace templates can each range from 5MB up to 1.5GB. Che and its dependent core containers will consume about 500MB of RAM, and your running workspaces will each require at least 250MB RAM, depending upon user requirements and complexity of the workspace code and intellisense.

Boot2Docker, docker-machine, Docker for Windows, and Docker for Mac are all Docker variations that launch VMs with Docker running in the VM with access to Docker from your host. We recommend increasing your default VM size to at least 4GB. Each of these technologies have different ways to allow host folder mounting into the VM. Please enable this for your OS so that Che data is persisted on your host disk.

### Software:
* Docker 1.11+

The Che CLI - a Docker image - manages the other Docker images and supporting utilities that Che uses during its configuration or operations phases. The CLI also provides utilities for downloading an offline bundle to run Che while disconnected from the network.

Given the nature of the development and release cycle it is important that you have the latest version of Docker installed because any issue that you encounter might have already been fixed with a newer Docker release.

Install the most recent version of the Docker Engine for your platform using the [official Docker releases](http://docs.docker.com/engine/installation/), including support for Mac and Windows!  If you are on Linux, you can also install using:
```bash
wget -qO- https://get.docker.com/ | sh
```

Verify that Docker is installed with:
```shell  
# Should print "Hello from Docker!"
docker run hello-world
```

Sometimes Fedora and RHEL/CentOS users will encounter issues with SElinux. Try disabling selinux with `setenforce 0` and check if resolves the issue. If using the latest docker version and/or disabling selinux does not fix the issue then please file a issue request on the [issues](https://github.com/eclipse/che/issues) page. 

#### Ports
The default port required to run Che is `8080`. Che performs a preflight check when it boots to verify that the port is available. You can pass `-e CHE_PORT=<port>` in Docker portion of the start command to change the port that Che starts on.

#### Internet Connection
You can install Che while connected to a network or offline, disconnected from the Internet. If you perform an offline intallation, you need to first download a Che assembly while in a DMZ with a network connection to DockerHub. 

# Versions
Each version of Che is available as a Docker image tagged with a label that matches the version, such as `eclipse/che-cli:5.0.0-M7`. You can see all versions available by running `docker run eclipse/che-cli version` or by [browsing DockerHub](https://hub.docker.com/r/eclipse/che-cli/tags/).

We maintain "redirection" labels which reference special versions of Che:

| Variable | Description |
|----------|-------------|
| `latest` | The most recent stable release. |
| `5.0.0-latest` | The most recent stable release on the 5.x branch. |
| `nightly` | The nightly build. |

The software referenced by these labels can change over time. Since Docker will cache images locally, the `eclipse/che-cli:<version>` image that you are running locally may not be current with the one cached on DockerHub. Additionally, the `eclipse/che-cli:<version>` image that you are running references a manifest of Docker images that Che depends upon, which can also change if you are using these special redirection tags.

In the case of 'latest' images, when you initialize an installation using the CLI, we encode a `/instance/che.ver` file with the numbered version that latest references. If you begin using a CLI version that mismatches what was installed, you will be presented with an error.

To avoid issues that can appear from using 'nightly' or 'latest' redirections, you may:
1. Verify that you have the most recent version with `docker pull eclipse/che-cli:<version>`.
2. When running the CLI, commands that use other Docker images have an optional `--pull` and `--force` command line option [which will instruct the CLI to check DockerHub]() for a newer version and pull it down. Using these flags will slow down performance, but ensures that your local cache is current.

If you are running Che using a tagged version that is a not a redirection label, such as `5.0.0-M7`, then these caching issues will not happen, as the software installed is tagged and specific to that particular version, never changing over time.

# Volume Mounts
We use volume mounts to configure certain parts of Che. The presence or absence of certain volume mounts will trigger certain behaviors in the system. For example, you can volume mount a Che source git repository with `:/repo` to activate development mode where we start Che's containers using source code from the repository instead of the software inside of the default containers.

At a minimum, you must volume mount a local path to `:/data`, which will be the location that Che installs its configuration, user data, version and log information. Che also leaves behind a `cli.log` file in this location to debug any odd behaviors while running the system. In this folder we also create a `che.env` file which contains all of the admin configuration that you can set or override in a single location.

You can also use volume mounts to override the location of where your user or backup data is stored. By default, these folders will be created as sub-folders of the location that you mount to `:/data`. However, if you do not want your `/instance`, and `/backup` folder to be children, you can set them individually with separate overrides.

```
docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock
                    -v <local-path>:/data
                    -v <a-different-path>:/data/instance
                    -v <another-path>:/data/backup
                       eclipse/che-cli:<version> [COMMAND]    

```

# Hosting
If you are hosting Che at a cloud service like DigitalOcean, `CHE_HOST` must be set to the server's IP address or its DNS. 

We will attempt to auto-set `CHE_HOST` by running an internal utility `docker run --net=host eclipse/che-ip:nightly`. This approach is not fool-proof. This utility is usually accurate on desktops, but usually fails on hosted servers. You can explicitly set this value to the IP address of your server:
```
docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock
                    -v <local-path>:/data
                    -e CHE_HOST=<your-ip-or-host>
                       eclipse/che-cli:<version> [COMMAND]
```

# Proxy Installation
You can install and operate Che behind a proxy:
1. Configure each physical node's Docker daemon with proxy access.
2. Optionally, override workspace proxy settings for users if you want to restrict their Internet access.

Before starting Che, configure [Docker's daemon for proxy access](https://docs.docker.com/engine/admin/systemd/#/http-proxy). If you have Docker for Windows or Docker for Mac installed on your desktop and installing Che, these utilities have a GUI in their settings which let you set the proxy settings directly.

Please be mindful that your `HTTP_PROXY` and/or `HTTPS_PROXY` that you set in the Docker daemon must have a protocol and port number. Proxy configuration is quite finnicky, so please be mindful of providing a fully qualified proxy location.

If you configure `HTTP_PROXY` or `HTTPS_PROXY` in your Docker daemon, we will add `localhost,127.0.0.1,CHE_HOST` to your `NO_PROXY` value where `CHE_HOST` is the DNS or IP address. We recommend that you add the short and long form DNS entry to your Docker's `NO_PROXY` setting if it is not already set.

We will add some values to `che.env` that contain some proxy overrides. You can optionally modify these with overrides:
```
CHE_HTTP_PROXY=<YOUR_PROXY_FROM_DOCKER>
CHE_HTTPS_PROXY=<YOUR_PROXY_FROM_DOCKER>
CHE_NO_PROXY=localhost,127.0.0.1,<YOUR_CHE_HOST>
CHE_HTTP_PROXY_FOR_WORKSPACES=<YOUR_PROXY_FROM_DOCKER>
CHE_HTTPS_PROXY_FOR_WORKSPACES=<YOUR_PROXY_FROM_DOCKER>
CHE_NO_PROXY_FOR_WORKSPACES=localhost,127.0.0.1,<YOUR_CHE_HOST>
```

The last three entries are injected into workspaces created by your users. This gives your users access to the Internet from within their workspaces. You can comment out these entries to disable access. However, if that access is turned off, then the default templates with source code will fail to be created in workspaces as those projects are cloned from GitHub.com. Your workspaces are still functional, we just prevent the template cloning.

# Offline Installation
We support offline (disconnected from the Internet) installation and operation. This is helpful for restricted environments, regulated datacenters, or offshore installations. The offline installation downloads the CLI, core system images, and any stack images while you are within a network DMZ with DockerHub access. You can then move those files to a secure environment and start Che.

### 1. Save Che Images
While connected to the Internet, download Che's Docker images:
```shell
docker run <docker-goodness> eclipse/che-cli:<version> offline
```
The CLI will download images and save them to `/backup/*.tar` with each image saved as its own file. You can save these files to a differnet location by volume mounting a local folder to `:/data/backup`. The version tag of the CLI Docker image will be used to determine which versions of dependent images to download. There is about 1GB of data that will be saved.

The default execution will download none of the optional stack images, which are needed to launch workspaces of a particular type. There are a few dozen stacks for different programming languages and some of them are over 1GB in size. It is unlikely that your users will need all of the stacks, so you do not need to download all of them. You can get a list of available stack images by running `eclipse/che-cli offline --list`. You can download a specific stack by running `eclipse/che-cli offline --image:<image-name>` and the `--image` flag can be repeatedly used on a single command line.

### 2. Start Che In Offline Mode
Place the TAR files into a folder in the offline computer. If the files are in placed in a folder named `/tmp/offline`, you can run Che in offline mode with: 

```shell
# Load the CLI
docker load < /tmp/offline/eclipse_che-cli:<version>.tar

# Start Che in offline mode
docker run <other-properties> -v /tmp/offline:/data/backup eclipse/che-cli:<version> start --offline
```
The `--offline` parameter instructs the Che CLI to load all of the TAR files located in the folder mounted to `/data/backup`. These images will then be used instead of routing out to the Internet to check for DockerHub. The preboot sequence takes place before any CLI functions make use of Docker. The `eclipse/che-cli start`, `eclipse/che-cli download`, and `eclipse/che-cli init` commands support `--offline` mode which triggers this preboot seequence.

# Uninstall
```shell
# Remove your Che configuration and destroy user projects and database
docker run eclipse/che-cli:<version> destroy [--quiet|--cli]

# Deletes Che's images from your Docker registry
docker run eclipse/che-cli:<version> rmi

# Delete the Che CLI
docker rmi -f eclipse/che-cli
```

# Licensing
Che is licensed with the Eclipse Public License. 

# Configuration
Change Che's port, hostname, oAuth, Docker, git, and networking by setting [Eclipse Che properties]().