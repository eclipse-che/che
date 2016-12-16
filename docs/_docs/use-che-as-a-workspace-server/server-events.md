---
tags: [ "eclipse" , "che" ]
title: Events
excerpt: ""
layout: docs
permalink: /:categories/events/
---
Events are defined in Che in order to circulate information between processes in a publish/subscribe model. Event notifications are provided on Websocket channels. Clients can subscribe to these channels.
```shell  
# Get the workspace Id
curl http://localhost:8080/ide/api/workspace

# Open websocket connection
wscat -c ws://localhost:8080/ide/api/ws/workspacesq6co30qcxi1kqsj

# Subscribe to output channel
> {"uuid":"12345678-1234-1234-1234-123456789123"method":"POST"path":null,"headers":[{"name":"x-everrest-websocket-message-type"value":"subscribe-channel"}],"body":"{\"channel\":\"process:output:ABCDEFGH-AAAA-BBBB-CCCC-ABCDEFGHIJKL\"}"}

# In another window execute a command
curl -X POST -H 'Content-Type: application/json' -d '{ "name": "build\ "type": "mvn\ "attributes": {}, "commandLine": "mvn -f /projects/my-first-sample clean install" }' http://localhost:8080/ide/api/machine/machineugqib6icjyj2afva/command?outputChannel=process:output:ABCDEFGH-AAAA-BBBB-CCCC-ABCDEFGHIJKL

# Get output messages (in subscription window)
< {"method":"POST"headers":[{"name":"x-everrest-websocket-message-type"value":"subscribe-channel"}],"responseCode":200,"body":"{\"channel\":\"process:output:ABCDEFGH-AAAA-BBBB-CCCC-ABCDEFGHIJKL\"}"uuid":"12345678-1234-1234-1234-123456789123"}
< {"headers":[{"name":"x-everrest-websocket-channel"value":"process:output:ABCDEFGH-AAAA-BBBB-CCCC-ABCDEFGHIJKL"},{"name":"x-everrest-websocket-message-type"value":"none"}],"responseCode":0,"body":"\"[STDOUT] [INFO] Scanning for projects...\""}
< ...
```
