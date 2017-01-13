---
tags: [ "eclipse" , "che" ]
title: PlatformIO in Che
excerpt: ""
layout: tutorials
permalink: /:categories/platformio/
---
{% include base.html %}
This example uses the EmonTX connected to a RaspberryPi3 with the PlatformIO remote agent and Eclipse Che's PlatformIO stack.
![d11893a8-a2e5-11e6-9252-830ab42c4f0e.jpg]({{ base }}/assets/imgs/d11893a8-a2e5-11e6-9252-830ab42c4f0e.jpg)
1. Connect the EmonTX to USB to RS232 TTL Converter Adapter Module and then connect that to the RaspberryPi3
2. Install PlatformIO: ```sudo pip install https://github.com/platformio/platformio/archive/develop.zip```
3. Login to your PlatformIO account: ```pio account login```
4. Start the PlatformIO agent: ```pio remote agent start&```
5. Go to the Che dashboard and  create workspace from ```PlatformIO``` stack
![2f03377e-a2e7-11e6-8464-24cd8d2fd2b4.png]({{ base }}/assets/imgs/2f03377e-a2e7-11e6-8464-24cd8d2fd2b4.png)
6. In Che choose Workspace > Import Project and import the EmonTX firmware code from GitHub: https://github.com/openenergymonitor/emonTxFirmware.git
![a321b248-a2e7-11e6-8229-ba11a26c72c0.png]({{ base }}/assets/imgs/a321b248-a2e7-11e6-8229-ba11a26c72c0.png)
7. In the project import wizard, set the project type to ```C++```.
6. Open ```/emonTxFirmware/emonTxV3/RFM/emonTxV3.4/emonTxV3_4_DiscreteSampling```
7. Right click on the folder and select Convert to Project
8. Choose a ```C++``` project.
![439fcb7e-a2e8-11e6-9b3a-4b2e1d5d45d9.png]({{ base }}/assets/imgs/439fcb7e-a2e8-11e6-9b3a-4b2e1d5d45d9.png)
7. Open terminal and login ```pio account login``` you need to do it once per workspace.
8. Run command ```Remote device list```. Make sure your device is listed.
![01436220-a2ea-11e6-895f-be29fd448965.png]({{ base }}/assets/imgs/01436220-a2ea-11e6-895f-be29fd448965.png)
You can use the built-in commands to upload and list devices.

Finally, you can track devices with a serial monitor by entering ```pio remote device monitor - -baud 115200```
![95fbdfd2-a2ea-11e6-8773-6308cb848962.png]({{ base }}/assets/imgs/95fbdfd2-a2ea-11e6-8773-6308cb848962.png)
