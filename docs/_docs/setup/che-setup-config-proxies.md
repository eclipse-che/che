---
tags: [ "eclipse" , "che" ]
title: Config&#58 Proxies
excerpt: "Configure Che workspaces to operate over a proxy."
layout: docs
permalink: /:categories/config-proxies/
---
Your users may need their workspaces to operate over a proxy to the Internet. Che has three dependencies to the Internet:
1. Docker, in order to download Docker images from DockerHub.
2. Importers, in order to clone sample projects or source code at an external repository to mount into a workspace.
2. Workspaces created by users, which have their own internal operating system. Users that want to reach the Internet from within their workspace, such as for maven or npm, also need a proxy configuration.
# Installation Proxies  
The Vagrant installer downloads software from the Internet. The steps to configure Vagrant to work over a proxy are provided in the [Usage: Vagrant](doc:usage) docs. If you plan to run Che as a Docker container, then your local system must have Docker configured to work over a proxy. The steps to configure that are on this page.

The Vagrant installer allows you to configure proxies from within the `Vagrantfile` and we automated the internal configuration of the VM and Che server. It's fast and painless. The remaining instructions on this page will help you configure Che server to work over a proxy if you are setting up a server of your own.
# Docker Over Proxy  
When a user creates a workspace, we will perform `docker pull`. This command will communicate with Docker Hub. You need to configure the [Docker daemon to communicate over a proxy](https://docs.docker.com/engine/admin/systemd/#http-proxy).
# Importers Over Proxy  
When a user creates a project from a template or sample, we clone that source code from a Git or Subversion repository. You need to configure the operating system that the Che server is running on to run over a proxy.

[Linux](http://www.cyberciti.biz/faq/linux-unix-set-proxy-environment-variable/)
[Mac](http://kb.netgear.com/app/answers/detail/a_id/25191/~/configuring-tcp%2Fip-and-proxy-settings-on-mac-osx)
[Windows](http://windows.microsoft.com/en-us/windows/change-internet-explorer-proxy-server-settings#1TC=windows-7)
# Proxies for Che Workspaces  
After a workspace is created, the user will be within a Docker container that has its own operating system. This operating system must also be configured to work over a proxy. Set the proxy for your workspaces within the `che.properties` file.
```ruby  
# Available starting in Che 4.2
# Set these values in che.properties and restart the server
http.proxy=<proto>://<user>:<pass>@<host>:<port>
https.proxy=<proto>://<user>:<pass>@<host>:<port>\
```
