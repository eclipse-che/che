---
tags: [ "eclipse" , "che" ]
title: Setup&#58 ARTIK USB
excerpt: ""
layout: artik
permalink: /:categories/usb-connection-setup/
---
After the workspace has been created, the first time that it is opened in the IDE, the ARTIK device discovery and management panel will appear. The workspace will manage your devices over SSH and configured by IP address.

You can discover the IP address of USB-connected devices automatically using an embedded service powered by the Android Debug Bridge (ADB), which is running on the device and also has its client installed into your workspace.

### Prerequisites for USB Discovery
The ADB daemon must be running on the device. Usually it is already running but if it's not or if you can't discover the device execute the following commands:
```shell  
# On the device: Start the daemon
service adbd start

# On the device: Have the daemon start upon device boot
systemctl enable adbd.service

# In Che workspace: Verify that discovery is working
adb devices    # Returns a discovered device\
```
Your Artik device must be connected over regular USB (do not use micro-USB).  If ADB discovery service fails to find any USB-connected devices, a `No USB devices found` message will be displayed. If that happens please [post an issue to this GitHub issues](https://github.com/eclipse/che/issues) page.

### Window User Using Boot2Docker with Virtualbox
On Windows machines using Boot2Docker and Virtualbox the USB controller must be enabled through Virtualbox Manager GUI:
- Stop the Boot2Docker machine.
- Select `USB 2.0 (EHCI) controller` in the Virtualbox UI and then hit the plus sign on the right.
- Make sure your ARTIK device is plugged in with a full size male to male USB cable and search for a USB device called `Android` and add it to the filter.
![Virtualboxaddusb.jpg]({{ base }}/assets/imgs/Virtualboxaddusb.jpg)
