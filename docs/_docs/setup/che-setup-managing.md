---
tags: [ "eclipse" , "che" ]
title: Managing
excerpt: ""
layout: docs
permalink: /:categories/managing/
---
## Scaling
Codenvy workspaces can run on different physical nodes that are part of a Codenvy cluster managed by Docker Swarm. This is an essential part of managing large development teams, as workspaces are both RAM and CPU intensive operations, and developers do not like to share their computing power. You will want to allocate enough nodes and resources to handle the number of concurrently *running* workspaces, each of which will have its own RAM and CPU requirements.

Each Codenvy instance generates a configuration for the nodes in its cluster. Run `codenvy add-node` for instructions on what to run on each physical node that should be added to the cluster. A script on the new node will install some software from the Codenvy master node, configure its Docker daemon, and then register itself as a member of the Codenvy cluster.

You can remove nodes with `codenvy remove-node <ip>`.

## Upgrading
Upgrading Codenvy is done by downloading a `codenvy/cli:<version>` that is newer than the version you currently have installed. You can run `codenvy version` to see the list of available versions that you can upgrade to.

For example, if you have 5.0.0-M2 installed and want to upgrade to 5.0.0-M8, then:
```
# Get the new version of Codenvy
docker pull codenvy/cli:5.0.0-M8

# You now have two codenvy/cli images (one for each version)
# Perform an upgrade - use the new image to upgrade old installation
docker run <volume-mounts> codenvy/cli:5.0.0-M8 upgrade
```

The upgrade command has numerous checks to prevent you from upgrading Codenvy if the new image and the old version are not compatible. In order for the upgrade procedure to advance, the CLI image must be newer that the version in `/instance/codenvy.ver`.

The upgrade process:
1. Performs a version compatibility check
2. Downloads new Docker images that are needed to run the new version of Codenvy
3. Stops Codenvy if it is currently running
4. Triggers a maintenance window
5. Backs up your installation
6. Initializes the new version
7. Starts Codenvy

## Backup
You can run `codenvy backup` to create a copy of the relevant configuration information, user data, projects, and workspaces. We do not save workspace snapshots as part of a routine backup exercise. You can run `codenvy restore` to recover Codenvy from a particular backup snapshot. The backup is saved as a TAR file that you can keep in your records.

### Microsoft Windows and NTFS
Due to differences in file system types between NTFS and what is commonly used in the Linux world, there is no convenient way to directly host mount Postgres database data from within the container onto your host. We store your database data in a Docker named volume inside your boot2docker or Docker for Windows VM. Your data is persisted but if the underlying VM is destroyed, then the data will be lost.

However, when you do a `codenvy backup`, we do copy the Postgres data from the container's volume to your host drive, and make it available as part of a `codenvy restore` function. The difference is that if you are browsing your `/instance` folder, you will not see the database data on Windows.

## Migration
We currently do not support migrating from the puppet-based configuration of Codenvy to the Dockerized version. We do have a manual process which can be followed to move data between the puppet and Dockerized versions. The versions must be identical. Contact us to let our support team perform this migration for you.

## Disaster Recovery
Codenvy is not designed to be a "5-9s" system, however there are steps that can be taken by an operations team responsible for Codenvy to ensure a quick recovery from any crashes.

A secondary Codenvy system should be installed and kept at the same version level as the primary system. On a nightly or more frequent basis the Codenvy data store, Docker images and configuration can be transferred to the secondary system.

In the event of a failure of the primary, the secondary system can be powered on and traffic re-routed to it. Users accounts and workspaces will appear in the state they were in as of the last data transfer.  

### Initial Setup
#### Create the Secondary System
Install Codenvy on a secondary system taking care to ensure that the version matches the primary system, remember to include the license file in this system. The secondary system should have the same number and size of nodes as the primary system.

#### Transfer Data from Primary System
On the primary system's master node:

1. Execute `codenvy backup`.
2. Run `docker images` to get a list of all the images used in Codenvy.
3. Run `docker save` for each of the listed images to create a TAR of each image for transfer.
4. In the `/etc/puppet/manifests/nodes/codenvy/` directory copy the `codenvy.pp` file to a location where it is ready to transfer.

On the secondary system's master node:

1. Execute `codenvy restore`.
2. Run `docker load` against each of the TARs generated from the primary system.
3. Replace the `codenvy.pp` file at `/etc/puppet/manifests/nodes/codenvy/` with the version copied from the primary system.
4. Restart the Codenvy system.

#### Setup Integrations
Any integrations that are used in the system (like LDAP, JIRA and others) should be configured identically on the secondary system.

#### Setup Network Routing
Codenvy requires a DNS entry. In the event of a failure traffic will need to be re-routed from the primary to secondary systems. There are a number of ways to accomplish this - consult with your networking team to determine which is most appropriate for your environment.

#### Test the Secondary System
Log into the secondary system and ensure that it works as expected, including any integrations. The tests should include logging in and instantiating a workspace at minimum. Once everything checks out you can leave the system idle (hot standby) or power it down (cold standby).

#### Encourage Developers to Commit
The source of truth for code should be the source code repository. Developers should be encouraged to commit their changes nightly (at least) so that the code is up-to-date.

### On-Going Maintenance
#### Version Updates
Each time the primary system is updated the secondary system should be updated as well.  Test both systems after update to confirm that they are functioning correctly.

#### Adding / Removing Nodes
Each time the primary system nodes change (new nodes are added, existing are removed, or node resources are significantly changed) the same changes should be made to the secondary nodes.

#### Nightly Data Transfers
On a periodic basis (we suggest nightly) the data transfer steps below should be executed. These can be scripted.  This is best done off-hours.

On the primary system's master node:

1. Execute `codenvy backup`.
2. Run `docker images` to get a list of all the images used in Codenvy.
3. Run `docker save` for each of the listed images to create a TAR of each image for transfer.
4. In the `/etc/puppet/manifests/nodes/codenvy/` directory copy the `codenvy.pp` file to a location where it is ready to transfer.

On the secondary system's master node:

1. Execute `codenvy restore`.
2. Run `docker load` against each of the TARs generated from the primary system.
3. Replace the `codenvy.pp` file at `/etc/puppet/manifests/nodes/codenvy/` with the version copied from the primary system.
4. Restart the Codenvy system.

### Triggering Failover
If there is a failure with the primary system, start the secondary system and log in to ensure that everything is working as expected. Then re-route traffic to the secondary nodes.
