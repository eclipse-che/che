/*********************************************************************
 * Copyright (c) 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import * as mocha from 'mocha';
import { Driver } from './Driver';
import { e2eContainer } from '../inversify.config';
import { TYPES } from '../types';
import * as fs from 'fs';

const driver: Driver = e2eContainer.get(TYPES.Driver);

class CheReporter extends mocha.reporters.Spec {

  constructor(runner: mocha.Runner, options: mocha.MochaOptions) {
    super(runner, options);

    runner.on('end', async function (test: mocha.Test) {
      await driver.get().quit().catch(err => { throw err })
    })


    runner.on('fail', async function (test: mocha.Test) {

      const reportDirPath: string = './report'
      const testFullTitle: string = test.fullTitle().replace(/\s/g, '_')
      const testTitle: string = test.title.replace(/\s/g, '_')

      const testReportDirPath: string = `${reportDirPath}/${testFullTitle}`
      const screenshotFileName: string = `${testReportDirPath}/screenshot-${testTitle}.png`

      //create reporter dir if not exist
      await fs.exists(reportDirPath, async isDirExist => {
        if (!isDirExist) {
          await fs.mkdir(reportDirPath, err => {
            if (err) {
              throw err
            }
          })
        }
      })

      //create dir for collected data if not exist
      await fs.exists(testReportDirPath, async isDirExist => {
        if (!isDirExist) {
          await fs.mkdir(testReportDirPath, err => {
            if (err) {
              throw err
            }
          })
        }
      })

      //take screenshot and write to file
      const screenshot = await driver.get().takeScreenshot().catch(err => { throw err });
      const screenshotStream = fs.createWriteStream(screenshotFileName)
      screenshotStream.write(new Buffer(screenshot, 'base64'))
      screenshotStream.end()
    })
  }
}


module.exports = CheReporter;

