---
tags: [ "eclipse" , "che" ]
title: Samples
excerpt: "Sample object defines code sample and commands that can be used to bootstrap the content of a new project."
layout: docs
permalink: /:categories/samples/
---
Samples object defines code sample and commands that can be used to bootstrap the content of a new project.
(Learn more on [Samples](doc:samples) )

## Samples Object

Sample apps are referenced in a JSON file:
```json  
samples : {
  name        : STRING,  // Name of the project
  displayName : STRING,  // Name of the project, which will be displayed for the client without restrictions for the value
  path        : STRING,  // Path to the project in a workspace  behaviors
  description : STRING,  // Description of the project to appear on the dashboard
  projectType : STRING,  // Project type.  Different types have different attributes. Find all the projects types descriptions in the Factory docs
  mixins      : {},      // Mixins add sets of behavior attributes to a project.
  attributes  : {},      // List of project type attributes. List varies by type selected
  modules     : {},      // (Optional) Project components. Set by server
  problems    : {},      // (Optional) Errors occurred in the project. Set by server
  source      : {},      // The source code imported  attributes to a project
  commands    : {},      // Set of the commands available for the project.
  links       : {},      // List of the method links
  category    : STRING,  // Category to be displayed in the IDE Create Project wizard
  tags        : ARRAY    // Values used to filter samples
}
```
### Source
```json  
samples.source : {                    
  type       : [git | svn | zip],           // Version control system
  location   : URL,                         // Location of source code in version control system or location of a remote zip archive
  parameters : {}                           // (Optional) Parameter list to configure access. Parameter variables vary by type
    }
```
When using `source.type` with `git` or `svn`, the `source.location` should be URL of a publicly available repo. Referencing private repos over HTTPS will result in clone failure unless credentials are provided in the URL itself. Using SSH URLs is possible, however, a user will need ssh key to complete this operation, therefore, it is recommended to use HTTPS URLs to public repos.
```json  
"source":{                        
      "type":"git",                 
      "location":"https://github.com/tomitribe/tomee-jaxrs-angular-starter-project.git",
      "parameters":{}                 
    },
```

`zip` archives are referenced as URLs to remotely hosted archives that are publicly available i.e. require no login/password to be downloaded. It is not possible to reference local URLs unless you run a local server to host them (in this case a local IP is used e.g. `http://192.168.0.10/projecs/myproject.zip`).  

```json  
"source":{                        
      "type":"zip",                  
      "location":"http://192.168.0.10/projecs/myproject.zip",
      "parameters":{}
    },
```

#### Parameters
```json  
samples.source.parameters : {      
  branch   : STRING,           // Clone from this branch
  keepVcs  : [true | false],   // Keep the .git folder after clone.
  commitId : STRING,           // Clone from a commit point. Branch precedes this property
  keepDir  : STRING,           // Sparse Checkout to clone only sub-directory of repository
  fetch    : REF-SPEC          // Clone from patch set of provided ref-spec
}
```

### Commands
When authoring a project sample we recommend to predefine commands to register build and run actions. [Learn more about commands.](https://eclipse-che.readme.io/docs/commands)
```json  
samples.commands : [{  
  // List of pre-defined commands
}]
```
See [Command](https://eclipse-che.readme.io/docs/workspace#section-command-object) reference.


### Tags
Tags are used for stacks and samples objects. Those values are used to determine if a sample is compatible with a stack.
```json  
samples.tags : [{        
  "tag1"                             //list of strings representing tags
  "tag2"  
  "..."
}]
```
## Samples Reference
```json  
[  
  {  
    "name":"web-javaee-jaxrs",        
    "displayName":"web-javaee-jaxrs",
    "path":"/web-javaee-jaxrs",       
    "description":"A basic example demonstrating JAXRS running on Apache TomEE",
    "projectType":"maven",         
    "mixins":[],                      
    "attributes":{                    
      "language":[
        "java"
      ]
    },
    "modules":[],                     
    "problems":[],                   
    "source":{                        
      "type":"git",                  
      "location":"https://github.com/tomitribe/tomee-jaxrs-angular-starter-project.git",                         
      "parameters":{}                 
    },
    "commands":[                      
      {  
        "name":"build",               
        "type":"mvn",                 
        "commandLine":"mvn -f ${current.project.path} clean install && cp ${current.project.path}/target/*.war $TOMEE_HOME/webapps/ROOT.war",   
        "attributes":{                
          "previewUrl":""             
        }
      },
      {  
        "name":"run tomee",       
        "type":"custom",        
        "commandLine":"$TOMEE_HOME/bin/catalina.sh run",
        "attributes":{  
          "previewUrl":"http://${server.port.8080}"
        }
      },
      {  
        "name":"stop tomee",       
        "type":"custom",      
        "commandLine":"$TOMEE_HOME/bin/catalina.sh stop",
        "attributes":{  
          "previewUrl":""
        }
      },
      {  
        "name":"build and run",
        "type":"mvn",
        "commandLine":"mvn -f ${current.project.path} clean install && cp ${current.project.path}/target/*.war $TOMEE_HOME/webapps/ROOT.war && $TOMEE_HOME/bin/catalina.sh run",
        "attributes":{  
           "previewUrl":"http://${server.port.8080}"
        }
      },
      {  
        "name":"debug",        
        "type":"mvn",        
        "commandLine":"mvn -f ${current.project.path} clean install && cp ${current.project.path}/target/*.war $TOMEE_HOME/webapps/ROOT.war && $TOMEE_HOME/bin/catalina.sh jpda run",        
        "attributes":{  
          "previewUrl":"http://${server.port.8080}"
        }
      }
    ],
    "links":[],
    "category":"Samples              
    "tags":[                           
      "maven",
      "java",
      "javaee",
      "jaxrs"
    ]
  }
]
```
