/*********************************************************************
 * Copyright (c) 2019-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import * as mocha from 'mocha';
import { TYPES, CLASSES } from '../configs/inversify.types';
import * as fs from 'fs';
import * as rm from 'rimraf';
import { logging } from 'selenium-webdriver';
import { DriverHelper } from './DriverHelper';
import { ScreenCatcher } from './ScreenCatcher';
import { ITestWorkspaceUtil } from './workspace/ITestWorkspaceUtil';
import { TimeoutConstants } from '../constants/TimeoutConstants';
import { Logger } from './Logger';
import { e2eContainer } from '../configs/inversify.config';
import { WriteStream } from 'fs';
import { StringUtil } from './StringUtil';
import { BaseTestConstants } from '../constants/BaseTestConstants';
import { ChromeDriverConstants } from '../constants/ChromeDriverConstants';
import { OAuthConstants } from '../constants/OAuthConstants';
import { ReporterConstants } from '../constants/ReporterConstants';
import { PluginsTestConstants } from '../constants/PluginsTestConstants';

const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
const screenCatcher: ScreenCatcher = e2eContainer.get(CLASSES.ScreenCatcher);
let methodIndex: number = 0;
let deleteScreencast: boolean = true;
let testWorkspaceUtil: ITestWorkspaceUtil = e2eContainer.get(TYPES.WorkspaceUtil);

class CheReporter extends mocha.reporters.Spec {

  private static latestWorkspace: string = '';

  constructor(runner: mocha.Runner, options: mocha.MochaOptions) {
    super(runner, options);

    runner.on('start', async (): Promise<void> => {
      let launchInformation: string =
        `################## Launch Information ##################

      TS_SELENIUM_BASE_URL: ${BaseTestConstants.TS_SELENIUM_BASE_URL}
      TS_SELENIUM_HEADLESS: ${ChromeDriverConstants.TS_SELENIUM_HEADLESS}
      TS_SELENIUM_OCP_USERNAME: ${OAuthConstants.TS_SELENIUM_OCP_USERNAME}
      TS_SELENIUM_EDITOR:   ${BaseTestConstants.TS_SELENIUM_EDITOR}

      TS_SELENIUM_HAPPY_PATH_WORKSPACE_NAME: ${BaseTestConstants.TS_SELENIUM_HAPPY_PATH_WORKSPACE_NAME}
      TS_SELENIUM_DELAY_BETWEEN_SCREENSHOTS: ${ReporterConstants.TS_SELENIUM_DELAY_BETWEEN_SCREENSHOTS}
      TS_SELENIUM_REPORT_FOLDER: ${ReporterConstants.TS_SELENIUM_REPORT_FOLDER}
      TS_SELENIUM_EXECUTION_SCREENCAST: ${ReporterConstants.TS_SELENIUM_EXECUTION_SCREENCAST}
      DELETE_SCREENCAST_IF_TEST_PASS: ${ReporterConstants.DELETE_SCREENCAST_IF_TEST_PASS}
      TS_SELENIUM_REMOTE_DRIVER_URL: ${ChromeDriverConstants.TS_SELENIUM_REMOTE_DRIVER_URL}
      DELETE_WORKSPACE_ON_FAILED_TEST: ${BaseTestConstants.DELETE_WORKSPACE_ON_FAILED_TEST}
      TS_SELENIUM_LOG_LEVEL: ${ReporterConstants.TS_SELENIUM_LOG_LEVEL}
      TS_SELENIUM_LAUNCH_FULLSCREEN: ${ChromeDriverConstants.TS_SELENIUM_LAUNCH_FULLSCREEN}

      TS_COMMON_DASHBOARD_WAIT_TIMEOUT: ${TimeoutConstants.TS_COMMON_DASHBOARD_WAIT_TIMEOUT}
      TS_SELENIUM_START_WORKSPACE_TIMEOUT: ${TimeoutConstants.TS_SELENIUM_START_WORKSPACE_TIMEOUT}
      TS_WAIT_LOADER_PRESENCE_TIMEOUT: ${TimeoutConstants.TS_WAIT_LOADER_PRESENCE_TIMEOUT}

      TS_SAMPLE_LIST: ${PluginsTestConstants.TS_SAMPLE_LIST}

      ${process.env.MOCHA_DIRECTORY ? 'MOCHA_DIRECTORY: ' + process.env.MOCHA_DIRECTORY : 'MOCHA_DRIRECTORY is not set'}
      ${process.env.USERSTORY ? 'USERSTORY: ' + process.env.USERSTORY : 'USERSTORY is not set'}
`;

      if (ReporterConstants.TS_SELENIUM_PRINT_TIMEOUT_VARIABLES) {
        launchInformation += `\n      TS_SELENIUM_PRINT_TIMEOUT_VARIABLES is set to true: \n`;
        Object.entries(TimeoutConstants).forEach(
          ([key, value]) => launchInformation += `\n         ${key}: ${value}`);
      } else {
        launchInformation += `\n      to output timeout variables, set TS_SELENIUM_PRINT_TIMEOUT_VARIABLES to true`;
      }

      launchInformation += `\n ######################################################## \n`;

      console.log(launchInformation);

      rm.sync(ReporterConstants.TS_SELENIUM_REPORT_FOLDER);
    });

    runner.on('test', async function (test: mocha.Test): Promise<void> {
      if (!ReporterConstants.TS_SELENIUM_EXECUTION_SCREENCAST) {
        return;
      }

      methodIndex = methodIndex + 1;
      const currentMethodIndex: number = methodIndex;
      let iterationIndex: number = 1;

      while (!(test.state === 'passed' || test.state === 'failed')) {
        await screenCatcher.catchMethodScreen(test.title, currentMethodIndex, iterationIndex);
        iterationIndex = iterationIndex + 1;

        await driverHelper.wait(ReporterConstants.TS_SELENIUM_DELAY_BETWEEN_SCREENSHOTS);
      }
    });

    runner.on('pass', async (test: mocha.Test) => {
      if (BaseTestConstants.TS_LOAD_TESTS) {
        const loadTestReportFolder: string = ReporterConstants.TS_SELENIUM_LOAD_TEST_REPORT_FOLDER;
        const loadTestFilePath: string = loadTestReportFolder + '/load-test-results.txt';
        const report: string = test.title + ': ' + test.duration + '\r';
        if (!fs.existsSync(loadTestReportFolder)) {
          fs.mkdirSync(loadTestReportFolder);
        }
        fs.appendFileSync(loadTestFilePath, report);
      }
    });


    runner.on('end', async function (): Promise<void> {
      // ensure that fired events done
      await driverHelper.wait(5000);

      // close driver
      await driverHelper.getDriver().quit();

      // delete screencast folder if conditions matched
      if (deleteScreencast && ReporterConstants.DELETE_SCREENCAST_IF_TEST_PASS) {
        rm.sync(ReporterConstants.TS_SELENIUM_REPORT_FOLDER);
      }
    });

    runner.on('fail', async function (test: mocha.Test): Promise<void> {
      Logger.error(`CheReporter runner.on.fail: ${test.fullTitle()} failed after ${test.duration}ms`);
      // raise flag for keeping the screencast
      deleteScreencast = false;

      Logger.trace(`FullTitle:${test.fullTitle()}`);
      const testFullTitle: string = StringUtil.sanitizeTitle(test.fullTitle());
      Logger.trace(`FullTitleSanitized:${testFullTitle}`);
      Logger.trace(`TestTitle:${test.title}`);
      const testTitle: string = StringUtil.sanitizeTitle(test.title);
      Logger.trace(`TestTitleSanitized:${testTitle}`);

      const testReportDirPath: string = `${ReporterConstants.TS_SELENIUM_REPORT_FOLDER}/${testFullTitle}`;
      const screenshotFileName: string = `${testReportDirPath}/screenshot-${testTitle}.png`;
      const pageSourceFileName: string = `${testReportDirPath}/pagesource-${testTitle}.html`;
      const browserLogsFileName: string = `${testReportDirPath}/browserlogs-${testTitle}.txt`;


      // create reporter dir if not exist
      const reportDirExists: boolean = fs.existsSync(ReporterConstants.TS_SELENIUM_REPORT_FOLDER);

      if (!reportDirExists) {
        fs.mkdirSync(ReporterConstants.TS_SELENIUM_REPORT_FOLDER);
      }

      // create dir for failed test report if not exist
      const testReportDirExists: boolean = fs.existsSync(testReportDirPath);

      if (!testReportDirExists) {
        fs.mkdirSync(testReportDirPath);
      }

      // take screenshot and write to file
      const screenshot: string = await driverHelper.getDriver().takeScreenshot();
      const screenshotStream: WriteStream = fs.createWriteStream(screenshotFileName);
      screenshotStream.write(Buffer.from(screenshot, 'base64'));
      screenshotStream.end();

      // take page source and write to file
      const pageSource: string = await driverHelper.getDriver().getPageSource();
      const pageSourceStream: WriteStream = fs.createWriteStream(pageSourceFileName);
      pageSourceStream.write(Buffer.from(pageSource));
      pageSourceStream.end();

      // take browser console logs and write to file
      const browserLogsEntries: logging.Entry[] = await driverHelper.getDriver().manage().logs().get('browser');
      let browserLogs: string = '';

      browserLogsEntries.forEach(log => {
        browserLogs += `\"${log.level}\" \"${log.type}\" \"${log.message}\"\n`;
      });

      const browserLogsStream: WriteStream = fs.createWriteStream(browserLogsFileName);
      browserLogsStream.write(Buffer.from(browserLogs));
      browserLogsStream.end();

      // stop and remove running workspace
      if (BaseTestConstants.DELETE_WORKSPACE_ON_FAILED_TEST) {
        Logger.warn('Property DELETE_WORKSPACE_ON_FAILED_TEST se to true - trying to stop and delete running workspace.');
        await testWorkspaceUtil.stopAndDeleteWorkspaceByName(CheReporter.latestWorkspace);
      }

    });
  }
}

export = CheReporter;
