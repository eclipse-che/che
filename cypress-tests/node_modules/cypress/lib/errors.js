'use strict';

var _templateObject = _taggedTemplateLiteral(['\n    Search for an existing issue or open a GitHub issue at\n\n      ', '\n  '], ['\n    Search for an existing issue or open a GitHub issue at\n\n      ', '\n  ']),
    _templateObject2 = _taggedTemplateLiteral(['\n    \nPlease reinstall Cypress by running: ', '\n  '], ['\n    \\nPlease reinstall Cypress by running: ', '\n  ']),
    _templateObject3 = _taggedTemplateLiteral(['\n\n    Reasons this may happen:\n      \n    - node was installed as \'root\' or with \'sudo\'\n    - the cypress npm package as \'root\' or with \'sudo\'\n    \n    Please check that you have the appropriate user permissions.\n  '], ['\\n\n    Reasons this may happen:\n      \n    - node was installed as \'root\' or with \'sudo\'\n    - the cypress npm package as \'root\' or with \'sudo\'\n    \n    Please check that you have the appropriate user permissions.\n  ']),
    _templateObject4 = _taggedTemplateLiteral(['\n\n    We expected the binary to be installed here: ', '\n \n    Reasons it may be missing:\n\n    - You\'re caching \'node_modules\' but are not caching this path: ', '\n    - You ran \'npm install\' at an earlier build step but did not persist: ', '\n\n    Properly caching the binary will fix this error and avoid downloading and unzipping Cypress.\n\n    Alternatively, you can run \'cypress install\' to download the binary again.\n\n    ', '\n  '], ['\\n\n    We expected the binary to be installed here: ', '\n \n    Reasons it may be missing:\n\n    - You\'re caching \'node_modules\' but are not caching this path: ', '\n    - You ran \'npm install\' at an earlier build step but did not persist: ', '\n\n    Properly caching the binary will fix this error and avoid downloading and unzipping Cypress.\n\n    Alternatively, you can run \'cypress install\' to download the binary again.\n\n    ', '\n  ']),
    _templateObject5 = _taggedTemplateLiteral(['\n    There was a problem spawning Xvfb.\n\n    This is likely a problem with your system, permissions, or installation of Xvfb.\n    '], ['\n    There was a problem spawning Xvfb.\n\n    This is likely a problem with your system, permissions, or installation of Xvfb.\n    ']),
    _templateObject6 = _taggedTemplateLiteral(['\n    Install XVFB and run Cypress again.\n\n    Read our documentation on dependencies for more information:\n\n      ', '\n\n    If you are using Docker, we provide containers with all required dependencies installed.\n    '], ['\n    Install XVFB and run Cypress again.\n\n    Read our documentation on dependencies for more information:\n\n      ', '\n\n    If you are using Docker, we provide containers with all required dependencies installed.\n    ']),
    _templateObject7 = _taggedTemplateLiteral(['\n    This is usually caused by a missing library or dependency.\n\n    The error below should indicate which dependency is missing.\n\n      ', '\n\n    If you are using Docker, we provide containers with all required dependencies installed.\n  '], ['\n    This is usually caused by a missing library or dependency.\n\n    The error below should indicate which dependency is missing.\n\n      ', '\n\n    If you are using Docker, we provide containers with all required dependencies installed.\n  ']),
    _templateObject8 = _taggedTemplateLiteral(['\n    Please search Cypress documentation for possible solutions:\n\n      ', '\n\n    Check if there is a GitHub issue describing this crash:\n\n      ', '\n\n    Consider opening a new issue.\n  '], ['\n    Please search Cypress documentation for possible solutions:\n\n      ', '\n\n    Check if there is a GitHub issue describing this crash:\n\n      ', '\n\n    Consider opening a new issue.\n  ']),
    _templateObject9 = _taggedTemplateLiteral(['\n    The environment variable CYPRESS_BINARY_VERSION has been renamed to CYPRESS_INSTALL_BINARY as of version ', '\n    '], ['\n    The environment variable CYPRESS_BINARY_VERSION has been renamed to CYPRESS_INSTALL_BINARY as of version ', '\n    ']),
    _templateObject10 = _taggedTemplateLiteral(['\n    You should set CYPRESS_INSTALL_BINARY instead.\n    '], ['\n    You should set CYPRESS_INSTALL_BINARY instead.\n    ']),
    _templateObject11 = _taggedTemplateLiteral(['\n    The environment variable CYPRESS_SKIP_BINARY_INSTALL has been removed as of version ', '\n    '], ['\n    The environment variable CYPRESS_SKIP_BINARY_INSTALL has been removed as of version ', '\n    ']),
    _templateObject12 = _taggedTemplateLiteral(['\n      To skip the binary install, set CYPRESS_INSTALL_BINARY=0\n    '], ['\n      To skip the binary install, set CYPRESS_INSTALL_BINARY=0\n    ']),
    _templateObject13 = _taggedTemplateLiteral(['\n    Platform: ', ' (', ')\n    Cypress Version: ', '\n  '], ['\n    Platform: ', ' (', ')\n    Cypress Version: ', '\n  ']),
    _templateObject14 = _taggedTemplateLiteral(['', ''], ['', '']);

function _taggedTemplateLiteral(strings, raw) { return Object.freeze(Object.defineProperties(strings, { raw: { value: Object.freeze(raw) } })); }

var os = require('os');
var chalk = require('chalk');

var _require = require('common-tags'),
    stripIndent = _require.stripIndent,
    stripIndents = _require.stripIndents;

var _require2 = require('ramda'),
    merge = _require2.merge;

var util = require('./util');
var state = require('./tasks/state');

var issuesUrl = 'https://github.com/cypress-io/cypress/issues';
var docsUrl = 'https://on.cypress.io';
var requiredDependenciesUrl = docsUrl + '/required-dependencies';

// common errors Cypress application can encounter
var failedDownload = {
  description: 'The Cypress App could not be downloaded.',
  solution: 'Please check network connectivity and try again:'
};

var failedUnzip = {
  description: 'The Cypress App could not be unzipped.',
  solution: stripIndent(_templateObject, chalk.blue(issuesUrl))
};

var missingApp = function missingApp(binaryDir) {
  return {
    description: 'No version of Cypress is installed in: ' + chalk.cyan(binaryDir),
    solution: stripIndent(_templateObject2, chalk.cyan('cypress install'))
  };
};

var binaryNotExecutable = function binaryNotExecutable(executable) {
  return {
    description: 'Cypress cannot run because the binary does not have executable permissions: ' + executable,
    solution: stripIndent(_templateObject3)
  };
};

var notInstalledCI = function notInstalledCI(executable) {
  return {
    description: 'The cypress npm package is installed, but the Cypress binary is missing.',
    solution: stripIndent(_templateObject4, chalk.cyan(executable), util.getCacheDir(), util.getCacheDir(), chalk.blue('https://on.cypress.io/not-installed-ci-error'))
  };
};

var nonZeroExitCodeXvfb = {
  description: 'XVFB exited with a non zero exit code.',
  solution: stripIndent(_templateObject5)
};

var missingXvfb = {
  description: 'Your system is missing the dependency: XVFB',
  solution: stripIndent(_templateObject6, chalk.blue(requiredDependenciesUrl))
};

var missingDependency = {
  description: 'Cypress failed to start.',
  // this message is too Linux specific
  solution: stripIndent(_templateObject7, chalk.blue(requiredDependenciesUrl))
};

var invalidCacheDirectory = {
  description: 'Cypress cannot write to the cache directory due to file permissions',
  solution: ''
};

var versionMismatch = {
  description: 'Installed version does not match package version.',
  solution: 'Install Cypress and verify app again'
};

var unexpected = {
  description: 'An unexpected error occurred while verifying the Cypress executable.',
  solution: stripIndent(_templateObject8, chalk.blue(docsUrl), chalk.blue(issuesUrl))
};

var removed = {
  CYPRESS_BINARY_VERSION: {
    description: stripIndent(_templateObject9, chalk.green('3.0.0')),
    solution: stripIndent(_templateObject10)
  },
  CYPRESS_SKIP_BINARY_INSTALL: {
    description: stripIndent(_templateObject11, chalk.green('3.0.0')),
    solution: stripIndent(_templateObject12)
  }
};

var CYPRESS_RUN_BINARY = {
  notValid: function notValid(value) {
    var properFormat = '**/' + state.getPlatformExecutable();

    return {
      description: 'Could not run binary set by environment variable CYPRESS_RUN_BINARY=' + value,
      solution: 'Ensure the environment variable is a path to the Cypress binary, matching ' + properFormat
    };
  }
};

function getPlatformInfo() {
  return util.getOsVersionAsync().then(function (version) {
    return stripIndent(_templateObject13, os.platform(), version, util.pkgVersion());
  });
}

function addPlatformInformation(info) {
  return getPlatformInfo().then(function (platform) {
    return merge(info, { platform: platform });
  });
}

function formErrorText(info, msg) {
  var hr = '----------';

  return addPlatformInformation(info).then(function (obj) {
    var formatted = [];

    function add(msg) {
      formatted.push(stripIndents(_templateObject14, msg));
    }

    add('\n      ' + obj.description + '\n\n      ' + obj.solution + '\n\n    ');

    if (msg) {
      add('\n        ' + hr + '\n\n        ' + msg + '\n\n      ');
    }

    add('\n      ' + hr + '\n\n      ' + obj.platform + '\n    ');

    if (obj.footer) {
      add('\n\n        ' + hr + '\n\n        ' + obj.footer + '\n      ');
    }

    return formatted.join('\n');
  });
}

var raise = function raise(text) {
  var err = new Error(text);

  err.known = true;
  throw err;
};

var throwFormErrorText = function throwFormErrorText(info) {
  return function (msg) {
    return formErrorText(info, msg).then(raise);
  };
};

module.exports = {
  raise: raise,
  // formError,
  formErrorText: formErrorText,
  throwFormErrorText: throwFormErrorText,
  errors: {
    nonZeroExitCodeXvfb: nonZeroExitCodeXvfb,
    missingXvfb: missingXvfb,
    missingApp: missingApp,
    notInstalledCI: notInstalledCI,
    missingDependency: missingDependency,
    versionMismatch: versionMismatch,
    binaryNotExecutable: binaryNotExecutable,
    unexpected: unexpected,
    failedDownload: failedDownload,
    failedUnzip: failedUnzip,
    invalidCacheDirectory: invalidCacheDirectory,
    removed: removed,
    CYPRESS_RUN_BINARY: CYPRESS_RUN_BINARY
  }
};