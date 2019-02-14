'use strict';

var _arguments = arguments;

var _templateObject = _taggedTemplateLiteral(['\n    URL: ', '\n    ', '\n  '], ['\n    URL: ', '\n    ', '\n  ']),
    _templateObject2 = _taggedTemplateLiteral(['\n          Failed downloading the Cypress binary.\n          Response code: ', '\n          Response message: ', '\n        '], ['\n          Failed downloading the Cypress binary.\n          Response code: ', '\n          Response message: ', '\n        ']);

function _taggedTemplateLiteral(strings, raw) { return Object.freeze(Object.defineProperties(strings, { raw: { value: Object.freeze(raw) } })); }

var la = require('lazy-ass');
var is = require('check-more-types');
var os = require('os');
var url = require('url');
var path = require('path');
var debug = require('debug')('cypress:cli');
var request = require('request');
var Promise = require('bluebird');
var requestProgress = require('request-progress');

var _require = require('common-tags'),
    stripIndent = _require.stripIndent;

var _require2 = require('../errors'),
    throwFormErrorText = _require2.throwFormErrorText,
    errors = _require2.errors;

var fs = require('../fs');
var util = require('../util');

var defaultBaseUrl = 'https://download.cypress.io/';

var getBaseUrl = function getBaseUrl() {
  if (util.getEnv('CYPRESS_DOWNLOAD_MIRROR')) {
    var baseUrl = util.getEnv('CYPRESS_DOWNLOAD_MIRROR');

    if (!baseUrl.endsWith('/')) {
      baseUrl += '/';
    }

    return baseUrl;
  }

  return defaultBaseUrl;
};

var prepend = function prepend(urlPath) {
  var endpoint = url.resolve(getBaseUrl(), urlPath);
  var platform = os.platform();
  var arch = os.arch();

  return endpoint + '?platform=' + platform + '&arch=' + arch;
};

var getUrl = function getUrl(version) {
  if (is.url(version)) {
    debug('version is already an url', version);

    return version;
  }

  return version ? prepend('desktop/' + version) : prepend('desktop');
};

var statusMessage = function statusMessage(err) {
  return err.statusCode ? [err.statusCode, err.statusMessage].join(' - ') : err.toString();
};

var prettyDownloadErr = function prettyDownloadErr(err, version) {
  var msg = stripIndent(_templateObject, getUrl(version), statusMessage(err));

  debug(msg);

  return throwFormErrorText(errors.failedDownload)(msg);
};

// downloads from given url
// return an object with
// {filename: ..., downloaded: true}
var downloadFromUrl = function downloadFromUrl(_ref) {
  var url = _ref.url,
      downloadDestination = _ref.downloadDestination,
      progress = _ref.progress;

  return new Promise(function (resolve, reject) {
    debug('Downloading from', url);
    debug('Saving file to', downloadDestination);

    var redirectVersion = void 0;

    var req = request({
      url: url,
      followRedirect: function followRedirect(response) {
        var version = response.headers['x-version'];

        debug('redirect version:', version);
        if (version) {
          // set the version in options if we have one.
          // this insulates us from potential redirect
          // problems where version would be set to undefined.
          redirectVersion = version;
        }

        // yes redirect
        return true;
      }
    });

    // closure
    var started = null;

    requestProgress(req, {
      throttle: progress.throttle
    }).on('response', function (response) {
      // start counting now once we've gotten
      // response headers
      started = new Date();

      // if our status code does not start with 200
      if (!/^2/.test(response.statusCode)) {
        debug('response code %d', response.statusCode);

        var err = new Error(stripIndent(_templateObject2, response.statusCode, response.statusMessage));

        reject(err);
      }
    }).on('error', reject).on('progress', function (state) {
      // total time we've elapsed
      // starting on our first progress notification
      var elapsed = new Date() - started;

      var eta = util.calculateEta(state.percent, elapsed);

      // send up our percent and seconds remaining
      progress.onProgress(state.percent, util.secsRemaining(eta));
    })
    // save this download here
    .pipe(fs.createWriteStream(downloadDestination)).on('finish', function () {
      debug('downloading finished');

      resolve(redirectVersion);
    });
  });
};

var start = function start(_ref2) {
  var version = _ref2.version,
      downloadDestination = _ref2.downloadDestination,
      progress = _ref2.progress;

  if (!downloadDestination) {
    la(is.unemptyString(downloadDestination), 'missing download dir', _arguments);
  }

  if (!progress) {
    progress = { onProgress: function onProgress() {
        return {};
      } };
  }

  var url = getUrl(version);

  progress.throttle = 100;

  debug('needed Cypress version: %s', version);
  debug('downloading cypress.zip to "' + downloadDestination + '"');

  // ensure download dir exists
  return fs.ensureDirAsync(path.dirname(downloadDestination)).then(function () {
    return downloadFromUrl({ url: url, downloadDestination: downloadDestination, progress: progress });
  }).catch(function (err) {
    return prettyDownloadErr(err, version);
  });
};

module.exports = {
  start: start,
  getUrl: getUrl
};