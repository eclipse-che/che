---
tags: [ "eclipse" , "che" ]
title: Stacks
excerpt: "Manage stacks using Che REST API"
layout: docs
permalink: /:categories/stack/
---
## List All Stacks
```curl  
curl --header 'Accept: application/json' http://localhost:8080/api/stack?tags=Node.JS\
```
Query parameter `tags` is optional and used to narrow down search results.

It's possible to use multiple query parameters, e.g. `http://localhost:8080/api/stack?tags=Java&tags=Ubuntu`

Swagger: http://localhost:8080/swagger/#!/stack/searchStacks

##Create a New Stack

JSON with stack configurations is sent in a POST request:
```curl  
curl -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{"name":"my-custom-stack"source":{"origin":"codenvy/ubuntu_jdk8"type":"image"},"components":[],"tags":["my-custom-stack"Ubuntu"Git"Subversion"],"id":"stack15l7wsfqffxokhle"workspaceConfig":{"environments":{"default":{"machines":{"default":{"attributes":{"memoryLimitBytes":"1048576000"},"servers":{},"agents":["org.eclipse.che.terminal"org.eclipse.che.ws-agent"org.eclipse.che.ssh"]}},"recipe":{"location":"codenvy/ubuntu_jdk8"type":"dockerimage"}}},"defaultEnv":"default"projects":[],"name":"default"commands":[],"links":[]},"creator":"che"description":"Default Blank Stack."scope":"general"}' http://localhost:8080/api/stack
```
Swagger:
```json  
{
  "name": "my-custom-stack\n  "source": {
    "origin": "codenvy/ubuntu_jdk8\n    "type": "image"
  },
  "components": [],
  "tags": [
    "my-custom-stack\n    "Ubuntu\n    "Git\n    "Subversion"
  ],
  "id": "stackocriwhwviu1kjm2r\n  "workspaceConfig": {
    "environments": {
      "default": {
        "machines": {
          "default": {
            "attributes": {
              "memoryLimitBytes": "1048576000"
            },
            "servers": {},
            "agents": [
              "org.eclipse.che.terminal\n              "org.eclipse.che.ws-agent\n              "org.eclipse.che.ssh"
            ]
          }
        },
        "recipe": {
          "location": "codenvy/ubuntu_jdk8\n          "type": "dockerimage"
        }
      }
    },
    "defaultEnv": "default\n    "projects": [],
    "name": "default\n    "commands": [],
    "links": []
  },
  "creator": "che\n  "description": "Default Blank Stack.\n  "scope": "general"
}
```
## Update an Existing Stack

JSON with updated stack configs is sent in a PUT request:
```curl  
curl -X PUT --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{"name":"my-custom-stack"source":{"origin":"codenvy/ubuntu_jdk8"type":"image"},"components":[],"tags":["my-custom-stack"Ubuntu"Git"Subversion"],"id":"stacki7jf4x4n2cz6r3cr"workspaceConfig":{"environments":{"default":{"machines":{"default":{"attributes":{"memoryLimitBytes":"1048576000"},"servers":{},"agents":["org.eclipse.che.terminal"org.eclipse.che.ws-agent"org.eclipse.che.ssh"]}},"recipe":{"location":"codenvy/ubuntu_jdk8"type":"dockerimage"}}},"defaultEnv":"default"projects":[],"name":"default"commands":[],"links":[]},"creator":"che"description":"NEW-DESCRIPTION"scope":"general"}' http://localhost:8080/api/stack/${id}
```
Swagger:
```json  
{
  "name": "my-custom-stack\n  "source": {
    "origin": "codenvy/ubuntu_jdk8\n    "type": "image"
  },
  "components": [],
  "tags": [
    "my-custom-stack\n    "Ubuntu\n    "Git\n    "Subversion"
  ],
  "workspaceConfig": {
    "environments": {
      "default": {
        "machines": {
          "default": {
            "attributes": {
              "memoryLimitBytes": "1048576000"
            },
            "servers": {},
            "agents": [
              "org.eclipse.che.terminal\n              "org.eclipse.che.ws-agent\n              "org.eclipse.che.ssh"
            ]
          }
        },
        "recipe": {
          "location": "codenvy/ubuntu_jdk8\n          "type": "dockerimage"
        }
      }
    },
    "defaultEnv": "default\n    "projects": [],
    "name": "default\n    "commands": [],
    "links": []
  },
  "creator": "che\n  "description": "Default Blank Stack.\n  "scope": "general"
}
```
##Delete a Stack

Stack ID is passed as a path parameter:
```curl  
curl -X DELETE --header 'Accept: application/json' http://localhost:8080/api/stack/${id}\
```
