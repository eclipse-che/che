'use strict';

var state = require('./state');
var logger = require('../logger');
var fs = require('../fs');
var util = require('../util');

var path = function path() {
  logger.log(state.getCacheDir());

  return undefined;
};

var clear = function clear() {
  return fs.removeAsync(state.getCacheDir());
};

var list = function list() {
  return fs.readdirAsync(state.getCacheDir()).filter(util.isSemver).then(function (versions) {
    logger.log(versions.join(', '));
  });
};

module.exports = {
  path: path,
  clear: clear,
  list: list
};