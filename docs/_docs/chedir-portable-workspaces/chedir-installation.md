---
tags: [ "eclipse" , "che" ]
title: Installation
excerpt: "Installing, upgrading and uninstalling Chedir"
layout: docs
permalink: /:categories/installation/
---
{% include base.html %}
Installing Chedir is extremely easy. Just [get the Che CLI](https://eclipse-che.readme.io/docs/che-getting-started#1-get-the-eclipse-che-cli) and you have everything you need to run Chedir on any operating system. Docker and Git Bash (installed by Docker) are required for the Che CLI.
# Backwards Compatibility  
Chedir requires Eclipse Che 4.7+. Chefiles are not supported to run on older versions of Che.
# Upgrading  
You will automatically get newer versions of Chedir when you upgrade Eclipse Che. You can upgrade Eclipse Che with the CLI by `che update`. If you want to use a particular version of Chedir you can set `CHE_VERSION` as an environment variable and Chedir will use that particular version. You can run multiple versions of Chedir at the same time.
# From Source  
Chedir is provided as a Docker container which you can run instead of using the CLI. The CLI captures your environmental information and invokes the container with the appropriate syntax.
```shell  
# Mac / Linux
docker run -v /var/run/docker.sock:/var/run/docker.sock \
           -v "$PWD":"$PWD" --rm eclipse/che-file \
           $PWD <init|up>

# Windows
# Replace $PWD to be the absolute path to the current directory.
# Use case-sensitive format with forward slashes: /c/Users/some_path/
```
# Build Chedir Docker Container
```shell  
git clone http://github.com/eclipse/che-dockerfiles
cd che-dockerfiles
./lib/typescript/compile.sh && ./che-dir/build.sh\
```

# Uninstallation  
You can remove Chedir by deleting the Chedir docker image from your system.
```shell  
docker rmi -f eclipse/che-dir\
```
If you'd also like to remove the Che CLI and Che:
```shell  
# Remove the Che container
docker rmi -f eclipse/che-server

# Remove your workspaces and the CLI

# Linux / Mac
rm -rf $CHE_DATA_FOLDER
rm -rf /usr/local/bin/che


# Windows
rmdir /s %CHE_DATA_FOLDER%
del che.bat
del che.sh\
```
