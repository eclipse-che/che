### Testing local or remote Eclipse Che instance with a Docker container

## Build container
```
$ build.sh  (on Unix)
> build.bat (on Windows)
```

## Run a test
```
docker run --rm -v /var/run/docker.sock:/var/run/docker.sock codenvy/che-test <name-of-test>
```

## Get available tests
```
docker run --rm -v /var/run/docker.sock:/var/run/docker.sock codenvy/che-test
```

## Get help on a test
```
docker run --rm -v /var/run/docker.sock:/var/run/docker.sock codenvy/che-test <name-of-test> --help
```

