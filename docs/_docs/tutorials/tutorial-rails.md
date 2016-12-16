---
tags: [ "eclipse" , "che" ]
title: Rails in Che
excerpt: ""
layout: tutorials
permalink: /:categories/rails/
---
Ruby on Rails, or simply Rails, is a web application framework written in Ruby under MIT License. Rails is a model–view–controller (MVC) framework, providing default structures for a database, a web service, and web pages.
```text  
# When on the User Dashboard, create a new project using a Ruby_Rails stack and a Rails web app sample.\
```

```text  
# Alternatively, you may skip importing a project and generate one by yourself (so, create an empty workspace in a Dashboard). When in the IDE, go to Consoles panel and create a new Terminal. In the Terminal go to the /projects directory and execute `rails new {your-app-name}` command.

# Refresh project tree to see new Rails app created.\
```

```text  
# In the IDE, create two custom commands with the following syntax:
Title:    run-bundler
Command:  bundle install
Preview:  <empty>

Title:    run
Command:  cd ${current.project.path} && rails server -b 0.0.0.0
Preview:  http://${server.port.3000}\
```
These commands are imported with a Rails sample, so they are already in the commands widget when you open the IDE.
```text  
# Test your application
1. Execute `run-bundler` command.
2. After a successful gem dependencies installation, execute `run` command.
3. Click Preview URL to see your application started.\
```
