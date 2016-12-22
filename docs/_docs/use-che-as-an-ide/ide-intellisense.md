---
tags: [ "eclipse" , "che" ]
title: Intellisense
excerpt: ""
layout: docs
permalink: /:categories/intellisense/
---
Eclipse Che uses a language server protocol to provide intellisense for various languages. Each language server protocol is an [agent](https://eclipse-che.readme.io/docs/workspace-agents) that is injected into the [runtime machine dev-machine](https://eclipse-che.readme.io/docs/machines) by adding it to a [runtime stack](https://eclipse-che.readme.io/docs/stacks) configuration or an existing [workspace](https://eclipse-che.readme.io/docs/machines#section-dashboard-machine-information) in the dashboard.
# Language Intellisense  
We currently support the following intellisense languages.

| Language   | Agent Name   
| --- | ---
| Java   | org.eclipse.che.ws-agent   
| C#   | org.eclipse.che.ls.csharp   
| PHP   | org.eclipse.che.ls.php   
| JSON   | org.eclipse.che.ls.json   

##Syntax Highlighting
Certain words will be highlighted with color defined by the [language server protocol](https://eclipse-che.readme.io/docs/intellisense#language-server-protocol).

##Syntax Error Checking
Certain code will be identified when there is an error in the code defined by a [language server protocol](https://eclipse-che.readme.io/docs/intellisense#language-server-protocol). Usually errors are identified to the user by underlining in red the code and providing a error icon to the left side on the line of the code. Hovering the mouse over the error icon or underlined code will give suggestion on how to correct the code.

##Auto-Complete
Auto-complete can be used by the user to determine possible code outcomes defined by a [language server protocol](https://eclipse-che.readme.io/docs/intellisense#language-server-protocol). In order to activate this feature the user needs to type <ctrl+space> which will bring up a menu listing possible code outcomes known/selected by the language server. If only one possible out come exists to the language server, the code will be inserted into the editor.

##Refactoring
Renaming files or parts of code such as variables/functions/classes requires refactoring to change any parts of other code that may refer to it by name. Eclipse Che refactoring is defined by a [language server protocol](https://eclipse-che.readme.io/docs/intellisense#language-server-protocol).
# Language Server Protocol  
A language server has two phases of management within Che: installation and initialization.  The installation is about how the language server gets installed into the stack, so that it is present for Che to turn on or off. The initialization process is the launching of the language server so that it is a running process within the workspace, and that the editor can connect to it to provide intellisense capabilities.

The installation of a language server must happen within the definition of a workspace, or as part of a stack. Language servers are packaged within "agents" which are scripts that install the language server and its dependencies into a workspace. The agent installation can happen either as part of a stack (when the workspace is first started), or dynamically when a file of an associated type is first opened in the editor. It's recommended, for performance, to always include the associated agent within a workspace / stack definition.

Once a language server is included in the workspace as an agent, then there needs to be a launcher that controls when the language server is started and stopped. Technically, agents can be start / stopped for any number of reasons. For language servers, those agents will be typically start / stopped when the editor opens a file of a certain extension or when a plugin of a certain type is created. Right now, launchers need to be authored as Che extensions that are packaged with Che itself. We will also look to make these dynamic in the future.
