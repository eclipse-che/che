# How to Build Theia Image

## Build image manually using build scripts
To build Theia image manually You could use build.sh scripts.
Dockerfile uses multi-stage build feature, so first of all You need build builder image -
theia-dev:

Example:

```shell
$ ../theia-dev/build.sh
```

After that, You can build Theia image.

Example:

```bash
$ ./build.sh --build-args:GITHUB_TOKEN=$GITHUB_TOKEN,THEIA_VERSION=0.3.18
```

## Theia version

There's a default Theia version set in the script. This version is then injected in all package.jsons.
You can override `THEIA_VERSION` by exporting the env before running the script

## GITHUB_TOKEN

Once of Theia dependencies calls GitHub API during build to download binaries. It may happen that GitHub API rate limit is exceeded.
As a result build fails. It may not happen at all. If it happens, obtain GitHub API token
