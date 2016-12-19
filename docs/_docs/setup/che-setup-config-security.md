---
tags: [ "eclipse" , "che" ]
title: Config&#58 Security
excerpt: ""
layout: docs
permalink: /:categories/config-security/
---
Eclipse Che is designed as a single identity system to be used by an individual or small team working in a trusted environment. The following outlines some of the security capabilities and known gaps in Che.

# Securing Che Ports  
Firewall rules can be added to prevent access to ports that shouldn't be externally accessible. Refer to [network topology docs](https://eclipse-che.readme.io/docs/networking#topology) for additional information on ports.

When a user requires access to Che server remotely, outside of the local network/localhost, firewall rules can be setup to allow only certain ip-addresses access to Che server ports mention earlier.
# Limiting Che Ports  
Eclipse Che uses Docker to power its workspaces. Docker uses the [ephemeral port range](https://en.wikipedia.org/wiki/Ephemeral_port) when exposing ports for services running in the container. So when a Tomcat server is started on port 8080 inside a Che workspace Docker automatically selects an available port from the ephemeral range at runtime to map to that Tomcat instance.

Docker will select its ports from anywhere in the ephemeral range. If you wish to reduce the size of the ephemeral range in order to improve security you can do so, however, keep in mind that each Che workspace will use at least 2 ports plus whatever ports are required for the services the user adds to their workspace.

Limiting the ephemeral range can only be done at the host level - you can read more about it (and some of the risks in doing so) here: http://www.ncftp.com/ncftpd/doc/misc/ephemeral_ports.html

To change the ephemeral range:
  * On Linux: http://www.ncftp.com/ncftpd/doc/misc/ephemeral_ports.html#Linux
  * On Windows: http://www.ncftp.com/ncftpd/doc/misc/ephemeral_ports.html#Windows
# Securing a Workspace from the Che Host  
Exposing host file system files/folders through volume mounts such as environment variable `CHE_WORKSPACE_VOLUME` and giving workspace/docker container privileged rights using Che configuration property `machine.docker.privilege_mode=true` could allow workspaces/docker containers to effect the host operating system.

These options are useful for certain development situations but should be minimized to increase security to the host system whenever possible.
# Workspace Permissions  
Eclipse Che is a single identity system. All users accessing a Che server share an identity and user preferences. There are no identity-based permissions since all users share the same identity.  Users have access and control over all workspace environments.

For a simple separation of workspaces for small development teams, without requiring workspace permissions, administrators can create separate Che server for each user. Each server, if ran on same host, would need to be setup on different ports using the `CHE_PORT` environment variable, different names using the  `CHE_SERVER_CONTAINER_NAME` environment variable and different data/configuration folders using environment variable `CHE_CONF_FOLDER` and `CHE_DATA_FOLDER`.  With this configuration there is no synchronization of workspaces or configuration between these instances - workspaces would need to be exported/imported from Che server to Che server manually.
```shell  
docker run --rm -t --env CHE_PORT=8080 \
                   --env CHE_SERVER_CONTAINER_NAME=che-server-user1 \
                   --env CHE_CONF_FOLDER=/home/user1/che/conf \
                   --env=CHE_DATA_FOLDER=/home/user1/che/ \
                   -v /var/run/docker.sock:/var/run/docker.sock eclipse/che start

docker run --rm -t --env CHE_PORT=8081 \
                   --env CHE_SERVER_CONTAINER_NAME=che-server-user2 \
                   --env CHE_CONF_FOLDER=/home/user2/che/conf \
                   --env=CHE_DATA_FOLDER=/home/user2/che/ \
                   -v /var/run/docker.sock:/var/run/docker.sock eclipse/che start

#Setup firewall rules for ports 8080 and 8081 to only allow certain ipaddress access.\
```
Codenvy provides an implementation of Eclipse Che that is multi-tenant, multi-user with distributed access and permissions controls for teams. Each user has a different login which enables access controls, workspace collaboration, and other forms of sharing. Learn more at [https://codenvy.com/getting-started/](https://codenvy.com/getting-started/).
# Authenticated Access  
The Che server itself is unauthenticated. Che is extensible allowing different dashboard front ends or proxies to implement authenticated access to the Che server. Bitnami's deployment of Eclipse Che includes an authenticated front-end implemented as a proxy. Many users deploy nginx in front of Che to provide an authentication layer within the system.

Bitnami requires an existing account with cloud providers such as Google, Amazon AWS, or Microsoft Azure which may require monthly service charges from cloud providers.  Refer to [Usage: Private Cloud](doc:usage-bitnami) for additional information.

Codenvy also provides an implementation of Eclipse Che that has multi-user and multi-tenant capabilities.
# HTTPS  
HTTPS is not provided by Eclipse Che. It would require a more complex architecture with multi-service deployments, making Che more challenging for developers and small teams to use.
