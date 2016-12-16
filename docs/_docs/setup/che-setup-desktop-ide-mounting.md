---
tags: [ "eclipse" , "che" ]
title: Usage&#58 Local IDE Sync
excerpt: "Using your desktop IDE with an Eclipse Che workspace."
layout: docs
permalink: /:categories/desktop-ide-mounting/
---
#### Requires 5.0.0-M7
}  

Che ships a super fast Fuse-based mount and sync mechanism. This is delivered as a Docker container that combines `sshfs` with `unison`. You can perform a mount on any operating system that supports Docker. However, if you are on Windows using Boot2Docker, you can only mount directories in `%userprofile%`.
# List Workspaces  
You can get a list of workspaces in a Che server that have an SSH agent deployed and are also a dev-machine where `/projects` are deployed. This means it is sync-ready.
```text  
$ che action list-workspaces
NAME      ID                   STATUS
florent   workspace93kd748390  STOPPED
mysql     workspacewia89343k4  RUNNING

# Options
--url <url>           # Che or Codenvy host where workspaces are running
--user <email>        # Codenvy user name
--password <password> # Codenvy password\
```

# Mount and Sync  
## How
We provide a Docker container that bridges your remote Che workspace with your local file system. Inside of the `che-mount` Docker container, we create an `sshfs` connection to the remote workspace using your user name, password, and workspace port number. Inside of that Docker container, we then use `unison`, a high performance file system synchronizer to synchronize the contents of the remote workspace with a directory within the container. Your local host will then volume mount the synchronized directory, for which those files appear. The `unison` synchronizer is run every minute, and will capture both changes made locally on your host and any changes made in the remote workspace.

This particular approach is fast because your local IDE has local I/O performance for all file actions. The synchronizer runs in the background asynchronously, and synchronizes your local changes into the workspace. The reverse course is true as well. This asynchronous approach provides a non-blocking I/O performance that is essential.

## Use
To synchronize your IDE you'll use the [Eclipse Che CLI](che-cli) installed. The synchronization will take place with the current directory. It is probably best to start in an empty directory, otherwise the utility will synchronize the contents of an existing directory onto the workspace.

```shell  
mkdir sync
cd sync
che mount <ws-name> (or che mount <ws-id>)

# Options
--url <url>           # Che or Codenvy host where workspaces are running
--user <email>        # Codenvy user name
--password <password> # Codenvy password\
```

This will make a secure connection to the workspace and unison-sync the contents to the local host directory. You will be asked for the password that you retrieved from the SSH configuration. The synchronization will run continuously and the command will not return until you press CTRL-C, at which point the synchronization will be terminated.
![fef09b90-a696-11e6-9a37-70f827677830.gif]({{ base }}/assets/imgs/fef09b90-a696-11e6-9a37-70f827677830.gif)
## Optimize
The utility is designed to synchronize everything in your `/projects` folder that is within the workspace. Synchronization is impacted by the size and nature of files that are contained within the directory. If you have libraries such as with an NPM or maven repository, it may be unnecessary and taxing to synchronization all of those files.

You can improvement performance of the synchronization by providing a unison profile.  A unison profile allows you to specify path specifications of files that must be included or ignored, and there are multiple tactics for specifying the list including regex and directory pattern matching.

The CLI will automatically install your unison profile.  Create a `default.prf` file in `~/.che/unison` for Mac / Linux or in `%userprofile%/.che/unison` for Windows. The Che CLI will copy this file and make it your default profile within the che-mount utility.

[Profiles](https://www.cis.upenn.edu/~bcpierce/unison/download/releases/stable/unison-manual.html#profile)
[Ignore Directories and Files](https://www.cis.upenn.edu/~bcpierce/unison/download/releases/stable/unison-manual.html#ignore)
[Path Specification](https://www.cis.upenn.edu/~bcpierce/unison/download/releases/stable/unison-manual.html#pathspec)
# Mount Without Sync  
If you just want to mount the remote workspace to a local directory, you can do so with `sshfs`. Mounting will allow all file system writes to take place immediately. However, all changes are sent over the network. If the workspace and desktop IDE are both on the same machine, then this method may be preferred. However, the greater the network latency, the slower the sync will be.

You can still use your local IDE with the local file mount. You may want a local file mount instead of a sync if you want to eliminate the risk of any synchronization conflicts from clients accessing the same file system locally and remotely within the workspace at the same time.

Windows users can use sshfs by installing free software [win-sshfs](https://code.google.com/archive/p/win-sshfs/) for Window versions up to 7 or by purchasing software such as [SFTP Net Drive](https://www.eldos.com/) for Windows 8.0, 8.1 or 10. Mac users can use sshfs by installing free open source software [FUSE](https://osxfuse.github.io/) and associated sshfs extension. Linux has sshfs built into it kernel so most Linux distributions require a small sshfs package to be installed.
```shell  
# On Linux & Mac - first install sshfs.  Then:
sshfs -p <ws-ssh-port> user@<che-ws-ip-address>:/projects /mnt/che\
```

# Docker Variant  
The CLI in turn calls a Docker container, which you can use as well.
```shell  
# On linux:
docker run --rm -it --cap-add SYS_ADMIN --device /dev/fuse
            --name che-mount
            -v ${HOME}/.ssh:${HOME}/.ssh
            -v /etc/group:/etc/group:ro
            -v /etc/passwd:/etc/passwd:ro
            -v <path-to-sync-profile>:/profile
            -u $(id -u ${USER})
            -v <host-dir>:/mnthost eclipse/che-mount <ws-id|ws-name>

# On Mac or Windows:
docker run --rm -it --cap-add SYS_ADMIN --device /dev/fuse
           --name che-mount
           -v <path-to-sync-profile>:/profile
           -v <host-dir>:/mnthost eclipse/che-mount <ip> <ws-id|ws-name>

# where <ip> is the IP address of your Docker daemon discoverable by:
docker run --rm --net host \
            alpine sh -c \
            “ip a show <network-if>"

# where <network-if> is ‘eth0’, or if using boot2docker, then ‘eth1’.

# Verify directories are in the mount container:
docker exec -ti che-mount sh
  >ls /mnthost
  >ls /mntssh
  >exit
```
