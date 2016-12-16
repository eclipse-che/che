---
tags: [ "eclipse" , "che" ]
title: Usage&#58 CLI
excerpt: "Installing and using the Eclipse Che CLI"
layout: docs
permalink: /:categories/cli/
---
#### Experimental
The CLI is currently for experimentation. Please provide feedback by [logging issues](https://github.com/eclipse/che/issues) in our GitHub repo.  

The CLI simplifies operation of Che and is available on all the OS that Che supports. Additionally, the CLI simplifies updating and executing the different tools that we have packaged as Docker containers.
# Install  
You can install the CLI scripts from the [Che GitHub repo](https://github.com/eclipse/che). Windows users will need to first install [git for Windows](https://git-scm.com/download/win).
```shell  
$ curl -sL https://raw.githubusercontent.com/eclipse/che/master/che.sh > che
$ chmod 755 che && mv che /usr/local/bin/\
```
```shell  
\
```

```shell  
# You need both che.bat and che.sh
curl -sL https://raw.githubusercontent.com/eclipse/che/master/che.sh > che.sh
curl -sL https://raw.githubusercontent.com/eclipse/che/master/che.bat > che.bat

# Add the files to your PATH
set PATH=<path-to-cli>;%PATH%\
```
```shell  
\
```
### Upgrade the CLI
The URLs provided are for the latest version of the CLI that is saved within our source repositories. We tag and version the CLI for each version of Che. You can grab right right files with `che-<version>.sh` and `che-<version>.bat` from `https://install.codenvycorp.com/che/`.
```shell  
# The latest released version - save on top of your existing CLI files
https://install.codenvycorp.com/che/che.bat
https://install.codenvycorp.com/che/che.sh

# Specific version of the CLI
https://install.codenvycorp.com/che/che-4.6.2.bat
https://install.codenvycorp.com/che/che-4.6.2.sh\
```

# Use  

```text  
Usage: che [COMMAND]
           start                              Starts che server
           stop                               Stops che server
           restart                            Restart che server
           update                             Pulls specific version, respecting CHE_VERSION
           profile add <name>                 Add a profile to ~/.che/
           profile set <name>                 Set this profile as the default for che CLI
           profile unset                      Removes the default profile - leaves it unset
           profile rm <name>                  Remove this profile from ~/.che/
           profile update <name>              Update profile in ~/.che/
           profile info <name>                Print the profile configuration
           profile list                       List available profiles
           mount <local-path> <ws-ssh-port>   Synchronize workspace to a local directory
           dir init                           Initialize directory with che configuration
           dir up                             Create workspace from source in current directory
           dir down                           Stop workspace running in current directory
           dir status                         Display status of che in current directory
           action <action-name> [--help]      Start action on che instance
           test <test-name> [--help]          Start test on che instance
           info [ --all                       Run all debugging tests
                  --server                    Run che launcher and server debugging tests
                  --networking                Test connectivity between che sub-systems
                  --cli                       Print CLI (this program) debugging info
                  --create [<url>]            Test creating a workspace and project in che
                           [<user>]
                           [<pass>] ]\
```

# Profiles  
Most Che configuration parameters are done through system environment variables. If you have these set, the CLI will detect these values and pass them along to the `che-launcher`, `che-server`, `che-mount`, `che-dev`, and `che-dir` utilities. You can save sets of environment configurations as a profile.

When creating a profile, the CLI will take the values of currently set Che environment variables and place them into a profile. If you "set" a profile, then those environment variables will be loaded before any of the Che utilities are called.

You can use profiles to set up different configurations of Che servers so that you can switch between different servers, launch them, and avoid having conflicts with container names, ports, and output.
```shell  
# Configure some non-standard environment variables
export CHE_PORT=9000
export CHE_SERVER_CONTAINER_NAME=my-home-che-server

# Add a profile named food-network
che profile add food-network
INFO:
INFO: Added new che CLI profile ~/.che/profiles/food-network.
INFO:

# Display its contents
che profile info food-network
DEBUG: ---------------------------------------
DEBUG: ---------   CLI PROFILE INFO   --------
DEBUG: ---------------------------------------
DEBUG:
DEBUG: Profile ~/.che/profiles/food-network contains:
DEBUG: CHE_DIR_IMAGE_NAME=eclipse/che-dir
DEBUG: CHE_LAUNCHER_IMAGE_NAME=eclipse/che-launcher
DEBUG: CHE_MOUNT_IMAGE_NAME=eclipse/che-mount
DEBUG: CHE_PORT=9000
DEBUG: CHE_SERVER_CONTAINER_NAME=my-home-che-server
DEBUG: CHE_SERVER_IMAGE_NAME=eclipse/che-server
DEBUG: CHE_TEST_IMAGE_NAME=eclipse/che-test

# Set the food-network profile to be used by other utilities
che profile set food-network
INFO:
INFO: Set active che CLI profile to ~/.che/profiles/food-network.
INFO:

# Start Che with the currently set configuration
che start

# Unset the default configuration (CLI uses your current environment values)
che profile unset

# Update an existing profile with the current values of environment variables
export CHE_PORT=10000
che profile update food-network

# List all available profiles
che profile list\
```

# Chefiles  

#### Experimental
Chefiles are experimental starting with 4.6. The Chefile syntax is not locked and may change frequently.  

Chefiles let you create and configure lightweight, portable developer workspaces using a git repo as the basis for a project in a workspace. If you do not have a Che server running, one will be started in the background.

The source code that is in the current directory will be used to populate the project(s) within the workspace. Git, version control, editing, and commands used within Che will be executed against the files in the directory, which are mounted within the workspace.

Create a single file for your project to describe the type of workspace you want, the software that needs to be installed, and the way you want to access the machine. Store this file with your project code. Run a single command - `che up` — and watch Che put together a complete workspace in a Che server.
```shell  
# Initilize a directory with a Chefile configuration - optional
che init

# Convert the current directory into a Che workspace, starting Che if necessary
che up

# Stop the workspace associated with the current directory
che down\
```
## Example
```shell  
$ git clone http://github.com/benoitf/spring-petclinic
$ cd spring-petclinic
$ che up

INFO: ECLIPSE CHE: FOUND IMAGE eclipse/che-dir:nightly
INFO: ECLIPSE CHE FILE: LAUNCHING CONTAINER
INFO: ECLIPSE CHE FILE: ADDED CHE CONFIGURATION
INFO: ECLIPSE CHE FILE: STARTING CHE
INFO: ECLIPSE CHE: ALREADY HAVE IMAGE eclipse/che-server:nightly
INFO: ECLIPSE CHE: CONTAINER STARTING
INFO: ECLIPSE CHE: SERVER LOGS AT "docker logs -f che-server"
INFO: ECLIPSE CHE: SERVER BOOTING...
INFO: ECLIPSE CHE: BOOTED AND REACHABLE

Open browser to http://10.0.75.2:8080/che/local
```
This example creates a new workspace named local in a Che server running in the background. When the command started, Che was not started, and the Che launcher was silently called to start Che with its default configuration.

## File Structure
```text  
Chefile                    # Optional configuration file
/.che/conf/che.properties  # Used to define the behavior of the Che server
/.che/workspaces           # Workspace meta data\
```
## Chefile
Add an optional Chefile to your directory to provide instructions on how Che should launch itself and rules for how the workspace should be created.
```json  
che.server.type = [local | codenvy]
che.server.ip = localhost
che.server.port = 8080
che.server.user = admin
che.server.pass = password
che.server.startup = [insert startup params to pass to docker run]

# Default is latest, but can be "nightly" or a tagged version
che.server.version = latest

# new = always create new workspace for every che up command
# reuse = create new if not exist, otherwise reopen existing workspace
workspace.create = [new | reuse]
workspace.recipe = [default | file | inline | url]
workspace.recipe.location = {}
workspace.name = “happy”
workspace.ram = 2048

# Something similar to a vagrant provisioner syntax
workspace.command.add =

# Default = local directory
# Where to load the code for the workspace from
# A workspace can have multiple projects imported
project.importer = [directory | zip | git | svn]
project.location = http://github.com/eclipse/che
project.type = maven
project[2].importer = [directory | zip | git | svn]
```
## Docker Syntax
Like most everything we do with Che, Chefiles are packaged and executed as Docker containers. You can run the Docker container directly. `${CURRENT_DIRECTORY}` must be an absolute directory and if you are using Docker for Windows, the format of the drive must be `/c/my/path`.
```text  
  docker run -it --rm --name chefile \
         -v /var/run/docker.sock:/var/run/docker.sock \
         -v "$CURRENT_DIRECTORY":"$CURRENT_DIRECTORY" \
         eclipse/che-dir \
         "${CURRENT_DIRECTORY}" < up | init | down >
```

# Under the Covers  
The Che CLI is a simplification provided for launching our various Docker containers. We provide Docker images for performing certain, repetitive tasks. You can use these containers directly.

The full syntax for how to build and run each Docker image is provided in the Dockerfile for each one, which is maintained in our GitHub repository. Each of these images have `:latest`, `:nightly`, and `:<version>` tags for the images on DockerHub.


| Image>>>>>>>>>>>>>>   | Description   | `eclipse/che-dev`   
| --- | --- | ---
| `eclipse/che-dir`   | `eclipse/che-ip`   | `eclipse/che-launcher`\n`eclipse/che`   
| `eclipse/che-mount`   | `eclipse/che-server`   | `eclipse/che-test`   
| An image that contains all of the libraries and utilties necessary to compile Che extensions and custom assemblies.   | An image that enables the conversion of local directories into Che workspaces. Think of it as Vagrant, but for Che servers and workspaces.   | An image that returns the IP address of your Docker daemon. Used by our various containers to discover their environment.   
| An image that is responsible for launching `eclipse/che-server` with a proper configuration. Configuration of how to launch `eclipse/che-server` varies by the operating system, user defined environment variables, and Docker installation type.\n\nThe syntax for using this container is the same as documented at [Usage: Local](doc:usage-docker).   | An image that synchornizes a remote Che workspace to a local path.   | The Che server itself. Contains the application server and libraries to launch a single instance of Che.   
| An image that performs smoke tests against Che servers by creating workspaces and projects to verify that the system's networking and other properties have been properly established.   | Dockerfile   | [Dockerfile](https://github.com/eclipse/che-dockerfiles/blob/master/che-dev/Dockerfile)   
| [Dockerfile](https://github.com/eclipse/che-dockerfiles/blob/master/che-dir/Dockerfile)   | [Dockerfile](https://github.com/eclipse/che-dockerfiles/blob/master/che-ip/Dockerfile)   | [Dockerfile](https://github.com/eclipse/che-dockerfiles/blob/master/che-launcher/Dockerfile)   
| [Dockerfile](https://github.com/eclipse/che-dockerfiles/blob/master/che-mount/Dockerfile)   | [Dockerfile](https://github.com/eclipse/che/blob/master/dockerfiles/che-server/Dockerfile)   | [Dockerfile](https://github.com/eclipse/che-dockerfiles/blob/master/che-test/Dockerfile)   
