---
tags: [ "eclipse" , "che" ]
title: Commands
excerpt: ""
layout: docs
permalink: /:categories/commands/
---
Commands are script-like instructions that are injected into the workspace machine for execution. Commands are executed at by selecting a command from the IDE toolbar `CMD` drop down. You can add or edit commands at `Run > Edit Commands` or `CMD > Edit Commands` drop down.

Commands are saved in the configuration storage of your workspace and will be part of any workspace export.

Commands have type like projects. Plug-in authors can register different command types that will inject additional behaviors into the command when it is executed. For example, Che provides a Maven command type for any project that has the maven project type. Maven commands have knowledge of how maven works and will auto-set certain flags and simplify the configuration.

# Authoring  
You can create any number of commands. The name of a command is not restricted to camelCase. A command may contain a single instruction or a succession of commands. For example:
```shell  
# each command starts from a new line
cd /projects/spring
mvn clean install

# a succession of several commands where `;` stands for a new line
cd /projects/spring; mvn clean install

# a succession of several commands where execution of a subsequent command depends on execution of a preceeding one - if there's no /projects/spring directory, `mvn clean install` won't be executed
cd /projects/spring && mvn clean install\
```
It is possible to check for conditions, use for loops and other bash syntax:
```shell  
# copy build artifact only if build is a success
mvn -f ${current.project.path} clean install
  if [[ $? -eq 0 ]]; then
    cp /projects/kitchensink/target/*.war /home/user/wildfly-10.0.0.Beta2/standalone/deployments/ROOT.war
    echo "BUILD ARTIFACT SUCCESSFULLY DEPLOYED..."
else
    echo "FAILED TO DEPLOY NEW ARTIFACT DUE TO BUILD FAILURE..."
fi
```

# Macros  
{{ site.product_mini_name_capitalized }} provides macros that can be used within a command or preview URL to reference workspace objects.

| Macro   | Details   
| --- | ---
| `${current.project.path}` | Absolute path to the project or module currently selected in the project explorer tree.
| `${current.class.fqn}` | The fully qualified package.class name of the Java class currently active in the editor panel.
| `${current.project.relpath}` | The path to the currently selected project relative to `/projects`. Effectively removes the `/projects` path from any project reference.
| `${editor.current.file.name}` | Currently selected file in editor   
| `${editor.current.file.path}` | Absolute path to the selected file in editor   
| `${editor.current.file.relpath}` | Path relative to the `/projects` folder to the selected file in editor   
| `${editor.current.project.name}` | Project name of the file currently selected in editor   
| `${editor.current.project.type}` | Project type of the file currently selected in editor   
| `${explorer.current.file.name}` | Currently selected file in project tree   
| `${explorer.current.file.path}` | Absolute path to the selected file in project tree   
| `${explorer.current.file.relpath}` | Path relative to the `/projects` folder in project tree   
| `${explorer.current.project.name}` | Project name of the file currently selected in explorer   
| `${explorer.current.project.type}` | Project type of the file currently selected in explorer   
| `${server.<name>}` | Returns protocol, hostname and port of an internal server. `<name>` is defined by the same of the internal service that you have exposed in your workspace recipe. <br><br> Returns the hostname and port of a service or application you launch inside of a machine. <br><br> The hostname resolves to the hostname or the IP address of the workspace machine. This name varies depending upon where Docker is running and whether it is embedded within a VM.  See [Networking]({{base}}/docs/setup/configuration/index.html#networking). <br><br> The port returns the Docker ephemeral port that you can give to your external clients to connect to your internal service. Docker uses ephemeral port mapping to expose a range of ports that your clients may use to connect to your internal service. This port mapping is dynamic.   
| `${server.<name>.protocol}` | Returns protocol of a server registered by name   
| `${server.<name>.hostname}` | Returns hostname of a server registered by name   
| `${server.<name>.port}` | Returns port of a server registered by name
| `${server.port.<port>}` | Returns the hostname and port of a service or application you launch inside of a machine. <br><br>The hostname resolves to the hostname or the IP address of the workspace machine. This name varies depending upon where Docker is running and whether it is embedded within a VM. See [Networking](({{base}}/docs/setup/configuration/index.html#networking)).<br><br>The port returns the Docker ephemeral port that you can give to your external clients to connect to your internal service. Docker uses ephemeral port mapping to expose a range of ports that your clients may use to connect to your internal service. This port mapping is dynamic.<br><br>Let's say you launched a process inside your machine and bound it to `<port>`. A remote client can connect to your workspace by taking the IP address of the machine and the port of your service. Docker provides a dynamic port number to external clients for each service running internally. This macro will return the value Docker assigned for external clients to use. <br><br>For example, in your workspace, you launch a service that binds to port 8080. Then `${server.port.8080}` macro may return 32769, which is the port to give to remote clients to connect to the internal service.   
| `${workspace.name}` | Returns the name of the workspace   


# Machine Environment Variables  
The workspace machine has a set of system environment variables that have been exported. They are reachable from within your command scripts using `bash` syntax.
```shell  
# List all available machine system environment variables
export

# Reference an environment variable, where $TOMCAT_HOME points to /home/user/tomcat8
$TOMCAT_HOME/bin/catalina.sh run\
```
