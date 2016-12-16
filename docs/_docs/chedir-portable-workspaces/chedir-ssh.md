---
tags: [ "eclipse" , "che" ]
title: SSH
excerpt: "SSH into your workspace"
layout: docs
permalink: /:categories/ssh/
---
You can use Chedir to SSH into the newly created workspace, whether it is local or remote. It does not matter what operating system that you are using, this technique also supports Microsoft Windows without having to install putty!
```shell  
che dir ssh\
```
The command has local context of the Che server and workspace that is associated with the Chefile in the current directory. Chedir looks up the appropriate context and then initiates an SSH connection.
