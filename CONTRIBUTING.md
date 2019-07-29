
# Contributing to Che

- [Other Che repositories](#other-che-repositories)
- [Devfile to contribute](#devfile-to-contribute)
- [Contribute to ...](#contribute-to-...)
  - [Dashboard](#dashboard)
  - [Che Server a.k.a WS master](#che-server-a.k.a-ws-master)

## Other Che repositories

Che is composed of multiple sub projects. For each projects we provide a *CONTRIBUTE.md* file describing how to setup the development environment to start your contribution. Most of the time, we encourage you to use Che to contribute to Che.

- [eclipse/che](https://github.com/eclipse/che) (this repository) is the main project repository that contains:
   - Che master: orchestrates the che workspaces with devfiles on Kubernetes
   - Che dashboard: UI to manage workspaces, devfiles, etc.
   - Che main container images: source code, dockerfiles to build our main docker images. Note that Che-theia related dockerfiles are located in che-theia repo.
   - End2end test: source code of our integration tests.
- [che-theia](https://github.com/eclipse/che-theia): Theia IDE integrated in Che.
- [chectl](https://github.com/che-incubator/chectl): The CLI to install Che, create and start workspaces and devfiles
- [che-plugin-registry](https://github.com/eclipse/che-plugin-registry): The default set of che plugins (vscode extension + containers) or editors that could be installed on any Che workspaces.
- [che-devfile-registry](https://github.com/eclipse/che-devfile-registry): The default set of devfiles that would be made available on the Che dashboard stacks.
- [redhat-developer/devfile](https://github.com/redhat-developer/devfile): Contribute to the devfile documentation: https://redhat-developer.github.io/devfile/
- [che-plugin-broker](https://github.com/eclipse/che-plugin-broker): The workspace microservice that is in charge of analyzing, preparing and installing the workspace components defined in a Devfile.
- [che-operator](https://github.com/eclipse/che-operator): Che operator to deploy, update and manage K8S/OpenShift resources of Che.
- [che-docs](https://github.com/eclipse/che-docs): Eclipse Che documentation https://www.eclipse.org/che/docs/ source code.
- [che-website](https://github.com/eclipse/che-website): https://eclipse.org/che website source code.
- [che-workspace-client](https://github.com/eclipse/che-workspace-client): JS library to interact with a che-server.
- [che-machine-exec](https://github.com/eclipse/che-machine-exec): Interface to execute tasks and terminals on other containers within a workspace.

## Devfile to contribute
We are trying to provide a devfile for each areas where you could contribute. Each devfile could be run on any Che instances to setup a *ready-to-code* developer environment. Beware that each of them may need a certain amount of memory.
Devfile could be launched through a factory or [chectl](https://github.com/che-incubator/chectl) cli.

```bash
$ chectl workspace:start -f devfiles/che-theia-all.devfile.yaml
```
or
```bash
$ chectl workspace:start -f https://raw.githubusercontent.com/eclipse/che-theia/master/devfiles/che-theia-all.devfile.yaml
```
or `https://<CheInstance>/f?url=https://raw.githubusercontent.com/eclipse/che-theia/master/devfiles/che-theia-all.devfile.yaml`

## Contribute to ...
Let's cover the developer flow for theses projects:

### Dashboard
Dashboard source code is located in [<this.repo>/dashboard](./dashboard/) folder.
It is an AngularJS application. Here is the developer workflow if you want to contribute to it:

#### Devfile for dashboard development

The devfile: [<this.repo>/dashboard/devfile.yaml](./dashboard/devfile.yaml)

In this section, we show how to setup a Che 7 environment to work on the che dashboard, and how to use it.
For the whole workflows, we will need a workspace with such containers:

- Dashboard Dev container (a.k.a dash-dev): Dashdev is a all in one container for running commands such as build, test or start the dashboard server.

All containers have `/projects` folder mounted, which is shared among them.


Developer workflow:

1. Start the workspace with the devfile, it is cloning Che repo.
2. Build
3. Code ...
4. Run unit test
5. Start dashboard server and preview


#### Step 1: Start the workspace with the devfile, it is cloning Che repo.
In this section we are going to start a new workspace to work on che-theia. The new workspace will have few projects cloned: `theia` and `che-theia`. It will also setup the containers and commands in the `My workspace` view. We will use these commands in the next steps.

The devfile could be started using `chectl`:

```
chectl workspace:start -f https://raw.githubusercontent.com/eclipse/che/master/dashboard/devfile.yaml
```
At workspace start, Che will clone Che source code (including the dashboard)



#### Step 2: Build

In this section we are going to build the dashboard project.

You can use the che command `dashboard_build` (command pallette > Run task > … or containers view)
Basically, this command will run

```bash
# [dash-dev]
$ yarn
```
#### Step 3: Code ...

#### Step 4: Run unit test (optional)
In this step, we will run the Dashboard unit tests:

You can use the che command `dashboard_test` (command pallette > Run task > … or containers view)
Basically, this command will run

```bash
# [dash-dev]
$ yarn test
```
#### Step 5: Start dashboard server and preview
In this step, we will run the dashboard server and see the live reloadable preview.

You can use the che command `dashboard_dev_server` (command pallette > Run task > … or containers view)
```bash
# [dashboard_dev_server]
$ node_modules/.bin/gulp serve --server=<che_api_url>
```


### Che server a.k.a WS master
To be provided soon.
