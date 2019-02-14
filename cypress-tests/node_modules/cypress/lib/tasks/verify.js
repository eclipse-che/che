'use strict';

var _templateObject = _taggedTemplateLiteral(['\n      Cypress executable not found at: ', '\n    '], ['\n      Cypress executable not found at: ', '\n    ']),
    _templateObject2 = _taggedTemplateLiteral(['\n          Smoke failed.\n\n          Command was: ', '\n\n          Returned: ', '\n        '], ['\n          Smoke failed.\n\n          Command was: ', '\n\n          Returned: ', '\n        ']),
    _templateObject3 = _taggedTemplateLiteral(['\n  It looks like this is your first time using Cypress: ', '\n  '], ['\n  It looks like this is your first time using Cypress: ', '\n  ']),
    _templateObject4 = _taggedTemplateLiteral(['\n        ', ' You have set the environment variable: ', '', ':\n        \n              This overrides the default Cypress binary path used.\n        '], ['\n        ', ' You have set the environment variable: ', '', ':\n        \n              This overrides the default Cypress binary path used.\n        ']),
    _templateObject5 = _taggedTemplateLiteral(['\n          The supplied binary path is not executable\n          '], ['\n          The supplied binary path is not executable\n          ']),
    _templateObject6 = _taggedTemplateLiteral(['\n      \n      \n      ', ' Warning: Binary version ', ' does not match the expected package version ', '\n\n        These versions may not work properly together.\n      '], ['\n      \n      \n      ', ' Warning: Binary version ', ' does not match the expected package version ', '\n\n        These versions may not work properly together.\n      ']);

function _taggedTemplateLiteral(strings, raw) { return Object.freeze(Object.defineProperties(strings, { raw: { value: Object.freeze(raw) } })); }

var _ = require('lodash');
var chalk = require('chalk');
var Listr = require('listr');
var debug = require('debug')('cypress:cli');
var verbose = require('@cypress/listr-verbose-renderer');

var _require = require('common-tags'),
    stripIndent = _require.stripIndent;

var Promise = require('bluebird');
var logSymbols = require('log-symbols');

var _require2 = require('../errors'),
    throwFormErrorText = _require2.throwFormErrorText,
    errors = _require2.errors;

var util = require('../util');
var logger = require('../logger');
var xvfb = require('../exec/xvfb');
var state = require('./state');

var checkExecutable = function checkExecutable(binaryDir) {
  var executable = state.getPathToExecutable(binaryDir);

  debug('checking if executable exists', executable);

  return util.isExecutableAsync(executable).then(function (isExecutable) {
    debug('Binary is executable? :', isExecutable);
    if (!isExecutable) {
      return throwFormErrorText(errors.binaryNotExecutable(executable))();
    }
  }).catch({ code: 'ENOENT' }, function () {
    if (util.isCi()) {
      return throwFormErrorText(errors.notInstalledCI(executable))();
    }

    return throwFormErrorText(errors.missingApp(binaryDir))(stripIndent(_templateObject, chalk.cyan(executable)));
  });
};

var runSmokeTest = function runSmokeTest(binaryDir) {
  debug('running smoke test');
  var cypressExecPath = state.getPathToExecutable(binaryDir);

  debug('using Cypress executable %s', cypressExecPath);

  var onXvfbError = function onXvfbError(err) {
    debug('caught xvfb error %s', err.message);

    return throwFormErrorText(errors.missingXvfb)('Caught error trying to run XVFB: "' + err.message + '"');
  };

  var onSmokeTestError = function onSmokeTestError(err) {
    debug('Smoke test failed:', err);

    return throwFormErrorText(errors.missingDependency)(err.stderr || err.message);
  };

  var needsXvfb = xvfb.isNeeded();

  debug('needs XVFB?', needsXvfb);

  var spawn = function spawn() {
    var random = _.random(0, 1000);
    var args = ['--smoke-test', '--ping=' + random];
    var smokeTestCommand = cypressExecPath + ' ' + args.join(' ');

    debug('smoke test command:', smokeTestCommand);

    return Promise.resolve(util.exec(cypressExecPath, args)).catch(onSmokeTestError).then(function (result) {
      var smokeTestReturned = result.stdout;

      debug('smoke test stdout "%s"', smokeTestReturned);

      if (!util.stdoutLineMatches(String(random), smokeTestReturned)) {
        debug('Smoke test failed:', result);
        throw new Error(stripIndent(_templateObject2, smokeTestCommand, smokeTestReturned));
      }
    });
  };

  if (needsXvfb) {
    return xvfb.start().catch(onXvfbError).then(spawn).finally(function () {
      return xvfb.stop().catch(onXvfbError);
    });
  }

  return spawn();
};

function testBinary(version, binaryDir) {
  debug('running binary verification check', version);

  logger.log(stripIndent(_templateObject3, chalk.cyan(version)));

  logger.log();

  // if we are running in CI then use
  // the verbose renderer else use
  // the default
  var renderer = util.isCi() ? verbose : 'default';

  if (logger.logLevel() === 'silent') renderer = 'silent';

  var rendererOptions = {
    renderer: renderer
  };

  var tasks = new Listr([{
    title: util.titleize('Verifying Cypress can run', chalk.gray(binaryDir)),
    task: function task(ctx, _task) {
      debug('clearing out the verified version');

      return state.clearBinaryStateAsync(binaryDir).then(function () {
        return Promise.all([runSmokeTest(binaryDir), Promise.resolve().delay(1500)] // good user experience
        );
      }).then(function () {
        debug('write verified: true');

        return state.writeBinaryVerifiedAsync(true, binaryDir);
      }).then(function () {
        util.setTaskTitle(_task, util.titleize(chalk.green('Verified Cypress!'), chalk.gray(binaryDir)), rendererOptions.renderer);
      });
    }
  }], rendererOptions);

  return tasks.run();
}

var maybeVerify = function maybeVerify(installedVersion, binaryDir) {
  var options = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : {};

  return state.getBinaryVerifiedAsync(binaryDir).then(function (isVerified) {

    debug('is Verified ?', isVerified);

    var shouldVerify = !isVerified;

    // force verify if options.force
    if (options.force) {
      debug('force verify');
      shouldVerify = true;
    }

    if (shouldVerify) {
      return testBinary(installedVersion, binaryDir).then(function () {
        if (options.welcomeMessage) {
          logger.log();
          logger.log('Opening Cypress...');
        }
      });
    }
  });
};

var start = function start() {
  var options = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};

  debug('verifying Cypress app');

  var packageVersion = util.pkgVersion();
  var binaryDir = state.getBinaryDir(packageVersion);

  _.defaults(options, {
    force: false,
    welcomeMessage: true
  });

  var parseBinaryEnvVar = function parseBinaryEnvVar() {
    var envBinaryPath = util.getEnv('CYPRESS_RUN_BINARY');

    debug('CYPRESS_RUN_BINARY exists, =', envBinaryPath);
    logger.log(stripIndent(_templateObject4, chalk.yellow('Note:'), chalk.white('CYPRESS_RUN_BINARY='), chalk.cyan(envBinaryPath)));
    logger.log();

    return util.isExecutableAsync(envBinaryPath).then(function (isExecutable) {
      debug('CYPRESS_RUN_BINARY is executable? :', isExecutable);
      if (!isExecutable) {
        return throwFormErrorText(errors.CYPRESS_RUN_BINARY.notValid(envBinaryPath))(stripIndent(_templateObject5));
      }
    }).then(function () {
      return state.parseRealPlatformBinaryFolderAsync(envBinaryPath);
    }).then(function (envBinaryDir) {
      if (!envBinaryDir) {
        return throwFormErrorText(errors.CYPRESS_RUN_BINARY.notValid(envBinaryPath))();
      }

      debug('CYPRESS_RUN_BINARY has binaryDir:', envBinaryDir);

      binaryDir = envBinaryDir;
    }).catch({ code: 'ENOENT' }, function (err) {
      return throwFormErrorText(errors.CYPRESS_RUN_BINARY.notValid(envBinaryPath))(err.message);
    });
  };

  return Promise.try(function () {
    debug('checking environment variables');
    if (util.getEnv('CYPRESS_RUN_BINARY')) {
      return parseBinaryEnvVar();
    }
  }).then(function () {
    return checkExecutable(binaryDir);
  }).tap(function () {
    return debug('binaryDir is ', binaryDir);
  }).then(function () {
    return state.getBinaryPkgVersionAsync(binaryDir);
  }).then(function (binaryVersion) {

    if (!binaryVersion) {
      debug('no Cypress binary found for cli version ', packageVersion);

      return throwFormErrorText(errors.missingApp(binaryDir))('\n      Cannot read binary version from: ' + chalk.cyan(state.getBinaryPkgPath(binaryDir)) + '\n    ');
    }

    debug('Found binary version ' + chalk.green(binaryVersion) + ' installed in: ' + chalk.cyan(binaryDir));

    if (binaryVersion !== packageVersion) {
      // warn if we installed with CYPRESS_INSTALL_BINARY or changed version
      // in the package.json
      logger.log('Found binary version ' + chalk.green(binaryVersion) + ' installed in: ' + chalk.cyan(binaryDir));
      logger.log();
      logger.warn(stripIndent(_templateObject6, logSymbols.warning, chalk.green(binaryVersion), chalk.green(packageVersion)));

      logger.log();
    }

    return maybeVerify(binaryVersion, binaryDir, options);
  }).catch(function (err) {
    if (err.known) {
      throw err;
    }

    return throwFormErrorText(errors.unexpected)(err.stack);
  });
};

module.exports = {
  start: start,
  maybeVerify: maybeVerify
};