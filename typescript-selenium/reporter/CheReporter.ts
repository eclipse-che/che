/*********************************************************************
 * Copyright (c) 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import * as mocha from 'mocha'
import { Driver } from '../driver/Driver';
import { e2eContainer } from '../inversify.config';
import { TYPES } from '../types';
import * as fs from 'fs'
import * as path from 'path'
import { inject, injectable } from 'inversify';
import { types } from 'util';
import { ThenableWebDriver } from 'selenium-webdriver';

const driver: Driver = e2eContainer.get(TYPES.Driver);

class CheReporter extends mocha.reporters.Spec {
  
  constructor(runner: mocha.Runner, options: mocha.MochaOptions) {
    super(runner, options);

    runner.on('fail', async function (test) {
      const fileName: string = `screenshot-${test.title}.png`

      let screenshot = await driver.get().takeScreenshot().catch(err => {
          throw err
      });
  
      let stream = fs.createWriteStream(fileName)
  
      stream.write(new Buffer(screenshot, 'base64'))
      stream.end()
  
      console.log("====>>>> The ", fileName, " file saved")
      return
      })
    }
  }


//   async saveScreenshot(test: mocha.Test): Promise<void> {

//     const fileName: string = `screenshot-${test.title}.png`

//     let screenshot = await this.driver.takeScreenshot().catch(err => {
//         throw err
//     });

//     let stream = fs.createWriteStream(fileName)

//     stream.write(new Buffer(screenshot, 'base64'))
//     stream.end()

//     console.log("====>>>> The ", fileName, " file saved")
//     return
// }


module.exports = CheReporter;

