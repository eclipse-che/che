var os = require('os')
var path = require('path')
var homedir = require('os-homedir')

function posix (id) {
  var cacheHome = process.env.XDG_CACHE_HOME || path.join(homedir(), '.cache')
  return path.join(cacheHome, id)
}

function darwin (id) {
  return path.join(homedir(), 'Library', 'Caches', id)
}

function win32 (id) {
  var appData = process.env.LOCALAPPDATA || path.join(homedir(), 'AppData', 'Local')
  return path.join(appData, id, 'Cache')
}

var implementation = (function () {
  switch (os.platform()) {
    case 'android': return posix
    case 'darwin': return darwin
    case 'freebsd': return posix
    case 'linux': return posix
    case 'win32': return win32
    default: throw new Error('Your OS "' + os.platform() + '" is currently not supported by node-cachedir.')
  }
}())

module.exports = function (id) {
  if (typeof id !== 'string') {
    throw new TypeError('id is not a string')
  }
  if (id.length === 0) {
    throw new Error('id cannot be empty')
  }
  if (/[^0-9a-zA-Z-]/.test(id)) {
    throw new Error('id cannot contain special characters')
  }

  return implementation(id)
}
