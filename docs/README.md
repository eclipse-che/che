# Eclipse Che Installation and Operation
Eclipse Che is an Eclipse next-generation IDE, developer workspace server, and cloud IDE.

### Quick Start
With Docker 1.11+ on Windows, Mac, or Linux:
```
$ docker run eclipse/che-cli start
```
This command will give you additional instructions on how to run the Che CLI while setting your hostname, configuring volume mounts, and testing your Docker setup. For example, a complete execution might be:

```
$ docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock -v /c/che:/data eclipse/che-cli start
```

### TOC
- [Beta](#beta)
- [Getting Help](#getting-help)
- [Getting Started](#getting-started)
- [System Requirements](#system-requirements)
    + [Hardware](#hardware)
    + [Software](#software)
    + [Sizing](#sizing)
- [Installation](#installation)
    + [Linux:](#linux)
    + [Mac:](#mac)
    + [Windows:](#windows)
    + [Verification:](#verification)
    + [Proxies](#proxies)
    + [Offline Installation](#offline-installation)
- [Usage](#usage)
    + [Hosting](#hosting)
- [Uninstall](#uninstall)
- [Configuration](#configuration)
    + [Saving Configuration in Version Control](#saving-configuration-in-version-control)
    + [Logs and User Data](#logs-and-user-data)    + [oAuth](#oauth)
    + [LDAP](#ldap)
    + [Development Mode](#development-mode)
    + [Licensing](#licensing)
    + [Hostname](#hostname)
    + [HTTP/S](#https)
    + [SMTP](#smtp)
    + [Workspace Limits](#workspace-limits)
    + [Private Docker Registries](#private-docker-registries)
- [Managing](#managing)
    + [Scaling](#scaling)
    + [Upgrading](#upgrading)
    + [Backup (Backup)](#backup-backup)
    + [Migration](#migration)
    + [Disaster Recovery](#disaster-recovery)
- [CLI Reference](#cli-reference)
- [Architecture](#architecture)
- [Team](#team)

## Getting Help
If you require immediate help, [Che](http://che.com) provides email and phone support options for Eclipse Che.

If you run into an issue, please [open a GitHub issue](http://github.com/eclipse/che/issues) providing:
- the host distribution and release version
- output of the `docker version` command
- output of the `docker info` command
- the full Docker run syntax you used for the `che-cli <command>`
- the output of `cli.log` - see [CLI Reference](#cli-reference)

## System Requirements
Eclipse Che installs on Linux, Mac and Windows. 

#### Hardware
* 1 core
* 2GB RAM
* 200MB disk space

Eclipse Che requires 200 MB storage and 1 GB RAM for internal services. The RAM, CPU and storage resources required for your users' workspaces are additive. Che's Docker images consume ~900MB of disk and the Docker images for your workspace templates can each range from 5MB up to 1.5GB. Che and its dependent core containers will consume about 500MB of RAM, and your running workspaces will each require at least 250MB RAM, depending upon user requirements and complexity of the workspace code and intellisense.

Boot2Docker, docker-machine, Docker for Windows, and Docker for Mac are variations that launch VMs with Docker running in the VM with access to Docker from your host. We recommend increasing your default VM size to at least 4GB. Each of these technologies have different ways to allow host folder mounting into the VM. Please enable this for your OS so that Che data is persisted on your host disk.

#### Software
* Docker 11.1+

The Eclipse Che CLI - a Docker image - manages the other Docker images and supporting utilities that Che uses during its configuration or operations phases. The CLI also provides utilities for downloading an offline bundle to run Che while disconnected from the network.

Given the nature of the development and release cycle it is important that you have the latest version of Docker installed because any issue that you encounter might have already been fixed with a newer docker release.

Install the most recent version of Docker Engine for your platform using the [official Docker releases](http://docs.docker.com/engine/installation/), including support for Mac and Windows!  If you are on Linux, you can also install using:
```bash
wget -qO- https://get.docker.com/ | sh
```

#### Sizing
Che's core services and workspaces run on a common, but single node.

You need to have enough RAM to support the number of concurrent *running* workspaces. A single user may have multiple running workspaces, but generally the common scenario is a user running a single workspace at a time. Workspace sizes are set by users when they create new workspaces, but you can define workspace limits in the configuration file that prevent RAM sprawl.

For sizing, determine how much RAM you want each user to consume at a time, and then estimate the peak concurrent utilization to determine how much system-wide RAM you will want. For example, internally at Che, we regularly have 75 concurrently running workspaces, each sized at 16 GB RAM, for a total expectation of 1.2TB of RAM. If you are unable to purchase a single server that supports the maximum RAM that you require, you can consider upgrading to Che, which is an Eclipse Che implementation that supports multiple physical nodes with distributed workspaces. 

Compilation is CPU-heavy and most compilation events are queued to a single CPU. You can assume that the number of cores available to the node that is running Che will determine the maximum amount of parallel compilation activities that occur.

The default configuration of workspaces is to auto-snapshot the workspace runtime to disk whenever it is stopped, whether by the user or through idle timeout. Many stack base images can grow to be >1GB, especially if you are installing complicated software inside of them, and thus their snapshots can be sizable as well. If you allow users to have many workspaces, even if they are stopped, each of those workspaces will have a snapshot on disk. Che's implementation of Eclipse Che provides system admin limits to cap workspaces, workspaces per user, and RAM for running workspaces. 

## Installation
The Che CLI (a Docker image) is downloaded when you first execute `docker run eclipse/che-cli:<version>` command. The CLI downloads other images that run Eclipse Che and its supporting utilities. The CLI also provides utilities for downloading an offline bundle to run Che while disconnected from a network.

#### Nightly and Latest
Each version of Che is available as a Docker image tagged with a label that matches the version, such as `eclipse/che-cli:5.0.0-M7`. You can see all versions available by running `docker run eclipse/che-cli version` or by [browsing DockerHub](https://hub.docker.com/r/eclipse/che-cli/tags/).

We maintain "redirection" labels which reference special versions:

| Variable | Description |
|----------|-------------|
| `latest` | The most recent stable release. |
| `5.0.0-latest` | The most recent stable release on the 5.x branch. |
| `nightly` | The nightly build. |

The software referenced by these labels can change over time. Since Docker will cache images locally, the `eclipse/che-cli:<version>` image that you are running locally may not be current with the one cached on DockerHub. Additionally, the `eclipse/che-cli:<version>` image that you are running references a manifest of Docker images that Che depends upon, which can also change if you are using these special redirection tags.

In the case of 'latest' images, when you initialize an installation using the CLI, we encode your `/instance/che.ver` file with the numbered version that latest references. If you begin using a CLI version that mismatches what was installed, you will be presented with an error.

To avoid issues that can appear from using 'nightly' or 'latest' redirections, you may:
1. Verify that you have the most recent version with `docker pull eclipse/che-cli:<version>`.
2. When running the CLI, commands that use other Docker images have an optional `--pull` and `--force` command line option which will instruct the CLI to check DockerHub for a newer version and pull it down. Using these flags will slow boot up performance, but ensures that your local cache is current.

If you are running Che using a tagged version that is a not a redirection label, such as `5.0.0-M7`, then these caching issues will not happen, as the software installed is tagged and specific to that particular version, never changing over time.

#### Linux:
There is nothing additional you need to install other than Docker.

#### Mac:
There is nothing additional you need to install other than Docker.

#### Windows:
There is nothing additional you need to install other than Docker.

#### Verification:
You can verify that the CLI is working:
```
docker run eclipse/che-cli
```
The CLI is bound inside of Docker images that are tagged with different versions. If you were to run `eclipse/che-cli:5.0.0-latest` this will run the latest shipping release of Che and the CLI. This list of all versions available can be seen by running `che-cli version` or browsing the list of [tags available in Docker Hub](https://hub.docker.com/r/eclipse/che-cli/tags/).

#### Proxies
You can install and operate behind a proxy. There are three parts to configure:
1. Configuring Docker proxy access so that Che can download its images from DockerHub.
2. Configuring Che's system containers so that they can proxy to the Internet.
3. Optionally, configuring workspace proxy settings to allow users within a workspace to proxy to the Internet.

Before starting Che, configure [Docker's daemon for proxy access](https://docs.docker.com/engine/admin/systemd/#/http-proxy). 

Che's system runs on Java, and the JVM requires proxy environment variables in our `JAVA_OPTS`. We use the JVM for the core Che server and the workspace agents that run within each workspace. TODO: HOW DO YOU CONFIGURE THIS? Please be mindful of the proxy URL formatting. Proxies are unforgiving if do not enter the URL perfectly, inclduing the protocol, port and whether they allow a trailing slash/.

If you would like your users to have proxified access to the Internet from within their workspace, those workspace runtimes need to have proxy settings applied to their environment variables in their .bashrc or equivalent. TODO: HOW DO YOU CONFIGURE THIS?

A `NO_PROXY` variable is required if you use a fake local DNS. Java and other internal utilities will avoid accessing a proxy for internal communications when this value is set.

#### Offline Installation
We support the ability to install and run while disconnected from the Internet. This is helpful for certain restricted environments, regulated datacenters, or offshore installations. 

##### Save Docker Images
While connected to the Internet, download Che's Docker images:
```
docker run eclipse/che-cli offline
``` 
The CLI will download images and save them to `/data/backup/*.tar` with each image saved as its own file. The `/backup` folder will be created as a subdirectory of the folder you volume mounted to `:/data`. You can optionally save these files to a differnet location by volume mounting that folder to `:/backup`. The version tag of the CLI Docker image will be used to determine which versions of dependent images to download. There is about 1GB of data that will be saved.

##### Save Che CLI
```
docker save eclipse/che-cli:<version>
```

##### Save Che Stacks
Out of the box, Che has configured a few dozen stacks for popular programming languages and frameworks. These stacks use "recipes" which contain links to Docker images that are needed to create workspaces from these stacks. These workspace runtime images are not saved as part of `che-cli offline`. There are many of these images and they consume a lot of disk space. Most users do not require all of these stacks and most replace default stacks with custom stacks using their own Docker images. If you'd like to get the images that are associated with Che's stacks:
```
docker save <che-stack-image-name> > backup/<base-image-name>.tar
```
The list of stack images that you can download are sourced from Eclipse Che's [Dockerfiles repository](https://github.com/eclipse/che-dockerfiles/tree/master/recipes). Each folder is named the same way that our images are stored.  The `alpine_jdk8` folder represents the `eclipse/alpine_jdk8` Docker image, which you would save with `docker save eclipse/alpine_jdk8 > backup/alpine_jdk8.tar`.

##### Start Offline
Extract your files to an offline computer with Docker already configured. Install the CLI files to a directory on your path and ensure that they have execution permissions. Execute the CLI in the directory that has the `offline` sub-folder which contains your tar files. Then start Che in `--offline` mode:
```
docker run eclipse/che-cli:<version> start --offline
```
When invoked with the `--offline` parameter, the CLI performs a preboot sequence, which loads all saved `backup/*.tar` images including any stack images you saved. The preboot sequence takes place before any CLI configuration, which itself depends upon Docker. The `che-cli start`, `che-cli download`, and `che-cli init` commands support `--offline` mode which triggers this preboot seequence.

## Usage
#### Syntax
```
USAGE: 
  docker run <DOCKER_PARAMETERS> ${CHE_IMAGE_FULLNAME} [COMMAND]

MANDATORY DOCKER PARAMETERS:
  -v <LOCAL_PATH>:${CHE_CONTAINER_ROOT}         Where user, instance, and log data saved
  -v /var/run/docker.sock:/var/run/docker.sock
  -it

OPTIONAL DOCKER PARAMETERS:
  -e CHE_HOST=<YOUR_HOST>              IP address or hostname where ${CHE_FORMAL_PRODUCT_NAME} will serve its users
  -e CHE_PORT=<YOUR_PORT>              Port where ${CHE_FORMAL_PRODUCT_NAME} will bind itself to
  -v <LOCAL_PATH>:/data/instance       Where instance, user, log data will be saved
  -v <LOCAL_PATH>:/data/backup         Where backup files will be saved
  -v <LOCAL_PATH>:/cli                 Where the CLI trace log is saved
  -v <LOCAL_PATH>:/repo                ${CHE_FORMAL_PRODUCT_NAME} git repo to activate dev mode
  -v <LOCAL_PATH>:/sync                Where remote ws files will be copied with sync command
  -v <LOCAL_PATH>:/unison              Where unison profile for optimzing sync command resides
    
COMMANDS:
  help                                 This message
  version                              Installed version and upgrade paths
  init                                 Initializes a directory with a ${CHE_FORMAL_PRODUCT_NAME} install
  start                                Starts ${CHE_FORMAL_PRODUCT_NAME} services
  stop                                 Stops ${CHE_FORMAL_PRODUCT_NAME} services
  restart                              Restart ${CHE_FORMAL_PRODUCT_NAME} services
  destroy                              Stops services, and deletes ${CHE_FORMAL_PRODUCT_NAME} instance data
  rmi                                  Removes the Docker images for <version>, forcing a repull
  config                               Generates a ${CHE_FORMAL_PRODUCT_NAME} config from vars; run on any start / restart
  upgrade                              Upgrades ${CHE_FORMAL_PRODUCT_NAME} from one version to another with migrations and backups
  download                             Pulls Docker images for the current ${CHE_FORMAL_PRODUCT_NAME} version
  backup                               Backups ${CHE_FORMAL_PRODUCT_NAME} configuration and data to ${CHE_CONTAINER_ROOT}/backup volume mount
  restore                              Restores ${CHE_FORMAL_PRODUCT_NAME} configuration and data from ${CHE_CONTAINER_ROOT}/backup mount
  offline                              Saves ${CHE_FORMAL_PRODUCT_NAME} Docker images into TAR files for offline install
  info                                 Displays info about ${CHE_FORMAL_PRODUCT_NAME} and the CLI
  ssh <wksp-name> [machine-name]       SSH to a workspace if SSH agent enabled
  sync <wksp-name>                     Synchronize workspace with current working directory
  action <action-name>                 Start action on ${CHE_FORMAL_PRODUCT_NAME} instance
  test <test-name>                     Start test on ${CHE_FORMAL_PRODUCT_NAME} instance
```

In these docs, when you see `che-cli [COMMAND]`, it is assumed that you run the CLI with the full `docker run ...` syntax. We short hand the docs for readability.

#### Sample Start
For example, to start the nightly build of Che with its data saved on Windows in `C:\tmp`:
`docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock -v /c/tmp:/data eclipse/che-cli:5.0.0-latest start`

This installs a configuration, downloads Che's Docker images, run pre-flight port checks, boot Che's services, and run post-flight checks. You do not need root access to start Che, unless your environment requires it for Docker operations.

A successful start will display:
```
INFO: (che cli): Downloading cli-latest
INFO: (che cli): Checking registry for version 'nightly' images
INFO: (che-cli config): Generating che-cli configuration...
INFO: (che-cli config): Customizing docker-compose for Windows
INFO: (che-cli start): Preflight checks
         port 8080:  [OK]

INFO: (che-cli start): Starting containers...
INFO: (che-cli start): Server logs at "docker logs -f che_che_1"
INFO: (che-cli start): Server booting...
INFO: (che-cli start): Booted and reachable
INFO: (che-cli start): Ver: 5.0.0-M8-SNAPSHOT
INFO: (che-cli start): Use: http://10.0.75.2
INFO: (che-cli start): API: http://10.0.75.2/swagger
```

#### Versions
While we provide `nightly`, `latest`, and `5.0.0-latest` [redirection versions](#nightly-and-latest) which are tags that simplify helping you retrieve a certain build, you should always run Cche with a specific version label to avoid [redirection caching issues](#nightly-and-latest). So, running `docker run eclipse/che-cli` is great syntax for testing and getting started quickly, you should always run `docker run eclipse/che-cli:<version>` for production usage.

#### Volume Mounts
If you volume mount a single local folder to `<your-local-path>:/data`, then Che creates `/data/che.env` (configuration file), `/data/instance` (user data, projects, runtime logs, and database folder), and `/data/backup` (data backup folder).

However, if you do not want your `/instance`, and `/backup` folder to all be children of the same parent folder, you can set them individually with separate overrides.

```
docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock
                    -v <local-Che-folder>:/data
                    -v <local-instance-path>:/data/instance
                    -v <local-backup-path>:/data/backup
                       eclipse/che-cli:<version> [COMMAND]    

```

#### Hosting
If you are hosting Che at a cloud service like DigitalOcean, set `CHE_HOST` to the server's IP address or its DNS. We use an internal utility, `eclipse/che-ip`, to determine the default value for `CHE_HOST`, which is your server's IP address. This works well on desktops, but usually fails on hosted servers requiring you to explicitly set this value.

```
docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock
                    -v <local-path>:/data
                    -e CHE_HOST=<your-ip-or-host>
                       eclipse/che-cli:<version> [COMMAND]
``` 

## Uninstall
```
# Remove your che-cli configuration and destroy user projects and database
docker run eclipse/che-cli destroy

# Deletes Che's images from your Docker registry
docker run eclipse/che-cli rmi
```

## Configuration
Configuration is done with environment variables in `che.env` placed into the root of the folder you volume mounted to `:/data`. Environment variables are stored in `che.env`, a file that is generated during the `che-cli init` phase. If you rerun `che-cli init` in an already initialized folder, the process will abort unless you pass `--force`, `--pull`, or `--reinit`. 

Each variable is documented with an explanation and usually commented out. If you need to set a variable, uncomment it and configure it with your value. You can then run `che-cli config` to apply this configuration to your system. `che-cli start` also reapplies the latest configuration.

You can run `che-cli init` to install a new configuration into an empty directory. This command uses the `che-cli/init:<version>` Docker container to deliver a version-specific set of puppet templates into the folder.

If you run `che-cli config`, Che runs puppet to transform your puppet templates into a Che instance configuration, placing the results into `/data/instance` if you volume mounted that, or into a `instance` subdirectory of the path you mounted to `/data`.  Each time you start Che, `che-cli config` is run to ensure instance configuration files are properly generated and consistent with the configuration you have specified in `che.env`.

#### Saving Configuration in Version Control
Administration teams that want to version control your che-cli configuration should save `che.env`. This is the only file that should be saved with version control. It is not necessary, and even discouraged, to save the other files. If you were to perform a `che-cli upgrade` we may replace these files with templates that are specific to the version that is being upgraded. The `che.env` file maintains fidelity between versions and we can generate instance configurations from that.

The version control sequence would be:
1. `che-cli init` to get an initial configuration for a particular version.
2. Edit `che.env` with your environment-specific configuration.
3. Save `che.env` to version control.
4. When pulling from version control, copy `che.env` into the root of the folder you volume mount to `:/data`.
5. You can then run `che-cli config` or `che-cli start` and the instance configuration will be generated from this file.
    
#### Logs and User Data
When Che initializes itself, it stores logs, user data, database data, and instance-specific configuration in the folder mounted to `/data/instance` or an `instance` subfolder of what you mounted to `/data`.  

Che's containers save their logs in the same location:
```
/logs/che/2016                 # Server logs
/logs/che/che-machine-logs     # Workspace logs
```

User data is stored in:
```
/data/che                      # Project backups (we synchronize projs from remote ws here)
/data/registry                 # Workspace snapshots
```

Instance configuration is generated by Che and is updated by our internal configuration utilities. These 'generated' configuration files should not be modified and stored in:
```
/instance/che.ver                           # Version of Che installed
/instance/docker-compose-container.yml      # Docker compose to launch internal services
/instance/docker-compose.yml                # Docker compose to launch Che from the host without contianer
/instance/config                            # Configuration files which are input mounted into the containers
```

#### oAuth
TODO: ADD in CHE OAUTH

#### Development Mode
For Che developers that are building and customizing Che from its source repository, you can run Che in development mode where your local assembly is used instead of the one that is provided in the default containers downloaded from DockerHub. This allows for a rapid edit / build / run cycle. 

Dev mode is activated by volume mounting the Che git repository to `:/repo` in your Docker run command.
```
docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock \
                    -v <local-path>:/data \
                    -v <local-repo>:/repo \
                       eclipse/che-cli:<version> [COMMAND]
``` 
Dev mode will use files from your host repository in three ways:

1. During the `che-cli config` phase, the source repository's `/dockerfiles/init/modules` and `/dockerfiles/init/manifests` will be used instead of the ones that are included in the `eclipse/che-init` container.
2. During the CLI bootstrap phase, the source repository's `/dockerfiles/cli/cli.sh` file will be used instead of the one within the `eclipse/che-cli` container. This allows CLI developers to iterate without having to rebuild `eclipse/che-cli` container after each change.
3. During the `che-cli start` phase, a local assembly from `assembly/assembly-main/target/eclipse-che-*` is mounted into the `eclipse/che-server` runtime container. You must `mvn clean install` the `assembly/assembly-main/` folder prior to activating development mode.

To activate jpda suspend mode for debugging Che server initialization, in the `che.env`:
```
CHE_DEBUG_SUSPEND=true
```
To change the Che debug port, in the `che.env`:
```
CHE_DEBUG_PORT=8000
```

#### Licensing
Eclipse Che is open sourced under the Eclipse Public License.

#### Hostname
The IP address or DNS name of where the Che endpoint will service your users. If you are running this on a local system, we auto-detect this value as the IP address of your Docker daemon. On many systems, especially those from cloud hosters like DigitalOcean, you may have to explicitly set this to the external IP address or DNS entry provided by the provider. You can pass it during initialization to the docker command:

```
docker run <other-syntax-here> -e CHE_HOST=<ip address or dns entry> eclipse/che-cli:<version> start
```

#### Workspace Limits
TODO: REVIEW LIST OF LIMITS

#### Private Docker Registries
Some enterprises use a trusted Docker registry to store their Docker images. If you want your workspace stacks and machines to be powered by these images, then you need to configure each registry and the credentialed access. Once these registries are configured, then you can have users or team leaders create stacks that use recipes with Dockerfiles or images using the `FROM <your-registry>/<your-repo>` syntax.

There are different configurations for AWS EC2 and the Docker regsitry. You can define as many different registries as you'd like, using the numerical indicator in the environment variable. In case of adding several registries just copy set of properties and append `REGISTRY[n]` for each variable.

In `che.env` file:
```
Che_DOCKER_REGISTRY_AUTH_REGISTRY1_URL=url1
Che_DOCKER_REGISTRY_AUTH_REGISTRY1_USERNAME=username1
Che_DOCKER_REGISTRY_AUTH_REGISTRY1_PASSWORD=password1

Che_DOCKER_REGISTRY_AWS_REGISTRY1_ID=id1
Che_DOCKER_REGISTRY_AWS_REGISTRY1_REGION=region1
Che_DOCKER_REGISTRY_AWS_REGISTRY1_ACCESS__KEY__ID=key_id1
Che_DOCKER_REGISTRY_AWS_REGISTRY1_SECRET__ACCESS__KEY=secret1
```

## Managing

#### Scaling
You can resize the physical node that you are running Che without disruptings its services. You can also consider running a Che farm of multiple Che servers with a load balancer or proxy in front, though this does require manual configuration. You can see GitHub issues of users in our GitHub history that have set this up. 

You can also consider using Codenvy, which has an embedded system for adding and removing additional physical nodes to provide additional resources for scaling workspaces.

#### Upgrading
Upgrading Che is done by downloading a `eclipse/che-cli:<version>` that is newer than the version you currently have installed. For example, if you have 5.0.0-M2 installed and want to upgrade to 5.0.0-M7, then:
```
# Get the new version of Che
docker pull eclipse/che-cli:5.0.0-M7

# You now have two eclipse/che-cli images (one for each version)
# Perform an upgrade - use the new image to upgrade old installation
docker run <volume-mounts> eclipse/che-cli:5.0.0-M7 upgrade
``` 

The upgrade command has numerous checks to prevent you from upgrading Che if the new image and the old version are not compatible. In order for the upgrade procedure to advance, the CLI image must be newer that the version in `/instance/che.ver`.

The upgrade process: a) performs a version compatibility check, b) downloads new Docker images that are needed to run the new version of Che, c) stops Che if it is currently running triggering a maintenance window, d) backs up your installation, e) initializes the new version, and f) starts Che.

You can run `che-cli version` to see the list of available versions that you can upgrade to.

#### Backup
You can run `che-cli backup` to create a copy of the relevant configuration information, user data, projects, and workspaces. We do not save workspace snapshots as part of a routine backup exercise. You can run `che-cli restore` to recover Che from a particular backup snapshot. The backup is saved as a TAR file that you can keep in your records.

#### Migration
We currently do not support migrating from the puppet-based configuration of Che to the Dockerized version. We do have a manual process which can be followed to move data between the puppet and Dockerized versions. The versions must be identical. Contact us to let our support team perform this migration for you.

#### Disaster Recovery
We maintain a disaster recovery [policy and best practices](http://che.readme.io/v5.0/docs/disaster-recovery).

## CLI Reference
The CLI is configured to hide most error conditions from the output screen. The CLI prints internal stack traces and error output to `cli.log`. The 'cli.log' is saved in the same folder where you mounted `:/data`.

### `che-cli init`
Initializes an empty directory with a Che configuration and instance folder where user data and runtime configuration will be stored. You must provide a `<path>:/data` volume mount, then Che creates an `instance` and `backup` subfolder of `<path>`. You can optionally override the location of `instance` by volume mounting an additional local folder to `:/data/instance`. You can optionally override the location of where backups are stored by volume mounting an additional local folder to `:/data/backup`.  After initialization, a `che.env` file is placed into the root of the path that you mounted to `:/data`. 

These variables can be set in your local environment shell before running and they will be respected during initialization:

| Variable | Description |
|----------|-------------|
| `CHE_HOST` | The IP address or DNS name of the Che service. We use `eclipse/che-ip` to attempt discovery if not set. |

Che depends upon Docker images. We use Docker images in three ways:
1. As cross-platform utilites within the CLI. For example, in scenarios where we need to perform a `curl` operation, we use a small Docker image to perform this function. We do this as a precaution as many operating systems (like Windows) do not have curl installed.
2. To perform initialization and configuration of Che such as with `eclipse/che-init`. This image contains templates that are delivered as a payload and installed onto your computer. These payload images have different files based upon the image's version.
4. To run the Che server.

You can control the nature of how che-cli downloads these images with command line options. All image downloads are performed with `docker pull`. 

| Mode>>>> | Description |
|------|-------------|
| `--no-force` | Default behavior. Will download an image if not found locally. A local check of the image will see if an image of a matching name is in your local registry and then skip the pull if it is found. This mode does not check DockerHub for a newer version of the same image. |
| `--pull` | Will always perform a `docker pull` when an image is requested. If there is a newer version of the same tagged image at DockerHub, it will pull it, or use the one in local cache. This keeps your images up to date, but execution is slower. |
| `--force` | Performs a forced removal of the local image using `docker rmi` and then pulls it again (anew) from DockerHub. You can use this as a way to clean your local cache and ensure that all images are new. |
| `--offline` | Loads Docker images from `backup/*.tar` folder during a pre-boot mode of the CLI. Used if you are performing an installation or start while disconnected from the Internet. |

You can reinstall Che on a folder that is already initialized and preserve your `/data/che.env` values by passing the `--reinit` flag.

### `che-cli config`
Generates a Che instance configuration thta is placed in `/data/instance`. This command uses puppet to generate configuration files for the Che server and Docker swarm This command is executed on every `start` or `restart`.

If you are using a `eclipse/che-cli:<version>` image and it does not match the version that is in `/instance/che.ver`, then the configuration will abort to prevent you from running a configuration for a different version than what is currently installed.

This command respects `--no-force`, `--pull`, `--force`, and `--offline`.

### `che-cli start`
Starts Che and its services using `docker-compose`. If the system cannot find a valid configuration it will perform a `che-cli init`. Every `start` and `restart` will run a `che-cli config` to generate a new configuration set using the latest configuration. The starting sequence will perform pre-flight testing to see if any ports required by Che are currently used by other services and post-flight checks to verify access to key APIs.  

### `che-cli stop`
Stops all of the Che service containers and removes them.

### `che-cli restart`
Performs a `che-cli stop` followed by a `che-cli start`, respecting `--pull`, `--force`, and `--offline`.

### `che-cli destroy`
Deletes `/docs`, `che.env` and `/data/instance`, including destroying all user workspaces, projects, data, and user database. If you pass `--quiet` then the confirmation warning will be skipped. 

We write the `cli.log` to your ':/data' directory. By default, this log is not destroyed in a `che-cli destroy` command so that you can maintain a record of all CLI executions. You can have this file removed from your host with the `--cli` parameter.

### `che-cli offline`
Saves all of the Docker images that Che requires into `/backup/*.tar` files. Each image is saved as its own file. If the `backup` folder is available on a machine that is disconnected from the Internet and you start Che with `--offline`, the CLI pre-boot sequence will load all of the Docker images in the `/backup/` folder.

### `che-cli rmi`
Deletes the Docker images from the local registry that Che has downloaded for this version.

### `che-cli download`
Used to download Docker images that will be stored in your Docker images repository. This command downloads images that are used by the CLI as utilities, for Che to do initialization and configuration, and for the runtime images that Che needs when it starts.  This command respects `--offline`, `--pull`, `--force`, and `--no-force` (default).  This command is invoked by `che-cli init`, `che-cli config`, and `che-cli start`.

This command is invoked by `che-cli init` before initialization to download the images for the version specified by `eclipse/che-cli:<version>`.

### `che-cli version`
Provides information on the current version and the available versions that are hosted in Che's repositories. `che-cli upgrade` enforces upgrade sequences and will prevent you from upgrading one version to another version where data migrations cannot be guaranteed.

### `che-cli upgrade`
Manages the sequence of upgrading Che from one version to another. Run `che-cli version` to get a list of available versions that you can upgrade to.

Upgrading Che is done by using a `eclipse/che-cli:<version>` that is newer than the version you currently have installed. For example, if you have 5.0.0-M2 installed and want to upgrade to 5.0.0-M7, then:
```
# Get the new version of Che
docker pull eclipse/che-cli:5.0.0-M7

# You now have two eclipse/che-cli images (one for each version)
# Perform an upgrade - use the new image to upgrade old installation
docker run <volume-mounts> eclipse/che-cli:5.0.0-M7 upgrade
``` 

The upgrade command has numerous checks to prevent you from upgrading Che if the new image and the old version are not compatiable. In order for the upgrade procedure to proceed, the CLI image must be newer than the value of '/instance/che.ver'.

The upgrade process: a) performs a version compatibility check, b) downloads new Docker images that are needed to run the new version of Che, c) stops Che if it is currently running triggering a maintenance window, d) backs up your installation, e) initializes the new version, and f) starts Che.

You can run `che-cli version` to see the list of available versions that you can upgrade to.

### `che-cli info`
Displays system state and debugging information. `--network` runs a test to take your `Che_HOST` value to test for networking connectivity simulating browser > Che and Che > workspace connectivity.

### `che-cli backup`
Tars your `/instance` into files and places them into `/backup`. These files are restoration-ready.

### `che-cli restore`
Restores `/instance` to its previous state. You do not need to worry about having the right Docker images. The normal start / stop / restart cycle ensures that the proper Docker images are available or downloaded, if not found.

This command will destroy your existing `/instance` folder, so use with caution, or set these values to different folders when performing a restore.
