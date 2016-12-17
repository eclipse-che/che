---
title: Installation
excerpt: "Install Codenvy in a public cloud or on your own servers."
layout: docs
permalink: /docs/admin-guide/installation/
---
# System Requirements
Codenvy installs on Linux, Mac and Windows.

#### Hardware
* 2 cores
* 3GB RAM
* 3GB disk space

Codenvy requires 2 GB storage and 4 GB RAM for internal services. The RAM, CPU and storage resources required for your users' workspaces are additive. Codenvy's Docker images consume ~900MB of disk and the Docker images for your workspace templates can each range from 5MB up to 1.5GB. Codenvy and its dependent core containers will consume about 500MB of RAM, and your running workspaces will each require at least 250MB RAM, depending upon user requirements and complexity of the workspace code and intellisense.

Boot2Docker, docker-machine, Docker for Windows, and Docker for Mac are all Docker variations that launch VMs with Docker running in the VM with access to Docker from your host. We recommend increasing your default VM size to at least 4GB. Each of these technologies have different ways to allow host folder mounting into the VM. Please enable this for your OS so that Codenvy data is persisted on your host disk.

#### Software
* Docker 1.10+

The Codenvy CLI - a Docker image - manages the other Docker images and supporting utilities that Codenvy uses during its configuration or operations phases. The CLI also provides utilities for downloading an offline bundle to run Codenvy while disconnected from the network.

Given the nature of the development and release cycle it is important that you have the latest version of docker installed because any issue that you encounter might have already been fixed with a newer docker release.

Install the most recent version of the Docker Engine for your platform using the [official Docker releases](http://docs.docker.com/engine/installation/), including support for Mac and Windows!  If you are on Linux, you can also install using:
```bash
wget -qO- https://get.docker.com/ | sh
```

Sometimes Fedora and RHEL/CentOS users will encounter issues with SElinux. Try disabling selinux with `setenforce 0` and check if resolves the issue. If using the latest docker version and/or disabling selinux does not fix the issue then please file a issue request on the [issues](https://github.com/codenvy/codenvy/issues) page. If you are a licensed customer of Codenvy, you can get prioritized support with support@codenvy.com.

#### Resources
You will need at least 8GB RAM and 16GB storage to run Codenvy. See the [sizing guide](https://codenvy.readme.io/docs/installation#sizing) before you install to ensure you’ll have sufficient resources for the number of developers and size of workspaces you’ll use.

#### Hostnames
Codenvy must be accessed over DNS hostnames. You must configure your clients with a [hosts rule or setup a network-wide DNS entry](http://codenvy.readme.io/docs/networking). The default installation configures Codenvy with `http://codenvy.onprem/` as the initial hostname. You can [change Codenvy's hostname](http://codenvy.readme.io/docs/networking#change-hostname) at any time.

#### Ports
TODO - UPDATE PORT DESCRIPTION SECTION

#### Internet Connection
You can install Codenvy while connected to a network or offline, disconnected from the Internet. If you perform an offline intallation, you need to first download a Codenvy assembly while in a DMZ with a network connection to DockerHub. 

# Installation
## Syntax
```
USAGE:
  docker run -it --rm <DOCKER_PARAMETERS> codenvy/cli:<version> [COMMAND]

MANDATORY DOCKER PARAMETERS:
  -v <LOCAL_PATH>:/data                Where user, instance, and log data saved

OPTIONAL DOCKER PARAMETERS:
  -e CODENVY_HOST=<YOUR_HOST>          IP address or hostname where Codenvy will serve its users
  -v <LOCAL_PATH>:/data/instance       Where instance, user, log data will be saved
  -v <LOCAL_PATH>:/data/backup         Where backup files will be saved
  -v <LOCAL_PATH>:/cli                 Where the CLI trace log is saved
  -v <LOCAL_PATH>:/repo                Codenvy git repo to activate dev mode
  -v <LOCAL_PATH>:/sync                Where remote ws files will be copied with sync command
  -v <LOCAL_PATH>:/unison              Where unison profile for optimzing sync command resides

COMMANDS:
  action <action-name>                 Start action on Codenvy instance
  add-node                             Adds a physical node to serve workspaces intto the Codenvy cluster
  backup                               Backups Codenvy configuration and data to /data/backup volume mount
  config                               Generates a Codenvy config from vars; run on any start / restart
  destroy                              Stops services, and deletes Codenvy instance data
  download                             Pulls Docker images for the current Codenvy version
  help                                 This message
  info                                 Displays info about Codenvy and the CLI
  init                                 Initializes a directory with a Codenvy install
  list-nodes                           Lists all physical nodes that are part of the Codenvy cluster
  offline                              Saves Codenvy Docker images into TAR files for offline install
  remove-node <ip>                     Removes the physical node from the Codenvy cluster
  restart                              Restart Codenvy services
  restore                              Restores Codenvy configuration and data from /data/backup mount
  rmi                                  Removes the Docker images for <version>, forcing a repull
  ssh <wksp-name> [machine-name]       SSH to a workspace if SSH agent enabled
  start                                Starts Codenvy services
  stop                                 Stops Codenvy services
  sync <wksp-name>                     Synchronize workspace with current working directory
  test <test-name>                     Start test on Codenvy instance
  upgrade                              Upgrades Codenvy from one version to another with migrations and backups
  version                              Installed version and upgrade paths
```

In these docs, we shorthand `codenvy [COMMAND]`, for the full `docker run ...` syntax for readability.

## Sample Start
Install and start the nightly build with user data saved on Windows at C:\tmp:
`docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock -v /c/tmp:/data codenvy/cli:5.0.0-latest start`

This installs a Codenvy configuration, downloads Codenvy's Docker images, run pre-flight port checks, boot Codenvy's services, and run post-flight checks. You do not need root access to start Codenvy, unless your environment requires it for Docker operations.

A successful start will display:
```
INFO: (codenvy cli): Downloading cli-latest
INFO: (codenvy cli): Checking registry for version 'nightly' images
INFO: (codenvy config): Generating codenvy configuration...
INFO: (codenvy config): Customizing docker-compose for Windows
INFO: (codenvy start): Preflight checks
         port 80:  [OK]
         port 443: [OK]
         port 5000: [OK]

INFO: (codenvy start): Starting containers...
INFO: (codenvy start): Server logs at "docker logs -f codenvy_codenvy_1"
INFO: (codenvy start): Server booting...
INFO: (codenvy start): Booted and reachable
INFO: (codenvy start): Ver: 5.0.0-M6-SNAPSHOT
INFO: (codenvy start): Use: http://10.0.75.2
INFO: (codenvy start): API: http://10.0.75.2/swagger
```
The administrative login is:
```
user: admin
pass: password
```
## Versions
Each version of Codenvy is available as a Docker image tagged with a label that matches the version, such as `codenvy/cli:5.0.0-M7`. You can see all versions available by running `docker run codenvy/cli version` or by [browsing DockerHub](https://hub.docker.com/r/codenvy/cli/tags/).

We maintain "redirection" labels which reference special versions of Codenvy:

| Variable | Description |
|----------|-------------|
| `latest` | The most recent stable release of Codenvy. |
| `5.0.0-latest` | The most recent stable release of Codenvy on the 5.x branch. |
| `nightly` | The nightly build of Codenvy. |

The software referenced by these labels can change over time. Since Docker will cache images locally, the `codenvy/cli:<version>` image that you are running locally may not be current with the one cached on DockerHub. Additionally, the `codenvy/cli:<version>` image that you are running references a manifest of Docker images that Codenvy depends upon, which can also change if you are using these special redirection tags.

In the case of 'latest' images, when you initialize an installation using the CLI, we encode your `/instance/codenvy.ver` file with the numbered version that latest references. If you begin using a CLI version that mismatches what was installed, you will be presented with an error.

To avoid issues that can appear from using 'nightly' or 'latest' redirectoins, you may:
1. Verify that you have the most recent version with `docker pull eclipse/cli:<version>`.
2. When running the CLI, commands that use other Docker images have an optional `--pull` and `--force` command line option [which will instruct the CLI to check DockerHub](/docs/cli#codenvy-init) for a newer version and pull it down. Using these flags will slow down performance, but ensures that your local cache is current.

If you are running Codenvy using a tagged version that is a not a redirection label, such as `5.0.0-M7`, then these caching issues will not happen, as the software installed is tagged and specific to that particular version, never changing over time.

## Volume Mounts
We use volume mounts to configure certain parts of Codenvy. The presence or absence of certain volume mounts will trigger certain behaviors in the system. For example, you can volume mount a Codenvy source git repository with `:/repo` to active development mode where we start Codenvy's containers using source code from the repository instead of the software inside of the default containers.

At a minimum, you must volume mount a local path to `:/data`, which will be the location that Codenvy installs its configuration, user data, version and log information. Codenvy also leaves behind a `cli.log` file in this location to debug any odd behaviors while running the system. In this folder we also create a `codenvy.env` file which contains all of the admin configuration that you can set or override in a single location.

You can also use volume mounts to override the location of where your user or backup data is stored. By default, these folders will be created as sub-folders of the location that you mount to `:/data`. However, if you do not want your `/instance`, and `/backup` folder to be children, you can set them individually with separate overrides.

```
docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock
                    -v <local-path>:/data
                    -v <a-different-path>:/data/instance
                    -v <another-path>:/data/backup
                       codenvy/cli:<version> [COMMAND]    

```

## Hosting
If you are hosting Codenvy at a cloud service like DigitalOcean, `CODENVY_HOST` must be set to the server's IP address or its DNS. 

We will attempt to auto-set `CODENVY_HOST` by running an internal utility `docker run --net=host eclipse/che-ip:nightly`. This approach is not fool-proof. This utility is usually accurate on desktops, but usually fails on hosted servers. You can explicitly set this value:

```
docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock
                    -v <local-path>:/data
                    -e CODENVY_HOST=<your-ip-or-host>
                       codenvy/cli:<version> [COMMAND]
```

The Codenvy CLI (a Docker image) is downloaded when you first execute docker run codenvy/cli:<version> command. The CLI downloads other images that run Codenvy and its supporting utilities. The CLI also provides utilities for downloading an offline bundle to run Codenvy while disconnected from the network.

## Proxy Installation
You can install and operate Codenvy behind a proxy:
1. Configure each physical node's Docker daemon with proxy access.
2. Optionally, override the default workspace proxy settings for users if you want to restrict their Internet access.

Before starting Codenvy, configure [Docker's daemon for proxy access](https://docs.docker.com/engine/admin/systemd/#/http-proxy). If you plan to scale Codenvy with multiple host nodes, each host node must have its Docker daemon configured for proxy access. If you have Docker for Windows or Docker for Mac installed on your desktop and installing Codenvy, these utilities have a GUI in their settings which let you set the proxy settings directly.

Please be mindful that your `HTTP_PROXY` and/or `HTTPS_PROXY` that you set in the Docker daemon must have a protocol and port number. Proxy configuration is quite finnicky, so please be mindful of providing a fully qualified proxy location.

If you configure `HTTP_PROXY` or `HTTPS_PROXY` in your Docker daemon, we will add `localhost,127.0.0.1,codenvy-swarm,CODENVY_HOST` to your `NO_PROXY` value where `CODENVY_HOST` is the DNS or IP address. We recommend that you add the short and long form DNS entry to your Docker's `NO_PROXY` setting if it is not already set.

We will add some values to `codenvy.env` that contain some proxy overrides. You can optionally modify these with different values:
```
CODENVY_HTTP_PROXY_FOR_CODENVY=<YOUR_PROXY_FROM_DOCKER>
CODENVY_HTTPS_PROXY_FOR_CODENVY=<YOUR_PROXY_FROM_DOCKER>
CODENVY_NO_PROXY_FOR_CODENVY=localhost,127.0.0.1,codenvy-swarm,<YOUR_CODENVY_HOST>
CODENVY_HTTP_PROXY_FOR_CODENVY_WORKSPACES=<YOUR_PROXY_FROM_DOCKER>
CODENVY_HTTPS_PROXY_FOR_CODENVY_WORKSPACES=<YOUR_PROXY_FROM_DOCKER>
CODENVY_NO_PROXY_FOR_CODENVY_WORKSPACES=localhost,127.0.0.1,<YOUR_CODENVY_HOST>
```

The last three entries are injected into workspaces created by your users. This gives your users access to the Internet from within their workspaces. You can comment out these entries to disable access. However, if that access is turned off, then the default templates with source code will fail to be created in workspaces as those projects are cloned from GitHub.com. Your workspaces are still functional, we just prevent the template cloning.

## Offline Installation
We support offline (disconnected from the Internet) installation and operation. This is helpful for  restricted environments, regulated datacenters, or offshore installations. The offline installation downloads the CLI, core system images, and any stack images while you are within a network DMZ with DockerHub access. You can then move those files to a secure environment and start Codenvy.

#### 1. Save Codenvy Images
While connected to the Internet, download Codenvy's Docker images:
```
codenvy offline
```
The CLI will download images and save them to `/backup/*.tar` with each image saved as its own file. You can save these files to a differnet location by volume mounting a local folder to `:/data/backup`. The version tag of the CLI Docker image will be used to determine which versions of dependent images to download. There is about 1GB of data that will be saved.

The default execution will download none of the optional stack images, which are needed to launch workspaces of a particular type. There are a few dozen stacks for different programming languages and some of them are over 1GB in size. It is unlikely that your users will need all of the stacks, so you do not need to download all of them. You can get a list of available stack images by running `codenvy offline --list`. You can download a specific stack by running `codenvy offline --image:<image-name>` and the `--image` flag can be repeatedly used on a single command line.

#### 2. Start Codenvy In Offline Mode
Place the TAR files into a folder in the offline computer. If the files are in placed in a folder named `/tmp/offline`, you can run Codenvy in offline mode with: 

```
# Load the CLI
docker load < /tmp/offline/codenvy_cli:<version>.tar

# Start Codenvy in offline mode
docker run <other-properties> -v /tmp/offline:/data/backup codenvy/cli:<version> start --offline
```
The `--offline` parameter instructs Codenvy CLI to load all of the TAR files located in the folder mounted to `/data/backup`. These images will then be used instead of routing out to the Internet to check for DockerHub. The preboot sequence takes place before any CLI functions make use of Docker. The `codenvy start`, `codenvy download`, and `codenvy init` commands support `--offline` mode which triggers this preboot seequence.

## Uninstall
```
# Remove your Codevy configuration and destroy user projects and database
docker run codenvy/cli:<version> destroy [--quiet|--cli]

# Deletes Codenvy's images from your Docker registry
docker run codenvy/cli:<version> rmi

# Delete the Codenvy CLI
docker rmi -f codenvy/cli
```
