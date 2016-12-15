---
tags: [ "eclipse" , "che" ]
title: Config&#58 Scaling
excerpt: "Sizing guide and approaches for scaling Che to millions of concurrent developers"
layout: docs
permalink: /:categories/scaling/
---
Eclipse Che is a workspace server. It supports the provisioning and management of numerous workspaces for users. The default configuration of Che has a single identity per server, where the identity manages IDE preferences and SSH keys for workspaces and GitHub.

There are three aspects to scaling Che:
1. Multi-client [collaboration](https://eclipse-che.readme.io/docs/scaling#multi-user-collaboration) within a workspace
2. Scaling Che using a [Che farm](https://eclipse-che.readme.io/docs/scaling#scaling-che-using-a-che-farm)
3. [Upgrade to Codenvy](https://eclipse-che.readme.io/docs/scaling#scaling-che-with-codenvy)
# Workspace Sizing  
The Che server requires 256MB RAM and can handle 1000s of concurrent workspaces.

For each workspace, assume that the Che RAM overhead is 200MB-1.5GB, but it varies widely by the type of intellisense you add into the workspace. Your users will get the remaining RAM for use by their commands. So if you create a workspace with 2GB of RAM, some of that will be used by Che for its internal management of the workspace itself.

The RAM variation and amount is relatively high and a function of which plug-ins are deployed into the workspace agent. For example, the Java plug-in which uses JDT core to do intellisense can require up to 1GB of RAM.  If a developer is doing intensive compilation or dependency analysis, the workspace will bear the burden of providing the resources needed for these actions.

Your workspace RAM can go higher if your users are creating multiple machines. Each workspace is given at least one machine. If you permit developers to launch other machines in a single workspace, those machines by default do not have a workspace agent and all of the RAM allocated to that machine will be granted to the user.

Storage is consumed by:
1. Images downloaded and cached by Che for creating new workspaces.
2. Project files.
3. Workspace snapshots, which create new images saved in a registry.

Generally, workspace images start at 180MB. If you permit workspace snapshots, those files can grow quite significantly, especially if your developers have large dependency sets such as maven repositories that they want captured in the snapshot.
# Multi-Client Collaboration  
Workspaces are both portable and shared. Multiple browser clients (and humans!) can connect to a single Che server running multiple workspaces, or if you prefer, to a single workspace. Users within a single workspace can make use of the runtime and project files. Che implements a last-write-wins policy when multiple users modify the same file.
# Scaling Che Using a Che Farm  

![Capture_.PNG]({{ base }}/assets/imgs/Capture_.PNG)
To scale out Che, you can either attempt to separate some Che services onto different nodes, or run a Che farm.

1.  (Coming Soon). Your workspace runtimes are Docker containers that do not have to reside on the same machine as the Che server, which is the workspace master. You can optionally deploy Docker onto a different physical node apart from the Che server. You can then size the Docker node based upon the number of concurrent workspaces that you want to have active and / or snapshot. Since Che has a single embedded identity in its default configuration, SSH keys and IDE preferences will be shared across all users accessing workspaces on a single server.

2.  You can deploy Che in a farm with an Nginx router. Each user would be provisioned their own Che instance, either running on its own port in a VM. In this configuration, each user can have their own workspaces and identity profile. Note that since Che exports two IP addresses, one for Che and another for the workspace machine running Docker, your router will need to manage traffic for all possible routes [between browser, Che and machines.](doc:networking)
# Scaling Che with Codenvy  
Your Eclipse Che workspaces and plug-ins will work within [Codenvy](http://codenvy.com). Codenvy is a multi-tenant, multi-user and elastic cloud installed locally or used as a SaaS:
* Workspace distribution with an embedded Docker Swarm
* Operational solutions for monitoring, scaling, upgrading and archiving workspaces
* Team management, permissions and resource policy management tools
* User authentication, single-sign on, and LDAP
* Self-service user registration
![ScaleCodenvy.PNG]({{ base }}/assets/imgs/ScaleCodenvy.PNG)
Codenvy uses Puppet to install, configure, and update various internal services. This creates a simple management interface for administrators with flexibility on how many physical nodes to allocate along with the resource policy management that is applied to users and accounts.
