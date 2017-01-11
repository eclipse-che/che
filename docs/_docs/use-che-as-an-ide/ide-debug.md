---
tags: [ "eclipse" , "che" ]
title: Debug
excerpt: ""
layout: docs
permalink: /:categories/debug/
---
{% include base.html %}
# Java  
Java debugger is deployed with the workspace agent, i.e. runs in the workspace. It can connect to local processes (those running in a workspace) or remote ones.

Breakpoints are set with a single click on a line number in the editor. You can set breakpoints before attaching a debugger:
![breakpoint.png]({{ base }}/assets/imgs/breakpoint.png)
In a Debug Dialog (**Run > Edit Debug Configurations...**), choose if you want to attach to a process in a local workspace or a remote machine. If localhost is chosen, a drop down menu will show all ports that are exposed in a container. You may either choose an existing port or provide your own.
![debug-configurations.png]({{ base }}/assets/imgs/debug-configurations.png)
## Java Console Apps

To debug console apps, pass debug arguments to JVM:
```shell  
mvn clean install && java -jar -Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=y ${current.project.path}/target/*.jar\
```
## Java Web Apps

To debug a web application, you need to start a web server in a debug mode. Debug arguments may vary depending on the web server in use. For example, to start Tomcat in a debug mode, run:
```text  
$TOMCAT_HOME/bin/catalina.sh jpda run\
```
You can add debug commands to CMD widget to permanently save them with the workspace config.
# GDB  
## Debugging Local Binary

Compile your app with `-g` argument, go to `Run > Edit Debug Configurations > GDB`. Create a new configuration, check `Debug local binary` box. By default, binary path is `${current.project.path/a.out}`. When the debugger attaches, this macro is translated into an absolute path to a currently selected project. `a.out` is the default binary name. If you have compiled binary with a different name, change it:
![debug.png]({{ base }}/assets/imgs/debug.png)
Set a breakpoint in code, go to `Run > Debug > YourDebugConfiguration`. Once a debugger attaches, there's a notification in the top right corner. A debugger panel will open.

## Remote Debugging with GDB server

Similar to Java debugger, one needs to start a process that a debugger will connect to. In case of GDB, this is `gdbserver` which is installed in C/CPP runtime by default.

To **run** gdbserver, execute the following:

`gdbserver :$port /path/to/binary/file` where `$port` is a random port that you will then connect to. It is important to provide an absolute path to a binary file if you run gdbserver in a command.

It is important to make sure that the binary has been compiled with `-g` argument, i.e. with attached sources.

When gdbserver starts, it produces some output with info on process ID and port it's listening on. When a remote client connects to gdbserver, there will be a message with IP address of a remote connection.

To **stop** gdbserver, terminate the command in Consoles panel (if the server has been started as a command). If gdbserver has been started in a terminal, Ctrl+C does not kill it. Open another terminal into a machine, and run:

`kill $(ps ax | grep "[g]dbserver" | awk {'print $1;}')`

This commands grabs gdbserver PID and kills the process.

## Connect to GDB server

Go to `Run > Debug Configurations > Edit Debug Configurations` and enter host (localhost if gdbserver has been started in the same workspace environment), port and path to the binary file being debugged. By default, binary name is `a.out`. If you have compiled your binary with `-o` argument, you need to provide own custom binary name in a debug configuration.

Save your configuration, choose it at `Run > Debug Configuration` and attach the debugger, having previously set breakpoints in source files.

### Connection Timeouts

Latency or poor connectivity may cause issues with remote debugging. A local GDB may fail to receive a timely response from the remote server. To fix it, set a default timeout for a local GDB. In the terminal, run:

`echo  "set remotetimeout 10" > ~/.gdbinit`

You may set a bigger timeout, say, 20 seconds, if there are serious connectivity issues with a remote GDB server.

It is also possible to add this command as a Dockerfile instruction for a custom recipe:

```shell  
FROM eclipse/cpp_gcc
RUN echo  "set remotetimeout 10" > /home/user/.gdbinit
```
