---
tags: [ "eclipse" , "che" ]
title: Subversion Repos in Che
excerpt: ""
layout: tutorials
permalink: /:categories/subversion/
---
{% include base.html %}
Apache Subversion (often abbreviated SVN, after the command name svn) is a software versioning and revision control system distributed as free software under the Apache License.

Che supports SVN through the commands line.
```text  
# In the dashboard, create a new workspace from Java stack, which has Subversion installed.

# You can also use eclipse/ubuntu_jdk8 image with Subversion installed as a base for your own custom stack. If you use non-Java Ready-To-Go stacks or build a custom stack, install subversion manually.

# On Ubuntu, Debian
sudo apt-get update && sudo apt-get install svn -y

# On CentOS, Fedora, RHEL
sudo yum update && sudo yum install svn -y\
```

```text  
# On the Consoles panel click `(+)` button, navigate to `projects` directory and checkout your project with the following command:
svn checkout

# Refresh project tree and see your project imported there. Click `(>)` to open and configure it.\
```

```text  
# In the Terminal navigate to your project directory and update it with the following command:
svn update\
```

```text  
# Commit changes to the remote hosting.
1. In the Terminal run `svn add . --force` command to schedule files, directories, or symbolic links in your project for addition to the repository.

2. Commit changes with `svn commit -m “your-commit-message”` command.\
```
