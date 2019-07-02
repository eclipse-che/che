/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import * as mocha from 'mocha';
import { IDriver } from './IDriver';
import { e2eContainer } from '../inversify.config';
import { TYPES, CLASSES } from '../inversify.types';
import * as fs from 'fs';
import { TestConstants } from '../TestConstants';
import { logging } from 'selenium-webdriver';
import { DriverHelper } from '../utils/DriverHelper';

const driver: IDriver = e2eContainer.get(TYPES.Driver);
const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);

class CheReporter extends mocha.reporters.Spec {

  constructor(runner: mocha.Runner, options: mocha.MochaOptions) {
    super(runner, options);

    runner.on('start', async (test: mocha.Test) => {
      const launchInformation: string =
        `################## Launch Information ##################

      TS_SELENIUM_BASE_URL: ${TestConstants.TS_SELENIUM_BASE_URL}
      TS_SELENIUM_HEADLESS: ${TestConstants.TS_SELENIUM_HEADLESS}

      TS_SELENIUM_DEFAULT_ATTEMPTS: ${TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS}
      TS_SELENIUM_DEFAULT_POLLING: ${TestConstants.TS_SELENIUM_DEFAULT_POLLING}
      TS_SELENIUM_DEFAULT_TIMEOUT: ${TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT}

      TS_SELENIUM_LANGUAGE_SERVER_START_TIMEOUT: ${TestConstants.TS_SELENIUM_LANGUAGE_SERVER_START_TIMEOUT}
      TS_SELENIUM_LOAD_PAGE_TIMEOUT: ${TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT}
      TS_SELENIUM_START_WORKSPACE_TIMEOUT: ${TestConstants.TS_SELENIUM_START_WORKSPACE_TIMEOUT}
      TS_SELENIUM_WORKSPACE_STATUS_ATTEMPTS: ${TestConstants.TS_SELENIUM_WORKSPACE_STATUS_ATTEMPTS}
      TS_SELENIUM_WORKSPACE_STATUS_POLLING: ${TestConstants.TS_SELENIUM_WORKSPACE_STATUS_POLLING}
      TS_SELENIUM_PLUGIN_PRECENCE_ATTEMPTS: ${TestConstants.TS_SELENIUM_PLUGIN_PRECENCE_ATTEMPTS}
      TS_SELENIUM_PLUGIN_PRECENCE_POLLING: ${TestConstants.TS_SELENIUM_PLUGIN_PRECENCE_POLLING}
      TS_SELENIUM_HAPPY_PATH_WORKSPACE_NAME: ${TestConstants.TS_SELENIUM_HAPPY_PATH_WORKSPACE_NAME}
      TS_SELENIUM_USERNAME: ${TestConstants.TS_SELENIUM_USERNAME}
      TS_SELENIUM_PASSWORD: ${TestConstants.TS_SELENIUM_PASSWORD}

########################################################
      `;
      console.log(launchInformation);
    });

    runner.on('end', async function (test: mocha.Test) {
      // ensure that fired events done
      await driver.get().sleep(5000);

      // close driver
      await driver.get().quit();
    });

    runner.on('fail', async function (test: mocha.Test) {

      const reportDirPath: string = './report';
      const testFullTitle: string = test.fullTitle().replace(/\s/g, '_');
      const testTitle: string = test.title.replace(/\s/g, '_');

      const testReportDirPath: string = `${reportDirPath}/${testFullTitle}`;
      const screenshotFileName: string = `${testReportDirPath}/screenshot-${testTitle}.png`;
      const pageSourceFileName: string = `${testReportDirPath}/pagesource-${testTitle}.html`;
      const browserLogsFileName: string = `${testReportDirPath}/browserlogs-${testTitle}.txt`;


      // create reporter dir if not exist
      const reportDirExists: boolean = fs.existsSync(reportDirPath);

      if (!reportDirExists) {
        fs.mkdirSync(reportDirPath);
      }

      // create dir for failed test report if not exist
      const testReportDirExists: boolean = fs.existsSync(testReportDirPath);

      if (!testReportDirExists) {
        fs.mkdirSync(testReportDirPath);
      }

      // take screenshot and write to file
      const screenshot: string = await driver.get().takeScreenshot();
      const screenshotStream = fs.createWriteStream(screenshotFileName);
      screenshotStream.write(new Buffer(screenshot, 'base64'));
      screenshotStream.end();

      // take pagesource and write to file
      const pageSource: string = await driver.get().getPageSource();
      const pageSourceStream = fs.createWriteStream(pageSourceFileName);
      pageSourceStream.write(new Buffer(pageSource));
      pageSourceStream.end();

      // take browser console logs and write to file
      const browserLogsEntries: logging.Entry[] = await driverHelper.getDriver().manage().logs().get('browser');
      let browserLogs: string = '';

      browserLogsEntries.forEach(log => {
        browserLogs += `\"${log.level}\" \"${log.type}\" \"${log.message}\"\n`;
      });

      const browserLogsStream = fs.createWriteStream(browserLogsFileName);
      browserLogsStream.write(new Buffer(browserLogs));
      browserLogsStream.end();
    });
  }
}

export = CheReporter;
