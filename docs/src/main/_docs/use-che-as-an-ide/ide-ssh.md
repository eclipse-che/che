---
tags: [ "eclipse" , "che" ]
title: SSH
excerpt: "Connect to your workspaces using SSH"
layout: docs
permalink: /:categories/ssh/
---
{% include base.html %}
Workspaces are configured with an SSH agent, which runs an SSH daemon within your workspace runtime. You can connect to your workspace on the command line and get root access to the runtime (similar to what the Web terminal provides) from other machines. You can optionally disable the SSH agent for your workspace from within the dashboard.

# Public / Private Key Generation
If your workspace has the SSH agent activated in the dashboard, then Che runs an SSH daemon within the machines that are part of your workspace. The SSH agent is activated by default with all new workspaces and you can manually disable it within the dashboard. If your workspace is powered by Docker Compose, then the SSH agent is deployed into every container that makes up your compose services. You can optionally remove the agent from selected machines of your compose services from within the dashboard.

Each new workspace has a default key-pair generated for it. The private key for the workspace is inserted into each machine and they will all share the same public key. You can generate a new key-pair in the dashboard, or remove the default one to be replaced with yours.

Eclipse Che does not have any user authentication, so any client can connect to the Che server REST API and then request the public key for the workspace. In Codenvy, API clients must be authenticated with appropriate permissions before they can request the public key for a workspace.

We then provide various client utilities (authored in Docker!) for connecting to a workspace using SSH.

# List Workspaces  
You can get a list of workspaces in a Che server that have an SSH agent deployed. These are the workspaces that you can SSH into.
```shell  
$ che action list-workspaces
NAME      ID                   STATUS
florent   workspace93kd748390  STOPPED
mysql     workspacewia89343k4  RUNNING

# Options
--url <url>           # Che or Codenvy host where workspaces are running
--user <email>        # Codenvy user name
--password <password> # Codenvy password\
```

# Connect  
You can connect to your workspace using our Docker containers, the Che CLI, or your off-the-shelf SSH client such as `ssh` on Linux/Mac or `putty` on Windows.

One nice aspect of our SSH capabilities is that they are all done inside of a Docker container, letting any OS connect to a Che workspace using the same sytnax without the user having to install specialized tools for each OS.

### SSH With Che CLI
If you have the Che CLI installed, you can SSH into any workspace. This command is available starting in Che 5.0.0-M7 release. You may need to use one of the container-based options or the command provided within the IDE that uses your native SSH client if you do not have this release.
```shell  
# Connect to the machine in a workspace that is designated as the dev machine.
# Each workspace always has one machine that is a dev machine with a dev agent on it.
che ssh <ws-name>
che ssh <ws-id>

# If in Codenvy, you can optionally append a user namespace to a workspace name.
# For example, <namespace:ws-name> such as "florent:first-workspace".

# Connect to a secondary machine in the workspace if you started multiple machines
# using Docker compose.
che ssh <ws-name> [machine-name]
che ssh <ws-id> [machine-name]

# Options
--url <url>           # Che or Codenvy host where workspaces are running
--user <email>        # Codenvy user name
--password <password> # Codenvy password
```
The CLI is aware of the locally running Che server based upon the configuration that you have provided it within your environment variables. If your Che server only has a single workspace, it will connect to that workspace. If your Che server does not have any workspaces, it will present an error. If your Che server has two or more running workspaces, it will display a list of available workspaces that you can connect to.

The same is true for the machines - if there is a single machine, it will choose a single one. If there are multiple machines, it will present for you a list of different machines that you can connect to.
![8f99a700-a696-11e6-8d8a-414e38ec26b2.gif]({{ base }}/assets/imgs/8f99a700-a696-11e6-8d8a-414e38ec26b2.gif)
### SSH With Containers
We provide a utiltiy `eclipse/che-action` which performs various actions against a Che server. One of the actions is to SSH. In Eclipse Che, this utility auto-discovers the right key to use by querying the Che server. In Codenvy, you will need to provide the key or authenticate in advance.
```shell  
che action workspace-ssh <ws-name>
che action workspace-ssh <ws-id>

[-s,--url]=<value>      Defines the url of Che or Codenvy to connect to
[-u,--user]=<value>     Defines the Codenvy user name to authenticate with
[-w,--password]=<value> Defines the Codenvy password to authenticate with\
```
### SSH With Native Tools
If you want to use your native SSH tools to connect to a workspace, you can get the connectivity information that you need to use with one of our utilities. You can then pass this information into `ssh` or `putty` to make a direct connection.
```text  
$ che action get-ssh-data <ws-name>
$ che action get-ssh-data <ws-id>
SSH_IP=192.168.65.2
SSH_PORT=32900
SSH_USER=user
SSH_PRIVATE_KEY='
-----BEGIN RSA PRIVATE KEY-----
ws-private-key-listed-here
-----END RSA PRIVATE KEY-----

# Options
--url <url>           # Che or Codenvy host where workspaces are running
--user <email>        # Codenvy user name
--password <password> # Codenvy password\
```
