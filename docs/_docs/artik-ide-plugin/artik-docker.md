---
tags: [ "eclipse" , "che" ]
title: Setup&#58 Docker
excerpt: "Configuring Docker for the ARTIK IDE."
layout: artik
permalink: /:categories/docker/
---
ARTIK IDE workspaces are based on a Docker image. You can either pull that image from a public registry, like Docker Hub, or a private registry which is managed by yourself. Images in a registry can be publicly visible or private, which require user credentials to access. You can also set up a private registry to act as a mirror to Docker Hub. And, if you are running the ARTIK IDE behind a proxy, you can configure the Docker daemon registry to operate behind a proxy.
