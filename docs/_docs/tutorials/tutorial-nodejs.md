---
tags: [ "eclipse" , "che" ]
title: Node.js in Che
excerpt: "Create a new Node project, install additional NPM modules, and save as a snapshot"
layout: tutorials
permalink: /:categories/nodejs/
---
# 1. Start Che  
Use your SaaS account for the following, or if you have [installed Che](https://eclipse-che.readme.io/v5.0/docs/che-getting-started), open a terminal and use the Che startup script:
```smarty  
# Launch Che
che start\
```
When you execute this command, you'll see the URL for the user dashboard.

The Che dashboard will open. It is where you manage your projects and workspaces. If you do not have any projects in Che, you'll be asked to create a new project.  If you already have projects in Che, click on `New Project` button in the menu bar.
# 2. Create a Node Project  
There are several options which you can choose to start your Node.js project:

### Select Source
![ScreenShot2016-09-30at5.53.20PM.png]({{ base }}/assets/imgs/ScreenShot2016-09-30at5.53.20PM.png)
This informs Che where the source code for your project is going to come from. It is possible to start a new blank, template, sample project or import one from another location. Choosing the first option will present you with a set of samples that are preconfigured. If you already have a project at a valid URL, choose the second option.  Che gives you choices on how to source the project from Git, GitHub, ZIP, etc..

We will create a project from a provided template.

###Select Stack
![ScreenShot2016-09-30at6.13.02PM.png]({{ base }}/assets/imgs/ScreenShot2016-09-30at6.13.02PM.png)
Your project will be inserted into a workspace, which has a provided Docker runtime. Stacks are the recipes or images used to define the runtime environment for the workspace where your project will be placed. There are three ways to choose a stack:

There are three different options provided in here:
1. *Ready-To-Go Stacks.* Environments that have a large variety of tools already installed optimized for projects of a particular type. For this example, we will select the Node stack which will create a container with Ubuntu git, nodejs, npm, gulp, bower, grunt, yeoman, angular, and karma installed.
2. *Stack Library.* Offers finer grained stacks that can be used to create specific technology layers needed for a project. Ubuntu and Debian stacks, for example, are minimal stacks with only an operating system and Che tools installed.
3. *Custom Stack.* You can provide your own custom stack.

Choose the `Ready-To-Go Node` stack.

### Configure Workspace
![ScreenShot2016-09-30at5.55.07PM.png]({{ base }}/assets/imgs/ScreenShot2016-09-30at5.55.07PM.png)
Paste your workspace name and configure its RAM. RAM will be the memory limit applied to the machines running your workspace environment. Create a new workspace with any name and set its RAM to 1GB.

### Select Template.
![ScreenShot2016-09-30at6.15.03PM.png]({{ base }}/assets/imgs/ScreenShot2016-09-30at6.15.03PM.png)
A template is a Che-provided set of code, configuration, and commands that can be imported to operate within Che. There are two types of templates:
1. Ready-to-run project samples. These samples have a compilable source tree and embedded commands. The list of templates available are filtered based upon the stack chosen.
2. Wizard-driven project configuration. This creates a blank project and then opens the IDE's project configuration wizard to let you scaffold a new project based upon a set of configurable parameters. This option is only available if there an appropriate project type available for the stack chosen.

Choose the `web-nodejs-simple` template.

### Project Metadata
You can set a name and description of your project. The name is what will appear as the root node in the project explorer in the IDE.

### Create the Project

Select `Create`.  The project construction process goes through a number of steps including creating a new workspace, downloading an image to use for the workspace environment, instantiating that environment, installing Che services into the workspace, and then creating your project.
# 3. Run Your Project  
The project explorer gives you navigation to the various files that make up the project. The editor provides a variety of JavaScript, CSS, and HTML intellisense, multiple key bindings, and a sublime-style navigator.
![Capture.PNG]({{ base }}/assets/imgs/Capture.PNG)
This project has two custom commands (located in the `CMD` drop-down selector in the toolbar:
1. `install dependencies`. This will execute a `npm install --no-bin-links && bower install` process in your workspace. There are quite a few dependencies that will be downloaded and installed into your workspace. Be patient as this command runs.
2. `run`. This will start the grunt server.

Go ahead and run your project. The output will be displayed in the `Consoles` panel. If you run the project, the commands have an embedded `Preview URL` that will be displayed for your application. It will look something like: `http://192.168.99.100:32768/`. After the grunt server has started, your application will load in another browser tab when you click on that link.
![Capture.PNG]({{ base }}/assets/imgs/Capture.PNG)
You can change the content of any command by editing it. The option to modify commands is available from the `CMD` drop-down in the toolbar.
# 4. Install New NPM Modules  
Now let’s install some new NPM modules, such as [Express](https://www.npmjs.com/package/express).

Che provides a terminal with access to your machine. In the `Consoles` panel, click `New Terminal (+)` button.  This will open up a bash terminal.

, go to projects/{your-project-name} directory and run commands there..
```shell  
cd /projects/your-project-name

# Install express
sudo npm install --no-bin-links express

# Verify express is working
express\
```
The `--no-bin-links` flag is necessary to avoid causing problems if your machine is running on the Windows operating system.

You can now use the express generator to create a new application with express.
```shell  
# Install the generator
sudo npm  install -g express-generator@4

# Create the app
express /tmp/foo && cd /tmp/foo

# Install app dependencies
npm install

# Start the server
npm start\
```
Your express application will be started on port `3000`. But how do you reach it?

Docker exposes your express server port to a random port in the ephemeral range. Each time you launch Che, this port will be different. Switch to `Operations Perspective` (button in upper right corner of toolbar). In the Servers tab, look for an entry where the initial exposed port is `3000`.  The table will provide for you an appropriate server address such as `192.168.99.100:32787`. Copy this address into your browser, and you should see your express app!
# 5. Snapshot Workspace  
Che synchronizes your projects in and out of your workspace in between executions of the server. Changes made to projects are synchronized to long term storage. However, if your workspace is shut down, the internal state of the environment will not be saved to disk unless you snapshot it. The internal stage of the environment is any file that is not part of your project tree. For example, the express application installed in the previous steps will update the NPM repository within the environment. If the workspace is stopped (by you or the server), then when the workspace is restarted, it is restarted from the originating image that we downloaded when creating the project.

A snapshot creates a new image that will be used to load the workspace, based upon the current contents of the environment.

To snapshot the workspace right-click on the workspace in the left nav bar and choose "Snapshot."  The workspace will be stopped and snapshotted.
