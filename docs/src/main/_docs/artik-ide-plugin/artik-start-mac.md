---
tags: [ "eclipse" , "che" ]
title: Getting Started - Mac & Linux
excerpt: ""
layout: artik
permalink: /:categories/start-mac/
---
{% include base.html %}
The Samsung ARTIK IDE is based upon Eclipse Che and runs on Windows, Mac or Linux.

Mac and Linux users follow the instructions below.
Windows users use the instructions on [this page](../../docs/artik/).

# How to Get Help  
**Support:** If the unthinkable happens, or you have a question, you can post [issues on our GitHub page](https://github.com/eclipse/che/issues). Please follow the [guidelines on issue reporting](https://github.com/eclipse/che/blob/master/CONTRIBUTING.md).

**Documentation:** We put a lot of effort into our docs. Please add suggestions on areas for improvement.
# 0. Pre-Reqs  
Before installing the ARTIK IDE you will need:
1. Docker 1.8+
  - Docker for [Mac](https://docs.docker.com/engine/installation/mac/).
  - Docker for [Linux](https://docs.docker.com/engine/installation/).
2. A bash shell
3. A Chrome or FireFox browser.

```shell  
# Verify Docker is properly installed
# Should print "Hello from Docker!" otherwise check Docker install docs
docker run hello-world

# Should open a bash shell
bash
```  
If using Docker for Mac, add the following to your `~/.bash_profile`.
```

# 1. Get ARTIK IDE CLI  
On Linux / Mac, from in a bash shell execute:
```text  
sudo curl -sL https://raw.githubusercontent.com/codenvy/artik-ide/master/artik-ide.sh > /usr/local/bin/artik-ide
sudo chmod +x /usr/local/bin/artik-ide\
```

#### Installing Behind a Proxy
If you are behind a proxy, you need to [configure your proxy settings](../../docs/che-config-proxies) for Docker and the ARTIK IDE.  

# 2. Start ARTIK IDE  

#### Locahost on Mac OS
On Mac OS the ARTIK IDE will be available at http://localhost:8080  


```shell  
# Change where workspaces are saved. Default = /home/user/che
# export CHE_DATA_FOLDER=/home/user/reginald

# If ARTIK will be accessed from other machines define your host IP
# export CHE_HOST_IP=<che-host-ip>

# Start ARTIK IDE
artik-ide start
INFO: ARTIK-IDE: Found image codenvy/che-launcher:nightly
INFO: ARTIK-IDE: Starting launcher
INFO: ARTIK-IDE: Already have image codenvy/artikide:nightly
INFO: ARTIK-IDE: Starting container...
INFO: ARTIK-IDE: Server logs at "docker logs -f artik-ide"
INFO: ARTIK-IDE: Server booting...
INFO: ARTIK-IDE: Booted and reachable
INFO: ARTIK-IDE: http://192.168.99.100:8080
# Copy the URL shown in the terminal to your browser to open the dashboard
# Mac OS users will open the IDE at http://localhost:8080

# Stop ARTIK IDE
artik-ide stop

# Update ARTIK IDE to the newest version
export CHE_VERSION=latest
artik-ide update

# List all CLI commands
artik-ide help
```

#### Advanced Configuration
There are many aspects of ARTIK IDE like port and hostname that can be configured by [setting Eclipse Che properties](../../docs/usage-docker#environment-variables).  


# 3. Create Workspaces and Projects  
The ARTIK IDE provides a step-by-step wizard for creating your first workspace. It provides stacks for ARTIK and Android, as well as many other languages through the "Stack Library."  Stacks will populate the workspace with a runtime, SDK, and libraries needed for building new projects that will run on an ARTIK board.

A workspace can have one or more projects. Each project can have a different type that supports different kinds of programming languages and build frameworks. When you create your first workspace, you can provide the project from a Git repository or using one of the included templates.
![createwsandproject.jpg]({{ base }}/assets/imgs/createwsandproject.jpg)
Choose the ARTIK stack and then select from one of the many sample projects. [Tutorial: Artik Blink LED](../../docs/tutorial-artik-blink-led) is a good starter tutorial that uses the Ready-to-run project template `artik-blink-led`.

# 4. Setup an ARTIK Device  
Review Samsung ARTIK getting started docs at [https://developer.artik.io/documentation/getting-started-beta/powering-up.html](https://developer.artik.io/documentation/getting-started-beta/powering-up.html) and [https://developer.artik.io/documentation/getting-started-beta/communicating-pc.html](https://developer.artik.io/documentation/getting-started-beta/communicating-pc.html). This will help understand how to power up the ARTIK device(connect power and press SW3 switch) and how to setup communication with the ARTIK device.

# 5. Discover ARTIK Device IP Address  
The quickest way to get started is to connect your ARTIK device to the computer running the ARTIK IDE with a male USB to male USB cable: [quick connection over USB discovery](../../docs/artik-usb-connection-setup). However, this cable doesn't ship with the ARTIK device and connecting over the network is often required in the long-term.

Connect your ARTIK board to your network router/switch via network cable. The ARTIK device will then obtain an IP address automatically using DHCP. To discover your ARTIK IP address log into your router and search the table of clients for the name "localhost". Also, you can discover your artik board IP address with the following utility.
```shell  
#Determine your current computers IP to search network for ARTIK Board.
export HOST_IP=$(docker run --rm --net host alpine sh -c "ip a show eth1" | \
                    grep 'inet ' | cut -d/ -f1 | awk '{ print $2}')

#Quick search for ARTIK device via open 22 port
docker run -ti --rm artik-tools jdrummond/artik-tools -q -i %HOST_IP% -t 5

#When above results in multiple ip addresses
docker run -ti --rm artik-tools jdrummond/artik-tools -i %HOST_IP% -t 20

#SSH in ARTIK device using ip address from above to test ip address validity.
#ARTIK device default username/password root/root.
ssh root@<ip-address>
```

```shell  
#Determine your current computers IP to search network for ARTIK Board.
for /f "skip=1 tokens=2 delims=: " %f in ('nslookup %COMPUTERNAME% ^| find /i "Address"') do set HOST_IP=%f

#Quick search for ARTIK device via open 22 port
docker run -i --rm -t artik-tools jdrummond/artik-tools -q -i %HOST_IP% -t 5

#When above results in multiple ip addresses
docker run -i --rm -t artik-tools jdrummond/artik-tools -i %HOST_IP% -t 20

#SSH in ARTIK device using ip address from above to test ip address validity.
#ARTIK device default username/password root/root.
bash
>ssh root@<ip-address>
```
**Note: When the ARTIK device is powered up, it will request a new ip address each time. Be sure to determine the new ipaddress each time or give your ARTIK device a static ip address.**
# 6. Connect ARTIK Device with ARTIK IDE  
Use the ARTIK device manager in a workspace to connect an ARTIK device to the ARTIK IDE.

1. Click ARTIK icon on the toolbar in workspace.
2. Name your device, provide ARTIK device [IP address](../../docs/artik#5-discover-artik-device-ip-address) and port(default 22) and username/password(default root/root).

3. Specify replication path on the device. This is the directory where project files will be backed up on the device. It can be both existing or a non existing directory (in the latter case it will be created). Project source files (including binaries) are automatically `scp`'ed into all connected targets when changes in a workspace file system are caught. It means that when a binary is rebuilt, it's readily available on the device in about a 2-3 seconds.
4. `Save` then `Connect`.
5. Once connected, ARTIK device tree will be created in processes area. Selecting the terminal icon will give access to the terminal console inside of ARTIK device. Also, the target environment will automatically change to ARTIK. This is important to note as all workspace commands will be ran inside the ARTIK device. Usually, building/compiling code is done inside workspace by setting target to `default` and executing/running commands are done inside the ARTIK device by setting target to `artik_device_<#>`.
![artikmanager.jpg]({{ base }}/assets/imgs/artikmanager.jpg)

![artikmanageradddevice.jpg]({{ base }}/assets/imgs/artikmanageradddevice.jpg)

# 7. Build, Run and Debug  
See: [Getting Started - Windows](../../docs/artik#8-build)

# 9. Production and Development Profiles  
Your Artik device needs certain software to make it possible for Artik IDE to debug apps, sync project files, make use of C/C++ and Node SDKs.

Turning on a development profile will install the minimum software package that includes node.js, rsync and a gdbserver.

A production profile removes this software from the system.

Profiles are available at **Artik > Profiles** or by right clicking the device in machines panel in the bottom.
# 10. Troubleshooting  
If you experience problems, please file an issue on the [Eclipse Che GitHub page](https://github.com/eclipse/che/issues) where Che and ARTIK engineers can help you.
# 11. Contribute to the ARTIK IDE  
* **Build From Source**: We love that idea. Che is a multi-module project, so you can build all of it, or just some of it. Start in the /assembly/assembly-main/ repository for the fastest build. We maintain build instructions on the [ARTIK IDE GitHub repository](https://github.com/codenvy/artik-ide).
