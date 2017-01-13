---
tags: [ "eclipse" , "che" ]
title: Sourcegraph in Che
excerpt: ""
layout: tutorials
permalink: /:categories/sourcegraph/
---
{% include base.html %}
Sourcegraph is a version-control platform built with Code Intelligence.
```shell  
# In the dashboard, create a new project and import from source:
https://src.sourcegraph.com/sourcegraph

# Choose Custom Stack option. Create the workspace using the following image:
FROM codenvy/sourcegraph

# Name the project `sourcegraph` (this should be the **exact** name since it is then used in commands and `GOPATH`)\
```

```shell  
# Open Command Widget and create a custom command:
Title: run

Command: mkdir -p $GOPATH/src/src.sourcegraph.com/sourcegraph 2>/dev/null;  mv -v $GOPATH/* $GOPATH/src/src.sourcegraph.com/sourcegraph 2>/dev/null; sudo su - postgres -c '/usr/lib/postgresql/9.4/bin/pg_ctl -D /var/lib/postgresql/db -l /var/lib/postgresql/logfile start' && cd $GOPATH/src/src.sourcegraph.com/sourcegraph && make dep && make serve-dev

Preview: http://${server.port.3080}\
```

```text  
# Test your application
1. Open /src/src.sourcegraph.com/sourcegraph
2. Make some edits
3. Run the `run` command.
5. You can refresh the web app in the preview URL to see your changes.\
```
