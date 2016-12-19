---
tags: [ "eclipse" , "che" ]
title: Getting Started&#58 Local
excerpt: ""
layout: docs
permalink: /:categories/getting-started/
---
# How to Get Help  
**Support:** If the unthinkable happens, or you have a question, you can post [issues on our GitHub page](https://github.com/eclipse/che/issues). Please follow the [guidelines on issue reporting](https://github.com/eclipse/che/blob/master/CONTRIBUTING.md).

**Documentation:** We put a lot of effort into our docs. Please add suggestions on areas for improvement.
# 0. Pre-Reqs  
To run Eclipse Che, you need:
1. Docker 1.8+ for [Linux](https://docs.docker.com/engine/installation/), [Windows](https://docs.docker.com/engine/installation/windows/) or [Mac](https://docs.docker.com/engine/installation/mac/).
2. Bash.

If you are using boot2docker for Windows or Mac, environment variables must be set in each terminal session.

Once Docker and bash are installed:
```shell  
# Should print "Hello from Docker!"
docker run hello-world

# Should open a bash shell (Windows too! - Docker installs Git Bash for Windows)
bash

# Windows users may have to add Git Bash to their path in system environment variables
# set PATH=<path-to-git>;%PATH%
set PATH=C:\Program Files\Git\bin\;%PATH%
```

# 1. Get the Eclipse Che CLI  

```shell  
sudo curl -sL https://raw.githubusercontent.com/eclipse/che/master/che.sh > /usr/local/bin/che
sudo chmod +x /usr/local/bin/che\
```

```shell  
# You need both che.bat and che.sh
> curl -sL https://raw.githubusercontent.com/eclipse/che/master/che.sh > che.sh
> curl -sL https://raw.githubusercontent.com/eclipse/che/master/che.bat > che.bat

# Add the files to your PATH
set PATH=<path-to-cli>;%PATH%\
```

# 2. Start Che  

```shell  
# Change where workspaces are saved. Default = /home/user/che
# export CHE_DATA=/home/user/reginald

# If Che will be accessed from other machines define your host IP
# export CHE_HOST_IP=<che-host-ip>

# Start Eclipse Che
che start
INFO: ECLIPSE CHE: Starting launcher
INFO: ECLIPSE CHE: Already have image eclipse/che-server:nightly
INFO: ECLIPSE CHE: Starting container...
INFO: ECLIPSE CHE: Server logs at "docker logs -f che-server"
INFO: ECLIPSE CHE: Server booting...
INFO: ECLIPSE CHE: Booted and reachable
INFO: ECLIPSE CHE: http://192.168.99.100:8080

# Stop Eclipse Che
che stop

# Update Eclipse Che to the nightly build
export CHE_VERSION=nightly
che update

# List all Che CLI commands
che help
```

```shell  
# If boot2docker, set workspace storage to a subdirectory of `%userprofile%`.
# If Docker for Windows on Windows 10, set to any directory.
# Docker is case sensitive and requires forward slashes without colons.
mkdir /c/Users/%userprofile%/<che-data-directory>
set CHE_DATA=/c/Users/%userprofile%/<che-data-directory>

# If Che will be accessed from other machines define your host IP
# set CHE_HOST_IP=<che-host-ip>

# Start Eclipse Che
che start
INFO: ECLIPSE CHE: Starting launcher
INFO: ECLIPSE CHE: Already have image eclipse/che-server:nightly
INFO: ECLIPSE CHE: Starting container...
INFO: ECLIPSE CHE: Server logs at "docker logs -f che-server"
INFO: ECLIPSE CHE: Server booting...
INFO: ECLIPSE CHE: Booted and reachable
INFO: ECLIPSE CHE: http://192.168.99.100:8080

# Stop Eclipse Che
che stop

# Update Eclipse Che to the nightly build
set CHE_VERSION=nightly
che update

# List all Che CLI commands
che help
```

#### Installing Behind a Proxy
If you are behind a proxy, you need to [configure your proxy settings](https://eclipse-che.readme.io/docs/configuration-proxies) for Docker and Eclipse Che.  


#### Configuration
Change Che's port, hostname, oAuth, Docker, git, and networking by setting [Eclipse Che properties](https://eclipse-che.readme.io/docs/usage-docker#environment-variables).  


# 3. Develop with Che  
Now that Che is running there are a lot of fun things to try:
- Become familiar with Che through [one of our tutorials](https://eclipse-che.readme.io/docs/get-started-with-java-and-che).
- [Import a project](https://eclipse-che.readme.io/docs/import-a-project) and setup [git authentication](https://eclipse-che.readme.io/docs/git).
- Use [commands](https://eclipse-che.readme.io/docs/commands) to build and run a project.
- Create a [preview URL](https://eclipse-che.readme.io/docs/previews) to share your app.
- Setup a [debugger](https://eclipse-che.readme.io/docs/debug).
- Experiment with [chedir](https://dash.readme.io/project/eclipse-che/docs/getting-started-chedir).
- Create a [custom stack](https://eclipse-che.readme.io/docs/stacks).
