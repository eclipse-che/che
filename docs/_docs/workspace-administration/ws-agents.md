---
tags: [ "eclipse" , "che" ]
title: Workspace Agents
excerpt: ""
layout: docs
permalink: /:categories/agents/
---
{% include base.html %}
Agents are scripts that are executed after a [runtime machines](https://eclipse-che.readme.io/docs/machines) is created. They add additional capabilities to the machines they're injected in - for example to allow terminal access or enhanced language services. Agents allow these services to be injected into machines built from stock Dockerfiles or Compose files.
# Adding Agents to a Machine  
Agents are added to [machines](https://eclipse-che.readme.io/docs/machines) through [runtime stack](https://eclipse-che.readme.io/docs/stacks) configuration. Eclipse Che's included stacks have been pre-configured to use certain agents. The agents needed for each pre-defined stack is determined by common tasks or file types found in [projects](https://eclipse-che.readme.io/docs/projects).

Adding agents to your own machines can be done by editing [machine information in the user dashboard](https://eclipse-che.readme.io/docs/machines#section-dashboard-machine-information).
# Adding Agents to a Custom Stack  
Stacks use JSON format for configuration. Agents are included in the machines definition. Each stack requires a machine named `dev-machine` which always requires the terminal, ws-agent, and SSH agents. Language server agents need to be added to the dev-machine if you want [intellisense](https://eclipse-che.readme.io/docs/intellisense) features when using the workspace IDE.
```json  
.......
  workspaceConfig": {
    "environments": {
      "default": {
        "recipe": {
          "location": "eclipse/ubuntu_jdk8\n          "type": "dockerimage"
        },
        "machines": {
          "dev-machine": {
            "servers": {},
            "agents": [
              "org.eclipse.che.terminal\n              "org.eclipse.che.ws-agent\n              "org.eclipse.che.ssh\n              "org.eclipse.che.ls.php"
            ],
            "attributes": {
              "memoryLimitBytes": "2147483648"
            }
          }
        }
      }
    },
.......

```

# Creating New Agents  
Currently, all agents must be pre-defined within Che and [saved in this location](https://github.com/eclipse/che/tree/master/wsmaster/che-core-api-agent/src/main/resources/agents) as part of the Che repository. We are thinking about a public registry for agents where they can be added and removed, but this is a future activity.

Each agent is saved as a JSON file. The name of the file without the extension is used as the ID for the agent within the agents list in the dashboard.  For example, the CSharp agent is identified as `org.eclipse.che.csharp`.  Inside of workspaces and stacks.  In the JSON there is a new data object called `agents : []` which is an array of agents that are to be included. The array would list the IDs of each agent to include.

Agents have a name, a set of other agents that they depend upon, properties and a "script", which defines how the agent's package is to be installed into the workspace. This script handles only installation and doesn't define the startup of the agent.  The script section of the agent is difficult to read, but we also include the same scripts in this [scripts directory](https://github.com/eclipse/che/blob/master/wsmaster/che-core-api-agent/src/main/resources/agents/org.eclipse.che.ls.php.json) and our CI systems convert those scripts to embed them inline within your agent during the packaging phase.

The scripts that you must provide with an agent have a large `if` block where you provide installation logic for each Linux distribution that we support. You can follow our templates for how to build agents of your own.

To add a custom language server agent you have to accomplish two steps:
* Implement a [language server launcher](https://github.com/eclipse/che/blob/master/wsagent/che-core-api-languageserver/src/main/java/org/eclipse/che/api/languageserver/launcher/LanguageServerLauncher.java). Have a look at [Json language server launcher](https://github.com/eclipse/che/blob/master/plugins/plugin-json/che-plugin-json-server/src/main/java/org/eclipse/che/plugin/json/languageserver/JsonLanguageServerLauncher.java)
