---
title: Introduction
excerpt: ""
layout: docs
permalink: /docs/admin-guide/introduction/
---
# Codenvy Installation and Operation
Codenvy makes cloud workspaces for develoment teams. Install, run, and manage Codenvy with Docker.

### Download
This is the administration guide for the on-premises installation of Codenvy. This document discusses the installation, configuration, and operation of a Codenvy cloud that you host with a public provider like Amazon, Azure, or Google or on your own hardware behind the firewall.

The administration of your account and users for the hosted version of Codenvy at [codenvy.io](http://codenvy.io) is covered in the User Guide.

### Quick Start
With Docker 1.10+ on Windows, Mac, or Linux:
```
$ docker run -it codenvy/cli start
```
This gives you additional instructions on how to run the Codenvy CLI while setting your hostname, configuring volume mounts, and testing your Docker setup.

The full syntax for running Codenvy is:
```
$ docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock -v <path>:/data codenvy/cli:5.0.0-latest start
```
where `<path>` should be replaced with a local directory where you would like Codenvy to perform the installation, save user data, and runtime logs.

### Get Help
If you are a Codenvy customer, you can open an email ticket for 24/7/365 support.

We want everyone to have a great experience installing and running Codenvy. If you run into an issue, please [open a GitHub issue](http://github.com/codenvy/codenvy/issues) providing:
- your OS distribution and versionversion
- output of `docker version` command
- output of `docker info` command
- the full `docker run ...` syntax you used on the command line
- the output of `cli.log` - see [CLI Reference](/docs/admin-guide/cli/)
