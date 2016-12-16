---
tags: [ "eclipse" , "che" ]
title: Share
excerpt: ""
layout: docs
permalink: /:categories/sharing/
---
Eclipse Che helps teams collaborate in reproducible workspace environments. Team members can contribute to a project using the same environment (on any OS), knowing that all behaviors will match on every developer's machine.
# Sharing Workspaces  
## Setting Up Che as a Shared Server
An Eclipse Che server can be setup to run on an external port using the environment variable  `CHE_HOST_IP` when starting the Che server. Once the Che server is running, team members can use this remote IP to connect to the same Che server.

## Export Workspaces from Separate Servers
If team members are all using their own Che servers, they can share their workspace environment with other team members manually. Exporting a workspace can be done after a [workspace](doc:create-workspaces-and-projects) has been created from the Workspaces page in the dashboard:
![che-sharing2.jpg]({{ base }}/assets/imgs/che-sharing2.jpg)
There are two exporting options available there, **Export to File** and **Export to Cloud** under Settings tab:
![che-sharing3.jpg]({{ base }}/assets/imgs/che-sharing3.jpg)
Finally, it's possible to take a snapshot of a running workspace, export it to a Docker image registry and share it with others.

### Export to File
In the user dashboard's workspace detail view for a specific workspace:
1. Click the "Export As A File" button to bring up the Export Workspace window. A custom recipe for the workspace will be shown. It includes environment and project information for the workspace.
2. Click "Download" or "Copy to Clipboard" to get a copy of the workspace JSON which can be imported into another Che instance.

To import the JSON in another Che instance:
1. Ensure that the two instances are running the same Che version.
2. In the user dashboard select "Workspaces" from the left navigation bar.
3. Click "Add Workspace".
4. Under "Select Source" choose "Import an existing workspace configuration".
5. Paste the JSON in the text editing area.
![che-sharing4.jpg]({{ base }}/assets/imgs/che-sharing4.jpg)
### Export to Cloud
Export to Cloud pushes the Eclipse Che workspace to a network-reachable Che or Codenvy system. This is tested with the hosted Che system at https://beta.codenvy.com/.

In the user dashboard's workspace detail view for a specific workspace:
1. Click the "Export To Private Cloud" button to bring up the Export Workspace window.
2. Enter the username and password used to authenticate on the remote system.
3. Enter the URL of the remote system (e.g. https://beta.codenvy.com/)
4. Click "Export"
This will push the workspace JSON to the remote host and start the workspace there.

### Export to a Docker Registry
Create a snapshot of the workspace you want to export by right-clicking the workspace name in the user dashboard or left navigation bar.

Use the Docker CLI to rename, login and push the workspace Docker image to a Docker repository.

In order to access the Docker CLI, you will need access to the host terminal. Below is an example of renaming and pushing a workspace Docker image to Docker Hub repository using the Docker CLI.
```shell  
#list all images. Look for the newest snapshot then tag it.
docker images
docker tag machine_snapshot_<hash> <repository>/<image-name>

#Login to your Docker Hub account
docker login

docker push <repository>/<image-name>\
```

#### Projects Not Transferred
}  

## Share with Codenvy Factories
Codenvy, which is based on Eclipse Che, includes "Factories" - templates used to generate new or open existing workspaces with a URL. Factories can be used to clone existing workspaces or repeatedly generate consistent workspaces for teams. For additional information see Codenvy's documentation on [Factories](../../docs/workspace-automation).
# Pair Programming  
The Che community has been working on pair programming technology that will allow multiple users to work in the same workspace instance. This could be done through multiple Che editors, or with a mix of Che and desktop IDEs.

Demonstrations have shown that two developer will not only see changes made to by each other in real time but also will be able to see each others identifiable cursor positions. Currently, two team members should not work on the same file at the same time. Check the PR status at [https://github.com/eclipse/che/pull/2131](https://github.com/eclipse/che/pull/2131).
# Multi-Identity  
Eclipse Che is a single identity system meaning that although multiple users can access the same server they share the same profile, including any keys and tokens.

Codenvy has built an enterprise offering on top of Eclipse Che that adds multi-tenancy and multi-user with distributed access and permissions controls for teams. Each user has a different login which enables access controls, workspace collaboration, and other forms of sharing.
