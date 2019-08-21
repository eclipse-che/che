const fs = require('fs');
const path = require('path');
const tcpPortUsed = require('tcp-port-used');
function getPortFromArgs(args) {
  let port = 9515;
  if (!args){
    return port;
  }
  const portRegexp = /--port=(\d*)/;
  const portArg = args.find(function (arg) {
    return portRegexp.test(arg);
  });
  if (portArg){
    port = parseInt(portRegexp.exec(portArg)[1]);
  }
  return port;
}
process.env.PATH = path.join(__dirname, 'chromedriver') + path.delimiter + process.env.PATH;
exports.path = process.platform === 'win32' ? path.join(__dirname, 'chromedriver', 'chromedriver.exe') : path.join(__dirname, 'chromedriver', 'chromedriver');
exports.version = '76.0.3809.68';
exports.start = function(args, returnPromise) {
  let command = exports.path;
  if (!fs.existsSync(command)) {
    console.log('Could not find chromedriver in default path: ', command);
    console.log('Falling back to use global chromedriver bin');
    command = process.platform === 'win32' ? 'chromedriver.exe' : 'chromedriver';
  }
  const cp = require('child_process').spawn(command, args);
  cp.stdout.pipe(process.stdout);
  cp.stderr.pipe(process.stderr);
  exports.defaultInstance = cp;
  if (!returnPromise) {
    return cp;
  }
  const port = getPortFromArgs(args);
  const pollInterval = 100;
  const timeout = 10000;
  return tcpPortUsed.waitUntilUsed(port, pollInterval, timeout)
    .then(function () {
      return cp;
    });
};
exports.stop = function () {
  if (exports.defaultInstance != null){
    exports.defaultInstance.kill();
  }
};
