### Testing local or remote Eclipse Che instance with a Docker container

## Build container
```
$ build-docker-image.sh
```

## Run container
```
docker run --rm -v /var/run/docker.sock:/var/run/docker.sock codenvy/che-test <post-check> [URL of Che/Codenvy] [login] [password]
```
