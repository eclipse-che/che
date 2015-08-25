==========================================================================
 Copyright (c) 2012-2015 Codenvy, S.A.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
     Codenvy, S.A. - initial API and implementation
==========================================================================

                        Codenvy Platform SDK
                       ------------------------
                       Build Your Own Cloud IDE


=============
Requirements
=============

 *  Java Platform, Standard Edition 7 Development Kit (JDK 7) or later;
 *  Apache Maven version 3 or later.


===========
Installing
===========

1. Download and install JDK 7.

1.1. Download JDK, release version 7 or later, from
     http://www.oracle.com/technetwork/java/javase/downloads/index.html.

1.2. Install JDK according to the instructions included with the release.

2. Download and install Apache Maven.

2.1. Download Apache Maven, release version 3 or later, from
     http://maven.apache.org/download.cgi.

2.2. Install Apache Maven according to the instructions included with the release.

3. Make sure JAVA_HOME is set to the location of your JRE.
   Make sure M2_HOME is set to the location of your Maven.

4. Download and unpack Codenvy Platform SDK.

4.1. Download a binary distribution of Codenvy Platform SDK.

4.2. Unpack the archive where you would like to store the binaries.
     A directory called "codenvy-sdk-[version]" will be created.

===================
Running & Stopping
===================

1. In order to launch a Codenvy Platform SDK, start Tomcat:

On *nix:
      ./che.sh start

* This script will also download and unpack another Tomcat which is used as runtime for apps and extensions launched in Codenvy SDK. This step will be skipped if you already already done so.

After launching, Codenvy Platform SDK application will be available at http://localhost:8080/

2. To stop a Codenvy Platform SDK application, execute the following command:

On *nix:
      ./che.sh stop
      
=========================================================
How to configure oAuth with GitHub and SSH key management
=========================================================

* Go to GitHub.com and login to your account

* In account Settings, find Applications menu on the left

* Click register a new application (if you don't have any)

* Enter application name

* Homepage URL should be

=====================
http://localhost:8080
=====================

* Authorization callback URL should be

=======================================================
http://localhost:8080/api/oauth/callback
=======================================================

* Having registered or updated the app, you will have Client ID and Client Secret in the top right corner of the application page on GitHub

* Copy those values to a file located in conf folder of the SDK Tomcat package - /conf/github_client_secrets.json

* By default, values for Client ID and Client Secret are filled with "***". You need to replace those with real values obtained from GitHub application settings.

===================================================================
Local Storage for Projects, Preferences, GitHub Tokens and SSH keys
===================================================================

Codenvy SDK provides configuration file codenvy-api-configuration.properties located at webapps/api/WEB-INF/classes/codenvy with a set of parameters having default values. To persist local configuration settings each time a new SDK version is downloaded, this file should be copied to a permanent location, and a new local environment variable should be added:

export CHE_LOCAL_CONF_DIR=”[path to a location where config file is stored]“, for example:

export CHE_LOCAL_CONF_DIR=”/home/eugene/.codenvy”

======================
Local Projects Storage
======================
In configuration file:

vfs.local.fs_root_dir=${catalina.base}/temp/fs-root
vfs.local.fs_index_root_dir=${catalina.base}/temp/fs-root

replace with

vfs.local.fs_root_dir=/home/eugene/SDK/.projects
vfs.local.fs_index_root_dir=/home/eugene/SDK/.projects

==============================================
Local Storage for GitHub Client ID and Secrets
==============================================

In configuration file:

Replace *** with real values obtained from a registered GitHub app:

#security
oauth.github.clientid=***
oauth.github.clientsecret=***
oauth.github.authuri= https://github.com/login/oauth/authorize
oauth.github.tokenuri= https://github.com/login/oauth/access_token
oauth.github.redirecturis= http://localhost:8080/api/oauth/callback

==================================================
Local User Profile: Theme Preferences and SSH Keys
==================================================

User preferences are stored in a profile which is a JSON file profile.json. Such preferences include but (not limited to) SSH keys and Themes. By default, the profile is stored in temp folder in Tomcat root, however, you can specify its new permanent location in che.properties and actually move it there:

user.local.db=${catalina.base}/temp

replace with

 user.local.db=/your/local/path/

===============================================
Contribute 3rd-party extensions to Codenvy IDE
===============================================

1. To contribute 3rd-party extensions to Codenvy IDE, one need to do the following steps:

1.1. Place an appropriate jar-files into "ext" directory.

1.2. Execute the following script:

On *nix:
      ./extInstall.sh

1.3. Restart Codenvy IDE if it is currently running.

=================
Update Extensions
=================

1. Launch extension at Run menu
2. Click extension URL in the Output console
3. Click Update Extension button in the runner instance
4. The extension will be recompiled which may take around 1 minute

===============================
Codenvy Platform SDK Resources
===============================

SDK Docs: http://docs.codenvy.com/sdk/
Codenvy Platform API Reference: http://docs.codenvy.com/ide/site/apidocs/
