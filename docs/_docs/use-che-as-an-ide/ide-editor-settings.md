---
tags: [ "eclipse" , "che" ]
title: Editor
excerpt: ""
layout: docs
permalink: /:categories/editor-settings/
---
Che uses [Orion](https://orionhub.org/) editor, which provides syntax coloring, code folding, typing help like braces auto pair.

Editor settings can be configured at `Profile > Preferences > IDE > Editor`. Preferences include typing behaviors, key bindings, tabbing rules, language tools, whitespaces and ruler preferences.

You can edit your files as you would in any other editor (you can switch to vi or emacs keybindings in `Profile > Preferences`).
![editor-prefs.png]({{ base }}/assets/imgs/editor-prefs.png)
Additionally it's possible to configure error/warning preferences for Java compiler at `Profile > Preferences > Java Compiler > Errors/Warnings`.
![java-compiler-prefs.png]({{ base }}/assets/imgs/java-compiler-prefs.png)
All the preferences are saved per user.

In some cases editor needs to be refreshed for new settings to be applied.
# Using Multiple Panes  
## Multi-Pane Editors
Starting in Eclipse Che 4.7 you can split the editor into multiple panes. This allows easier navigation when trying to see different files or parts of files at the same time. `Split Vertical` and `Split Horizontal` can be selected through the drop down menu accessible by right clicking on a tab in the editor.
![editorpanes.gif]({{ base }}/assets/imgs/editorpanes.gif)
## Multi-Pane Consoles
Starting in Eclipse Che 4.7 you can split the consoles into multiple panes. This allows easier navigation when trying to see different console outputs at the same time. `Split Vertical` and `Split Horizontal` can be selected through the drop down menu accessible by at the top right of the console area. To put new console in a newly created pane select the open area below the tabs area.
![consolepanes.gif]({{ base }}/assets/imgs/consolepanes.gif)

# Preview HTML Files  
Preview an HTML file by right-clicking it in the project explorer and selecting preview from the popup menu.
![che-previewHTML.jpg]({{ base }}/assets/imgs/che-previewHTML.jpg)

# Alternative: Use a Desktop IDE  
You can use a desktop IDE or editor with an Eclipse Che workspace by following the instructions in our [Local IDE Sync](docs:desktop-ide-mounting) docs.
