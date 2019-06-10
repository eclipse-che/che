

### Introduction
Previously, two kind of recipes were available to bootstrap a cloud developer workspace and to make it portable: [Chefile](https://www.eclipse.org/che/docs/chefile.html)
and [Factories](https://www.eclipse.org/che/docs/factories-getting-started.html#try-a-factory).
As a continuation of this, the brand new `devfile` format was introduced, which combines simplicity and support for high variety of different components available to develop a container based application.

### What the devfile consists of
The minimal devfile sufficient to run a workspace from it, consists of the following parts:
 - Specification version
 - Name

Without any further configuration a workspace with default editor will be launched along with its default plugins which are configured on Che Server.
By default, `Che Theia` is configured as a default one along with `Che Machine Exec` plugin.

To get more functional workspace, the following parts can be added:
 - A list of components: the development components and user runtimes
 - A list of projects: the source code repositories
 - A list of commands: actions to manage the workspace components like running the dev tools, starting the runtime environments etc...

Example of the minimal devfile with project and standard plugins set (Theia editor + exec plugin):

```

---
apiVersion: 1.0.0
metadata:
  name: petclinic-dev-environment
projects:
  - name: petclinic
    source:
      type: git
      location: 'https://github.com/che-samples/web-java-spring-petclinic.git'
components:
  - alias: theia-editor
    type: cheEditor
    id: eclipse/che-theia/next
  - alias: exec-plugin
    type: chePlugin
    id: eclipse/che-machine-exec-plugin/0.0.1
```

For the detailed explanation of all devfile components assignment and possible values, please see the following resources:
 - [Specification repository](https://github.com/redhat-developer/devfile)  
 - [detailed json-schema documentation](https://redhat-developer.github.io/devfile/devfile).

### Getting Started
The simplest way to use devfile is to have it deployed into GitHub source repository and then create factory from this repo.
This is as simple as create `devfile.yaml` file in the root of your GH repo, and then execute the factory:
```
https://<your-che-host>/f?url=https://github.com/mygroup/myrepo
```

Also, it is possible to execute devfile by constructing the factory with the URL to it's raw content, for example,
```
https://<your-che-host>/f?url=https://pastebin.com/raw/ux6iCGaW
```
or sending a devfile to a dedicated REST API using curl/swagger, which will create new workspace and return it's configuration:
```
curl -X POST  -H "Authorization: <TOKEN>" -H "Content-Type: application/yaml" -d <devlile_content> https://<your-che-host>/api/devfile
```

If you're a user of `chectl` tool, it is also possible to execute workspace from devfile, using `workspace:start` command
parameter as follows:
```
chectl workspace:start --devfile=devfile.yaml
````
Please note that currently this way only works for the local (same machine) devfiles - URL can't be used here atm.

### Project details
A single devfile can specify several projects. For each project, one has to specify the type of the
source repository, its location and optionally also the directory to which the project should be
cloned to.

As an example, consider this devfile:

```yaml
apiVersion: 1.0.0
metadata:
  name: example-devfile
projects:
- name: frontend
  source:
    type: git
    location: https://github.com/acmecorp/frontend.git
- name: backend
  clonePath: src/github.com/acmecorp/backend
  source:
    type: git
    location: https://github.com/acmecorp/backend.git
```

In the example above, we see a devfile with 2 projects, `frontend` and `backend`, each located in
its own repository on github. `backend` has a specific requirement to be cloned into the
`src/github.com/acmecorp/backend` directory under the source root (implicitly defined by the Che
runtime) while frontend will be cloned into `frontend` directory under the source root.

### Supported component types
There are currently four types of components supported. There is two simpler types, such as `cheEditor` and `chePlugin` and
two more complex - `kubernetes` (or `openshift`) and `dockerimage`.
Please note that all components inside single devfile must have unique names.
Detailed component types explanation below:

#### cheEditor
Describes the editor which used in workspace by defining its id.
Devfile can only contain one component with `cheEditor` type.

```
...
components:
  - alias: theia-editor
    type: cheEditor
    id: eclipse/che-theia/next
```

If it is missing then a default editor will be provided along with its default plugins.
The default plugins will be provided also for an explicitly defined editor with the same ID as the default one (even if in a different version).
By default, `Che Theia` is configured as default editor along with `Che Machine Exec` plugin.
You're able to put `editorFree:true` attribute into Devfile attributes in case you do not need any editor in your workspace.

#### chePlugin
Describes the plugin which used in workspace by defining it's id.
It is allowed to have several `chePlugin` components.

```
...
  components:
   - alias: exec-plugin
     type: chePlugin
     id: eclipse/che-machine-exec-plugin/0.0.1
```

Both types above using id, which is slash-separated publisher, name and version of plugin from Che Plugin registry.  
List of available Che plugins and more information about registry can be found on https://github.com/eclipse/che-plugin-registry.

It is also possible to specify own registry for the cheEditor and chePlugin types, by using
`registryUrl` parameter as follows:

```
...
  components:
   - alias: exec-plugin
     type: chePlugin
     registryUrl: https://my-customregistry.com
     id: eclipse/che-machine-exec-plugin/0.0.1
```

As an alternative way of specifying editor or plugin, instead of using plugin id (+ optional registry),
it is possible to provide direct link to the plugin descriptor (typically, named `meta.yaml`) by using
the reference field:

```
...
  components:
   - alias: exec-plugin
     type: chePlugin
     reference: https://raw.githubusercontent.com.../plugin/1.0.1/meta.yaml
```

Please note it's not possible to mix id and reference in single plugin definition, they are mutually exclusive. 


For each of types above it is also possible to specify container(s) memory limit as follows:
```
...
  components:
   - alias: exec-plugin
     type: chePlugin
     id: eclipse/che-machine-exec-plugin/0.0.1
     memoryLimit: 256M
```
This limit will be applied to each container of given plugin.

A plugin may need to be precisely tuned and in such case plugin preferences should be used.
Example shows how jvm may be configured with plugin's preferences.
```
...
-
  id: redhat/java/0.38.0
  type: chePlugin
  preferences:
     java.jdt.ls.vmargs: '-noverify -Xmx1G -XX:+UseG1GC -XX:+UseStringDeduplication'
```

#### kubernetes/openshift
More complex component type, which allows to apply configuration from kubernetes/openshift lists. Content of the component may be provided either via `reference` attribute which points to the file with component content.
```
...
  components:
    - alias: mysql
      type: kubernetes
      reference: petclinic.yaml
      selector:
        app.kubernetes.io/name: mysql
        app.kubernetes.io/component: database
        app.kubernetes.io/part-of: petclinic
```
Alternatively, if you need to post devfile with such components to REST API, contents of K8S/Openshift list can be embedded into devfile using `referenceContent` field:

```
...
  components:
    - alias: mysql
      type: kubernetes
      reference: petclinic.yaml
      referenceContent: |
           kind: List
           items:
            -
             apiVersion: v1
             kind: Pod
             metadata:
              name: ws
             spec:
              containers:
              ... etc
```

As with `dockerimage` component described below, it is possible to override the entrypoint of the
containers contained in the Kubernetes/Openshift list using the `command` and `args` properties (as
[understood](https://kubernetes.io/docs/tasks/inject-data-application/define-command-argument-container/#notes)
by Kubernetes). Of course, there can be more containers in the list (contained in pods or pod
templates of deployments) and so there needs to be a way of selecting which containers to apply the
entrypoint changes to.

The entrypoints can be defined for example like this:

```yaml
...
  components:
    - alias: appDeployment
      type: kubernetes
      reference: app-deployment.yaml
      entrypoints:
      - parentName: mysqlServer
        command: ['sleep']
        args: ['infinity']
      - parentSelector:
          app: prometheus
        args: ['-f', '/opt/app/prometheus-config.yaml']
```

You can see that the `entrypoints` list contains constraints for picking the containers along with
the command/args to apply to them. In the example above, the constraint is `parentName: mysqlServer`
which will cause the command to be applied to all containers defined in any parent object called
`mysqlServer`. The parent object is assumed to be a top level object in the list defined in the
referenced file, e.g. `app-deployment.yaml` in the example above.

Other types of constraints (and their combinations) are possible:

* `containerName` - the name of the container
* `parentName` - the name of the parent object that (indirectly) contains the containers to override
* `parentSelector` - the set of labels the parent object needs to have

Combination of these constraints can be used to precisely locate the containers inside the
referenced Kubernetes list.

#### dockerimage
Component type which allows to define docker image based configuration of container in workspace.
Devfile can only contain one component with `dockerimage` type.

```
 ...
 components:
   - alias: maven
     type: dockerimage
     image: eclipe/maven-jdk8:latest
     volumes:
       - name: mavenrepo
         containerPath: /root/.m2
     env:
       - name: ENV_VAR
         value: value
     endpoints:
       - name: maven-server
         port: 3101
         attributes:
           protocol: http
           secure: 'true'
           public: 'true'
           discoverable: 'false'
     memoryLimit: 1536M
     command: ['tail']
     args: ['-f', '/dev/null']
```

### Commands expanded
Devfile allows to specify commands set to be available for execution in workspace. Each command may contain subset of actions, which are related to specific component, in whose container it will be executed.


```
 ...
 commands:
   - name: build
     actions:
       - type: exec
         component: mysql
         command: mvn clean
         workdir: /projects/spring-petclinic
```

### Devfile attributes

Devfile attributes may be used to configure some features.

#### Editor free
If editor is not specified Devfile then default one will be provided. In case when no editor is needed `editorFree` attribute should be used.
Default value is `false` and means that Devfile needs default editor to be provisioned if no one is defined.
Example of Devfile without editor
```yaml
apiVersion: 1.0.0
metadata:
  name: petclinic-dev-environment
components:
  - alias: myApp
    type: kubernetes
    local: my-app.yaml
attributes:
  editorFree: true
```

#### Ephemeral mode
By default volumes and PVCs specified in Devfile are bound to host folder to persist data even after container restart.
Sometimes it may be needed to disable data persistence for some reasons, like when volume backend is incredibly slow and it is needed to make workspace faster.
To achieve it the `persistVolumes` devfile attribute should be used. Default value is `true`, and in case of `false` `emptyDir` volumes will be used for configured volumes and PVC.
Example of Devfile with ephemeral mode enabled
```yaml
apiVersion: 1.0.0
metadata:
  name: petclinic-dev-environment
projects:
  - name: petclinic
    source:
      type: git
      location: 'https://github.com/che-samples/web-java-spring-petclinic.git'
attributes:
  persistVolumes: false
```

### Live working examples

  - [NodeJS simple "Hello World" example](https://che.openshift.io/f?url=https://raw.githubusercontent.com/redhat-developer/devfile/master/samples/web-nodejs-sample/devfile.yaml)
  - [NodeJS Application with Mongo DB example](https://che.openshift.io/f?url=https://raw.githubusercontent.com/redhat-developer/devfile/master/samples/web-nodejs-with-db-sample/devfile.yaml)
  - [Java Spring-Petclinic example](https://che.openshift.io/f?url=https://raw.githubusercontent.com/redhat-developer/devfile/master/samples/web-java-spring-petclinic/devfile.yaml)
  - [Theia frontend plugin example](https://che.openshift.io/f?url=https://raw.githubusercontent.com/redhat-developer/devfile/master/samples/theia-hello-world-frontend-plugin/devfile.yaml)
