---
tags: [ "eclipse" , "che" ]
title: Tutorial&#58 Blink LED
excerpt: ""
layout: artik
permalink: /:categories/tutorial-blink-led/
---
{% include base.html %}
The ARTIK device and many other other embedded device have their own equivalents to the standard printed "Hello World!" program. To show that a device can interact with digital devices in the physical world a device blinks an LED to say "Hello".

This tutorial assumes that the user has already gone thru the ARTIK [Getting Started](../../docs/artik/) documentation.


Open the ARTIK IDE in your web browser after issuing `artik-ide start`. Select `Dashboard` from the menu on the left. The will bring up the workspace and project creation interface to the right. Select `New from blank, template, or sample project`, select `Ready-to-go Stacks`, select `Artik`, name the workspace `artik`(could pick anything), change the ram to 2 GB, select the `Ready-to-run project samples` with our tutorial project `artik-blink-led`, and finally select the `Create` button either at the top right or located at the bottom.

![artik-blink-led-1.jpg]({{ base }}/assets/imgs/artik-blink-led-1.jpg)
ARTIK IDE will then start creating the workspace.
![artik-blink-led-2.jpg]({{ base }}/assets/imgs/artik-blink-led-2.jpg)

The ARTIK workspace once started will look like the following and bring up the `Manage Artik Devices` window. You will need to enter your ARTIK board ipaddress. The [Getting Started](../../docs/artik/#5-discover-artik-device-ip-address) page has information on how to get your ARTIK device ipaddress. A quick way though is to use the following docker CLI `docker run jdrummond/artik-tools -q -t 5 -i <ip-address-on-network>`. If more than one ipaddress is found, remove `-q` and try again. If a ipaddress is not found, make sure your ARTIK device is on with 3 leds on next to ANT3, connected to a network via wired/wireless and make sure you <ip-address-on-network> used is on the same network as your ARTIK device.
![artik-blink-led-3.jpg]({{ base }}/assets/imgs/artik-blink-led-3.jpg)

![artik-blink-led-4.jpg]({{ base }}/assets/imgs/artik-blink-led-4.jpg)
Close the `Manage Artik Devices` window once the connection has been made.

Click the drop down `CMD` menu located to the left of the blue play button. Select `artik-blink-led: build` from the drop down menu. Click the drop down menu `Targets` just to the left and select the `default` target. Hit the blue play button to run the `artik-blink-led: build` command. A tab will appear in the `Processes` area at the bottom. There will be no output from this command but don't worry it did create the assembly/binary file that can be used on the ARTIK device.
![artik-blink-led-5.jpg]({{ base }}/assets/imgs/artik-blink-led-5.jpg)

![artik-blink-led-6.jpg]({{ base }}/assets/imgs/artik-blink-led-6.jpg)
The build creates a file called `a.out` in the projects root directory. In order to see the new file click the `artik-blink-led` root directory in the `Project Explorer` on the left then click the `refresh` button at the top right. Once this is done you will be able to see the `a.out` file. Right click on the `a.out` file and select from the drop down menu `Push To Device > artik_device_1`. A green popup should come up indicated the success of the push and the location of the `a.out` on the ARTIK device which by default is `/root`.
![artik-blink-led-7.jpg]({{ base }}/assets/imgs/artik-blink-led-7.jpg)

![artik-blink-led-8.jpg]({{ base }}/assets/imgs/artik-blink-led-8.jpg)
Let's check to make sure our ARTIK device has our new `a.out` assembly file. Click the `terminal` button command in the `Processes` area next to the ARTIK target. Once open issue command `ls /root` on the ARTIK device terminal. You should be able to see the `a.out` file listed in the ARTIK device `/root` folder.
![artik-blink-led-9.jpg]({{ base }}/assets/imgs/artik-blink-led-9.jpg)
Now lets run `a.out` assembly on the ARTIK device. Select `Edit Commands ...` from the drop down `CMD` menu. Hit the `+` button next to `CUSTOM` to create a new command. Name the command `artik-blink-led: run` and enter `/root/a.out` for the command line. Hit the `save` button of the bottom right then close window. Next click the target window and select the target `artik_device_1`. This will make our new command `artik-blink-led: run` execute on the ARTIK device NOT in the workspace.
![artik-blink-led-11.jpg]({{ base }}/assets/imgs/artik-blink-led-11.jpg)

![artik-blink-led-12.jpg]({{ base }}/assets/imgs/artik-blink-led-12.jpg)
Place an LED into between pin 13 and `GND`(newer ARTIK device models) or `Vref`(older ARTIK device models) depending on your model of ARTIK board. Refer to [Blink an LED](https://developer.artik.io/documentation/tutorials/blink-an-led.html) tutorial on ARTIK's documentation page for additional information. The image below shows the LED plugged into the newer model using the pin 13 and `GND`.
![artik-blink-led-13.jpg]({{ base }}/assets/imgs/artik-blink-led-13.jpg)

![2016-09-16_18-38-15.gif]({{ base }}/assets/imgs/2016-09-16_18-38-15.gif)
