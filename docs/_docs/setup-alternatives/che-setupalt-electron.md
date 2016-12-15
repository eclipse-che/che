---
tags: [ "eclipse" , "che" ]
title: Usage&#58 Electron Clients
excerpt: "Install Electron, a fast OS-specific desktop client for Eclipse Che"
layout: docs
permalink: /:categories/electron/
---
We have created Electron clients that you can use instead of your browser. Electron applications are native OS applications that run a Chromium client. Electron is small and loads faster than a browser. It has all the power of Chrome packaged as an OS-specific binary. You can distribute these clients such that they are pre-configured with the URL of your Che server.
![Capture2.PNG]({{ base }}/assets/imgs/Capture2.PNG)

#### Eclipse Che Electron Clients
These clients are currently in beta and not part of the standard distribution. We will include them into the standard distribution as we get more comfortable with their usage and after Electron and Atom can be included as part of Eclipse product distributions.\n\nIf you discover issues, please take time to file an issue.  


# Download Eclipse Che Clients  
We have binary executables that you can download. You must run these packages on the command line and pass in the URL of your Che server.

| Platform   
| ---
| Download Link   
| [Linux 64](https://github.com/TylerJewell/che-electron/releases/download/4.0.0-beta/eclipse-che-electron-linux64.zip)   
| [Linux 32](https://github.com/TylerJewell/che-electron/releases/download/4.0.0-beta/eclipse-che-electron-linux32.zip)   
| [Windows 64](https://github.com/TylerJewell/che-electron/releases/download/4.0.0-beta/eclipse-che-electron-win64.zip)   
| [Windows 32](https://github.com/TylerJewell/che-electron/releases/download/4.0.0-beta/eclipse-che-electron-win32.zip)   

Currently, you need to checkout the `che-electron` repository and [build the clients using NPM](https://github.com/TylerJewell/che-electron) and the electron packager.
# Usage  

```shell  
# Windows
eclipse-che . <che-server-url>

# Mac
open eclipse-che.app . <che-server-url>

# Linux
./eclipse-che . <che-server-url>\
```

# Build Clients From Source  
