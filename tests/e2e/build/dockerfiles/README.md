# End-to-end typescript tests for Che 7

## Table Of Content

* [Requirements](#requirements)
* [What are these tests meant for](#what-are-these-tests-meant-for)
* [How to run it](#how-to-run-it)

## Requirements
To run these tests you need to have running Che instance. You can execute tests directly using ` npm ` but if you like to use this Docker image, you need to have 
installed Docker on your machine. 

## What are these tests meant for
These tests serves for testing Che 7 happy path. You can see description about these tests here: https://github.com/eclipse/che/tree/master/tests/e2e.

## How to run it
The easiest way is to run them via Docker. To build locally you have to execute the following command: 

``` 
docker build -f build/dockerfiles/Dockerfile -t eclipse/che-e2e:next .
```
This command builds docker image named ` eclipse/che-e2e:nightly `. This image is build nightly and pushed to registry, so you don't have to build that image locally.   
You can run the tests inside this docker image. You have to set URL of running Che and increase shared memory size (low shared memory makes chrome driver crash).

```
docker run --shm-size=256m -e TS_SELENIUM_BASE_URL=$URL eclipse/che-e2e:nightly
```

If you want to gather screenshots of fallen tests, you have to mount a volume to the docker file. Create a folder, when you want to have the screenshots saved. Then run a command:

```
docker run --shm-size=256m -v /full/path/to/your/folder:/tmp/e2e/report:Z -e TS_SELENIUM_BASE_URL=$URL eclipse/che-e2e:nightly
```

Happy Path test suite will be run by default when executing a docker run command. If you want to run another test suite, you can specify that via variable ` TEST_SUITE `. Available tests are e.g. ` test-happy-path `, ` test-operatorhub-installation ` and ` test-wkspc-creation-and-ls `.

```
docker run --shm-size=256m -e TEST_SUITE=test-happy-path -e TS_SELENIUM_BASE_URL=$URL eclipse/che-e2e:nightly
```

### Video recording
ffmpeg will record the screen and produce mp4 file. Disable the recording by specifying `VIDEO_RECORDING=false` env parameter.

```
docker run ... -e VIDEO_RECORDING=false ... eclipse/che-e2e:nightly
```

### Debugging
#### Running own code
If you have done some changes locally and you want to test them, you can mount your code directly to the Docker. If you do so, your mounted code will be executed instead of the code that is already in an image.

```
docker run --shm-size=256m -v /full/path/to/your/e2e:/tmp/e2e:Z -e TS_SELENIUM_BASE_URL=$URL eclipse/che-e2e:nightly
```

#### Watching Chrome
If you want to see what is going on in chrome inside a docker, you can use VNC. When running a docker, you can see API where you can connect. This API is on the first line of output and can look like that: ` You can watch locally using VNC with IP: 172.17.0.2 `. Then you can easily join VNC using this API: ` 172.17.0.2:0 `.

