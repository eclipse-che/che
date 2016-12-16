---
tags: [ "eclipse" , "che" ]
title: Config&#58 Networking
excerpt: ""
layout: docs
permalink: /:categories/networking/
---
Eclipse Che makes connections between three entities: the browser, the Che server running in a Docker container, and a workspace running in a Docker container.

If you distribute these components onto different nodes, hosts or IP addresses, then you may need to add additional configuration parameters to bridge different networks.

Also, since the Che server and your Che workspaces are within containers governed by a Docker daemon, you also need to ensure that these components have good bridges to communicate with the daemon.

Generally, if your browser, Che server and Che workspace are all on the same node, then `localhost` configuration will always work.
# WebSockets  
Che relies on web sockets to stream content between workspaces and the browser. We have found many networks and firewalls to block portions of Web socket communications. If there are any initial configuration issues that arise, this is a likely cause of the problem.
# Topology  
The Che server runs in its own Docker container, "Che Docker Container", and each workspace gets an embedded runtime which can be a set of additional Docker containers, "Docker Container(n)". All containers are managed by a common Docker daemon, "docker-ip", making them siblings of each other. This includes the Che server and its workspaces - each workspace runtime environment has a set of containers that is a sibling to the Che server, not a child.
![Capture_.PNG]({{ base }}/assets/imgs/Capture_.PNG)

# Connectivity  
The browser client initiates communication with the Che server by connecting to `che-ip`. This IP address must be accessible by your browser clients. Internally, Che runs on Tomcat which is bound to port `8080`. This port can be altered with [Configuration](doc:configuration).

When a user creates a workspace, the Che server connects to the Docker daemon at `docker-ip` and uses the daemon to launch a new set of containers that will power the workspace. These workspace containers will have a Docker-configured IP address, `workspace-ip`. The `workspace-ip` must also be reachable by your browser host.

Che goes through a progression algorithm to establish the protocol, IP address and port to establish communications when it is booting or starting a workspace. You can override certain parameters in Che's configuration to overcome issues with the Docker daemon, workspaces, or browsers being on different networks.

| Linux   | Windows/Mac   
| --- | ---
| >>>>>>>>>>>Connection>>>>>>   | `Che Server => Docker Daemon`   
| 1. Use the value of `che.properties` property named `docker.client.daemon_url`.\n\n2. Else, use the value of `DOCKER_HOST` system environment  variable. \n\n3. Else, use Unix socket over  `unix:///var/run/docker.sock`.   | 1) Use the value of `docker.client.daemon_url`.\n\n2) Else use the `DOCKER_HOST` environment  variable. If `DOCKER_HOST` value is malformed, catch `URISyntaxException` and use the default `https://192.168.99.100:2376`..\n\n3) Else uses default value: `https://192.168.99.100:2376`   
| `Che Server => Workspace`\n`Browser => Workspace`   | 1. Use the value of `che.properties` property named `machine.docker.local_node_host`.\n\n2. Else, use the value of `CHE_DOCKER_MACHINE_HOST` system environment variable.\n\n3. Else, if server connects to Docker via Unix socket then use `localhost`.\n\n4. Else, get the value that the Che server used when it connected to the Docker daemon `DOCKER_HOST`.   

### Docker Connectivity
There are multiple techniques for connecting to Docker including Unix sockets, localhost, and remote connections over TCP protocol. Depending upon the type of connection you require and the location of the machine node running Docker, we use different parameters.
# Ports  
Inside of your workspace containers, Che launches microservices on port `4401` and `4403`. We also launch SSH agents on port `22`. The bash terminal accessible in the workspace is also launched as an agent in the workspace on port `4411`. Custom stacks (configured in the dashboard) may expose additional services on different ports.

Docker uses ephemeral port mapping. The ports accessible to your clients start at port `32768` and go through a wide range. When we start services internal to Docker, they are mapped to one of these ports. It is these ports that the browser (or SSH) clients connect to, and would need to be opened if connecting through a firewall.

Additionally, if services are started within the workspace that expose their own ports, then those ports need to have an `EXPOSE <port>` command added to the workspace image Dockerfile. As a courtesy, we expose port `80` and `8080` within the container for any users that want to launch services on those ports.
# Firewall  
On Linux, a firewall may block inbound connections from within Docker containers to your localhost network. As a result, the workspace agent is unable to ping the Che server. You can check for the firewall and then disable it.
```shell  
# Check firewall status
sudo ufw status

# Disable firewall
sudo ufw disable

# Allow 8080 port of Che server
sudo ufw allow 8080/tcp\
```
