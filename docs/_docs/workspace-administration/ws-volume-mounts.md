---
tags: [ "eclipse" , "che" ]
title: Volume Mounts
excerpt: ""
layout: docs
permalink: /:categories/volume-mounts/
---
Volume mounts are used by Eclipse Che to mount remote external files and directories into the workspaces. Users can mount volumes external to docker and between other containers.
# Mounting a Single Volume  
Mount a single volume to all workspaces/containers by setting the environment variable `CHE_EXTRA_VOLUME_MOUNT` with `<host-mount-path>:<ws-mount-path>` before starting Che.

All workspaces will have access to this volume mount.
```shell  
# Linux/Mac
set CHE_EXTRA_VOLUME_MOUNT=~/.ssh:/home/user/.ssh

# Windows
export CHE_EXTRA_VOLUME_MOUNT=~/.ssh:/home/user/.ssh\
```

# Mounting Multiple Volumes  
Mount multiple volumes to all workspaces/containers by setting the environment variable `CHE_EXTRA_VOLUME_MOUNT` with `<host-mount-path1>:<ws-mount-path1>;<host-mount-path2>:<ws-mount-path2>` before starting Che. Each volume mount is separated by a semicolon `;`.  

All workspaces will have access to these mounted volumes.
```shell  
# Linux/Mac
set CHE_EXTRA_VOLUME_MOUNT=~/.ssh:/home/user/.ssh;~/.m2:/home/user/.m2

# Windows
export CHE_EXTRA_VOLUME_MOUNT=~/.ssh:/home/user/.ssh;~/.m2:/home/user/.m2\
```

# Sharing Volumes Between Containers  
Share volumes between containers by using `volumes_from` in Docker compose using json syntax. Refer to [Multi-Machine Workspaces in Che](doc:che-and-multi-machine-workspaces) for additional information.
```json  
...
"volumes_from" : [
  "some-machine\n  "db-machine"
]
...

```

# Setting Permissions  
Set read-only or read-write permissions to volumes by adding `:ro`(read-only) or `:rw`(read-write) to the end of the mount definition(s). When no permissions is set read-write permission is used.
```shell  
#Linux/Mac
set CHE_EXTRA_VOLUME_MOUNT=~/.ssh:/home/user/.ssh:ro;~/.m2:/home/user/.m2:rw

#Windows
set CHE_EXTRA_VOLUME_MOUNT=~/.ssh:/home/user/.ssh:ro;~/.m2:/home/user/.m2:rw\
```

```text  
...
"volumes_from" : [
  "some-machine:ro\n  "db-machine:rw"
]
...
```
