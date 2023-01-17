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
import { e2eContainer } from '../inversify.config';
import { TYPES, CLASSES } from '../inversify.types';
import * as fs from 'fs';
import * as rm from 'rimraf';
import { EditorType, TestConstants } from '../TestConstants';
import { logging } from 'selenium-webdriver';
import { DriverHelper } from '../utils/DriverHelper';
import { ScreenCatcher } from '../utils/ScreenCatcher';
import { ITestWorkspaceUtil } from '../utils/workspace/ITestWorkspaceUtil';
import { AskForConfirmationTypeTheia, PreferencesHandlerTheia, TerminalRendererTypeTheia } from '../utils/theia/PreferencesHandlerTheia';
import { CheApiRequestHandler } from '../utils/requestHandlers/CheApiRequestHandler';
import { TimeoutConstants } from '../TimeoutConstants';
import { Logger } from '../utils/Logger';
import { Sanitizer } from '../utils/Sanitizer';
import * as monacoPageObjects from 'monaco-page-objects';
import * as vscodeExtensionTesterLocators from 'vscode-extension-tester-locators';

const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
const screenCatcher: ScreenCatcher = e2eContainer.get(CLASSES.ScreenCatcher);
const sanitizer: Sanitizer = e2eContainer.get(CLASSES.Sanitizer);
let methodIndex: number = 0;
let deleteScreencast: boolean = true;
let testWorkspaceUtil: ITestWorkspaceUtil = e2eContainer.get(TYPES.WorkspaceUtil);

class CheReporter extends mocha.reporters.Spec {

  public static registerRunningWorkspace(workspaceName: string) {
    Logger.debug(`CheReporter.registerRunningWorkspace {${workspaceName}}`);
    CheReporter.latestWorkspace = workspaceName;
  }

  private static latestWorkspace: string = '';

  constructor(runner: mocha.Runner, options: mocha.MochaOptions) {
    super(runner, options);

    runner.on('start', async (test: mocha.Test) => {
      let launchInformation: string =
        `################## Launch Information ##################

      TS_SELENIUM_BASE_URL: ${TestConstants.TS_SELENIUM_BASE_URL}
      TS_SELENIUM_HEADLESS: ${TestConstants.TS_SELENIUM_HEADLESS}

      TS_SELENIUM_USERNAME: ${TestConstants.TS_SELENIUM_USERNAME}
      TS_SELENIUM_PASSWORD: ${TestConstants.TS_SELENIUM_PASSWORD}

      TS_SELENIUM_EDITOR:   ${TestConstants.TS_SELENIUM_EDITOR}

      TS_SELENIUM_HAPPY_PATH_WORKSPACE_NAME: ${TestConstants.TS_SELENIUM_HAPPY_PATH_WORKSPACE_NAME}
      TS_SELENIUM_DELAY_BETWEEN_SCREENSHOTS: ${TestConstants.TS_SELENIUM_DELAY_BETWEEN_SCREENSHOTS}
      TS_SELENIUM_REPORT_FOLDER: ${TestConstants.TS_SELENIUM_REPORT_FOLDER}
      TS_SELENIUM_EXECUTION_SCREENCAST: ${TestConstants.TS_SELENIUM_EXECUTION_SCREENCAST}
      DELETE_SCREENCAST_IF_TEST_PASS: ${TestConstants.DELETE_SCREENCAST_IF_TEST_PASS}
      TS_SELENIUM_REMOTE_DRIVER_URL: ${TestConstants.TS_SELENIUM_REMOTE_DRIVER_URL}
      DELETE_WORKSPACE_ON_FAILED_TEST: ${TestConstants.DELETE_WORKSPACE_ON_FAILED_TEST}
      TS_SELENIUM_LOG_LEVEL: ${TestConstants.TS_SELENIUM_LOG_LEVEL}
      TS_SELENIUM_LAUNCH_FULLSCREEN: ${TestConstants.TS_SELENIUM_LAUNCH_FULLSCREEN}
`;

      if (TestConstants.TS_SELENIUM_PRINT_TIMEOUT_VARIABLES) {
        launchInformation += `\n      TS_SELENIUM_PRINT_TIMEOUT_VARIABLES is set to true: \n`;
        Object.entries(TimeoutConstants).forEach(
          ([key, value]) => launchInformation += `\n         ${key}: ${value}`);
      } else {
        launchInformation += `\n      to output timeout variables, set TS_SELENIUM_PRINT_TIMEOUT_VARIABLES to true`;
      }

      launchInformation += `\n ######################################################## \n`;

      console.log(launchInformation);

      rm.sync(TestConstants.TS_SELENIUM_REPORT_FOLDER);
      if (TestConstants.TS_SELENIUM_REQUEST_INTERCEPTOR) {
        CheApiRequestHandler.enableRequestInteceptor();
      }
      if (TestConstants.TS_SELENIUM_RESPONSE_INTERCEPTOR) {
        CheApiRequestHandler.enableResponseInterceptor();
      }

      if (TestConstants.TS_SELENIUM_EDITOR === EditorType.THEIA) {
        let preferencesHandler: PreferencesHandlerTheia = e2eContainer.get(CLASSES.PreferencesHandlerTheia);
        await preferencesHandler.setConfirmExit(AskForConfirmationTypeTheia.never);
        await preferencesHandler.setTerminalType(TerminalRendererTypeTheia.dom);
      } else if (TestConstants.TS_SELENIUM_EDITOR === EditorType.CHE_CODE) {
        // init vscode-extension-tester monaco-page-objects
        monacoPageObjects.initPageObjects(TestConstants.TS_SELENIUM_MONACO_PAGE_OBJECTS_USE_VERSION, TestConstants.TS_SELENIUM_MONACO_PAGE_OBJECTS_BASE_VERSION, vscodeExtensionTesterLocators.getLocatorsPath(), driverHelper.getDriver(), 'google-chrome');
      }

    });

    runner.on('test', async function (test: mocha.Test) {
      if (!TestConstants.TS_SELENIUM_EXECUTION_SCREENCAST) {
        return;
      }

      methodIndex = methodIndex + 1;
      const currentMethodIndex: number = methodIndex;
      let iterationIndex: number = 1;

      while (!(test.state === 'passed' || test.state === 'failed')) {
        await screenCatcher.catchMethodScreen(test.title, currentMethodIndex, iterationIndex);
        iterationIndex = iterationIndex + 1;

        await driverHelper.wait(TestConstants.TS_SELENIUM_DELAY_BETWEEN_SCREENSHOTS);
      }
    });

    runner.on('pass', async (test: mocha.Test) => {
      if (TestConstants.TS_LOAD_TESTS) {
        const loadTestReportFolder: string = TestConstants.TS_SELENIUM_LOAD_TEST_REPORT_FOLDER;
        const loadTestFilePath: string = loadTestReportFolder + '/load-test-results.txt';
        const report = test.title + ': ' + test.duration + '\r';
        if (!fs.existsSync(loadTestReportFolder)) {
          fs.mkdirSync(loadTestReportFolder);
        }
        fs.appendFileSync(loadTestFilePath, report);
      }
    });


    runner.on('end', async function (test: mocha.Test) {
      // ensure that fired events done
      await driverHelper.wait(5000);

      // close driver
      await driverHelper.getDriver().quit();

      // delete screencast folder if conditions matched
      if (deleteScreencast && TestConstants.DELETE_SCREENCAST_IF_TEST_PASS) {
        rm.sync(TestConstants.TS_SELENIUM_REPORT_FOLDER);
      }
    });

    runner.on('fail', async function (test: mocha.Test) {
      Logger.error(`CheReporter runner.on.fail: ${test.fullTitle()} failed after ${test.duration}ms`);
      // raise flag for keeping the screencast
      deleteScreencast = false;

      Logger.trace(`FullTitle:${test.fullTitle()}`);
      const testFullTitle: string = sanitizer.sanitize(test.fullTitle());
      Logger.trace(`FullTitleSanitized:${testFullTitle}`);
      Logger.trace(`TestTitle:${test.title}`);
      const testTitle: string = sanitizer.sanitize(test.title);
      Logger.trace(`TestTitleSanitized:${testTitle}`);

      const testReportDirPath: string = `${TestConstants.TS_SELENIUM_REPORT_FOLDER}/${testFullTitle}`;
      const screenshotFileName: string = `${testReportDirPath}/screenshot-${testTitle}.png`;
      const pageSourceFileName: string = `${testReportDirPath}/pagesource-${testTitle}.html`;
      const browserLogsFileName: string = `${testReportDirPath}/browserlogs-${testTitle}.txt`;


      // create reporter dir if not exist
      const reportDirExists: boolean = fs.existsSync(TestConstants.TS_SELENIUM_REPORT_FOLDER);

      if (!reportDirExists) {
        fs.mkdirSync(TestConstants.TS_SELENIUM_REPORT_FOLDER);
      }

      // create dir for failed test report if not exist
      const testReportDirExists: boolean = fs.existsSync(testReportDirPath);

      if (!testReportDirExists) {
        fs.mkdirSync(testReportDirPath);
      }

      // take screenshot and write to file
      const screenshot: string = await driverHelper.getDriver().takeScreenshot();
      const screenshotStream = fs.createWriteStream(screenshotFileName);
      screenshotStream.write(Buffer.from(screenshot, 'base64'));
      screenshotStream.end();

      // take pagesource and write to file
      const pageSource: string = await driverHelper.getDriver().getPageSource();
      const pageSourceStream = fs.createWriteStream(pageSourceFileName);
      pageSourceStream.write(Buffer.from(pageSource));
      pageSourceStream.end();

      // take browser console logs and write to file
      const browserLogsEntries: logging.Entry[] = await driverHelper.getDriver().manage().logs().get('browser');
      let browserLogs: string = '';

      browserLogsEntries.forEach(log => {
        browserLogs += `\"${log.level}\" \"${log.type}\" \"${log.message}\"\n`;
      });

      const browserLogsStream = fs.createWriteStream(browserLogsFileName);
      browserLogsStream.write(Buffer.from(browserLogs));
      browserLogsStream.end();

      // stop and remove running workspace
      if (TestConstants.DELETE_WORKSPACE_ON_FAILED_TEST) {
        Logger.warn('Property DELETE_WORKSPACE_ON_FAILED_TEST se to true - trying to stop and delete running workspace.');
        await testWorkspaceUtil.stopAndDeleteWorkspaceByName(CheReporter.latestWorkspace);
      }

    });
  }
}

export = CheReporter;
