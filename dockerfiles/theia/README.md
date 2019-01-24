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

## CDN Support

This image can be built with CDN support, which means that it can be configured in a way that the client-side resources required to start
the IDE in the browser will be searched for on a CDN first, and then on the workspace IDE server if not found on the CDN.

In order to enable CDN support, the following **build arguments** are used:

- `CDN_PREFIX`

This is the base CDN URL (ending with a `/`) where the `theia` IDE artifacts will be made available,

- `MONACO_CDN_PREFIX`

This is the base CDN URL (ending with a `/`) where the Monaco-related artifacts used in Theia should be found.

Since Theia imports the Monaco dependencies from NPM packages, and bundles them as external files, there is a separate CDN prefix
for those Monaco-related files. This allows retrieving them from any CDN that is automatically synchronized with NPM,
such as http://unpkg.com/ or https://www.jsdelivr.com/.

NPM version number and file paths are added automatically by the Che-Theia CDN support.

For example, using JSDelivr, the following build argument should be added: `MONACO_CDN_PREFIX=https://cdn.jsdelivr.net/npm/`. 

Alternatively, if `CDN_PREFIX` and `MONACO_CDN_PREFIX` are provided as **environment variables**, the corresponding build arguments
will be added automatically by the `build.sh` script. This will make CDN support configuration easier in CI builds.

**Important note:** When CDN support is enabled, you should use the `build.sh` command to build the docker image (as show above), instead of the
native-docker way.

## Push files to Akamai NetStorage

When CDN support is enabled, the `build.sh` script allows pushing the artifacts to an Akamai NetStorage account.
The following **environment variables** can be set when calling the `build.sh` script, in order to push files to a NetStorage account:

- `AKAMAI_CHE_AUTH`

This is a mandatory multi-line environment variable that should contain the Akamai NetStorage configuration,
according to the following syntax:

```
[default]
key = <Secret key for the Akamai NetStorage account>
id = <NetStorage account ID>
group = <NetStorage storage group>
host = <NetStorage host>
cpcode = <NetStorage CPCode>
```

For more information, please refer to the Akamai NetStorage documentation or https://github.com/akamai/cli-netstorage

- `AKAMAI_CHE_DIR`

This optional environment variable allows overriding the base directory in which Theia IDE files will be pushed under
the configured NetStorage account. The default value is `che`.

The external URL where Theia IDE files will be available is built according to the following rules:

`<NetStorage storage group base URL>/${AKAMAI_CHE_DIR}/theia_artifacts/<theia IDE file path>`

For example if the NetStorage base URL is `https://assets.openshift.net`, and the `AKAMAI_CHE_DIR` is `che`,
then `CDN_PREFIX` build argument value would be set to:

`https://assets.openshift.net/che/theia_artifacts/`