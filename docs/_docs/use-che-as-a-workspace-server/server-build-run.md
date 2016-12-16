---
tags: [ "eclipse" , "che" ]
title: Editing Files
excerpt: ""
layout: docs
permalink: /:categories/build-run/
---
```shell  
curl -X POST -d 'var i = 1;' http://localhost:8080/api/ext/project/workspacesq6co30qcxi1kqsj/file/my-first-sample?name=newfile.js\
```

```shell  
curl -X PUT -d $'var i = 1;\nvar test = "hello Eclipse Che";' http://localhost:8080/api/ext/project/workspacesq6co30qcxi1kqsj/file/my-first-sample/newfile.js
```

```shell  
curl -X DELETE http://localhost:8080/api/ext/project/workspacex4zl7nvex1yldosj/my-first-sample/newfile.js\
```
