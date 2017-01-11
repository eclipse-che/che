---
tags: [ "eclipse" , "che" ]
title: IDE Setup
excerpt: ""
layout: docs
permalink: /:categories/setup-che-workspace/
---
{% include base.html %}
This page explains how to setup a developer workspace for Che using various IDEs. This includes checking out the sources. If you want to build an extension for Che, you should also follow these instructions.

Setting up your environment consist of three steps, (1) Getting the source code, (2), setting up your IDE for development, (3) and configuring the maven build.
![Selection_009.png]({{ base }}/assets/imgs/Selection_009.png)
Che is IDE agnostic. We provide configuration instructions for:
* [Eclipse (using the Eclipse Installer)](doc:setup-che-workspace#setup-the-eclipse-ide)
* [IntelliJ](doc:setup-che-workspace#setup-the-intellij-ide)
* [Che IDE (coming soon) ](doc:setup-che-workspace#setup-the-che-ide)

After setting up a workspace, you find documentation about how to setup your build and run environment for Che in the following guide: [Developing Extensions](doc:create-and-build-extensions).



# Pre-requisites  
Dependencies
- Docker 1.8+
- Maven 3.3.1+
- Oracle or OpenJDK Java 1.8


The M2_HOME and M2 variables should be set correctly.

To build the user dashboard submodule, you will need npm, bower, gulp, and python.
- Python `v2.7.x` (`v3.x.x` is currently not supported)
- Node.js `v4.x.x` (`v5.x.x` / `v6.x.x` are currently not supported)
- npm
- Bower
- gulp

Installation instructions for Node.js and npm can be found on the following [link](https://docs.npmjs.com/getting-started/installing-node). Bower and gulp are CLI utilities which are installed via npm:
```shell  
$ npm install --global bower gulp\
```

# Developers on Windows  

To build the Che core, you will need the maven-patch-plugin. Windows does not [support this plugin](http://maven.apache.org/plugins/maven-patch-plugin/faq.html#Why_doesnt_this_work_on_Windows), and we give instructions on how to skip this plugin when building. You can also optionally modify your build to [download the patch tool](http://gnuwin32.sourceforge.net/packages/patch.htm) and then add the patch tool to your `PATH`.

If you are a developer on Windows you'll not be able to do a complete build of Che by doing a `mvn clean install`. There are certain modules that require additional libraries and are OS specific. (dashboard and svn plugin)

In this situation, we recommend to build Che sources using the "che-dev" Docker image. This image has the dependencies necessary to build Che. You'll mount Che source code from your host to the container and then compile the code within the container.
```shell  
# For Windows, replace $HOME with maven repo directory.
# For Windows, replace $PWD with Che source code directory.

docker run -it --rm --name build-che
           -v "$HOME/.m2:/home/user/.m2"
           -v "$PWD":/home/user/che-build
           -w /home/user/che-build
           eclipse/che-dev
           mvn -DskipTests=true
               -Dfindbugs.skip=true
               -Dgwt.compiler.localWorkers=2 -T 1C
               -Dskip-validate-sources
               -Dmdep.analyze.skip=true
               -Dlicense.skip=true
               clean install

# For Mac + Linux - replace $PWD with the root path to build:
```
Alternatively you can also skip building certain submodules:
```shell  
# Each submodule may require additional software to build properly.
# You can skip a submodule to avoid installing additional software.
# For example, to skip building the dashboard:
mvn -pl '!dashboard' clean install\
```

# Eclipse IDE - Yatta Installer  
The [Yatta Installer for Eclipse Che](https://profiles.yatta.de/iQBd) installs Eclipse, the necessary plugins, checkout the Che source code, and configure a Che workspace. There is a lot going on, so this installation can take a few minutes.
#### The Yatta Installer Requires JavaFX
In case you use OpenJDK, you will need to install openjfx first.  


![ScreenShot2016-05-27at09.16.31.png]({{ base }}/assets/imgs/ScreenShot2016-05-27at09.16.31.png)

![yatta-installer.png]({{ base }}/assets/imgs/yatta-installer.png)
## Optional
You can consider to deactivate automatic builds in eclipse as this will run maven in places you may not want it to. Rebuilding some Che modules is not necessary unless you modify the code in that module. Otherwise, maven will grab the latest versioned module from Nexus.

If you plan to update files outside the workbench, then you can add a [native update hook refresh](http://help.eclipse.org/mars/index.jsp?topic=%2Forg.eclipse.platform.doc.user%2Ftasks%2Ftasks-52.htm).
# Eclipse IDE - Neon Installer  
If you install Eclipse manually, you will need to use the Eclipse Installer. Please get the Eclipse IDE for Java configured with the maven plugin.

1. Download Eclipse Neon from https://www.eclipse.org/downloads/eclipse-packages/ and select Eclipse IDE for Java EE Developers
![ScreenShot2016-10-21at15.15.16.png]({{ base }}/assets/imgs/ScreenShot2016-10-21at15.15.16.png)
2. Clone Eclipse Che sources on your local computer (repository URL: https://github.com/eclipse/che)

3. Once Eclipse installed, do `Import > Maven > Existing Maven Projects`
![import-existing-maven-projects.png]({{ base }}/assets/imgs/import-existing-maven-projects.png)
You will then be asked to select the existing projects to imported. Select them all.

The projects will be imported and you will see them in the explorer.

4. While the workspace will be built for the first time, you'll be asked to install the missing Eclipse plugins corresponding to the maven goals:
![install-plugin1.png]({{ base }}/assets/imgs/install-plugin1.png)

![install-plugin2.png]({{ base }}/assets/imgs/install-plugin2.png)

![install-plugin3.png]({{ base }}/assets/imgs/install-plugin3.png)

![install-plugin4.png]({{ base }}/assets/imgs/install-plugin4.png)
5.  Define the Maven command to build Che. Create a new "Run Configuration"
![ScreenShot2016-09-29at16.14.16.png]({{ base }}/assets/imgs/ScreenShot2016-09-29at16.14.16.png)
Double click on "Maven Build"

Choose "che-parent" or "assembly-main" to be the base directory:

![ScreenShot2016-10-21at11.13.34.png]({{ base }}/assets/imgs/ScreenShot2016-10-21at11.13.34.png)
Add the following goal `clean install`

Check the checkmark for skipping tests:
![ScreenShot2016-10-21at11.14.24.png]({{ base }}/assets/imgs/ScreenShot2016-10-21at11.14.24.png)
6. Build the workspace by executing the command you defined:
![ScreenShot2016-09-29at16.21.10.png]({{ base }}/assets/imgs/ScreenShot2016-09-29at16.21.10.png)
This will create an assembly in `{workspace-path}\che\assembly\assembly-main\target\eclipse-che-<version>\eclipse-che-<version>`
#### Tips
1. You might have Eclipse not discovering properly your environment variables. In this case, configure your environment variables with the custom command.\n\n\n2. It happens sometimes, that 'npm' is not having the right permissions to complete the build. In this case, remove your npm repository (.npm folder)  

7. Fixing the error marks

There are certains Maven goals that are not completed by Eclipse at once.
The idea is to require Eclipse to rebuild the workspace and update the project's dependencies. You can do that by doing right click on the projects (probably all the first time you are setupping the workspace).
![Screen_Shot_2016-10-06_at_14_46_36.png]({{ base }}/assets/imgs/Screen_Shot_2016-10-06_at_14_46_36.png)
8. To start Che from the custom assembly you just built, you can refer to this [Usage: Docker Launcher](doc:usage-docker#local-eclipse-che-binaries). Remind your custom assembly is located in `{workspace-path}\che\assembly\assembly-main\target\eclipse-che-<version>\eclipse-che-<version>`



# GWT Super Dev Mode for Eclipse  
GWT Super Dev Mode allows you to perform incremental compilations of Che's Java IDE into JavaScript. This makes redeploys nearly instant and allows for a natural style of IDE development.

You do not need GWT Super Dev Mode if you are building Che server-side or workspace extensions. This is only needed for customizations to the Java IDE which is transpiled into JavaScript.

## Install Google Plugin for Eclipse
This is a general purpose plugin that orchestrates integrating and configuring other Google software within Eclipse. The plugin is downloaded by entering a plugin URL in `Help > Install New Software`. The plugin URL is on [Google's Eclipse page]
![ScreenShot2016-10-06at15.04.47.png]({{ base }}/assets/imgs/ScreenShot2016-10-06at15.04.47.png)
(https://developers.google.com/eclipse/docs/getting_started).  You will be asked to install software for Google App Engine and other Google utilities. You only need the Eclipse plugin package.

## Download GWT SDK
[Download the GWT SDK 2.7.0 zip](http://www.gwtproject.org/versions.html) from Google's site. You will need to explode it and save in a directory on your compuer.

In Eclipse, go to `Window > Preferences > Google > Web Toolkit > SDKs > Add`. You will need to specify the directory where GWT is installed. The GWT zip has a few different sub-layers, so choose the directory where all of the JAR files are installed.

## Setup Run Configuration
In Eclipse, go to `Run > Run Configurations`, select `Java Application`, right click and select `New`.

In the `Main` tab, add the project `assembly-ide-war` with main class as `com.google.gwt.dev.codeserver.CodeServer`.
![ScreenShot2016-10-06at15.05.46.png]({{ base }}/assets/imgs/ScreenShot2016-10-06at15.05.46.png)
In the same panel, select the `Arguments` tab. We will add some content to `Program arguments`. Some of the parameters are mandatory, and you must add a `-src` parameter for each plugin that you have authored.
```shell  
-noincremental -src target/generated-sources/gen -src {path-to-your-extension}/src/main/java org.eclipse.che.ide.IDE\
```

![ScreenShot2016-10-06at15.06.23.png]({{ base }}/assets/imgs/ScreenShot2016-10-06at15.06.23.png)
In the `Classpath` tab, go to `User Entries > Add External Jars`. Add:
1. `gwt-codeserver.jar` (in the directory where you unzipped GWT zip),
2. `gwt-dev.jar`, (also in the same directory)
3. The JAR file for any extensions or plugins that you have built. You can find this JAR file in two locations. First, you can find it in the `/target` folder where you compiled the plugin. Second, you can also find it in your maven's local repository, typically in its `.m2` folder.
![ScreenShot2016-10-06at15.07.45.png]({{ base }}/assets/imgs/ScreenShot2016-10-06at15.07.45.png)
In the `Source` tab, remove any non-existent source folders. This is uncommon, but if you see something like `src/text/java` then these folders should be removed.



## Launch Super Dev Mode
Run super dev mode in Eclipse by `Run > Run Configurations`.  Select the run configuration that you just created and select `Run`. The first boot can take a few minutes as GWT is  recompiling the application with development mode hooks.

In the Eclipse console output, you will see a special URL - `http://localhost:9876/`. Launch a browser with this page. There will be two bookmarklets to drag onto your browser’s bookmark bar -  `Dev Mode On` and `Dev Mode Off`.
![ScreenShot2016-10-06at15.14.00.png]({{ base }}/assets/imgs/ScreenShot2016-10-06at15.14.00.png)
## Launch Che with Super Dev Mode
Run Che normally. You can use the CLI, the Che launcher, or Eclipse. Within your browser create a workspace and then identify the workspace name.  Open the workspace with the workspace name or ID that you captured, so this would be http://<che-url>/che/<ws-name>`.

Click the `Dev Mode On` bookmark on your booksmark bar. A message will appear asking you to recompile the application.  Select the `_app` and compile it.
![ScreenShot2016-10-06at15.22.26.png]({{ base }}/assets/imgs/ScreenShot2016-10-06at15.22.26.png)
The compilation will likely take 5 to 10 Minutes:
![ScreenShot2016-10-06at15.23.13.png]({{ base }}/assets/imgs/ScreenShot2016-10-06at15.23.13.png)
Che is now running in Super Dev Mode. You can now make incremental Java source file changes within your IDE and then have the browser trigger an incremental rebuild and reload.



## Debugging GWT Apps in the Chrome Browser
Google Chrome has an ability where you can set breakpoints for your Java GWT apps from within the Chrome browser itself, even though Chrome has loaded your GWT app as JavaScript!

For this to work, you will need to enable source maps in the Chrome developer console. Open the developer console and navigate to the `Sources` tab.
![debug_chrome.png]({{ base }}/assets/imgs/debug_chrome.png)
You will see the Java classes that make up your IDE plugin. You can open individual classes to set breakpoints. To set a breakpoint, right click on the line of code.
![debugger_chrome.png]({{ base }}/assets/imgs/debugger_chrome.png)
The app will pause in a chrome debugger on any breakpoint. You will see traces with exceptions if any. You can also step over, into and out of function calls. If you modify any of the Java source code in Eclipse, click on the `Dev Mode On` bookmark to recompile the application.


# GWT Super Dev Mode for IntelliJ  
The steps to configure Super Dev Mode for IntelliJ are largely similar to what you do for Eclipse.

## Download GWT SDK
[Download the GWT SDK 2.7.0 zip](http://www.gwtproject.org/versions.html) from Google's site. You will need to explode it and save in a directory on your compuer.

## Configure IntelliJ for GWT
JetBrains has a helpful page. There is [just a single step](https://www.jetbrains.com/help/idea/2016.2/enabling-gwt-support.html).

## Modify the Che Assembly
There are a few additional modifications to `/che/assembly/assembly-ide-war/pom.xml`. First, add a `gwt-dev` dependency and delete all 'provided' scopes.
```xml  
<dependency>
    <groupId>com.google.gwt</groupId>
    <artifactId>gwt-dev</artifactId>
    <version>${com.google.gwt.version}</version>
</dependency>

<!-- delete me -->
<scope>provided</scope>\
```
## Setup Run Configuration
In `Run > Edit Configurations > GWT Configuration`, add a new configuration. You must add `Dev Mode parameters` to include the items listed in the graphic below.
![sdm-intellij.jpg]({{ base }}/assets/imgs/sdm-intellij.jpg)
## Run Super Dev Mode
Follow the remaining steps for running a configuration to launch Super Dev Mode, running Che in Super Dev Mode, and then debugging your application within Chrome.
# Setup the Che IDE  

#### Building Che Extensions in Che Coming Soon
}  

You can create, build and run client-side Che extensions using the Che IDE.

1. Start Che.
2. Select `File > New > Project`.
3. Choose `Empty Extension Project` from the list of samples.
4. Enter a name for the project and click `Create`.
5. `Run` the extension to compile it and package into the existing assembly.
6. Che will launch another Che instance with your assembly.
