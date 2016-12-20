---
tags: [ "eclipse" , "che" ]
title: Import
excerpt: ""
layout: docs
permalink: /:categories/import-a-project/
---
There are several ways to import a project:
* `IDE > Import Project`
* `Dashboard > Create Project`
* Import via Git
* Import from a ZIP file
* Copy a project to Che's projects folder on the local file system
* Open a terminal in the IDE and create files in the `/projects` directory

Private repositories require SSH keys that need to be generated and uploaded to a code hosting server. It is done automatically for GitHub at `Profile > Preferences > SSH`. Click the GitHub icon and follow instructions.
# Project Configuration  
Che will auto-detect and configure Maven projects, while other project types need to be configured at `Project > Configuration`.
