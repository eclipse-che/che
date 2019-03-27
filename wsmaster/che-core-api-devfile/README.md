

### Introduction
Previously, two kind of recipes were available to bootstrap a cloud developer workspace and to make it portable: [Chefile](https://www.eclipse.org/che/docs/chefile.html) 
and [Factories](https://www.eclipse.org/che/docs/factories-getting-started.html#try-a-factory).
As a continuation of this, the brand new `devfile` format was introduced, which combines simplicity and support for high variety of different components available to develop a container based application.

### What the devfile consists of
The minimal devfile sufficient to run a workspace from it, consists of the following parts:
 - Specification version
 - Name
 - A list of components: the development components and user runtimes 
 
To get more functional workspace, the following parts can be added:
 - A list of projects: the source code repositories
 - A list of commands: actions to manage the workspace components like running the dev tools, starting the runtime environments etc...

Example of the minimal devfile with project and standard plugins set (Theia editor + exec plugin):

```

---
specVersion: 0.0.1
name: petclinic-dev-environment
projects:
  - name: petclinic
    source:
      type: git
      location: 'https://github.com/che-samples/web-java-spring-petclinic.git'
components:
  - name: theia-editor
    type: cheEditor
    id: org.eclipse.che.editor.theia:1.0.0
  - name: exec-plugin
    type: chePlugin
    id: che-machine-exec-plugin:0.0.1
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


 
### Supported component types
There are currently four types of components supported. There is two simpler types, such as `cheEditor` and `chePlugin` and 
two more complex - `kubernetes` (or `openshift`) and `dockerimage`.
Please note that all components inside single devfile must have unique names.
Detailed component types explanation below:

#### cheEditor 
Describes the editor which used in workspace by defining it's id. 
Devfile can only contain one component with `cheEditor` type.

```
...
components:
  - name: theia-editor
    type: cheEditor
    id: org.eclipse.che.editor.theia:1.0.0
```

#### chePlugin
Describes the plugin which used in workspace by defining it's id. 
It is allowed to have several `chePlugin` components.

```
...
  components:
   - name: exec-plugin
     type: chePlugin
     id: che-machine-exec-plugin:0.0.1
```

Both types above using composite id, which is colon-separated id and version of plugin from Che Plugin registry.  
List of available Che plugins and more information about registry can be found on https://github.com/eclipse/che-plugin-registry 



#### kubernetes/openshift
More complex component type, which allows to apply configuration from kubernetes/openshift lists. Content of the component may be provided either via `local` attribute which points to the file with component content.
```
...
  components:
    - name: mysql
      type: kubernetes
      local: petclinic.yaml
      selector:
        app.kubernetes.io/name: mysql
        app.kubernetes.io/component: database
        app.kubernetes.io/part-of: petclinic
```
Contents of the `local` file is currently read _ONLY_ if the devfile and local file both placed in the same public GitHub repository. 
So, alternatively, if you need to post devfile with such components to REST API, contents of K8S/Openshift list can be embedded into devfile using `localContent` field:

```
...
  components:
    - name: mysql
      type: kubernetes
      local: petclinic.yaml
       localContent: |
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
    - name: appDeployment
      type: kubernetes
      local: app-deployment.yaml
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
   - name: maven
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

### Live working examples

  - [NodeJS simple "Hello World" example](https://che.openshift.io/f?url=https://raw.githubusercontent.com/redhat-developer/devfile/master/samples/web-nodejs-sample/devfile.yaml)
  - [NodeJS Application with Mongo DB example](https://che.openshift.io/f?url=https://raw.githubusercontent.com/redhat-developer/devfile/master/samples/web-nodejs-with-db-sample/devfile.yaml)
  - [Java Spring-Petclinic example](https://che.openshift.io/f?url=https://raw.githubusercontent.com/redhat-developer/devfile/master/samples/web-java-spring-petclinic/devfile.yaml)
  - [Theia frontend plugin example](https://che.openshift.io/f?url=https://raw.githubusercontent.com/redhat-developer/devfile/master/samples/theia-hello-world-frontend-plugin/devfile.yaml)

### Planned features
There is still a lot of plans to extend Devfile possibilities, such as support multiple dockerimage components etc
