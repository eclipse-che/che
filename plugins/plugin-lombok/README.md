# Description

This provides a plugin to enable lombok in eclipse che.


## How to build plugin-lombok


### 1- Change Version

change version to your current eclipse che version in these files

	plugin-lombok/che-plugin-lombok-server/pom.xml
	plugin-lombok/pom.xml


### 2- Copy plugin folder to eclipse che plugins

put the repo inside plugins folder of che source

add ```<module>plugin-lombok</module>``` in plugins/pom.xml


You can insert the module anywhere in the list. After you have inserted it, run `mvn sortpom:sort` and maven will order the pom.xml for you.

### 3- Link to WS-Agent assembly

Introduce the server part of the extension as a dependency in `/che/assembly/assembly-wsagent-war`. 

Add: 

```
<dependency>
    <groupId>org.eclipse.che.plugin</groupId>
    <artifactId>che-plugin-lombok-server</artifactId>
    <scope>runtime<scope>
    <exclusions>
        <exclusion>
            <artifactId>lombok</artifactId>
            <groupId>org.projectlombok</groupId>
        </exclusion>
    </exclusions>
</dependency>
```

You can insert the dependency anywhere in the list. After you have inserted it, run `mvn sortpom:sort` and maven will order the pom.xml for you.

### 4- Add dependancy to che-parent


```
<dependency>
    <groupId>org.eclipse.che.plugin</groupId>
    <artifactId>che-plugin-lombok-server</artifactId>
    <version>${che.version}</version>
</dependency>
```

You can insert the dependency anywhere in the list. After you have inserted it, run `mvn sortpom:sort` and maven will order the pom.xml for you.

### 5- Build plugin-lombok

Run ```mvn clean install -Denforcer.skip=true``` in plugin-lombok folder

### 6- Rebuild Eclipse Che


```Shell
## Build a new IDE.war
### This IDE web app will be bundled into the assembly
cd che/assembly/assembly-ide-war
mvn clean install

## Create a new web-app that includes the server-side extension
cd che/assembly/assembly-wsagent-war
mvn clean install

## Creates a new workspace agent that includes new web app w/ your extension
cd assembly/assembly-wsagent-server
mvn clean install

## Create a new Che assembly that includes all new server- and client-side extensions
cd assembly/assembly-main
mvn clean install
```

or



```Shell
## Build whole assembly
### This will bundle into the assembly
cd che/assembly
mvn clean install
```


### 7- Change properties

```
## Replace che.workspace.java_opts with this line
che.workspace.java_opts=-Xms1024m -Xmx3072m -javaagent:/home/user/lombok.jar=ECJ -Djava.security.egd=file:/dev/./urandom

and put che.properties in <path>data/conf folder
```

### 8- Run Eclipse Che

```Shell
## Start Che using the Docker Server with your new assembly
### 1- Build docker Image containing new assembly
Under che/assembly folder run
`sudo docker build -t eclipse/che-server .`

### 2- Build Workspace Image
Create a workspace image with che-plugin-lombok-server-<version>.jar copied in /home/user/lombok.jar

You can start a container with codenvy/ubuntu_jdk8 and copy the jar to correct path and commit your contaner to a new image
later you have to start workspace with that image.
### 3- Run eclipse che with newly created image

sudo docker run \
	-p 9000:8080 -p 8000:8000 --name che-server --rm \
	-v /var/run/docker.sock:/var/run/docker.sock \
	-v <path to data folder>:/data \
	-v <path to data folder>/conf:/conf \
	-e CHE_LOG_LEVEL=DEBUG \
	-e CHE_IP=<your ip> \
	-e CHE_DEBUG_SERVER=true \
	eclipse/che-server
```

### Documentation resources

- IDE Setup: https://eclipse-che.readme.io/v5.0/docs/setup-che-workspace  
- Building Extensions: https://eclipse-che.readme.io/v5.0/docs/create-and-build-extensions
- Run local Eclipse Che binaries: https://eclipse-che.readme.io/v5.0/docs/usage-docker#local-eclipse-che-binaries
