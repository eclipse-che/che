---
tags: [ "eclipse" , "che" ]
title: Google App Engine and Che
excerpt: ""
layout: tutorials
permalink: /:categories/gae/
---
{% include base.html %}
```text  
# When on the Dashboard create a new project with PHP GAE or Python GAE stack from the Stack Library and a sample PHP or Python project. \
```

```text  
# Run command for PHP GAE project will have the following syntax:

Command: cd ${GAE} && ./dev_appserver.py 2>&1 --php_executable_path=/usr/bin/php5-cgi --skip_sdk_update_check true --host=0.0.0.0 --admin_host=0.0.0.0 ${current.project.path}
Preview URL: http://${server.port.8080}

# For Python GAE project:

Command: cd ${GAE} && ./dev_appserver.py 2>&1 --skip_sdk_update_check true --host=0.0.0.0 --admin_host=0.0.0.0 ${current.project.path}
Preview URL: http://${server.port.8080}\
```

```text  
# Choose `run` command from the CMD widget to test your application. Make some changes to the project and execute `run` command again to see changes. \
```

```text  
# Go to the Terminal, cd /home/user/google_appengine/ directory and run `./appcfg.py --noauth_local_webserver update /projects/{YOUR_PROJECT_NAME}/` command to deploy it. Copy link, enter a verification code and check your changed app at GAE.\
```
