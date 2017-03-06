'use strict';

var paths = {
      src: 'src',
      dist: 'dist',
      tmp: '.tmp',
      e2e: 'e2e'
    },
    HtmlScreenshotReporter = require('protractor-jasmine2-screenshot-reporter'),
    reporter = new HtmlScreenshotReporter({
      dest: 'target/protractor-tests-report',
      filename: 'report.html',
      showQuickLinks: true,
      reportOnlyFailedSpecs: false,
      captureOnlyFailedSpecs: true,
      pathBuilder: function(currentSpec, suites, browserCapabilities) {
        // will return chrome/your-spec-name.png
        return browserCapabilities.get('browserName') + '/' + currentSpec.fullName;
      }
    });


// An example configuration file.
exports.config = {
  // The address of a running selenium server.
  //seleniumAddress: 'http://localhost:4444/wd/hub',
  //seleniumServerJar: deprecated, this should be set on node_modules/protractor/config.json

  // Capabilities to be passed to the webdriver instance.
  capabilities: {
    'browserName': 'chrome',
    'chromeOptions': {
      args: ['--lang=en',
        '--window-size=1280,800']
    },

    // uncomment lines below if you want to run more than one browser instance and share tests between them.
    // it may cause test failures!
    //'shardTestFiles': true,
    //'maxInstances': 2
  },

  baseUrl: 'http://localhost:3000',

  // Spec patterns are relative to the current working directory when
  // protractor is called.
  specs: [paths.e2e + '/**/*.js'],

  // Options to be passed to Jasmine-node.
  jasmineNodeOpts: {
    showColors: true,
    defaultTimeoutInterval: 30000
  },

  // Setup the report before any tests start
  beforeLaunch: function () {
    return new Promise(function (resolve) {
      reporter.beforeLaunch(resolve);
    });
  },

  // Assign the test reporter to each running instance
  onPrepare: function () {
    jasmine.getEnv().addReporter(reporter);
  },

  // Close the report after all tests finish
  afterLaunch: function (exitCode) {
    return new Promise(function (resolve) {
      reporter.afterLaunch(resolve.bind(this, exitCode));
    });
  }

};
