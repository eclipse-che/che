---
tags: [ "eclipse" , "che" ]
title: Config&#58 Docker
excerpt: ""
layout: docs
permalink: /:categories/config-docker/
---
Eclipse Che workspaces are based upon a Docker image. You can either pull that image from a public registry, like Docker Hub, or a private registry which is managed by yourself. Images in a registry can be publicly visible or private, which require user credentials to access. You can also set up a private registry to act as a mirror to Docker Hub.  And, if you are running Eclipse Che behind a proxy, you can configure the Docker daemon registry to operate behind a proxy.
# Proxy for Docker  
If you are installing Eclipse Che behind a proxy and you want your users to create workspaces powered by images hosted at Docker Hub, then you will need to configure the Docker daemon used by Eclipse Che to [operate over a proxy](https://docs.docker.com/engine/admin/systemd/#http-proxy).
# Private Docker Images  
When users create a workspace in Eclipse Che, they must select a Docker image to power the workspace. We provide ready-to-go stacks which reference images hosted at the public Docker Hub. You can provide your own images that are stored in a local private registry or at Docker Hub. The images may be publicly or privately visible, even if they are part of a private registry.

### Accessing Private Images
You can configure Che to access private images in a public or private registry. Modify the `che.properties` to configure your private registry:
```text  
# Docker registry configuration.
# Note that you can configure many registries with different names.
#docker.registry.auth.your_registry_name.url=https://index.docker.io/v1/
#docker.registry.auth.your_registry_name.username=user-name
#docker.registry.auth.your_registry_name.password=user-password

# You can add as many registries as you need, e.g.:
docker.registry.auth.registry1.url
docker.registry.auth.registry2.url
\
```
Registries added in User Dashboard override registries added to `che.properties`.
# Private Docker Registries  
When creating a workspace, a user must reference a Docker image. The default location for images is located at Docker Hub. However, you can install your own Docker registry and host custom images within your organization.

When users create their workspace, they must reference the custom image in your registry. Whether you provide a custom stack, or you have users reference a custom workspace recipe from the dashboard, to access a private registry, you must provide the domain of the private registry in the `FROM` syntax of any referenced Dockerfiles.
```text  
# Syntax
FROM <repository>/<image>:<tag>

# Where repository is the hostname:port of your registry:
FROM my.registry.url:9000/image:latest\
```
To add a private registry, perform steps documented above (either adding  a registry to global configuration or in User Dashboard).
#### Custom Images
To get your custom image into a private registry, you will need to build it, tag it with the registry repository name, and push it into the registry. When tagging images into a private registry, they are always tagged with the fully qualified hostname of the registry that will host them. So it is not uncommon to see an image named `ops.codenvy.org:9000/myimage`.  


# Custom Dockerfiles  
Within Che, your workspaces are powered by a set of runtime environments. The default runtime is Docker. You can provide custom Dockerfiles or images that you author, which are used to power the workspaces used by your users.

You can:
1. Create a custom ready-to-go stack, which has a reference to your custom image and registry. Or:
2. Users can create a custom recipe when creating a workspace that references your registry.

## Provide Users Your Own Stack With Custom Dockerfile Recipe
See [Stacks](doc:stacks).
See [Templates](doc:templates).

## Users Create Custom Stack
When creating a workspace within Che, the user can select custom stack in the user dashboard. Your users can paste Dockerfile syntax which will be used to create a Docker image that is then used to create a runtime container for your workspace. The Dockerfile can reference base images at DockerHub or at a private registry.

## Privileged Mode
By default, Che workspaces powered by a Docker container are not configured with Docker privileged mode. Privileged mode is necessary if you want to enable certain features such as Docker in Docker. There are many security risks to activating this feature - please review the various issues with blogs posted online.  

```text  
# Activate your Che installation with priviliged mode
machine.docker.privilege_mode=true\
```

# Mirroring Docker Hub  
