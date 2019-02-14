'use strict';

var la = require('lazy-ass');
var is = require('check-more-types');
var cp = require('child_process');
var os = require('os');
var yauzl = require('yauzl');
var debug = require('debug')('cypress:cli');
var extract = require('extract-zip');
var Promise = require('bluebird');
var readline = require('readline');

var _require = require('../errors'),
    throwFormErrorText = _require.throwFormErrorText,
    errors = _require.errors;

var fs = require('../fs');
var util = require('../util');

// expose this function for simple testing
var unzip = function unzip(_ref) {
  var zipFilePath = _ref.zipFilePath,
      installDir = _ref.installDir,
      progress = _ref.progress;


  debug('unzipping from %s', zipFilePath);
  debug('into', installDir);

  if (!zipFilePath) {
    throw new Error('Missing zip filename');
  }

  return fs.ensureDirAsync(installDir).then(function () {
    return new Promise(function (resolve, reject) {
      return yauzl.open(zipFilePath, function (err, zipFile) {
        if (err) return reject(err);

        // debug('zipfile.paths:', zipFile)
        // zipFile.on('entry', debug)
        // debug(zipFile.readEntry())
        var total = zipFile.entryCount;

        debug('zipFile entries count', total);

        var started = new Date();

        var percent = 0;
        var count = 0;

        var notify = function notify(percent) {
          var elapsed = +new Date() - +started;

          var eta = util.calculateEta(percent, elapsed);

          progress.onProgress(percent, util.secsRemaining(eta));
        };

        var tick = function tick() {
          count += 1;

          percent = count / total * 100;
          var displayPercent = percent.toFixed(0);

          return notify(displayPercent);
        };

        var unzipWithNode = function unzipWithNode() {
          var endFn = function endFn(err) {
            if (err) {
              return reject(err);
            }

            return resolve();
          };

          var opts = {
            dir: installDir,
            onEntry: tick
          };

          return extract(zipFilePath, opts, endFn);
        };

        //# we attempt to first unzip with the native osx
        //# ditto because its less likely to have problems
        //# with corruption, symlinks, or icons causing failures
        //# and can handle resource forks
        //# http://automatica.com.au/2011/02/unzip-mac-os-x-zip-in-terminal/
        var unzipWithOsx = function unzipWithOsx() {
          var copyingFileRe = /^copying file/;

          var sp = cp.spawn('ditto', ['-xkV', zipFilePath, installDir]);

          // f-it just unzip with node
          sp.on('error', unzipWithNode);

          sp.on('close', function (code) {
            if (code === 0) {
              // make sure we get to 100% on the progress bar
              // because reading in lines is not really accurate
              percent = 100;
              notify(percent);

              return resolve();
            }

            return unzipWithNode();
          });

          return readline.createInterface({
            input: sp.stderr
          }).on('line', function (line) {
            if (copyingFileRe.test(line)) {
              return tick();
            }
          });
        };

        switch (os.platform()) {
          case 'darwin':
            return unzipWithOsx();
          case 'linux':
          case 'win32':
            return unzipWithNode();
          default:
            return;
        }
      });
    });
  });
};

var start = function start(_ref2) {
  var zipFilePath = _ref2.zipFilePath,
      installDir = _ref2.installDir,
      progress = _ref2.progress;

  la(is.unemptyString(installDir), 'missing installDir');
  if (!progress) {
    progress = { onProgress: function onProgress() {
        return {};
      } };
  }

  return fs.pathExists(installDir).then(function (exists) {
    if (exists) {
      debug('removing existing unzipped binary', installDir);

      return fs.removeAsync(installDir);
    }
  }).then(function () {
    return unzip({ zipFilePath: zipFilePath, installDir: installDir, progress: progress });
  }).catch(throwFormErrorText(errors.failedUnzip));
};

module.exports = {
  start: start
};