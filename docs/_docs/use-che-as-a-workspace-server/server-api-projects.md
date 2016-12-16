---
tags: [ "eclipse" , "che" ]
title: Projects
excerpt: ""
layout: docs
permalink: /:categories/api-projects/
---
Che provides developer services within a workspace server whose project type and edit-build-run cycle can be customized. Product teams can deliver on-demand workspaces for their user-developers through the Che IDE or another. In this section we describe API and services that make Che act as a workspace server.

# Definition
In Che a **Project** is an instance of a Project Type with concrete values for all attributes and a set of associated Source Code that resides within the Project’s file system.
```curl  
# Get workspace id
curl http://localhost:8080/api/workspace

# Create the project
curl -X POST -H 'Content-Type: application/json' -d '{"name":"my-first-sample\ "attributes":{"maven.version":["1.0-SNAPSHOT"], "maven.packaging":["jar"], "maven.source.folder":["src/main/java"], "maven.test.source.folder":["src/test/java"], "maven.artifactId":["my.artifactId"], "maven.groupId":["my.groupId"]}, "links":[], "type":"maven\ "source":{"location":null, "type":null, "parameters":{}}, "contentRoot":null, "modules":[], "path":null, "description":null, "problems":[], "mixins":[]}'
http://localhost:8080/api/ext/project/workspacex4zl7nvex1yldosj?name=my-first-sample
```

```curl  
curl -X DELETE http://localhost:8080/api/ext/project/workspacex4zl7nvex1yldosj/my-first-sample\
```
