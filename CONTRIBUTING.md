
# Contributing to Che

- [Other Che repositories](#other-che-repositories)
- [Devfile to contribute](#devfile-to-contribute)
- [Contribute to ...](#contribute-to-...)
  - [Dashboard](#dashboard)
  - [Che Server ](#che-server...)

## Other Che repositories

Che is composed of multiple sub projects. For each projects we provide a *CONTRIBUTE.md* file describing how to setup the development environment to start your contribution. Most of the time, we encourage you to use Che to contribute to Che.

<!-- begin repository list -->
Repository | Component | Description | Devfile | Documentation
--- | --- | ---  | --- | ---
[che](https://github.com/eclipse/che) | | (this repository) the main project repository | [devfile](https://github.com/eclipse/che/blob/main/devfile.yaml) | [doc](https://github.com/eclipse/che/blob/main/CONTRIBUTING.md#che-server-aka-ws-master)
---| [tests](https://github.com/eclipse/che/tree/main/tests) | source code of our integration tests. | | |
[che-server](https://github.com/eclipse-che/che-server/) | | Che server project repository | [devfile](https://github.com/eclipse-che/che-server/blob/HEAD/devfile.yaml) | [doc](https://github.com/eclipse/che/blob/HEAD/CONTRIBUTING.md#che-server-aka-ws-master)
---| [dockerfiles](https://github.com/eclipse-che/che-server/tree/HEAD/dockerfiles) | source code, dockerfiles to build our main docker images. Note that Che-theia related dockerfiles are located in che-theia repo. | | |
---| [che-server](https://github.com/eclipse-che/che-server/tree/HEAD/wsmaster) | orchestrates the Che workspaces with devfiles on Kubernetes | | |
---| [tests](https://github.com/eclipse/che/tree/master/tests) | source code of our integration tests. | | |
[che-theia](https://github.com/eclipse-che/che-theia) | | Theia IDE integrated in Che. | [devfile](https://github.com/eclipse-che/che-theia/blob/main/devfiles/che-theia-all.devfile.yaml) | [doc](https://github.com/eclipse-che/che-theia/blob/main/CONTRIBUTING.md)
---| [generator](https://github.com/eclipse-che/che-theia/tree/main/generator) | `che:theia init` CLI to prepare and build che-theia | | |
[chectl](https://github.com/che-incubator/chectl) | | The CLI to install Che, create and start workspaces and devfiles | [devfile](https://github.com/che-incubator/chectl/blob/master/devfile.yaml) | [doc](https://github.com/che-incubator/chectl/blob/master/CONTRIBUTING.md)
[dashboard](https://github.com/eclipse-che/che-dashboard) | | UI to manage workspaces, devfiles, etc. | [devfile](https://github.com/eclipse-che/che-dashboard/blob/main/devfile.yaml) | [doc](https://github.com/eclipse-che/che-dashboard/blob/main/CONTRIBUTING.md)
[devfile-registry](https://github.com/eclipse-che/che-devfile-registry) | | The default set of devfiles that would be made available on the Che dashboard stacks. |  | 
[docs](https://github.com/eclipse/che-docs) | | Eclipse Che documentation https://www.eclipse.org/che/docs/ source code. | [devfile](https://github.com/eclipse/che-docs/blob/master/devfile.yaml) | [doc](https://github.com/eclipse/che/blob/master/CONTRIBUTING.md#dashboard)
[machine-exec](https://github.com/eclipse-che/che-machine-exec) | | Interface to execute tasks and terminals on other containers within a workspace. | [devfile](https://github.com/eclipse-che/che-machine-exec/blob/master/devfile.yaml) | [doc](https://github.com/eclipse-che/che-machine-exec/blob/master/CONTRIBUTING.md)
[operator](https://github.com/eclipse-che/che-operator) | | Che operator to deploy, update and manage K8S/OpenShift resources of Che. | [devfile](https://github.com/eclipse-che/che-operator/blob/master/devfile.yaml) | 
[plugin-broker](https://github.com/eclipse/che-plugin-broker) | | The workspace microservice that is in charge of analyzing, preparing and installing the workspace components defined in a Devfile. | [devfile](https://github.com/eclipse/che-plugin-broker/blob/master/devfile.yaml) | [doc](https://github.com/eclipse/che-plugin-broker/blob/master/CONTRIBUTING.md)
[plugin-registry](https://github.com/eclipse-che/che-plugin-registry) | | The default set of Che plugins (vscode extension + containers) or editors that could be installed on any Che workspaces. |  | 
[website](https://github.com/eclipse/che-website) | | https://eclipse.org/che website source code. | [devfile](https://github.com/eclipse/che-website/blob/master/devfile.yaml) | 
[workspace-client](https://github.com/eclipse-che/che-workspace-client) | | JS library to interact with a che-server. |  | 
[che-sidecar-bazel](https://github.com/che-dockerfiles/che-sidecar-bazel) | | Eclipse Che Sidecar container for Bazel tooling |  | 
[che-sidecar-protobuf](https://github.com/che-dockerfiles/che-sidecar-protobuf) | | Eclipse Che Sidecar container for Protobuf tooling |  | 
[che-sidecar-tekton](https://github.com/che-dockerfiles/che-sidecar-tekton) | | Eclipse Che Sidecar container for Tekton tooling |  | 
[che-sidecar-kubernetes-tooling](https://github.com/che-dockerfiles/che-sidecar-kubernetes-tooling) | | Eclipse Che Sidecar container for Kubernetes tooling |  | 
[che-sidecar-openshift-connector](https://github.com/che-dockerfiles/che-sidecar-openshift-connector) | | Eclipse Che Sidecar container for OpenShift connector tooling |  | 
[che-container-tools](https://github.com/che-dockerfiles/che-container-tools) | | Base image used for sidecars that service container tooling plugins |  | 
[che-sidecar-python](https://github.com/che-dockerfiles/che-sidecar-python) | | Eclipse Che Sidecar container for python tooling |  | 
[che-custom-nodejs-deasync](https://github.com/che-dockerfiles/che-custom-nodejs-deasync) | | Provides a custom nodejs binary embedding deasync node-gyp module as builtin module |  | 
[che-sidecar-go](https://github.com/che-dockerfiles/che-sidecar-go) | | Eclipse Che sidecar container for go |  | 
[che-sidecar-sonarlint](https://github.com/che-dockerfiles/che-sidecar-sonarlint) | | Eclipse Che sidecar container for sonalint extension |  | 
[che-sidecar-dotnet](https://github.com/che-dockerfiles/che-sidecar-dotnet) | | Eclipse Che sidecar container for dotnet |  | 
[che-sidecar-shellcheck](https://github.com/che-dockerfiles/che-sidecar-shellcheck) | | Eclipse Che sidecar container for shellcheck |  | 
[che-sidecar-camelk](https://github.com/che-dockerfiles/che-sidecar-camelk) | | Eclipse Che sidecar container for camelk |  | 
[che-sidecar-vale](https://github.com/che-dockerfiles/che-sidecar-vale) | | Eclipse Che sidecar container for vale |  | 
[che-cert-manager-ca-cert-generator-image](https://github.com/che-dockerfiles/che-cert-manager-ca-cert-generator-image) | | CA cert generation job image used by chectl |  | 
[che-buildkit-base](https://github.com/che-dockerfiles/che-buildkit-base) | | Eclipse Che Sidecar container for buildkit tooling |  | 
[che-sidecar-scala](https://github.com/che-dockerfiles/che-sidecar-scala) | | Eclipse Che Sidecar container for scala tooling |  | 
[che-buildah-base](https://github.com/che-dockerfiles/che-buildah-base) | | Use this image to build docker images using buildah |  | 
[che-docker-registry-image-copier](https://github.com/che-dockerfiles/che-docker-registry-image-copier) | | copy images between public and private docker registry inside k8s cluster |  | 
[che-php-base](https://github.com/che-dockerfiles/che-php-base) | | Base image to be used for the PHP devfile |  | 
[che-tls-secret-creator](https://github.com/che-dockerfiles/che-tls-secret-creator) | | This images generates TLS certificates |  | 
[build-action](https://github.com/che-dockerfiles/build-action) | | Custom GitHub Action for building sidecar Dockerfiles |  | 
[che-sidecar-podman](https://github.com/che-dockerfiles/che-sidecar-podman) | | Eclipse Che Sidecar container for podman tooling |  | 
[che-sidecar-clang](https://github.com/che-dockerfiles/che-sidecar-clang) | | Eclipse Che Sidecar container for clang tooling |  | 
[che-sidecar-php](https://github.com/che-dockerfiles/che-sidecar-php) | | Eclipse Che Sidecar container for php tooling |  | 
[che-sidecar-java](https://github.com/che-dockerfiles/che-sidecar-java) | | Eclipse Che Sidecar container for java tooling |  | 
[che-sidecar-dependency-analytics](https://github.com/che-dockerfiles/che-sidecar-dependency-analytics) | | Eclipse Che Sidecar container for dependency analytics tooling |  | 
[che-sidecar-node](https://github.com/che-dockerfiles/che-sidecar-node) | | Eclipse Che Sidecar container for node tooling |  | 
[che-theia-openshift-auth](https://github.com/che-incubator/che-theia-openshift-auth) | | OpenShift authentication plugin |  | 
[configbump](https://github.com/che-incubator/configbump) | | Simple Kubernetes controller that is able to quickly synchronize a set of config maps |  | 
[workspace-data-sync](https://github.com/che-incubator/workspace-data-sync) | | Provides the ability to increase I/O performance for a developer workspaces |  | 
[che-workspace-telemetry-client](https://github.com/che-incubator/che-workspace-telemetry-client) | | abstract telemetry API and a Typescript implementation of the API. |  | 
[kubernetes-image-puller](https://github.com/che-incubator/kubernetes-image-puller) | | ensures that all nodes in the cluster have those images cached |  | 
<!-- end repository list -->

## Devfile to contribute

We are trying to provide a devfile for each areas where you could contribute. Each devfile could be run on any Che instances to setup a *ready-to-code* developer environment. Beware that each of them may need a certain amount of memory.
Devfile could be launched through a factory or [chectl](https://github.com/che-incubator/chectl) cli.

```bash
$ chectl workspace:start -f devfiles/che-theia-all.devfile.yaml
```

or

```bash
$ chectl workspace:start -f https://raw.githubusercontent.com/eclipse/che-theia/main/devfiles/che-theia-all.devfile.yaml
```

or `https://<CheInstance>/f?url=https://raw.githubusercontent.com/eclipse/che-theia/main/devfiles/che-theia-all.devfile.yaml`

## Contribute to ...

Let's cover the developer flow for these projects:

### Che server
There is a [devfile](https://github.com/eclipse-che/che-server/blob/HEAD/devfile.yaml) for development of Che server in Che.
To build Che one may run a predefined build task from the devfile.

Starting Che master requires some manual steps.
Open a terminal in runtime container (`che-server-runtime`) and perform:
 - First, set `CHE_HOME` environment variable with absolute path to parent folder of Che master's Tomcat.
   It might look like `/projects/che/assembly/assembly-main/target/eclipse-che-*-SNAPSHOT/eclipse-che-*-SNAPSHOT`.
 - Then set `CHE_HOST` with the endpoint of new Che master.
   If using the [devfile](devfile.yaml) the endpoint is `che-dev` and already set.
 - After, set `CHE_INFRASTRUCTURE_ACTIVE` according to your environment.
   For example: `openshift` (note, use `kubernetes` and `openshift` insted of `minikube` and `minishift` correspondingly).
 - Run `/entrypoint.sh`.
   After this, new Che master should be accesible from the `che-dev` endpoint.
   To reach Swagger use url from `che-dev` endpoint with `/swagger` suffix.

To start a workspace from Che server under development some additional configuration of the cluster is needed.
One should add rights for the service account to be able to perform all needed for Che server actions.
Example for Openshift (in case of Kubernetes replace `oc` with `kubectl`):
```bash
cat << EOF | oc apply -f -
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  labels:
    app: che
    component: che
  name: che-workspace-admin
  namespace: che
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: admin
subjects:
- kind: ServiceAccount
  name: che-workspace
  namespace: che
EOF
```

Also `CHE_API_INTERNAL`, `CHE_API_EXTERNAL` and `CHE_API` should be set in runner container and point to new Che server API.
If one uses provided devfile, they are already set to: `http://che-dev:8080/api`, which should be changed in case of https protocol.


## Pull Request Template and its Checklist

Che repositories includes a GitHub Pull Request Template. Contributors must read and complete the template. In particular there is a list of requirements that the author needs to fulfil to merge the PR. This sections goes into the details of this checklist.

### The Eclipse Contributor Agreement is valid

The author has completed the [Eclipse Contributor Agreement](https://accounts.eclipse.org/user/eca) and has signed the commits using his email.

### Code produced is complete

No `TODO` comments left in the PR source code.

### Code builds without errors

The author has verified that code builds, tests pass and linters are happy.

### Tests are covering the bugfix

If the Pull Request fixes a bug it must includes a new automated test. The test validates the fix and protect against future regressions.

### The repository devfile is up to date and works

The devfile commands used to build and run the application are still working.

### Sections "What issues does this PR fix or reference" and "How to test this PR" completed

Never omit the two sections "What issues does this PR fix or reference" and "How to test this PR".

### Relevant user documentation updated

The author has documented the changes to Che installation, usage or management in [Che documentation](https://github.com/eclipse/che-docs).

### Relevant contributing documentation updated

Document changes to the steps to contribute to the project in the `CONTRIBUTING.md` files.

### CI/CD changes implemented, documented and communicated

Update CI/CD scripts and documentation when the PR includes changes to the build, test, distribute or deploy procedures. Communicate CI/CD changes to the whole community with an email.
