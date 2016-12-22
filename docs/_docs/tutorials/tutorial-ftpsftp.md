---
tags: [ "eclipse" , "che" ]
title: FTP/SFTP and Che
excerpt: ""
layout: tutorials
permalink: /:categories/ftpsftp/
---
##Add FTP / SFTP into a workspace
Che has Midnight Commander installed in the Terminal, so it's possible to use it to connect to a remote FTP server.

Just open your workspace in the IDE, go to the Terminal, execute `mc` and press F9 to call the menu. Select `Left`/`Right` window, select `FTP Link`.

Enter FTP server details in one of the following formats:
```shell  
username:password@host           #for non-anonymous login;
host                             #for anonymous login;
!username:password@host          #for servers behind the firewall, through proxy servers;
username:password@host:port      #for servers using non std port;
username:password@host/directory #to go to specific directory.\
```
##Synchronize projects
Once you've connected to your remote FTP server, you can copy any files or projects between the `/projects` directory in your Che workspace and any directory on your remote FTP server.
