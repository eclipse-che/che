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
import { Driver } from '../driver/Driver';
import { e2eContainer } from '../inversify.config';
import { TYPES } from '../types';
import * as fs from 'fs';
import * as path from 'path';
import { inject, injectable } from 'inversify';
import { types } from 'util';
import { ThenableWebDriver } from 'selenium-webdriver';

const driver: Driver = e2eContainer.get(TYPES.Driver);

class CheReporter extends mocha.reporters.Spec {

  constructor(runner: mocha.Runner, options: mocha.MochaOptions) {
    super(runner, options);




    runner.on('fail', async function (test: mocha.Test) {
      
      const reportDirPath: string = './report'
      const testFullTitle: string = test.fullTitle().replace(/\s/g, '_')
      const testTitle: string = test.title.replace(/\s/g, '_')

      const testReportDirPath: string = `${reportDirPath}/${testFullTitle}`
      const fileName: string = `${testReportDirPath}/screenshot-${testTitle}.png`

      await fs.exists(reportDirPath, async isDirExist => {
        if (!isDirExist) {
          await fs.mkdir(reportDirPath, err => {
            if (err) {
              throw err
            }
          })
        }
      })

      await fs.exists(testReportDirPath, async isDirExist => {
        if (!isDirExist) {
          await fs.mkdir(testReportDirPath, err => {
            if (err) {
              throw err
            }
          })
        }
      })

      let screenshot = await driver.get().takeScreenshot().catch(err => {
        throw err
      });

      let stream = fs.createWriteStream(fileName)

      stream.write(new Buffer(screenshot, 'base64'))
      stream.end()
    })
  }
}


module.exports = CheReporter;

