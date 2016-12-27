---
tags: [ "eclipse" , "che" ]
title: Cuba Platform in Che
excerpt: ""
layout: tutorials
permalink: /:categories/cuba/
---
{% include base.html %}
CUBA Platform is a high level Java framework for faster enterprise software development.
```text  
#When on the User Dashboard, create a new project from existing sources:
https://github.com/Haulmont/platform-sample-sales

#Choose a Custom Stack and add the following recipe:
FROM codenvy/cuba\
```

```text  
#When the IDE opens, create a custom command:
Title:    start-platform
Command:  /home/user/studio-2.0.6/bin/studio -nogui
Preview:  http://${server.port.3080}\
```

```text  
#Click the preview URL. When in CUBA Platform, import an existing project from `/projects/platform-sample-sales`

#Build and run it. When a Tomcat starts, navigate to Machine perspective > Servers tabs. Find the port that corresponds to 8080.\
```
