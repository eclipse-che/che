'use strict';

var _ = require('lodash');
var os = require('os');
var path = require('path');
var debug = require('debug')('cypress:cli');

var fs = require('../fs');
var util = require('../util');

var getPlatformExecutable = function getPlatformExecutable() {
  var platform = os.platform();

  switch (platform) {
    case 'darwin':
      return 'Contents/MacOS/Cypress';
    case 'linux':
      return 'Cypress';
    case 'win32':
      return 'Cypress.exe';
    // TODO handle this error using our standard
    default:
      throw new Error('Platform: "' + platform + '" is not supported.');
  }
};

var getPlatFormBinaryFolder = function getPlatFormBinaryFolder() {
  var platform = os.platform();

  switch (platform) {
    case 'darwin':
      return 'Cypress.app';
    case 'linux':
      return 'Cypress';
    case 'win32':
      return 'Cypress';
    // TODO handle this error using our standard
    default:
      throw new Error('Platform: "' + platform + '" is not supported.');
  }
};

var getBinaryPkgPath = function getBinaryPkgPath(binaryDir) {
  var platform = os.platform();

  switch (platform) {
    case 'darwin':
      return path.join(binaryDir, 'Contents', 'Resources', 'app', 'package.json');
    case 'linux':
      return path.join(binaryDir, 'resources', 'app', 'package.json');
    case 'win32':
      return path.join(binaryDir, 'resources', 'app', 'package.json');
    // TODO handle this error using our standard
    default:
      throw new Error('Platform: "' + platform + '" is not supported.');
  }
};

/**
 * Get path to binary directory
*/
var getBinaryDir = function getBinaryDir() {
  var version = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : util.pkgVersion();

  return path.join(getVersionDir(version), getPlatFormBinaryFolder());
};

var getVersionDir = function getVersionDir() {
  var version = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : util.pkgVersion();

  return path.join(getCacheDir(), version);
};

var getCacheDir = function getCacheDir() {
  var cache_directory = util.getCacheDir();

  if (util.getEnv('CYPRESS_CACHE_FOLDER')) {
    var envVarCacheDir = util.getEnv('CYPRESS_CACHE_FOLDER');

    debug('using environment variable CYPRESS_CACHE_FOLDER %s', envVarCacheDir);
    cache_directory = path.resolve(envVarCacheDir);
  }

  return cache_directory;
};

var parseRealPlatformBinaryFolderAsync = function parseRealPlatformBinaryFolderAsync(binaryPath) {
  return fs.realpathAsync(binaryPath).then(function (realPath) {
    debug('CYPRESS_RUN_BINARY has realpath:', realPath);
    if (!realPath.toString().endsWith(getPlatformExecutable())) {
      return false;
    }

    if (os.platform() === 'darwin') {
      return path.resolve(realPath, '..', '..', '..');
    }

    return path.resolve(realPath, '..');
  });
};

var getDistDir = function getDistDir() {
  return path.join(__dirname, '..', '..', 'dist');
};

var getBinaryStatePath = function getBinaryStatePath(binaryDir) {
  return path.join(binaryDir, 'binary_state.json');
};

var getBinaryStateContentsAsync = function getBinaryStateContentsAsync(binaryDir) {
  return fs.readJsonAsync(getBinaryStatePath(binaryDir)).catch({ code: 'ENOENT' }, SyntaxError, function () {
    debug('could not read binary_state.json file');

    return {};
  });
};

var getBinaryVerifiedAsync = function getBinaryVerifiedAsync(binaryDir) {
  return getBinaryStateContentsAsync(binaryDir).tap(debug).get('verified');
};

var clearBinaryStateAsync = function clearBinaryStateAsync(binaryDir) {
  return fs.removeAsync(getBinaryStatePath(binaryDir));
};

/**
 * @param {boolean} verified
 */
var writeBinaryVerifiedAsync = function writeBinaryVerifiedAsync(verified, binaryDir) {
  return getBinaryStateContentsAsync(binaryDir).then(function (contents) {
    return fs.outputJsonAsync(getBinaryStatePath(binaryDir), _.extend(contents, { verified: verified }), { spaces: 2 });
  });
};

var getPathToExecutable = function getPathToExecutable(binaryDir) {
  return path.join(binaryDir, getPlatformExecutable());
};

var getBinaryPkgVersionAsync = function getBinaryPkgVersionAsync(binaryDir) {
  var pathToPackageJson = getBinaryPkgPath(binaryDir);

  debug('Reading binary package.json from:', pathToPackageJson);

  return fs.pathExistsAsync(pathToPackageJson).then(function (exists) {
    if (!exists) {
      return null;
    }

    return fs.readJsonAsync(pathToPackageJson).get('version');
  });
};

module.exports = {
  getPathToExecutable: getPathToExecutable,
  getPlatformExecutable: getPlatformExecutable,
  getBinaryPkgVersionAsync: getBinaryPkgVersionAsync,
  getBinaryVerifiedAsync: getBinaryVerifiedAsync,
  getBinaryPkgPath: getBinaryPkgPath,
  getBinaryDir: getBinaryDir,
  getCacheDir: getCacheDir,
  clearBinaryStateAsync: clearBinaryStateAsync,
  writeBinaryVerifiedAsync: writeBinaryVerifiedAsync,
  parseRealPlatformBinaryFolderAsync: parseRealPlatformBinaryFolderAsync,
  getDistDir: getDistDir,
  getVersionDir: getVersionDir
};