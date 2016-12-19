---
tags: [ "eclipse" , "che" ]
title: Stack
excerpt: "Sample code that can be used to bootstrap the content of a new project."
layout: docs
permalink: /:categories/stack/
---
JSON workspace object defines the contents and structure of a workspace. A workspace configuration is used to define the workspace to be generated.

## Stack Object
Stacks are referenced in JSON format:
```json  
{
  "description": STRING,     //Description of the stack to appear on dashboard
  "scope": STRING,
  "source": {},              // Information on dev-machine type and origin
  "tags": ARRAY,             // Values used to filter stacks in dashboard
  "workspaceConfig": {       // Configuration for workspace environment
    "environments": {        // Configuration for workspace environment
      "default": {           // Configuration for workspace environment
        "recipe": {},        // Recipe for workspace environment
        "machines": {        // Resources for each machine defined in recipe
          "dev-machine": {   // Required machine for workspace
            "agents": ARRAY, // Agents to inject into machine
            "servers": {},
            "attributes": {} // Define memory allocation for machine
          },
          "machine2": {      // Additional optional machine(s) for workspace
            "agents": ARRAY,
            "servers": {},
            "attributes": {}   
          }
        }
      }
    },
    "commands": ARRAY,      // Set of the commands available for machine(s)
    "projects": ARRAY,      // Project source code to import
    "defaultEnv": STRING,   // Environment to use and set to "default"  most often
    "name": STRING,         // Name of enfironment and set to "default" most often
    "links": []
  },
  "components": ARRAY,      // List of components and versions used in stack library
  "creator": STRING,        // Name of stack creator
  "name": STRING,           // Name of workspace configuration
  "id": STRING              // ID of workspace configuration
}

```


## WorkspaceConfig Object

WorkspaceConfig JSON:

```json  
workspaceConfig : {
  name           : STRING,    // The name of this workspace
  defaultEnv     : STRING,    // The name of env that powers this workspace
  environments   : [{}],      // Array of runtime envs this workspace uses
  projects       : [{}],      // List of projects included in the workspace
  commands       : [{}]       // Array of commands that build & run projects
}
```
Every workspace can have one or more environments which are used to run the code against a stack of technology. Every workspace has exactly one environment which acts as a special "development environment", for which projects are synchronized into and developer services are injected, such as intellisense, workspace agents, SSH, and plug-ins.  

Set `defaultEnv` to the name of the environment that should act as the Docker-powered environment that powers the workspace when it boots. This name must match the name given to an object in the `environments` array. Che will create a container off of this environment when the workspace is launched.

## Environments
Each environments are constructed of one or more machines, each one is an individual container. An environment can be comprised of multiple machines that are linked together, such as when you want a database running on a different machine than your debugger.
```json  
environment : {
  name           : STRING,     // Identifier and pretty name for environment
  recipe         : STRING,     // Define engine for composing machines network runtimes (compose, kubernetes pod)
  machineConfigs : [{}],       // Instructions for how Che builds a runtime
}
```

### MachineConfigs Object

MachineConfigs JSON:

```json  
environment.machineConfigs : [{
  name   : STRING,             // Name of the machine
  type   : STRING,             // What kind of machine - set to `docker` for containers
  limits : {                   // Limits for the machine
  	 ram : INT              // Memory in MB this machine will be allocated
  },
  dev    : [true | false],     // If true, injects dev services into machine
  source : {}                  // configure workspace agent runtime        
}
```

The source of a machine configuration object is supporting several types when using `docker` as machine configuration type, here are the supported source options

#### dockerfile type
It provides a docker runtime. Link to the Dockerfile recipe can be provided by a link, using `location` field or by providing directly the content of the Dockerfile, using `content `field
```json  
"source": {
  "type": "dockerfile",
  "location": "http://beta.codenvy.com/api/recipe/recipec0v4ta2uz6jok0bn/script"
}
```

```json  
"source": {
  "type": "dockerfile",
  "content": "FROM eclipse/ubuntu_jdk8
              RUN echo hello world
              ENV MYCUSTOM=VALUE"
}
```
#### image type

location can include the dockerhub image name
```json  
"source": {
  "type": "image",
  "location": "eclipse/ubuntu_jdk8"
}
```
 or for example include a registry url with a custom tag or a custom digest
```text  
"source": {
  "type": "image",
  "location": "myregistry:5000/eclipse/ubuntu_jdk8:myCustomTag"
}
```


### Mixins

Mixins:

```json  
project.mixins : [
  STRING, ...
]
```
A mixin adds additional behaviors to a project as a set of new project type attributes.  Mixins are reusable across any project type. You define the mixins to add to a project by specifying an array of strings, with each string containing the identifier for the mixin.  For example, `"mixins" : [ "git", "tour", "pullrequest" ]`.

| Mixin ID   | Description   
| --- | ---
| `git`   | Initiates the project with a git repository. Adds git menu functionality to the IDE. If a user in the IDE creates a new project and then initializes a git repository, then this mixin is added to that project.   
| `tour`  | Enables walk-me style guided tour functionality. You can author custom step by step tours that execute when users create a new workspace.  See [Tour](../../docs/tour) for specification and examples.   
| `pullrequest`   | Enables pull request workflow where server handles local & remote branching, forking, and pull request issuance. Pull requests generated from within server have another Factory placed into the comments of pull requests that a PR reviewer can consume. Adds contribution panel to the IDE. If this mixin is set, then it uses attribute values for `project.attributes.local_branch` and `project.attributes.contribute_to_branch`.   

The `pullrequest` mixin requires additional configuration from the `attributes` object of the project.  

### Attributes
Project attributes alter the behavior of the IDE or workspace.
```json  
project.attributes : {
  KEY : [VALUES], ...          // Each attribute and value is a String.
}
```
Different Eclipse Che plug-ins can add their own attributes to affect the behavior for the system.  Attribute configuration is always optional and if not provided within a workspace definition, the system will set itself.

#### Pull Request Attributes


| Known Attribute   | Description   
| --- | ---
| `local_branch` | Used in conjunction with the `pullrequest` mixin. If provided, the local branch for the project is set with this value. If not provided, then the local branch is set with the value of `project.source.parameters.branch` (the name of the branch from the remote).  If `local_branch` and `project.source.parameters.branch` are both not provided, then the local branch is set to the name of the checked out branch.   
| `contribute_to_branch` | Name of the branch that a pull request will be contributed to. Default is the value of `project.source.parameters.branch`, which is the name of the branch this project was cloned from.        

Here is a snippet that demonstrates full configuration of the contribution mixin.
```json  
factory.workspace.project : {
  "mixins"     : [ "pullrequest" ],

  "attributes" : {
    "local_branch"         : [ "timing" ],
    "contribute_to_branch" : [ "master" ]
  },

  "source" : {
    "type"       : "git",
    "location"   : "https://github.com/eclipse/che.git",
    "parameters" : {
      "keepVcs" : "true"
    }
  }
}
```

## Projects

Project object:

```json  
project : {
  name        : STRING,       // The name of the project
  type        : STRING,       // The project type defines plug-ins & behaviors
  description : STRING,       // Pretty description for display to users
  path        : STRING,       // Location in the workspace where this project lives
  source      : {},           // The source code repo for this project
  mixins      : [STRING],     // Adds behaviors to the project, such as enabling a pull request
  attributes  : {},           // Varies by project type
  modules     : [{}]          // Modules are project sub-units with type that can build & run
}\
```

A project has a type which causes special services to be added to the IDE and the default environment that is powering the workspace. Additionally, each project type has a specialized set of additional attributes that can alter the behavior of the project.  The icon next to your project name in the IDE explorer changes based upon the project type that it has.  You can also change project type in `Project > Configuration` in the IDE.

| Project Type | Description
| --- | ---  
| `blank`  |  A no-op project type. Inherits basic IDE functionality.   
| `maven`  | Installs a number of plug-ins including maven, Java, and ant. The maven project type provides a wizard for configuring projects, special editors for `pom.xml`, dozens of types of Java intellisense, and maven command types, which help developers write maven processes.   
| `node-js`   | Installs a number of plug-ins for JavaScript. Code completion for HTML, JavaScript and CSS actived.   
| `python`   | Inherits basic IDE functionality for Python.   
| `javac`   | Installs a number of plug-ins for Java. Enable classpath configuration, and Java Intellisense features.   
| `c`   | Inherits basic IDE functionality for C and GDB debugger.   
| `cpp`   | Inherits basic IDE functionality for C and GDB debugger.   

Set `project.path` to the relative location from the repository that contains the root of your project. Your workspace will have a root directory named `/projects` and this field is a relative path from that directory.

For example, let's take an example to create a workspace with three projects from three repositories: `project1`, `project2`, and `project3`.  These projects will be stored in `/projects/project1`, `/projects/project2`, and `/projects/project3`. The `path` attribute would be set to `/project1`, `/project2`, and `/project3`.

Every project belongs to a single version control repository. If you want the project within the workspace to have its code populated as a clone from a remote repository, then fill in the `projects.source` object with configuration information.

```json  
project.source : {
  type       : [git | svn | dockerfile],   
  location   : URL,            // Repo location
  parameters : {}              // (OPTIONAL) Attributes for version control
}

project.source.parameters : {      
  branch     : STRING,         // Clone from this branch
  startPoint : STRING,         // Branch to start at if value of 'branch' param isn't a valid branch
  keepVcs    : [true | false], // Keep the .git folder after clone.
  commitId   : STRING,         // Clone from a commit point. Branch precedes this property
  keepDir    : STRING,         // Clone all, but display only this subdir of repo
  fetch      : REF-SPEC        // Clone from patch set of provided ref-spec
}

```
Depending upon the type of version control system selected, you can configure the recipe to clone different repositories, branches, ref-specs. The `dockerfile` option is used by environments to reference a Dockerfile that will be used to build an image dynamically when the workspace is being constructed. The `git` and `svn` options are for cloning and synchronizing with Git and Subversion repositories.

Here is a simple example:
```json  
"source" : {  
  "location"   : "https://github.com/eclise/che.git",
  "type"       : "git"
}
```
This example clones a git repository hosted by Codenvy with a specific commit ID.

```json  
"source" : {  
  "project" : {  
    "location" : "http://codenvy.com/git/31/eb/be/workspace3u0vri1qaptw0vmr/spring",
    "type" : "git",
    "parameters" : {
      "commitId" : "db97e186e07ba881f23651d6238479cb2b1c3fcb"
    }
  }
}
```

### Modules
A module is a directory in a project that can be independently built and run.  Modules have their own project type and attributes, which can affect how the command behavior works for that directory apart from others or the project as a whole. Modules can be nested.
```json  
project.modules : [{
  name        : STRING,     // Name of the module
  description : STRING,     // (OPTIONAL) Pretty description to show to users
  path        : STRING,     // Relative path from workspace root to module root
  type        : STRING,     // Same as workspace.projects.type
  attributes  : {},         // (OPTIONAL) Same as workspace.projects.attributes
  mixins      : [STRING]    // (OPTIONAL) Same as workspace.projects.mixins
}]

```

## Commands
When authoring a project template we recommend to predefine commands to register build and run actions. [Learn more about commands.](../../docs/commands)
```json  
"commands" : {  
  "commandLine": "",                                     // Command to run on target machine
  "name": "",                                            // Unique Command name displayed in IDE
  "type": "custom"|"maven"|"java"|"gwt"|"gwt_sdm_che",    // Type will filter and provide different interface in IDE
  "attributes": {
    "previewUrl": "http://${server.port.8080}/${current.project.relpath}"
  }
}
```

```json  
command : {
  name        : STRING,       // Identifier and pretty name for command
  type        : STRING,       // Command type, such as "mvn"
  commandLine : STRING,       // Process to execute in the workspace
  workingDir  : STRING        // (Optional) Location in workpace to execute command
  attributes  : {
    previewUrl: STRING        // (Optional) Refer preview URL
  }
}
```
Each commands have a type and get translated into a process that is executed on the command line within the machine's environment. Some commands can be derived, such as `maven` commands where Che will apply the location and necessary flags for execution. Other commands can be custom, where the command line is executed within the environment as you have specified it.

The command line can use [Macros](../../docs/commands#macros).
See [Command](https://eclipse-che.readme.io/docs/workspace#section-command-object) reference.


### PreviewURL

Preview objects are stored as part of command. Che will generate the preview URL during the command execution and present the URL to the user as part of the command output. You can add a preview URL of any format within the command editor.


The previewURL can use [Macros](../../docs/commands#macros).


### Command Sample

Sample:

```json  
"command": {
  "commandLine" : "mvn clean install -f ${project.current.path} -Dmaven.test.skip=true",
  "name"        : "MCI",
  "type"        : "mvn"
}
```
This example will create an entry into the `CMD` drop down named `MCI` that will perform a `mvn clean install` command against the project or module that is selected in the project tree.

## Tags
Tags are used for stacks and sample objects. Those values are used to determine if a sample is compatible with a stack.
```json  
"tags" : {        
  "tag1",                             //list of strings representing tags
  "tag2",  
  "..."
}
```

## Sample Reference

JSON:

```json  
{
  "description": "Default Java Stack with JDK 8, Maven and Tomcat.",
  "scope": "general",
  "source": {
    "origin": "eclipse/ubuntu_jdk8",
    "type": "image"
  },
  "tags": [
    "Java",
    "JDK",
    "Maven",
    "Tomcat",
    "Subversion",
    "Ubuntu",
    "Git"
  ],
  "workspaceConfig": {
    "environments": {
      "default": {
        "recipe": {
          "location": "eclipse/ubuntu_jdk8",
          "type": "dockerimage"
        },
        "machines": {
          "dev-machine": {
            "agents": [
              "org.eclipse.che.terminal",
              "org.eclipse.che.ws-agent",
              "org.eclipse.che.ssh"
            ],
            "servers": {},
            "attributes": {
              "memoryLimitBytes": "2147483648"
            }
          }
        }
      }
    },
    "commands": [
      {
        "commandLine": "mvn clean install -f ${current.project.path}",
        "name": "build",
        "type": "mvn",
        "attributes": {}
      },
      {
        "commandLine": "mvn -f /projects/console-java-simple clean install",
        "name": "console-java-simple: build",
        "type": "mvn",
        "attributes": {
          "previewUrl": ""
        }
      },
      {
        "commandLine": "mvn -f /projects/console-java-simple clean install && java -jar /projects/console-java-simple/target/*.jar"
        "name": "console-java-simple: run",        
        "type": "mvn",
        "attributes": {
          "previewUrl": ""
        }
      }
    ],
    "projects": [
      {
        "source": {
          "location": "https://github.com/che-samples/console-java-simple.git",
          "type": "git",
            "parameters": {}
        },
        "problems": [],
        "links": [],
        "mixins": [],
        "name": "console-java-simple",
        "type": "maven",
        "path": "/console-java-simple",
        "attributes": {}
      }
    ],
    "defaultEnv": "default",
    "name": "default",
    "links": []
  },
  "components": [
    {
      "version": "1.8.0_45",
      "name": "JDK"
    },
    {
      "version": "3.2.2",
      "name": "Maven"
    },
    {
      "version": "8.0.24",
      "name": "Tomcat"
    }
  ],
  "creator": "ide",
  "name": "Java",
  "id": "java-default"
}
```
