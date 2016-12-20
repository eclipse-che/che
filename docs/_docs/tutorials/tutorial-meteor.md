---
tags: [ "eclipse" , "che" ]
title: Meteor in Che
excerpt: ""
layout: tutorials
permalink: /:categories/meteor/
---
# 1. Start Che  
Run `docker run --rm -t -v /var/run/docker.sock:/var/run/docker.sock eclipse/che start` and open your browser at `http://<your-che-host>:8080`

# 2. Start Workspace  
When in User Dashboard, go to `Workspaces` tab and click `(+)` button. It will take you to a Wizard with all the steps to create a new workspace.

### Select Source
We’ll create a new workspace using recipe, so choose an appropriate option in the menu.

###Select Stack
Our workspace will be created from a **Custom Stack** (tab on the far right). We’ll use a certified Debian based Codenvy image with NodeJS 5.6.0 and Meteor installed ([Recipe](https://raw.githubusercontent.com/eclispe/che-dockerfiles/master/meteor/latest/Dockerfile)).

Our custom recipe will be:

`FROM eclipse/meteor`

(There's also `eclipse/meteor:ubuntu` tag for those who love Ubuntu). There's only one instruction since all the commands are executed in the base image.

Finally, give your workspace a nice name, configure RAM and you are all good to go.

# 3. Create Meteor Project  
Once you workspace is created, open it in the IDE with `(>)` button (it will take a while for Docker to pull the base image). When in the IDE, go to the Consoles panel on the bottom of the IDE, click (+) button to open a new terminal tab and run meteor --version to check if it’s successfully installed.

Go to the projects directory and run meteor create {your-app-name} command to create a new Meteor app:

`cd /projects && meteor create simple-todo`

Click Refresh project tree button on the Project Explorer panel and find your soon-to-be project. Click `(>)` button to configure it. Choose Blank project type and save changes.         

# 4. Run Meteor Project  
In the Commands Widget, go to `CMD > Edit commands…` and click `(+)` button to create a custom command. Name the command, a definite it:

`cd ${current.project.path} && meteor`

Here, we use a special IDE macro that will be interpreted as an absolute path to a currently selected project.

Preview URL is very important. When a process starts on a particular port in the container, we can only access it from outside the container, on a mapped port that Docker has randomly chosen when launching the container. Here we will use a few more macros to build a preview URL:

`http://${server.port.3000}`  

`${server.port.3000}` will return `currentHost:mappedPort`. See: IDE Macros.

If you switch to a Machine perspective, Servers tab, you will find all available port mappings, including the meteor one.

Save your custom command, run it and click the preview URL. Congrats! Your first Meteor app is running.
# Q&A  
## What if I need a different Node version?

`eclipse/node` image is built on top of eclipse/debian_jre that has all the things required to run a Che workspace. Node installation was performed according to instructions in the official `node` Dockerfiles on DockerHub.

If you need a different node version, we recommend taking a look at the original [Dockerfile](https://github.com/eclipse/che-dockerfiles/blob/master/meteor/debian/Dockerfile) and grab instructions from official [Node Dockerfiles](https://github.com/nodejs/docker-node/tree/0c722500f66fb5f606a57824babe9798ae98667b).

## Can I install anything through npm?

Yes, if you need things like bower, gulp, ionic and what not, just add an extra RUN instruction to your custom recipe:
```shell  
FROM eclipse/meteor
RUN sudo npm install -g gulp bower grunt ionic\
```
## Can I override the default CMD?
