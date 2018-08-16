# How to Build Machine-Exec Image

## Build image manually
Example:
using build script:

```
./build.sh --tag:nightly
```

with native docker:

```
docker build -t eclipse/machine-exec:nightly .
```

# Run container from image manually:
To run container with machine-exec you need docker.sock. Command to run container manually:

```
docker run -v /var/run/docker.sock:/var/run/docker.sock eclipse/machine-exec:nightly
```

# How to use machine-exec image with Eclipse CHE workspace on the docker infrastructure:
Apply docker.sock path (by default it's `/var/run/docker.sock`) to the workspace volume property `CHE_WORKSPACE_VOLUME` in the che.env file:
Example:

```
CHE_WORKSPACE_VOLUME=/var/run/docker.sock
```

che.env file located in the CHE `data` folder. che.env file contains configuration properties for Eclipse CHE. All changes of the file become avaliable after restart Eclipse CHE.
