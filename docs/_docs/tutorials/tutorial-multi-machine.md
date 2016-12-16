---
tags: [ "eclipse" , "che" ]
title: Multi-Machine Workspaces in Che
excerpt: ""
layout: tutorials
permalink: /:categories/multi-machine/
---
A multi-machine recipe allows multiple runtimes to communicate/share data. In this tutorial we will be looking at an existing Java and MySQL application called Pet Clinic. The tutorial will help show how to create a multi-machine from an existing [runtime stack](doc:stacks) called "Java-MySQL", execute commands on different target runtimes, startup the Pet Clinic Tomcat server, view/interact with the Pet Clinic web page, and take a closer look at the "Java-MySQL" [runtime stack](doc:stacks) /[runtime recipe](doc:recipes) to get a better understanding of how multi-machine runtimes are created.

# 1. Start Che  
Use your SaaS account for the following, or if you have [installed Che](https://eclipse-che.readme.io/docs/che-getting-started), open a terminal and use the Che startup script:
```shell  
# Launch Che
che start\
```
When you execute this command, you'll see the URL for the Che server.

This URL can be used to open Che server's dashboard. It is where you manage your projects and workspaces.
# 2. Create Workspace  
Click the "Dasboard" menu item in the dashboard. Click the "New Workspace" button if there are existing workspaces or make sure "Select Source" category is set to "New from blank, template, or sample project" if one or more workspace exists.
![che-multimachine-tutorial3.jpg]({{ base }}/assets/imgs/che-multimachine-tutorial3.jpg)
Select "Java-MySQL" from the list of available stacks.
![che-multimachine-tutorial1.jpg]({{ base }}/assets/imgs/che-multimachine-tutorial1.jpg)
The other workspace information can remain as it is. Click the "create" button at the bottom to create the workspace.
![che-multimachine-tutorial2.jpg]({{ base }}/assets/imgs/che-multimachine-tutorial2.jpg)

# 3. Using IDE  
Once the workspace is created, the IDE will be loaded in the browser.

Each runtime can be identified in the processes section of the IDE. It will list the runtimes of "dev-machine" and "db" of our multi-machine workspace. The "db" runtime for this tutorial provides the database for the Java Spring application to use.

To make sure that the database is running we will issue the "show database" command to the "db" runtime. Select the "db" runtime item from the target drop down menu. Then make sure that "show databases" is selected in the command drop down menu and hit the play button.
![che-multimachine-tutorial4.jpg]({{ base }}/assets/imgs/che-multimachine-tutorial4.jpg)
This will run the "show databases" command in the "db" runtime and display the available database. Note that it is not required to know the database listed for this tutorial. This step merely shows how to successfully target different runtimes.

Switch the target back to "dev-machine", select the "web-java-petclinic: build and deploy" command, and click the play button. The Pet Clinic Java code will be compiled and the tomcat server started. Give the server ample amount of time to start as the server output may stay on `INFO  Version - HCANN000001: Hibernate Commons Annotations {4.0.5.Final}` for awhile. The tomcat server when it is ready will output `Server startup in <time> ms`. Click on the preview url link after the tomcat server is started to open the Pet Clinic web page.
![che-multimachine-tutorial6.jpg]({{ base }}/assets/imgs/che-multimachine-tutorial6.jpg)
The web page can interact in various ways with the database. Data can be added to the data by clicking the "Find owner" link, clicking the "Add owner" link, and filing in the form.
![che-multimachine-tutorial7.jpg]({{ base }}/assets/imgs/che-multimachine-tutorial7.jpg)

# 4. Editing, Building and Debugging  
Che is a fully featured IDE that just happens to be in the browser. You can explore the [editor](https://eclipse-che.readme.io/docs/editor-settings) which includes [intellisense](https://eclipse-che.readme.io/docs/intellisense) for some languages and [refactoring](https://eclipse-che.readme.io/docs/intellisense#section-refactoring).  It also includes [git and svn](https://eclipse-che.readme.io/docs/git) support built-in.

The example app has built in commands for [building](https://eclipse-che.readme.io/docs/build) and [running](https://eclipse-che.readme.io/docs/run#web-apps) the app.  You can also [debug](https://eclipse-che.readme.io/docs/debug) right inside Che.
# 5. About Docker and Compose  
Read this section to understand more about the multi-machine "Java-MySQL" [runtime stack](doc:stacks) used and it's [runtime recipe](doc:recipes). The "Java-MySQL" stack configuration is located in the "stacks" section in the dashboard. This stack can be found easier by typing "java" in the filter form.
![che-multimachine-tutorial8.jpg]({{ base }}/assets/imgs/che-multimachine-tutorial8.jpg)
Click on the "Java-MySQL" menu item which will bring up the stack's configuration page. There is various useful configuration information provided on this page as well as the [Runtime Stacks](doc:stacks) and [Runtime Recipes](doc:recipes) documentation pages. For this tutorial, we will be focusing on the recipe configuration and the "codenvy/mysql" dockerfile provided in the "Java-MySQL" stack.

The recipe uses docker compose syntax. Due to the limitation of the JSON syntax the compose recipe is written as a single line with `\n` indicating carriage return. The following is the recipe in expanded form to make reading easier.

```yaml    
services:
  db:
    image: codenvy/mysql
    environment:
      MYSQL_DATABASE: petclinic
      MYSQL_USER: petclinic
      MYSQL_PASSWORD: password
    mem_limit: 1073741824
  dev-machine:
    image: codenvy/ubuntu_jdk8
    mem_limit: 2147483648
    depends_on:
      - db\
```

Examining the code above you will see our two runtime machines "db" and "dev-machine". Every workspace requires a [machine](doc:machines) named "dev-machine".

In the recipe the `depends_on` parameter of the "dev-machine" allows it to connect to the "db" machine MySQL process' port 3306. The "dev-machine" configures it's MySQL client connection in the projects source code at `src/main/resources/spring/data-access.properties`. The url is defined by `jdbc.url=jdbc:mysql://db:3306/petclinic` which uses the database machine's name "db" and the MySQL server default port 3306.

Port 3306 is exposed in the "db" machines Dockerfile during build but is not required for "dev-machine" to connect to it. Exposing port 3306 is done to provide access to database that is external to "db" machine network via a random ephemeral port assigned by docker. The "dev-machine" by setting `depends_on: - db` creates a private network that allows it use of "db" machine's name as hostname and port 3306 without having to determine the ephemeral port docker assigned.

Exposing port 3306 is done to provide an option for an external administrator to log into the "db" machine MySQL server through a MySQL client on the ephemeral port assigned. The operations perspective interface provides the external ephemeral ports assigned by docker for all machines' exposed ports. Image below indicates only external ephemeral port 32800 assigned to "db" machine's exposed port 3306.
![che-mysql-tutorial1.jpg]({{ base }}/assets/imgs/che-mysql-tutorial1.jpg)
The "db" machine contains a MySQL database created by the Docker image "codenvy/mysql". Taking a closer look at the "codenvy/mysql" image Dockerfile's entry point script will show how the "db" machine configures the MySQL server at the workspace startup.

```text  
FROM alpine:3.4
EXPOSE 3306/tcp
ENV PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin

ADD entrypoint.sh /
CMD mkdir /docker-entrypoint-initdb.d
CMD ln -s /usr/local/bin/docker-entrypoint.sh /entrypoint.sh # backwards compat

........

CMD ["mysqld"]

........

VOLUME /var/lib/mysql
ENTRYPOINT ["docker-entrypoint.sh"]
```
The "docker-entrypoint.sh" script shown partly below uses environment variables to configure the MySQL server. In this stack's compose recipe, shown above, environment variables set the MySQL database's name and user/password information that is referenced in the "db" machine's entry point script (`$MYSQL_DATABASE`, `$MYSQL_USER`, and `$MYSQL_PASSWORD`).
```shell  
#!/bin/bash

.....

                if [ ! -z "$MYSQL_RANDOM_ROOT_PASSWORD" ]; then
                        MYSQL_ROOT_PASSWORD="$(pwgen -1 32)"
                        echo "GENERATED ROOT PASSWORD: $MYSQL_ROOT_PASSWORD"
                fi
                "${mysql[@]}" <<-EOSQL
                        -- What's done in this file shouldn't be replicated
                        --  or products like mysql-fabric won't work
                        SET @@SESSION.SQL_LOG_BIN=0;

                        DELETE FROM mysql.user ;
                        CREATE USER 'root'@'%' IDENTIFIED BY '${MYSQL_ROOT_PASSWORD}' ;
                        GRANT ALL ON *.* TO 'root'@'%' WITH GRANT OPTION ;
                        DROP DATABASE IF EXISTS test ;
                        FLUSH PRIVILEGES ;
                EOSQL

                if [ ! -z "$MYSQL_ROOT_PASSWORD" ]; then
                        mysql+=( -p"${MYSQL_ROOT_PASSWORD}" )
                fi

                if [ "$MYSQL_DATABASE" ]; then
             			echo "CREATE DATABASE IF NOT EXISTS \`$MYSQL_DATABASE\` ;" \
            			| "${mysql[@]}"
                        mysql+=( "$MYSQL_DATABASE" )
                fi

                if [ "$MYSQL_USER" -a "$MYSQL_PASSWORD" ]; then
                        echo "CREATE USER '$MYSQL_USER'@'%' IDENTIFIED BY \
                          '$MYSQL_PASSWORD' ;" | "${mysql[@]}"

                        if [ "$MYSQL_DATABASE" ]; then
                                echo "GRANT ALL ON \`$MYSQL_DATABASE\`.* TO \
                                  '$MYSQL_USER'@'%' ;" | "${mysql[@]}"
                        fi

                        echo 'FLUSH PRIVILEGES ;' | "${mysql[@]}"
                fi

.....

exec "$@"
```
