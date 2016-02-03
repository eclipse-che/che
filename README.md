# Eclipse Che
[![Join the chat at https://gitter.im/eclipse/che](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/eclipse/che?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Windows Build status](https://ci.appveyor.com/api/projects/status/ro78pmwomlklkwbo?svg=true)](https://ci.appveyor.com/project/codenvy/che)

[![Eclipse License](http://img.shields.io/badge/license-Eclipse-brightgreen.svg)](https://github.com/codenvy/che/blob/master/LICENSE)
[![latest 3.x](https://img.shields.io/badge/latest stable-3.x-green.svg)](https://github.com/codenvy/che/tree/3.x)
[![latest 4.x](https://img.shields.io/badge/latest dev-4.x-yellowgreen.svg)](https://github.com/codenvy/che/tree/master)



https://www.eclipse.org/che/

Next-generation Eclipse IDE. Open source workspace server and cloud IDE.

![Eclipse Che](https://www.eclipse.org/che/images/hero-home.png "Eclipse Che")

### Cloud IDE
Use your browser to program on any machine in any language. Edit, build, debug and deploy projects bound to source repositories.

### Workspace Server
Create developer workspaces with APIs. Add your project types, embed custom commands and host on any infrastructure.

### Plug-Ins
Use Che's built-in language plug-ins or write packaged extensions that transform Che's IDE into new tools and assemblies.

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
bin/che.sh  [ start | stop ]
bin/che.bat [ start | stop ]
```
Che will be available at ```localhost:8080```


### Repositories:
* **che-parent**:              [Maven parent POM] (http://github.com/codenvy/che-parent)
* **che-depmgt**:              [Maven dependency management POM] (http://github.com/codenvy/che-depmgt)
* **che-core**:                [Core components] (http://github.com/codenvy/che-core)
* **che-plugins**:             [Language & tooling extensions] (http://github.com/codenvy/che-plugins)
* **che-dashboard**:           [Workspace, project, user mgmt] (http://github.com/codenvy/che-dashboard)
* **che-websocket-terminal**:  [Embedded terminal for workspaces] (http://github.com/codenvy/che-websocket-terminal)
* **che-installer**:           [Che installation packages] (http://github.com/codenvy/che-installer)
* **che-tutorials**:           [SDK examples and tutorials] (http://github.com/codenvy/che-tutorials)
* **che-samples**:             [Templates and samples loaded into the IDE] (http://github.com/che-samples)
* **che-site**:                [Che web site content] (http://git.eclipse.org/c/www.eclipse.org/che.git)
* **che-specifications**:      [Roadmap and technical plans for the future] (http://github.com/codenvy/che-specifications)
* **cli**:                     [CLI for interacting with Che remotely] (http://github.com/codenvy/cli)

### Engage
* **Contribute:** We accept pull requests. Please see [how to contribute] (https://github.com/codenvy/che/blob/master/CONTRIBUTING.md).
* **Support:** You can report bugs using GitHub issues
* **Developers:** Plug-in developers can get API help at [che-dev@eclipse.org](email:che-dev@eclipse.org). 
* **Website:** [eclipse.org/che](https://eclipse.org/che)
