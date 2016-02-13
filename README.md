# Eclipse Che
[![Join the chat at https://gitter.im/eclipse/che](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/eclipse/che?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Windows Build status](https://ci.appveyor.com/api/projects/status/ro78pmwomlklkwbo?svg=true)](https://ci.appveyor.com/project/codenvy/che)

[![Eclipse License](http://img.shields.io/badge/license-Eclipse-brightgreen.svg)](https://github.com/codenvy/che/blob/master/LICENSE)
[![latest 3.x](https://img.shields.io/badge/latest stable-3.x-green.svg)](https://github.com/codenvy/che/tree/3.x)
[![latest 4.x](https://img.shields.io/badge/latest dev-4.x-yellowgreen.svg)](https://github.com/codenvy/che/tree/master)



https://www.eclipse.org/che/

Next-generation Eclipse IDE. Open source workspace server and cloud IDE.

![Eclipse Che](https://www.eclipse.org/che/images/hero-home.png "Eclipse Che")

### A Better Workspace
Workspaces are composed of projects and runtime environments. Create portable workspace replicas that run anywhere in the cloud or on your desktop ... [Read More](https://www.eclipse.org/che/features/#new-workspace)
![Eclipse Che](https://www.eclipse.org/che/images/features/img-features-a-new-kind-of-workspace.png "Che Workspace")

### Cloud IDE
A no-installation browser IDE and IOE accessible from any local or remote device. Thin, fast, and beautiful - it's the IDE our own engineers wanted ... [Read More](https://www.eclipse.org/che/features/#cloud-ide)
![Eclipse Che](https://www.eclipse.org/che/images/features/img-features-cloud-ide.png "Cloud IDE")

### Collaborative Workspace Server
Host Eclipse Che as a workspace server. Share tools, runtime and programming services across workspaces and teams. Control workspaces with REST APIs ... [Read More](https://www.eclipse.org/che/features/#collaborative)
![Eclipse Che](https://www.eclipse.org/che/images/features/img-features-collaborative-workspace-server.png "Che Dashboard")

### Docker-Powered Environments
Workspace runtimes are Docker-powered. Use our all-in-one stacks, pull from any registry, or author your own. Snapshot and embed runtimes into ... [Read More](https://www.eclipse.org/che/features/#docker-powered)
![Eclipse Che](https://www.eclipse.org/che/images/features/img-features-docker-powered.png "Docker Workspaces")

Che can be installed on any OS that supports Java 1.8 - desktop, server or cloud, and Maven 3.3.1 or higher. It has been tested on Ubuntu, Linux, MacOS and Windows. 

### License
Che is open sourced under the Eclipse Public License 1.0.

### Dependencies
* Docker 1.8+
* Maven 3.3.1+
* Java 1.8

### Clone

```sh
git clone https://github.com/codenvy/che.git
```
If master is unstable, checkout the latest tagged version.

### Build and Run
```sh
cd che
mvn clean install

# A new assembly is packaged into:
cd assembly-main/target/eclipse-che-<version>/eclipse-che-<version>

# Executable files are:
bin/che.sh
bin/che.bat
```
Che will be available at ```localhost:8080```.
If you want to run Che as a server, please see docs @ eclipse.org/che on additional flags to enable remote clients.


### Repositories
These repositories are for the core project hosted at `http://github.com/eclipse`.
```
/che
/che/assembly                                             # Generates binary assemblies of Che
/che/assembly/assembly-main                               # Final packaging phase
/che/assembly/assembly-ide-war                            # Creates the IDE.war from plug-ins & core
/che/assembly/assembly-machine-war                        # Creates the agent WAR from plug-ins & core
/che/assembly/assembly-machine-server                     # Creates the agent server that goes into ws
/che/core                                                 # Platform APIs
/che/dashboard                                            # AngularJS app for managing Che
/che/plugins                                              # IDE & agent plug-ins

/che-lib                                                  # Forked dependencies that require mods
/che-lib/swagger
/che-lib/terminal
/che-lib/websocket
/che-lib/pty
/che-lib/che-tomcat8-slf4j-logback

# /che and /che-lib depend upon /che-dependencies
/che-dependencies                                          # Maven dependencies used by che
/che-dev                                                   # Code style and license header

# /che-dependencies and /che-dev depend upon /che-parent
/che-parent                                                # Maven plugins and profiles
```

### Other Repositories
These are external repositories that provide additional tools for Eclipse Che.
```
http://github.com/codenvy/che-installer                    # Creates the Windows and JAR installer packages
http://github.com/codenvy/che-tutorials                    # SDK examples and tutorials (needs updating)
http://github.com/che-samples                              # GitHub organization with sample repos used in Che
http://git.eclipse.org/c/www.eclipse.org/che.git           # The content for eclipse.org/che Web site
http://github.com/codenvy/cli                              # Experimental CLI for managing Che workspaces on the CLI
```

### Engage
* **Contribute:** We accept pull requests. Please see [how to contribute] (https://github.com/codenvy/che/blob/master/CONTRIBUTING.md).
* **Support:** You can report bugs using GitHub issues.
* **Developers:** Plug-in developers can get API help at [che-dev@eclipse.org](email:che-dev@eclipse.org). 
* **Website:** [eclipse.org/che](https://eclipse.org/che).
