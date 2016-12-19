---
tags: [ "eclipse" , "che" ]
title: Setup&#58 Networking
excerpt: "Setting up networking for the ARTIK IDE."
layout: artik
permalink: /:categories/network/
---
The ARTIK IDE makes connections between three entities: the browser, the Che server (which runs the ARTIK IDE) running in a Docker container, and a workspace running in a Docker container.

If you distribute these components onto different nodes, hosts or IP addresses, then you may need to add additional configuration parameters to bridge different networks.

Also, since the ARTIK server and your ARTIK workspaces are within containers governed by a Docker daemon, you also need to ensure that these components have good bridges to communicate with the daemon.

Generally, if your browser, ARTIK server and ARTIK workspaces are all on the same node, then localhost configuration will always work.
