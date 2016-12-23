---
tags: [ "eclipse" , "che" ]
title: Release Notes
excerpt: "Notes on features and bugs fixed in ARTIK IDE releases."
layout: artik
permalink: /:categories/release-notes/
---
{% include base.html %}
# ARTIK IDE 1.1.0  
## Bugs Fixed
* Deleting a device from the list of Artik devices does not delete it from consoles panel
* Consoles panel: Firefox bugfixes
* [Delete ARTIK device on "Manage Artik Devices" page without disconnect](https://github.com/codenvy/artik-ide/issues/106)
* [The status (CPU, MEM, DISK) of ARTIK board can not show, after open the terminal](https://github.com/codenvy/artik-ide/issues/107)
* [Cannot open the terminal of ARTIK board, after connect multiple boards](https://github.com/codenvy/artik-ide/issues/108)


# ARTIK IDE 1.0  
### Features Added
* Show online documentation in the ARTIK IDE
* Added development profile for ARTIK device that installs appropriate dev libraries and readies device for application creation
* Added production profile for Artik devices which removes all dev-specific libraries and settings
* Resource monitor for Artik devices showing CPU, memory and disk activity
* Commands can be targeted to any device and pre-targeted to an expected device
* Use of rsync to replicate project sources between device and IDE workspace automatically
* Context sensitive help for Artik SDK keywords
* Device logging: added user customizable command to tail relevant logs
* Split editor views both horizontally and vertically
* Split terminal views both horizontally and vertically
* Packaging of ARTIK dockerfiles for workspace creation
* Simplified debugger setup (consolidation of menus)
* ARTIK API integration: SDK/API management wizard
* ARTIK API integration: Show current SDK/API versions
* ARTIK API integration: New SDK/API version detection
* ARTIK API integration: Change SDK/API version in workspace and device

# ARTIK IDE Beta  
### Features Added
* IDE features based on Eclipse Che
* Add devices panel with shortcut menu button
* Remote shell access to devices and workspace
* Commands for building a deploying binaries
* Push to device: scp selected files/folders to the selected target board
* Ability to trace logs
* Discovery of ARTIK devices plugged into USB via ADB bridge
* Code formatting and coloring for ARTIK supported languages
* Access to ARTIK example applications from inside the ARTIK IDE
* Creation of default ARTIK and Android [Stacks](../../docs/stacks) to the ARTIK IDE
