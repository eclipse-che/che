### Creating an Eclipse Che instance from a local directory

## Build Docker container
```
$ build.sh  (on Unix)
> build.bat (on Windows)
```

## Run container

Check no docker Eclipse Che container is alive and kill it if any
```
$ docker ps -a
```

Clone a folder
```
$ git clone https://github.com/che-samples/web-java-spring-petclinic
```

Go into this checkout directory
```
$ cd web-java-spring-petclinic
```

Run script
```
docker run -v /var/run/docker.sock:/var/run/docker.sock \
           -v "$PWD":"$PWD" --rm codenvy/che-file \
           $PWD <init|up>
```

note: if Eclipse Che is already started, it does not handle yet this state
