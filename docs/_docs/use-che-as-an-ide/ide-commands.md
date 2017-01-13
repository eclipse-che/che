---
tags: [ "eclipse" , "che" ]
title: Commands
excerpt: ""
layout: docs
permalink: /:categories/commands/
---
{% include base.html %}
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
Che provides macros that can be used within a command or preview URL to reference workspace objects.

| Macro   | Details   
| --- | ---
| Currently selected file in editor   | Absolute path to the selected file in editor   
| Path relative to the `/projects` folder to the selected file in editor   | Project name of the file currently selected in editor   
| Project type of the file currently selected in editor   | Currently selected file in project tree   
| Absolute path to the selected file in project tree   | Path relative to the `/projects` folder in project tree   
| Project name of the file currently selected in explorer   | Project type of the file currently selected in explorer   
| Returns protocol, hostname and port of an internal server. `<name>` is defined by the same of the internal service that you have exposed in your workspace recipe.\n\nReturns the hostname and port of a service or application you launch inside of a machine. \n\nThe hostname resolves to the hostname or the IP address of the workspace machine. This name varies depending upon where Docker is running and whether it is embedded within a VM.  See [Networking](doc:networking).\n\nThe port returns the Docker ephemeral port that you can give to your external clients to connect to your internal service. Docker uses ephemeral port mapping to expose a range of ports that your clients may use to connect to your internal service. This port mapping is dynamic.   | Returns protocol of a server registered by name   
| Returns hostname of a server registered by name   | Returns port of a server registered by name   
| Returns the name of the workspace   | `info`   
| `CHE_IP`   | IP address Che server will bind to. Used by browsers to contact workspaces. You must set this IP address if you want to bind the Che server to an external IP address that is not the same as Docker's.   
| The IP address set to the Docker host. This does cover 99% of situations, but on rare occassions we are not able to discover this IP address and you must provide it.   | `CHE_DEBUG_SERVER`   
| If `true`, then will launch the Che server with JPDA activated so that you a Java debugger can attach to the Che server for debugging plugins, extensions, and core libraries.   | `false`   
| `CHE_DEBUG_SERVER_PORT`   | The port that the JPDA debugger will listen.   
| `8000`   | `CHE_DEBUG_SERVER_SUSPEND`   
| If `true`, then activates `JPDA_SUSPEND` flag for Tomcat running the Che server. Used for advanced internal debugging of extensions.   | `false`   
| `CHE_PORT`   | The port the Che server will bind itself to within the Che container.   



| Che 4.6 Macros   | Details   
| --- | ---
| Absolute path to the project or module currently selected in the project explorer tree.   | The fully qualified package.class name of the Java class currently active in the editor panel.   
| The path to the currently selected project relative to `/projects`. Effectively removes the `/projects` path from any project reference.   | Project name of the file currently selected in editor   
| Project type of the file currently selected in editor   | Currently selected file in project tree   
| Absolute path to the selected file in project tree   | Path relative to the `/projects` folder in project tree   


# Machine Environment Variables  
The workspace machine has a set of system environment variables that have been exported. They are reachable from within your command scripts using `bash` syntax.
```shell  
# List all available machine system environment variables
export

# Reference an environment variable, where $TOMCAT_HOME points to /home/user/tomcat8
$TOMCAT_HOME/bin/catalina.sh run\
```
