Eclipse Che is a free and open Java framework for building Web client applications that are beautiful, fast, and modular. Che creates cross-browser applications by combining extensions into a single compiled and optimized application. Extensions are provided by Che and authored by you as Java classes that use GWT libraries. Your extensions are compiled with Che and translated to JavaScript with support for most modern browsers.

Che contains:
* A cloud IDE
* A kernel for loading, managing, and operating extensions authored as plug-ins
* A set of platform APIs for developer microservices (e.g., 'build project')
* Implementations of the APIs for desktop and cloud deployment
* Plug-ins for multiple programming languages, source code systems, builders and runners
* Everrest open source utility for mapping Java to Web Services
* A CLI for interacting with platform APIs
* An Eclipse plug-in for editing, building and running Che projects from within Eclipse
* Developer tooling for building and packaging plug-ins

![Eclipse Che](http://docs.codenvy-stg.com/wp-content/uploads/eclipse-che.png "Eclipse Che")


The IDE is a browser application that has a extensions packaged into it. Extensions make use of the platform APIs and run on the Eclipse Che kernel. The kernel is a servlet-based framework that loads and manages extensions.  The kernel can be run in any servlet container. Tomcat bundling is provided within Che. Tomcat is also included to make startup configuration simpler. 

Che can be installed on any operating system that supports Java 1.7 - desktop, server or cloud, and Maven 3.0.5 or higher. It has been tested on Ubuntu, Linux, MacOS and Windows. Java, GWT, GIN and Javascript are the core technologies used to build Che.

### License
Che is open sourced under the Eclipse Public License 1.0.

### Clone the Repository & Checkout Latest Stable Branch

```sh
git clone https://github.com/codenvy/sdk.git
cd sdk
git checkout 3.7.1
```

We advise against building the master branch as you may encounter intermediate errors. 

This builds the SDK, which includes our Java and git plug-ins.  If you want to build Che with all known plug-ins, build [github.com/codenvy/assembly-che] (https://github.com/codenvy/assembly-che).

### Build and Run Che
```sh
cd /sdk
mvn clean install
./che.sh [ start | stop ]
```

Che will be available at ```localhost:8080```

### Che Sub-Projects:
* **CLI**:                     [CLI for accessing APIs] (http://github.com/codenvy/cli)
* **CMF**:                     [Framework for creating diagram editors] (http://github.com/codenvy/cmf)
* **Commons**:                 [Shared libraries] (http://github.com/codenvy/commons)
* **Everrest**:                [A shell and command abstraction for Web services] (http://github.com/codenvy/everrest)
* **IDE**:                     [IDE] (http://github.com/codenvy/ide)
* **Maven Dep POM**:           [Maven dependencies] (http://github.com/codenvy/maven-depmgt-pom)
* **Maven Parent POM**         [Mane parent] (http://github.com/codenvy/maven-parent-pom)
* **Platform API**:            [REST APIs for platform services] (http://github.com/codenvy/platform-api)
* **Platform API GWT Client**: [Java client for APIs] (http://github.com/codenvy/platform-api-client-gwt)
* **AngularJS Plugin**:        [AngularJS autocomplete and editor] (http://github.com/codenvy/plugin-angularjs)
* **Bower Plugin**:            [Bower package management] (http://github.com/codenvy/plugin-bower)
* **Database Plugin**:         [Schema browser and SQL editor plugin] (http://github.com/codenvy/plugin-datasource)
* **CodeMirror Plugin**:       [Embeds CodeMirror editor] (http://github.com/codenvy/plugin-editor-codemirror)
* **Orion Plugin**:            [Embeds Orion editor] (http://github.com/codenvy/plugin-editor-orion)
* **Git Plugin**:              [User interface for git client] (http://github.com/codenvy/plugin-git)
* **Grunt Plugin**:            [Grunt server integrated into IDE] (http://github.com/codenvy/plugin-grunt)
* **Gulp Plugin**:             [Gulp builder] (http://github.com/codenvy/plugin-gulp)
* **Java Plugin**:             [Ant, maven, debugger, and code assistant] (http://github.com/codenvy/plugin-java)
* **NPM Plugin**:              [Manage npm packages within the IDE] (http://github.com/codenvy/plugin-npm)
* **Subversion Plugin**:       [User interface for subversion client] (http://github.com/codenvy/plugin-svn)
* **Eclipse Plug-In**:         [An Eclipse plug-in for running Che projects] (http://github.com/codenvy/eclipse-plugin)



### AngularJS Plugin Configuration
This plugin requires npm, Yeoman, bower and Grunt to be installed.

* npm setup guide : https://github.com/npm/npm
npm is bundled with nodejs which can be downloaded from: http://nodejs.org/download/

* Yeoman (and bower / Grunt dependencies can be installed with: npm install -g yo)
   more details on http://yeoman.io/gettingstarted.html

### Add Your Extensions to Che
1. Create, build and compile a Che extension into a Java JAR file. [Tutorial is here] (http://docs.codenvy.com/che/). You can create extension JARs within Che or your favorite IDE. Build extensions in Eclipse gives you super dev mode for Eclipse, which makes incremental compilation fast.

2. Copy the extension's JAR file to ```/assembly-sdk/target/tomcat-ide/ext``` directory of Che.  

3. Execute ```$ ./extInstall.sh``` script. Che will be re-compiled with your extension. This will take a few minutes.

4. Restart Che.

### Helping Eclipse Che

**Contribute:**: We accept pull requests, so if you feel like contributing to the project, you are definitely welcome to do so.

**Report Bugs:** You can report bugs, contribute, and post on the Eclipse Che forums at [https://www.eclipse.org/forums/index.php/t/787421/] (https://www.eclipse.org/forums/index.php/t/787421/). 

### Documentation & Tutorials
* **Che Binaries:** [http://docs.codenvy.com/download] (http://docs.codenvy.com/download/)
* **Che Getting Started:** [http://docs.codenvy.com/che/] (http://docs.codenvy.com/che/)
* **Che JavaDoc:** [docs.codenvy.com/che/javadoc/] (http://docs.codenvy.com/che/javadoc/)
* **Che Extension Tutorials:** [http://docs.codenvy.com/che/creating-extensions/#creating-che-apps] (http://docs.codenvy.com/che/creating-extensions/#creating-che-apps)


### Contact Information
* **Che Mail List:** [che-dev@eclipse.org](email:che-dev@eclipse.org)
* **Che Forums:** [https://www.eclipse.org/forums/index.php/t/787421/](https://www.eclipse.org/forums/index.php/t/787421/)
* **Website:** [eclipse.org/che](https://eclipse.org/che)
* **Che Project Management Page:** [https://projects.eclipse.org/projects/technology.che](https://projects.eclipse.org/projects/technology.che)
