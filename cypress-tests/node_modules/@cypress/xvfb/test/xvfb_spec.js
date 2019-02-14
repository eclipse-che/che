const { expect } = require('chai')

const Xvfb = require('../')

describe('xvfb', function () {
  context('onStderrData', function () {
    it('accepts callback function', function () {
      const cb = () => {}

      const xvfb = new Xvfb({
        onStderrData: cb,
      })

      expect(xvfb._onStderrData).to.eq(cb)
    })

    it('sets default function otherwise', function () {
      const xvfb = new Xvfb()

      expect(xvfb._onStderrData).to.be.a('function')
    })
  })

  context('issue: #1', function () {
    beforeEach(function () {
      this.xvfb = new Xvfb()
    })

    it('issue #1: does not mutate process.env.DISPLAY', function () {
      delete process.env.DISPLAY

      expect(process.env.DISPLAY).to.be.undefined

      this.xvfb._setDisplayEnvVariable()
      this.xvfb._restoreDisplayEnvVariable()

      expect(process.env.DISPLAY).to.be.undefined
    })
  })
})
