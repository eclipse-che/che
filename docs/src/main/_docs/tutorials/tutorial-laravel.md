---
tags: [ "eclipse" , "che" ]
title: Laravel in Che
excerpt: ""
layout: tutorials
permalink: /:categories/laravel/
---
{% include base.html %}
Laravel is a free, open-source PHP web framework, created by Taylor Otwell and intended for the development of web applications following the model-view-controller (MVC) architectural pattern.
```text  
# In the dashboard, create a new workspace from the custom stack using the following image:
FROM eclipse/php:laravel\
```

```text  
#In the Terminal execute `laravel new {your-project-name}` command to create a new project.\
```

```text  
# In the IDE, create a new command. Give it the syntax:
Title:    run
Command:  cd ${current.project.path} && php artisan serve --host=0.0.0.0 --port=3306
Preview:  http://${server.port.3306}\
```

```text  
# Test your application
1. Run the `run` command.
2. Open any file.
3. Make some edits.
4. Refresh the web app in the preview URL to see your changes.\
```
