'use strict';

var os = require('os');
var Promise = require('bluebird');
var Xvfb = require('@cypress/xvfb');
var R = require('ramda');
var debug = require('debug')('cypress:cli');
var debugXvfb = require('debug')('cypress:xvfb');

var _require = require('../errors'),
    throwFormErrorText = _require.throwFormErrorText,
    errors = _require.errors;

var xvfb = Promise.promisifyAll(new Xvfb({
  timeout: 5000, // milliseconds
  onStderrData: function onStderrData(data) {
    if (debugXvfb.enabled) {
      debugXvfb(data.toString());
    }
  }
}));

module.exports = {
  _debugXvfb: debugXvfb, // expose for testing

  _xvfb: xvfb, // expose for testing

  start: function start() {
    debug('Starting XVFB');

    return xvfb.startAsync().catch({ nonZeroExitCode: true }, throwFormErrorText(errors.nonZeroExitCodeXvfb)).catch(function (err) {
      if (err.known) {
        throw err;
      }

      return throwFormErrorText(errors.missingXvfb)(err);
    });
  },
  stop: function stop() {
    debug('Stopping XVFB');

    return xvfb.stopAsync();
  },
  isNeeded: function isNeeded() {
    return os.platform() === 'linux' && !process.env.DISPLAY;
  },


  // async method, resolved with Boolean
  verify: function verify() {
    return xvfb.startAsync().then(R.T).catch(function (err) {
      debug('Could not verify xvfb: %s', err.message);

      return false;
    }).finally(xvfb.stopAsync);
  }
};