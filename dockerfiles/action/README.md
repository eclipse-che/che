### Performing actions on a local or remote Eclipse Che instance with a Docker container

## Build container
```
$ build.sh  (on Unix)
```

## Run container
```
docker run --rm -v /var/run/docker.sock:/var/run/docker.sock eclipse/che-action <name-of-action>
```

## Get available actions
```
docker run --rm -v /var/run/docker.sock:/var/run/docker.sock eclipse/che-action
```

## Get help on a test
```
docker run --rm -v /var/run/docker.sock:/var/run/docker.sock eclipse/che-action <name-of-action> --help
```
