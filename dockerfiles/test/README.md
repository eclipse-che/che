### Testing local or remote Eclipse Che instance with a Docker container

## Build container
```
$ build.sh  (on Unix)
```

## Run a test
```
docker run --rm -v /var/run/docker.sock:/var/run/docker.sock eclipse/che-test <name-of-test>
```

## Get available tests
```
docker run --rm -v /var/run/docker.sock:/var/run/docker.sock eclipse/che-test
```

## Get help on a test
```
docker run --rm -v /var/run/docker.sock:/var/run/docker.sock eclipse/che-test <name-of-test> --help
```

