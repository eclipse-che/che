---
tags: [ "eclipse" , "che" ]
title: Getting Started - Windows
excerpt: ""
layout: artik
permalink: /:categories/start-windows/
---
The Samsung ARTIK IDE is based upon Eclipse Che and runs on Windows, Mac or Linux.

**Windows** users follow the instructions below.

**Mac and Linux** users [use this page](../../docs/artik-start-mac/).

# How to Get Help  
**Support:** If the unthinkable happens, or you have a question, you can post [issues on our GitHub page](https://github.com/eclipse/che/issues). Please follow the [guidelines on issue reporting](https://github.com/eclipse/che/blob/master/CONTRIBUTING.md).

**Documentation:** We put a lot of effort into our docs. Please add suggestions on areas for improvement.
# 0. Pre-Reqs  
Before installing the ARTIK IDE you will need:
- **Windows 10**
  1. Install [Docker for Windows](https://docs.docker.com/engine/installation/windows/) - ARTIK IDE requires Docker 1.8+.
  2. Enable [bash in Windows](http://www.pcworld.com/article/3106463/windows/how-to-get-bash-on-windows-10-with-the-anniversary-update.html) or install [git for Windows](https://git-scm.com/download/win) to get a bash shell.
  3. Use a Chrome or FireFox browser.
- **Windows 7 or 8**
  1. Install [Docker Toolbox](https://docs.docker.com/toolbox/toolbox_install_windows/).
  2. Install [git for Windows](https://git-scm.com/download/win) to get a bash shell.
  3. Use a Chrome or FireFox browser.

# 1. Verify Docker  
Add Git Bash to the system path by setting `PATH=<path-to-git>;%PATH%`:
```shell  
set PATH=C:\Program Files\Git\bin\;%PATH%\
```
Verify Docker by running the Docker hello world example.
```shell  
docker run hello-world\
```
You should see Docker output and a hello world message.

Now, verify that bash works:
```shell  
bash\
```
You should see a bash prompt. Keep the bash session open for the next step.
# 2. Get ARTIK IDE CLI  
You will need to set the directory where you would like to keep ARTIK IDE executables. In the example below we use `/c/Users/$USERNAME/artik-ide` but this can be changed.
#### Directories for Docker
Docker and bash are case sensitive and require forward slashes without colons for directory and path names. Errors can occur if directories have spaces in them.

From the bash prompt:
```shell  
mkdir /c/Users/$USERNAME/artik-ide
cd /c/Users/$USERNAME/artik-ide\
```
Now, download the `artik-ide.bat` and `artik-ide.sh` scripts:
```shell  
curl -sL https://raw.githubusercontent.com/codenvy/artik-ide/master/artik-ide.bat > artik-ide.bat

curl -sL https://raw.githubusercontent.com/codenvy/artik-ide/master/artik-ide.sh > artik-ide.sh\
```
Finally, set the location for workspace storage in the ARTIK IDE. If using boot2docker, set the ARTIK IDE workspace storage to a subdirectory of `%userprofile%`.

If running Docker for Windows on Windows 10 Professional or Enterprise, you can set it to any directory.
```shell  
mkdir %userprofile%\artik-ide\data\
```
Once downloaded you can exit bash:
```shell  
exit\
```

#### Installing Behind a Proxy
If you are behind a proxy, you need to [configure your proxy settings](../../docs/che-config-proxies/) for Docker and the ARTIK IDE.  



# 3. Start ARTIK IDE  
From the directory where you downloaded the `artik-ide.bat` set the `CHE_DATA_FOLDER` environment variable. This is persisted for each session. To persist it across sessions set it as a [system variable](http://www.computerhope.com/issues/ch000549.htm).
```shell  
set CHE_DATA_FOLDER=/c/Users/%USERNAME%/artik-ide/data
set PATH=<path-to-artik-ide-bat>;%PATH%\
```
If you have set system variables for the above you can start here for future sessions.

Start the ARTIK-IDE:
```shell  
artik-ide start

INFO: ARTIK-IDE: Found image eclipse/che-launcher
INFO: ARTIK-IDE: Starting launcher
INFO: ARTIK-IDE: Already have image codenvy/artikide
INFO: ARTIK-IDE: Starting container...
INFO: ARTIK-IDE: Server logs at "docker logs -f artik-ide"
INFO: ARTIK-IDE: Server booting...
INFO: ARTIK-IDE: Booted and reachable
INFO: ARTIK-IDE: http://192.168.99.100:8080
```
Copy the URL shown in the terminal to your browser to open the dashboard.

To stop the ARTIK IDE:
```shell  
artik-ide stop\
```
To update ARTIK IDE to the newest version you can set the `CHE_VERSION` to `latest`. Otherwise you can select a tag from the released versions at [Dockerhub](https://hub.docker.com/r/codenvy/artikide/tags/)
```shell  
set CHE_VERSION=latest
artik-ide update\
```
See all the CLI commands:
```shell  
artik-ide help\
```

#### Advanced Configuration
There are many aspects of ARTIK IDE like port and hostname that can be configured by [setting Eclipse Che properties](../../docs/usage-docker#environment-variables).  



# 4. Create Workspaces and Projects  
The ARTIK IDE provides a step-by-step wizard for creating your first workspace. It provides stacks for ARTIK and Android, as well as many other languages through the "Stack Library."  Stacks will populate the workspace with a runtime, SDK, and libraries needed for building new projects that will run on an ARTIK board.

A workspace can have one or more projects. Each project can have a different type that supports different kinds of programming languages and build frameworks. When you create your first workspace, you can provide the project from a Git repository or using one of the included templates.
![createwsandproject.jpg]({{ base }}/assets/imgs/createwsandproject.jpg)
Choose the ARTIK stack and then select from one of the many sample projects. [Tutorial: Artik Blink LED](../../docs/artik-tutorial-blink-led/) is a good starter tutorial that uses the Ready-to-run project template `artik-blink-led`.

# 5. Setup an ARTIK Device  
Review Samsung ARTIK getting started docs at [https://developer.artik.io/documentation/getting-started-beta/powering-up.html](https://developer.artik.io/documentation/getting-started-beta/powering-up.html) and [https://developer.artik.io/documentation/getting-started-beta/communicating-pc.html](https://developer.artik.io/documentation/getting-started-beta/communicating-pc.html). This will help understand how to power up the ARTIK device(connect power and press SW3 switch) and how to setup communication with the ARTIK device.

# 6. Discover ARTIK Device IP Address  
The quickest way to get started is to connect your ARTIK device to the computer running the ARTIK IDE with a male USB to male USB cable: [quick connection over USB discovery](../../docs/artik-usb-connection-setup/).However, this cable doesn't ship with the ARTIK device and connecting over the network is often required in the long-term.

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
```bash
>ssh root@<ip-address>
```
**Note: When the ARTIK device is powered up, it will request a new ip address each time. Be sure to determine the new ipaddress each time or give your ARTIK device a static ip address.**
# 7. Connect ARTIK Device with ARTIK IDE  
Use the ARTIK device manager in a workspace to connect an ARTIK device to the ARTIK IDE.

1. Click ARTIK icon on the toolbar in workspace.
2. Name your device, provide ARTIK device [IP address](#5-discover-artik-device-ip-address) and port(default 22) and username/password(default root/root).
3. Specify replication path on the device. This is the directory where project files will be backed up on the device. It can be both existing or a non existing directory (in the latter case it will be created). Project source files (including binaries) are automatically `scp`'ed into all connected targets when changes in a workspace file system are caught. It means that when a binary is rebuilt, it's readily available on the device in about a 2-3 seconds.
4. `Save` then `Connect`.
5. Once connected, ARTIK device tree will be created in processes area. Selecting the terminal icon will give access to the terminal console inside of ARTIK device. Also, the target environment will automatically change to ARTIK. This is important to note as all workspace commands will be ran inside the ARTIK device. Usually, building/compiling code is done inside workspace by setting target to `default` and executing/running commands are done inside the ARTIK device by setting target to `artik_device_<#>`.
![artikmanager.jpg]({{ base }}/assets/imgs/artikmanager.jpg)

![artikmanageradddevice.jpg]({{ base }}/assets/imgs/artikmanageradddevice.jpg)

![artikIDEafterconnection.jpg]({{ base }}/assets/imgs/artikIDEafterconnection.jpg)

# 8. Build  

#### Building in the IDE
We recommend only building in the IDE workspace, not on the device itself because this is simpler and the IDE is smart enough to cross-compile the binary and push it to the device so it can be instantly run there.  

1. Select source file in the project explorer, click **Compile**.
![compile.png]({{ base }}/assets/imgs/compile.png)
Once built the binaries are automatically synced to the device and ready to run or debug and will show up in project explorer.

#### Compilation Options
The default compilation command looks like this:`$CC -lartik-sdk-base $(for i in $(ls $CPATH)\ndo artik_sdk=-I$CPATH/$i\necho $artik_sdk\ndone) -lpthread -g` It is possible to change this command at `Project` > `Compilation Options`. This command is saved in project attributes and can be customized for each project. A user can also define the output binary name, which is **a.out** by default.  


#### Project Replication
The project files (including both source and binaries) are automatically synced into the `project replication folder` on the device. You can set the project replication folder when adding or editing a device in the Device Manager dialog box (press the ARTIK IDE button on the menu bar). Read more about [connecting devices](#6-connect-artik-device-with-artik-ide).  


# 9. Deploy, Run and Debug  
**Run**
To run your application. select the project you want to run (it can be a source file as well) and press **Run** button. Note that you should first compile the project. The binary is run on the device, while the output is streamed to consoles panel in the IDE.

**Debug**
Debugging requires `gdbserver` to be installed on the device.  Older devices may not have this. When you connect a new device, the IDE will check if gdbserver is installed. If it's not, there will be a prompt suggesting that dev mode is to be turned on (which installs required software).

To debug your application, choose the project (it can be source file as well), and click Debug (there will be dropdown with the list of connected devices).

When debug is initiated, gdbserver is started on the device, on port 1234, and the IDE debugger automatically connects to it.
![debug_.png]({{ base }}/assets/imgs/debug_.png)
**Deploy (optional)**
Because all files in the project are auto-synced to the device it's not necessary to deploy anything to the device. However, if you want to push files to specific locations on the device (not the project replication folder) you can right-click on the artifact and choose `Push to Device` then `Choose Target...`.

# 10. Production and Development Profiles  
Your Artik device needs certain software to make it possible for Artik IDE to debug apps, sync project files, make use of C/C++ and Node SDKs.

Turning on a development profile will install the minimum software package that includes node.js, rsync and a gdbserver.

A production profile removes this software from the system.

Profiles are available at **Artik > Profiles** or by right clicking the device in machines panel in the bottom.
# 11. Troubleshooting  
If you experience problems, please file an issue on the [Eclipse Che GitHub page](https://github.com/eclipse/che/issues) where Che and ARTIK engineers can help you.
# 12. Contribute to the ARTIK IDE  
* **Build From Source**: We love that idea. Che is a multi-module project, so you can build all of it, or just some of it. Start in the /assembly/assembly-main/ repository for the fastest build. We maintain build instructions on the [ARTIK IDE GitHub repository](https://github.com/codenvy/artik-ide).
