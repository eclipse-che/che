---
title: CLI Reference
excerpt: "Manage your Che installation on the command line."
layout: docs
permalink: /:categories/cli/
---
The CLI will hide most error conditions from standard out. Internal stack traces and error output is redirected to `cli.log`, which is saved in the host folder where `:/data` is mounted.

## init  
Initializes an empty directory with a Che configuration and instance folder where user data and runtime configuration will be stored. You must provide a `<path>:/data` volume mount, then Che creates a `instance` and `backup` subfolder of `<path>`. You can optionally override the location of `instance` by volume mounting an additional local folder to `/data/instance`. You can optionally override the location of where backups are stored by volume mounting an additional local folder to `/data/backup`.  After initialization, a `che.env` file is placed into the root of the path that you mounted to `/data`.

These variables can be set in your local environment shell before running and they will be respected during initialization:

| Variable | Description |
|----------|-------------|
| `CHE_HOST` | The IP address or DNS name of the Che service. We use `eclipse/che-ip` to attempt discovery if not set. |

Che depends upon Docker images. We use Docker images to:
1. Provide cross-platform utilites within the CLI. For example, in scenarios where we need to perform a `curl` operation, we use a small Docker image to perform this function. We do this as a precaution as many operating systems (like Windows) do not have curl installed.
2. Look up the master version and upgrade manifest, which is saved within the CLI docker image in the /version subfolder.
3. Perform initialization and configuration of Che such as with `eclipse/che-init`. This image contains templates to be installed onto your computer used by the CLI to configure Che for your specific OS.

You can control how Che downloads these images with command line options. All image downloads are performed with `docker pull`.

| Mode | Description |
|------|-------------|
| `--no-force` | Default behavior. Will download an image if not found locally. A local check of the image will see if an image of a matching name is in your local registry and then skip the pull if it is found. This mode does not check DockerHub for a newer version of the same image. |
| `--pull` | Will always perform a `docker pull` when an image is requested. If there is a newer version of the same tagged image at DockerHub, it will pull it, or use the one in local cache. This keeps your images up to date, but execution is slower. |
| `--force` | Performs a forced removal of the local image using `docker rmi` and then pulls it again (anew) from DockerHub. You can use this as a way to clean your local cache and ensure that all images are new. |
| `--offline` | Loads Docker images from `backup/*.tar` folder during a pre-boot mode of the CLI. Used if you are performing an installation or start while disconnected from the Internet. |

You can reinstall Che on a folder that is already initialized and preserve your `/data/che.env` values by passing the `--reinit` flag.

## config
Generates a Che instance configuration thta is placed in `/instance`. This command uses puppet to generate Docker Compose configuration files to run Che and its associated server. Che's server configuration is generated as a che.properties file that is volume mounted into the Che server when it boots. This command is executed on every `start` or `restart`.

If you are using a `eclipse/che-cli:<version>` image and it does not match the version that is in `/instance/che.ver`, then the configuration will abort to prevent you from running a configuration for a different version than what is currently installed.

This command respects `--no-force`, `--pull`, `--force`, and `--offline`.

## start
Starts Che and its services using `docker-compose`. If the system cannot find a valid configuration it will perform an `init`. Every `start` and `restart` will run a `config` to generate a new configuration set using the latest configuration. The starting sequence will perform pre-flight testing to see if any ports required by Che are currently used by other services and post-flight checks to verify access to key APIs.  

## stop
Stops all of the Che service containers and removes them.

## restart
Performs a `stop` followed by a `start`, respecting `--pull`, `--force`, and `--offline`.

## destroy
Deletes `/docs`, `che.env` and `/instance`, including destroying all user workspaces, projects, data, and user database. If you pass `--quiet` then the confirmation warning will be skipped. Passing `--cli` will also destroy the `cli.log`. By default this is left behind for traceability.

## offline
Saves all of the Docker images that Che requires into `/backup/*.tar` files. Each image is saved as its own file. If the `backup` folder is available on a machine that is disconnected from the Internet and you start Che with `--offline`, the CLI pre-boot sequence will load all of the Docker images in the `/backup/` folder.

`--list` option will list all of the core images and optional stack images that can be downloaded. The core system images and the CLI will always be saved, if an existing TAR file is not found. `--image:<image-name>` will download a single stack image and can be used multiple times on the command line. You can use `--all-stacks` or `--no-stacks` to download all or none of the optional stack images.

## Che rmi
Deletes the Docker images from the local registry that Che has downloaded for this version.

## Che download
Used to download Docker images that will be stored in your Docker images repository. This command downloads images that are used by the CLI as utilities, for Che to do initialization and configuration, and for the runtime images that Che needs when it starts.  This command respects `--offline`, `--pull`, `--force`, and `--no-force` (default).  This command is invoked by `Che init`, `Che config`, and `Che start`.

This command is invoked by `Che init` before initialization to download the images for the version specified by `eclipse/che-cli:<version>`.

## Che version
Provides information on the current version and the available versions that are hosted in Che's repositories. `Che upgrade` enforces upgrade sequences and will prevent you from upgrading one version to another version where data migrations cannot be guaranteed.

## Che upgrade
Manages the sequence of upgrading Che from one version to another. Run `Che version` to get a list of available versions that you can upgrade to.

Upgrading Che is done by using a `eclipse/che-cli:<version>` that is newer than the version you currently have installed. For example, if you have 5.0.0-M2 installed and want to upgrade to 5.0.0-M7, then:
```
# Get the new version of Che
docker pull eclipse/che-cli:5.0.0-M7

# You now have two eclipse/che-cli images (one for each version)
# Perform an upgrade - use the new image to upgrade old installation
docker run <volume-mounts> eclipse/che-cli:5.0.0-M7 upgrade
```

The upgrade command has numerous checks to prevent you from upgrading Che if the new image and the old version are not compatiable. In order for the upgrade procedure to proceed, the CLI image must be newer than the value of '/instance/Che.ver'.

The upgrade process: a) performs a version compatibility check, b) downloads new Docker images that are needed to run the new version of Che, c) stops Che if it is currently running triggering a maintenance window, d) backs up your installation, e) initializes the new version, and f) starts Che.

You can run `Che version` to see the list of available versions that you can upgrade to.

## Che info
Displays system state and debugging information. `--network` runs a test to take your `Che_HOST` value to test for networking connectivity simulating browser > Che and Che > workspace connectivity.

## Che backup
Tars your `/instance` into files and places them into `/backup`. These files are restoration-ready.

## Che restore
Restores `/instance` to its previous state. You do not need to worry about having the right Docker images. The normal start / stop / restart cycle ensures that the proper Docker images are available or downloaded, if not found.

This command will destroy your existing `/instance` folder, so use with caution, or set these values to different folders when performing a restore.

## Che add-node
Adds a new physical node into the Che cluster. That node must have Docker pre-configured similar to how you have Docker configured on the master node, including any configurations that you add for proxies or an alternative key-value store like Consul. Che generates an automated script that can be run on each new node which prepares the node by installing some dependencies, adding the Che SSH key, and registering itself within the Che cluster.

## Che remove-node
Takes a single parameter, `ip`, which is the external IP address of the remote physical node to be removed from the Che cluster. This utility does not remove any software from the remote node, but it does ensure that workspace runtimes are not executing on that node.
