---
tags: [ "eclipse" , "che" ]
title: REST API
excerpt: "APIs for the workspace master and agent using Swagger"
layout: docs
permalink: /:categories/rest-api/
---
{% include base.html %}
Eclipse Che has activated Swagger annotations for its embedded APIs. There are APIs that are hosted within the Che server, which we call workspace master APIs for managing workspaces. And there are APIs that are hosted within each workspace, launched and hosted by a workspace agent that is injected into each workspace machine when it boots.  
# Workspace Master APIs  

![Swagger.PNG]({{ base }}/assets/imgs/Swagger.PNG)

```http  
http://localhost:8080/swagger/\
```

# Workspace Agent APIs  
Each workspace has its own set of APIs. The workspace agent advertises its swagger configuration using a special URL. You access the workspace agent's APIs through the workspace master. The workspace master connects to the agent, grabs the swagger configuration, and executes it within the workspace master.  You can find the hostname and port number of your workspace in the IDE in the operations perspective. The operations view displays a table of servers that are executing within the currently active workspace. One of those servers will be labeled as the workspace agent and it will display the hostname and port number of the agent.
```text  
http://{workspace-master-host}/swagger/?url=http://{workspace-agent-host}/ide/ext/docs/swagger.json

# Example
http://localhost:8080/swagger/?url=http://192.168.99.100:32773/ide/ext/docs/swagger.json\
```

```shell  
curl -X POST -H 'Content-Type: application/json' -d '{ "mixins": [ "git" ], "description": "A hello world Java application.\ "name": "console-java-simple\ "type": "maven\ "path": "/console-java-simple\ "attributes": { "maven.artifactId": [ "console-java-simple" ], "maven.parent.version": [ "" ], "maven.test.source.folder": [ "src/test/java" ], "maven.version": [ "1.0-SNAPSHOT" ], "maven.parent.groupId": [ "" ], "languageVersion": [ "1.8.0_45" ], "language": [ "java" ], "maven.source.folder": [ "src/main/java" ], "git.repository.remotes": [ "https://github.com/che-samples/console-java-simple.git" ], "maven.groupId": [ "org.eclipse.che.examples" ], "maven.packaging": [ "jar" ], "vcs.provider.name": [ "git" ], "maven.resource.folder": [], "git.current.branch.name": [ "master" ], "maven.parent.artifactId": [ "" ] } }' http://localhost:8080/api/ext/project/workspacesq6co30qcxi1kqsj?name=console-java-simple
```

```shell  
curl -X POST -H 'Content-Type: application/json' -d '{ "name": "build\ "type": "mvn\ "attributes": {}, "commandLine": "mvn -f ${current.project.path} clean install" }' http://localhost:8080/ide/api/workspace/workspacesq6co30qcxi1kqsj/command
```

```shell  
# Get the machine Id
curl http://localhost:8080/ide/api/machine?workspace=workspacesq6co30qcxi1kqsj

# Open websocket connection
wscat -c ws://localhost:8080/ide/api/ws/workspacesq6co30qcxi1kqsj

# Subscribe to output channel
> {"uuid":"12345678-1234-1234-1234-123456789123"method":"POST"path":null,"headers":[{"name":"x-everrest-websocket-message-type"value":"subscribe-channel"}],"body":"{\"channel\":\"process:output:ABCDEFGH-AAAA-BBBB-CCCC-ABCDEFGHIJKL\"}"}

# In other window execute the command
curl -X POST -H 'Content-Type: application/json' -d '{ "name": "build\ "type": "mvn\ "attributes": {}, "commandLine": "mvn -f /projects/console-java-simple clean install" }' http://localhost:8080/ide/api/machine/machineugqib6icjyj2afva/command?outputChannel=process:output:ABCDEFGH-AAAA-BBBB-CCCC-ABCDEFGHIJKL

# Get output messages (in subscription window)
< {"method":"POST"headers":[{"name":"x-everrest-websocket-message-type"value":"subscribe-channel"}],"responseCode":200,"body":"{\"channel\":\"process:output:ABCDEFGH-AAAA-BBBB-CCCC-ABCDEFGHIJKL\"}"uuid":"12345678-1234-1234-1234-123456789123"}
< {"headers":[{"name":"x-everrest-websocket-channel"value":"process:output:ABCDEFGH-AAAA-BBBB-CCCC-ABCDEFGHIJKL"},{"name":"x-everrest-websocket-message-type"value":"none"}],"responseCode":0,"body":"\"[STDOUT] [INFO] Scanning for projects...\""}
< ...
```
