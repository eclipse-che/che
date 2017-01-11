---
tags: [ "eclipse" , "che" ]
title: Project Setup
excerpt: ""
layout: docs
permalink: /:categories/project-setup/
---
{% include base.html %}
The first step in configuring a Che workspace from your directory is to create a Chefile. The purpose of the Chefile is to:
1. Mark the root directory that contains the source code to be mounted into your workspace. This directory will be used to create a single project in the workspace running in the Che server. The project will be mounted into `/projects` in the workspace. Many of the configuration options in Chedir are relative to this root directory.

2. Describe the way the Che server and workspace will be configured and the resources they need to run your project, as well as what workspace stack to use and how it should be accessed.

Chedir has a built-in command for initializing a directory for usage with Chedir: `che dir init`. For the purpose of this getting started guide, please follow along in your terminal:
```shell  
mkdir chedir_start
cd chedir_start
che dir init\
```
This will place a Chefile in your current directory. You can take a look at the Chefile if you want, it is filled with comments and examples. You can also run `che dir init` in a pre-existing directory that already has source code to set up Chedir for an existing project.

The Chefile is meant to be committed to version control with your project, if you use version control. This way, every person working with that project can benefit from Chedir without any upfront work.

## NEXT STEPS
