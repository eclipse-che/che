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
To run container with machine-exec you need docker sock. Command to run container manually:

```
docker run  -v /var/run/docker.sock:/var/run/docker.sock -ti eclipse/machine-exec:nightly
```

# Use machine-exec image with Eclipse CHE on the docker infrastructure:
To use machine-exec image on the docker infrastructure apply docker sock volume to you che.env file:
Example:
```

```