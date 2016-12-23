---
tags: [ "eclipse" , "che" ]
title: Managing
excerpt: ""
layout: docs
permalink: /:categories/managing/
---
{% include base.html %}
# Scaling
Eclipse Che is a workspace server. It supports the provisioning and management of numerous workspaces for users. The default configuration of Che has a single identity per server, where the identity manages IDE preferences and SSH keys for workspaces and GitHub.

There are three aspects to scaling Che:
1. Multi-client [collaboration]() within a workspace
2. Scaling Che using a [Che farm]()
3. [Upgrade to Codenvy](http://codenvy.com)

## Workspace Sizing  
The Che server requires 256MB RAM and can handle 1000s of concurrent workspaces.

For each workspace, assume that the Che RAM overhead is 200MB-1.5GB, but it varies widely by the type of intellisense you add into the workspace. Your users will get the remaining RAM for use by their commands. So if you create a workspace with 2GB of RAM, some of that will be used by Che for its internal management of the workspace itself.

The RAM variation and amount is relatively high and a function of which plug-ins are deployed into the workspace agent. For example, the Java plug-in which uses JDT core to do intellisense can require up to 1GB of RAM. If a developer is doing intensive compilation or dependency analysis, the workspace will bear the burden of providing the resources needed for these actions.

Your workspace RAM can go higher if your users are creating multiple machines. Each workspace is given at least one machine. If you permit developers to launch other machines in a single workspace, those machines by default do not have a workspace agent and all of the RAM allocated to that machine will be granted to the user.

Storage is consumed by:
1. Images downloaded and cached by Che for creating new workspaces.
2. Project files.
3. Workspace snapshots, which create new images saved in a registry.

Generally, workspace images start at 180MB. If you permit workspace snapshots, those files can grow quite significantly, especially if your developers have large dependency sets such as maven repositories that they want captured in the snapshot.

## Multi-Client Collaboration  
Workspaces are both portable and shared. Multiple browser clients (and humans!) can connect to a single Che server running multiple workspaces, or if you prefer, to a single workspace. Users within a single workspace can make use of the runtime and project files. Che implements a last-write-wins policy when multiple users modify the same file.

## Scaling Che Using a Che Farm  

![Capture_.PNG]({{ base }}/assets/imgs/Capture_.PNG)
You can deploy Che in a farm with an Nginx router. Each user would be provisioned their own Che instance, either running on its own port in a VM. In this configuration, each user can have their own workspaces and identity profile. Note that since Che exports two IP addresses, one for Che and another for the workspace machine running Docker, your router will need to manage traffic for all possible routes [between browser, Che and machines.]()

## Scaling Che with Codenvy  
Your Eclipse Che workspaces and plug-ins will work within [Codenvy](http://codenvy.com). Codenvy is a multi-tenant, multi-user and elastic cloud installed locally or used as a SaaS:
* Workspace distribution with an embedded Docker Swarm
* Operational solutions for monitoring, scaling, upgrading and archiving workspaces
* Team management, permissions and resource policy management tools
* User authentication, single-sign on, and LDAP
* Self-service user registration
![ScaleCodenvy.PNG]({{ base }}/assets/imgs/ScaleCodenvy.PNG)
Codenvy uses Docker to install, configure, and update various internal services. This creates a simple management interface for administrators with flexibility on how many physical nodes to allocate along with the resource policy management that is applied to users and accounts.

# Upgrading
Upgrading Che is done by downloading a `eclipse/che-cli:<version>` that is newer than the version you currently have installed. You can run `eclipse/che-cli version` to see the list of available versions that you can upgrade to.

For example, if you have 5.0.0-M2 installed and want to upgrade to 5.0.0-M8, then:
```
# Get the new version of Che
docker pull eclipse/che-cli:5.0.0-M8

# You now have two eclipse/che-cli images (one for each version)
# Perform an upgrade - use the new image to upgrade old installation
docker run <volume-mounts> eclipse/che-cli:5.0.0-M8 upgrade
```

The upgrade command has numerous checks to prevent you from upgrading Che if the new image and the old version are not compatible. In order for the upgrade procedure to advance, the CLI image must be newer that the version in `/instance/che.ver`.

The upgrade process:
1. Performs a version compatibility check
2. Downloads new Docker images that are needed to run the new version of Che
3. Stops Che if it is currently running
4. Triggers a maintenance window
5. Backs up your installation
6. Initializes the new version
7. Starts Che

# Backup
You can run `che backup` to create a copy of the relevant configuration information, user data, projects, and workspaces. We do not save workspace snapshots as part of a routine backup exercise. You can run `che restore` to recover che from a particular backup snapshot. The backup is saved as a TAR file that you can keep in your records. You can then use `che restore` to recover your user data and configuration.

# Security
Eclipse Che is designed as a single identity system to be used by an individual or small team working in a trusted environment. The following outlines some of the security capabilities and known gaps in Che.

## Securing Che Ports  
Firewall rules can be added to prevent access to ports that shouldn't be externally accessible. Refer to [network topology docs]() for additional information on ports.

When a remote user (outside the local network) requires access to Che, firewall rules can be setup to allow only certain ip-addresses access.

## Limiting Che Ports  
Eclipse Che uses Docker to power its workspaces. Docker uses the [ephemeral port range](https://en.wikipedia.org/wiki/Ephemeral_port) when exposing ports for services running in the container. So when a Tomcat server is started on port 8080 inside a Che workspace Docker automatically selects an available port from the ephemeral range at runtime to map to that Tomcat instance.

Docker will select its ports from anywhere in the ephemeral range. If you wish to reduce the size of the ephemeral range in order to improve security you can do so, however, keep in mind that each Che workspace will use at least 2 ports plus whatever ports are required for the services the user adds to their workspace.

Limiting the ephemeral range can only be done at the host level - you can read more about it (and some of the risks in doing so) here: http://www.ncftp.com/ncftpd/doc/misc/ephemeral_ports.html

To change the ephemeral range:
  * On Linux: http://www.ncftp.com/ncftpd/doc/misc/ephemeral_ports.html#Linux
  * On Windows: http://www.ncftp.com/ncftpd/doc/misc/ephemeral_ports.html#Windows

## Securing a Workspace from the Che Host  
It is possible for admins to mount files from your server's host file system to be available to your users within their workspaces with the `CHE_WORKSPACE_VOLUME` and `CHE_DOCKER_PRIVILEGED_MODE` parameters are potential secrity risks as you open the possiblity of sending host-specific files into a workspace or giving a workspace user access to the host system.  These options are useful for certain development situations but should be minimized to increase security to the host system whenever possible.

## Workspace Permissions  
Eclipse Che is a single identity system. All users accessing a Che server share an identity and user preferences. There are no identity-based permissions since all users share the same identity.  Users have access and control over all workspace environments.

For a simple separation of workspaces for small development teams, without requiring workspace permissions, administrators can create separate Che server for each user. Each server, if ran on same host, would need to be setup on different ports using the `CHE_PORT` environment variable and different data folders mounted `:/data`.

Codenvy provides an implementation of Eclipse Che that is multi-tenant, multi-user with distributed access and permissions controls for teams. Each user has a different login which enables access controls, workspace collaboration, and other forms of sharing. You can install Codenvy with a CLI that is nearly identical to Che with `docker run codenvy/cli start. Learn more at [https://codenvy.com](https://codenvy.com).

## Authenticated Access  
The Che server itself is unauthenticated. Che is extensible allowing different dashboard front ends or proxies to implement authenticated access to the Che server. Bitnami's deployment of Eclipse Che includes an authenticated front-end implemented as a proxy. Many users deploy nginx in front of Che to provide an authentication layer within the system.

Bitnami requires an existing account with cloud providers such as Google, Amazon AWS, or Microsoft Azure which may require monthly service charges from cloud providers.  Refer to [Usage: Private Cloud](doc:usage-bitnami) for additional information.

Codenvy also provides an implementation of Eclipse Che that has multi-user and multi-tenant capabilities.

## HTTP/S  
HTTPS is not provided by Eclipse Che. It would require a more complex architecture with multi-service deployments, making Che more challenging for developers and small teams to use. Codenvy gives you the option of running the system with HTTP/S and providing your own SSL certificate.
