---
tags: [ "eclipse" , "che" ]
title: Setup&#58 Proxies
excerpt: "Setting up the ARTIK IDE behind a proxy."
layout: artik
permalink: /:categories/proxies/
---
Your users may need their workspaces to operate over a proxy to the Internet. The ARTIK IDE has three dependencies to the Internet:

1. Docker, in order to download Docker images from DockerHub.
2. Importers, in order to clone sample projects or source code at an external repository to mount into a workspace.
3. Workspaces created by users, which have their own internal operating system. Users that want to reach the Internet from within their workspace, such as for `maven` or `npm`, also need a proxy configuration.
