# ChromeDriver

[![Build Status](https://dev.azure.com/giggio/node-chromedriver/_apis/build/status/giggio.node-chromedriver?branchName=master)](https://dev.azure.com/giggio/node-chromedriver/_build/latest?definitionId=27&branchName=master) [![Build Status](https://api.shippable.com/projects/5c01ad17718ee50700de68bc/badge?branch=master)](https://app.shippable.com/github/giggio/node-chromedriver/runs?branchName=master)
[![npm](https://img.shields.io/npm/dt/chromedriver.svg)](https://www.npmjs.com/package/chromedriver)

An NPM wrapper for Selenium [ChromeDriver](https://sites.google.com/a/chromium.org/chromedriver/).

## Building and Installing

```shell
npm install chromedriver
```

Or grab the source and

```shell
node ./install.js
```

What this is really doing is just grabbing a particular "blessed" (by
this module) version of ChromeDriver. As new versions are released
and vetted, this module will be updated accordingly.

The package has been set up to fetch and run ChromeDriver for MacOS (darwin),
Linux based platforms (as identified by Node.js), and Windows.  If you
spot any platform weirdness, let us know or send a patch.

## Force download

By default this package, when installed, will search for an existing
Chromedriver binary in your configured temp directory. If found, and it is the
correct version, it will simply copy it to your node_modules directory. You can
force it always download by configuring it:

```shell
npm install chromedriver --chromedriver-force-download
```

Or add property into your [`.npmrc`](https://docs.npmjs.com/files/npmrc) file.

```
chromedriver_force_download=true
```

Another option is to use PATH variable `CHROMEDRIVER_FORCE_DOWNLOAD`.

```shell
CHROMEDRIVER_FORCE_DOWNLOAD=true npm install chromedriver
```

## Custom binaries url

To use a mirror of the ChromeDriver binaries use npm config property `chromedriver_cdnurl`.
Default is `https://chromedriver.storage.googleapis.com`.

```shell
npm install chromedriver --chromedriver_cdnurl=https://npm.taobao.org/mirrors/chromedriver
```

Or add property into your [`.npmrc`](https://docs.npmjs.com/files/npmrc) file.

```
chromedriver_cdnurl=https://npm.taobao.org/mirrors/chromedriver
```

Another option is to use PATH variable `CHROMEDRIVER_CDNURL`.

```shell
CHROMEDRIVER_CDNURL=https://npm.taobao.org/mirrors/chromedriver npm install chromedriver
```

## Custom binaries file

To get the chromedriver from the filesystem instead of a web request use the npm config property `chromedriver_filepath`.

```shell
npm install chromedriver --chromedriver_filepath=/path/to/chromedriver_mac64.zip
```

Or add property into your [`.npmrc`](https://docs.npmjs.com/files/npmrc) file.

```
chromedriver_filepath=/path/to/chromedriver_mac64.zip
```

Another option is to use the PATH variable `CHROMEDRIVER_FILEPATH`

```shell
CHROMEDRIVER_FILEPATH=/path/to/chromedriver_mac64.zip
```

This variable can be used to set either a `.zip` file or the binary itself, eg:

```shell
CHROMEDRIVER_FILEPATH=/bin/chromedriver
```

## Custom download options

Install through a proxy.

```shell
npm config set proxy http://[user:pwd]@domain.tld:port
npm config set https-proxy http://[user:pwd]@domain.tld:port
```

Use different User-Agent.
```shell
npm config set user-agent "Mozilla/5.0 (X11; Linux x86_64; rv:52.0) Gecko/20100101 Firefox/52.0"
```

## Skipping chromedriver download

You may wish to skip the downloading of the chromedriver binary file, for example if you know for certain that it is already there or if you want to use a system binary and just use this module as an interface to interact with it.

To achieve this you can use the npm config property `chromedriver_skip_download`.

```shell
npm install chromedriver --chromedriver_skip_download=true
```

Or add property into your [`.npmrc`](https://docs.npmjs.com/files/npmrc) file.

```
chromedriver_skip_download=true
```

Another option is to use the PATH variable `CHROMEDRIVER_SKIP_DOWNLOAD`

```shell
CHROMEDRIVER_SKIP_DOWNLOAD=true
```

## Running

```shell
bin/chromedriver [arguments]
```

And npm will install a link to the binary in `node_modules/.bin` as
it is wont to do.

## Running with Selenium WebDriver

```javascript
require('chromedriver');
var webdriver = require('selenium-webdriver');
var driver = new webdriver.Builder()
  .forBrowser('chrome')
  .build();
```

(Tested for selenium-webdriver version `2.48.2`)

The path will be added to the process automatically, you don't need to configure it.
But you can get it from `require('chromedriver').path` if you want it.

## Running via node

The package exports a `path` string that contains the path to the
chromedriver binary/executable.

Below is an example of using this package via node.

```javascript
var childProcess = require('child_process');
var chromedriver = require('chromedriver');
var binPath = chromedriver.path;

var childArgs = [
  'some argument'
];

childProcess.execFile(binPath, childArgs, function(err, stdout, stderr) {
  // handle results
});

```

You can also use the start and stop methods:

```javascript
var chromedriver = require('chromedriver');

args = [
	// optional arguments
];
chromedriver.start(args);
// run your tests
chromedriver.stop();

```

With the latest version, you can optionally receive a Promise from the `chromedriver.start` function:

```javascript
var returnPromise = true;
chromedriver
  .start(args, returnPromise)
  .then(() => {
    console.log('chromedriver is ready');
  });
```

Note: if your tests are ran asynchronously, chromedriver.stop() will have to be
executed as a callback at the end of your tests

## Versioning

The NPM package version tracks the version of chromedriver that will be installed,
with an additional build number that is used for revisions to the installer.
You can use the package version number to install a specific version, or use the
setting to a specific version. To always install the latest version of Chromedriver,
use `LATEST` as the version number:

```shell
npm install chromedriver --chromedriver_version=LATEST
```

Or add property into your [`.npmrc`](https://docs.npmjs.com/files/npmrc) file.

```
chromedriver_version=LATEST
```

Another option is to use env variable `CHROMEDRIVER_VERSION`.

```shell
CHROMEDRIVER_VERSION=LATEST npm install chromedriver
```

## A Note on chromedriver

Chromedriver is not a library for NodeJS.

This is an _NPM wrapper_ and can be used to conveniently make ChromeDriver available.
It is not a Node.js wrapper.

## Supported Node.js versions

We will do our best to support every supported Node.js versions.
See [nodejs/Release](https://github.com/nodejs/Release) for
the current supported versions. You can also view our
[build scripts](https://github.com/giggio/node-chromedriver/blob/master/azure-pipelines.yml) and check the versions there.

## Contributing

Questions, comments, bug reports, and pull requests are all welcome.  Submit them at
[the project on GitHub](https://github.com/giggio/node-chromedriver/).

Bug reports that include steps-to-reproduce (including code) are the
best. Even better, make them in the form of pull requests.

We have added
[VS Code Remote support with containers](https://code.visualstudio.com/docs/remote/containers).
If you are on Windows, set `git config core.autocrlf input` so you don't get git errors.

## Author

[Giovanni Bassi](https://github.com/giggio), with collaboration from
[lots of good people](https://github.com/giggio/node-chromedriver/graphs/contributors).

Thanks for Obvious and their PhantomJS project for heavy inspiration! Check their project on [Github](https://github.com/Obvious/phantomjs/tree/master/bin).

## License

Licensed under the Apache License, Version 2.0.
