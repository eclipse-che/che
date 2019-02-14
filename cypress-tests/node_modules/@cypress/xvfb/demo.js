'use strict'

const Xvfb = require('.')
const Promise = require('bluebird')
// debug log messages from XVFB process
const debugXvfb = require('debug')('xvfb-process')

if (debugXvfb.enabled) {
  console.log('XVFB process error stream enabled')
}

function startStop () {
  const xvfb = Promise.promisifyAll(
    new Xvfb({
      timeout: 10000,
      onStderrData (data) {
        if (debugXvfb.enabled) {
          debugXvfb(data.toString())
        }
      },
    })
  )

  const retryLimit = 0
  const retryStart = (i = 0) => {
    return xvfb.startAsync().catch({ timedOut: true }, (e) => {
      console.log('Timed out', e.message)
      if (i < retryLimit) {
        return retryStart(i + 1)
      }
      throw e
    })
  }

  const retryStop = (i = 0) => {
    return xvfb.stopAsync().catch({ timedOut: true }, (e) => {
      console.log('Timed out stopping', e.message)
      if (i < retryLimit) {
        return retryStop(i + 1)
      }
      throw e
    })
  }

  return retryStart()
  .catch((err) => {
    console.error('error starting XVFB')
    console.error(err)
    process.exit(1)
  })
  .then((xvfbProcess) => {
    console.log('XVFB started', xvfbProcess.pid)
  })
  .delay(2000)
  .then(retryStop)
  .then(() => {
    console.log('xvfb stopped')
  })
  .catch((err) => {
    console.error('error stopping XVFB')
    console.error(err)
    process.exit(2)
  })
}

function testNprocs (N = 1) {
  console.log('testing %d procs STARTS NOW', N)
  const procs = []
  for (let k = 0; k < N; k += 1) {
    procs.push(startStop())
  }
  return Promise.all(procs).then(() => {
    console.log('all %d procs done', N)
    console.log('******')
  })
}

Promise.mapSeries([1, 2, 3, 4, 5, 6, 7, 8, 9, 10], () => testNprocs(1)).then(
  () => {
    console.log('all demo procs finished')
  },
  (err) => {
    console.error('err', err)
    process.exit(3)
  }
)
