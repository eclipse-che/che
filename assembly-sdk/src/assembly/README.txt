==========================================================================
 Copyright (c) 2012-2015 Codenvy, S.A.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
     Codenvy, S.A. - initial API and implementation
==========================================================================

ECLIPSE CHE
High performance, open source developer environments in the cloud.
http://www.eclipse.org/che/

======
About
======
1. Cloud IDE. Use your browser to program on any machine in any language. Edit build, debug and deploy projects bound to source repositories.

2. Workspace Server. Create developer environments with APIs. Add your project types, embed custom commands and host on any infrastructure.

3. Plug-Ins. Use Che's built-in language plug-ins or write packaged extensions that transform Che's IDE into new tools and assemblies.


=============
Requirements
=============
1. Java Platform, Standard Edition 8 Development Kit (JDK 8) or later;
2. Apache Maven version 3 or later.

===========
Installing
===========
1. Download and unpack a binary distribution of Eclipse Che.
2. A directory called "che-sdk-[version]" will be created.

==================
Build From Source
==================
You can also build a Che assembly from source.

1. Visit http://github.com/codenvy/che.
2. Follow instructions for building Che.

===================
Running & Stopping
===================
1. Launch Tomcat:

On *nix:
   ./che.sh start

On Windows:
   ./che.bat start

Che will be available at http://localhost:8080/

2. To stop:

On *nix:
   ./che.sh stop

On Windows:
   ./che.bat stop

======================================
Configure Properties, Storage & oAuth
======================================
1. System properties: https://eclipse-che.readme.io/docs/configuration#configuration-files
2. Workspace root:    https://eclipse-che.readme.io/docs/configuration#workspace-root-location
3. Project storage:   https://eclipse-che.readme.io/docs/configuration#project-storage-location
4. User profile:      https://eclipse-che.readme.io/docs/configuration#user-profile-location
5. GitHub oAuth:      https://eclipse-che.readme.io/docs/configuration#github-oauth

========================
Add / Remove Extensions
========================
1. Get JAR file of plug-in, or, create and build one.  https://eclipse-che.readme.io/docs/extension-development-workflow

2. Place JAR files into /ext/ directory.

3. Execute extension installation script:

On *nix:
   ./extInstall.sh

On Windows:
   ./extInstall.bat

4. Restart Che.

==========
Resources
==========
Home Page: http://eclipse.org/che
Docs:      https://eclipse-che.readme.io/docs
Source:    http://github.com/codenvy/che
