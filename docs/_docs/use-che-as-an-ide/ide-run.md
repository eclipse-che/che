---
tags: [ "eclipse" , "che" ]
title: Run
excerpt: ""
layout: docs
permalink: /:categories/run/
---
There are two ways to run your application in Che:

* using [commands](/docs/commands)
* in a Terminal
# Console Apps  
Create a command using `${current.project.path}` macro to execute jars, python scripts, run gulp or grunt commands etc:
```shell  
# execute a JAR
java -jar ${current.project.path}/target/application.jar

# execute python script
python ${current.project.path}/main.py\
```

#### Interactive Mode
If your application expects user input, run it from the Terminal, since commands are executed in a non-interactive mode.\n\nAlso, some commands have prompts (for instance, if or not a user is OK with sending use stats). If this is the case, commands have to be passed with confirmation flags, like `-y` or `echo yes | ${command}`.  


# Web Apps  
If your application should be deployed with a web server like Tomcat, Wildfly, Jetty or Apache, there are 3 mandatory things to be accomplished:

* copy sources/build artifacts to `'deployments'` directory of a chosen web server, for instance `/webapps` in Tomcat, `/standalone/deployments/` in Wildfly, `/var/www/html` for Apache.
* configure a web server (if necessary)
* start a web server (unless it is automatically started when a machine starts)
* find the right port which is mapped to the internal server port (`Servers` tab in machine perspective)

## Copy Sources/Build Artifacts

If this is a Java web app, generate `.war` artifact and copy it to web server's `'deployments'` directory with the following command:
```shell  
mvn -f ${current.project.path} clean install
cp ${current.project.path}/target/*.war /home/user/tomcat8/webapps/ROOT.war\
```

#### $TOMCAT_HOME
Pre-defined Java images have Tomcat 8 on board with $TOMCAT_HOME environment variable pointing to `home/user/tomcat8`  


```shell  
cp ${current.project.path}/target/*.war $TOMCAT_HOME/webapps/ROOT.war\
```
You may copy build artifact and keep its original name. In this case, the preview URL will be `http://${host}:${port}/${artifact.name}`. In the above example, build artifact is copied with `ROOT` name, which turns the app preview URL into `http://${host}:${port}`

## Start a Web Server

Use startup scripts (usually in `/bin` directory of a webserver). You can start a webserver manually, or add a start-up command to `CMD` instruction of a machine recipe (if you start a machine from a custom recipe).

## Preview URL

When sources/build artifacts are deployed to a webserver and the server is up, get application preview URL in **Servers** tab (Machine perspective icon in the top right corner - ![Machine perspective](https://files.readme.io/5gHpdHAMSNig96lwduwf_machine-perspective.png) :
![server.png]({{ base }}/assets/imgs/server.png)
If you use pre-built Che images, preview URLs are marked with Labels, like `tomcat8`, `apache2`, `asp.net.server` etc.
