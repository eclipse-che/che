---
tags: [ "eclipse" , "che" ]
title: Why Chedir?
excerpt: "Create and configure reproducible and portable developer workspaces."
layout: docs
permalink: /:categories/why/
---
Chedir provides easy to configure, reproducible and portable developer workspaces built on top of Docker and controlled by a single consistent workflow to help maximize the productivity and flexibility of you and your team.

To achieve its magic, Chedir uses Eclipse Che and Docker. Workspaces are provisioned inside of a Che server that is running locally or remotely. The workspace will have its own private runtime that is also based upon Docker or Docker Compose. Your source code is then synchronized from the current directory into the hosted workspace. You can then use provisioning tools such as shell scripts, Chef, or Puppet to define the software that is inside the workspace available to edit, build, run and debug your code.

Chedir is (positively) influenced by Vagrant. Where Vagrant treats a single VM as a broad abstraction as an "environment", Chedir applies a similar abstraction to a developer workspace, which is inclusive of multiple internal environments, the source code for projects mapped from repositories, and the tools + commands necessary to build and debug a project. After provisioning, users can connect their local IDE or use the embedded cloud IDE of Eclipse Che.
