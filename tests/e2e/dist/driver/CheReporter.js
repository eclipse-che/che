"use strict";
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : new P(function (resolve) { resolve(result.value); }).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
var __importStar = (this && this.__importStar) || function (mod) {
    if (mod && mod.__esModule) return mod;
    var result = {};
    if (mod != null) for (var k in mod) if (Object.hasOwnProperty.call(mod, k)) result[k] = mod[k];
    result["default"] = mod;
    return result;
};
/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
const mocha = __importStar(require("mocha"));
const inversify_config_1 = require("../inversify.config");
const inversify_types_1 = require("../inversify.types");
const fs = __importStar(require("fs"));
const rm = __importStar(require("rimraf"));
const TestConstants_1 = require("../TestConstants");
const driver = inversify_config_1.e2eContainer.get(inversify_types_1.TYPES.Driver);
const driverHelper = inversify_config_1.e2eContainer.get(inversify_types_1.CLASSES.DriverHelper);
const screenCatcher = inversify_config_1.e2eContainer.get(inversify_types_1.CLASSES.ScreenCatcher);
let methodIndex = 0;
let deleteScreencast = true;
let testWorkspaceUtil = inversify_config_1.e2eContainer.get(inversify_types_1.TYPES.WorkspaceUtil);
class CheReporter extends mocha.reporters.Spec {
    constructor(runner, options) {
        super(runner, options);
        runner.on('start', (test) => __awaiter(this, void 0, void 0, function* () {
            const launchInformation = `################## Launch Information ##################

      TS_SELENIUM_BASE_URL: ${TestConstants_1.TestConstants.TS_SELENIUM_BASE_URL}
      TS_SELENIUM_HEADLESS: ${TestConstants_1.TestConstants.TS_SELENIUM_HEADLESS}

      TS_SELENIUM_DEFAULT_ATTEMPTS: ${TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS}
      TS_SELENIUM_DEFAULT_POLLING: ${TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_POLLING}
      TS_SELENIUM_DEFAULT_TIMEOUT: ${TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT}

      TS_SELENIUM_LANGUAGE_SERVER_START_TIMEOUT: ${TestConstants_1.TestConstants.TS_SELENIUM_LANGUAGE_SERVER_START_TIMEOUT}
      TS_SELENIUM_LOAD_PAGE_TIMEOUT: ${TestConstants_1.TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT}
      TS_SELENIUM_START_WORKSPACE_TIMEOUT: ${TestConstants_1.TestConstants.TS_SELENIUM_START_WORKSPACE_TIMEOUT}
      TS_SELENIUM_WORKSPACE_STATUS_ATTEMPTS: ${TestConstants_1.TestConstants.TS_SELENIUM_WORKSPACE_STATUS_ATTEMPTS}
      TS_SELENIUM_WORKSPACE_STATUS_POLLING: ${TestConstants_1.TestConstants.TS_SELENIUM_WORKSPACE_STATUS_POLLING}
      TS_SELENIUM_PLUGIN_PRECENCE_ATTEMPTS: ${TestConstants_1.TestConstants.TS_SELENIUM_PLUGIN_PRECENCE_ATTEMPTS}
      TS_SELENIUM_PLUGIN_PRECENCE_POLLING: ${TestConstants_1.TestConstants.TS_SELENIUM_PLUGIN_PRECENCE_POLLING}
      TS_SELENIUM_HAPPY_PATH_WORKSPACE_NAME: ${TestConstants_1.TestConstants.TS_SELENIUM_HAPPY_PATH_WORKSPACE_NAME}
      TS_SELENIUM_USERNAME: ${TestConstants_1.TestConstants.TS_SELENIUM_USERNAME}
      TS_SELENIUM_PASSWORD: ${TestConstants_1.TestConstants.TS_SELENIUM_PASSWORD}
      TS_SELENIUM_DELAY_BETWEEN_SCREENSHOTS: ${TestConstants_1.TestConstants.TS_SELENIUM_DELAY_BETWEEN_SCREENSHOTS}
      TS_SELENIUM_REPORT_FOLDER: ${TestConstants_1.TestConstants.TS_SELENIUM_REPORT_FOLDER}
      TS_SELENIUM_EXECUTION_SCREENCAST: ${TestConstants_1.TestConstants.TS_SELENIUM_EXECUTION_SCREENCAST}
      DELETE_SCREENCAST_IF_TEST_PASS: ${TestConstants_1.TestConstants.DELETE_SCREENCAST_IF_TEST_PASS}
      TS_SELENIUM_REMOTE_DRIVER_URL: ${TestConstants_1.TestConstants.TS_SELENIUM_REMOTE_DRIVER_URL}
      DELETE_WORKSPACE_ON_FAILED_TEST: ${TestConstants_1.TestConstants.DELETE_WORKSPACE_ON_FAILED_TEST}
      TS_SELENIUM_LOG_LEVEL: ${TestConstants_1.TestConstants.TS_SELENIUM_LOG_LEVEL}

########################################################
      `;
            console.log(launchInformation);
            rm.sync(TestConstants_1.TestConstants.TS_SELENIUM_REPORT_FOLDER);
        }));
        runner.on('test', function (test) {
            return __awaiter(this, void 0, void 0, function* () {
                if (!TestConstants_1.TestConstants.TS_SELENIUM_EXECUTION_SCREENCAST) {
                    return;
                }
                methodIndex = methodIndex + 1;
                const currentMethodIndex = methodIndex;
                let iterationIndex = 1;
                while (!(test.state === 'passed' || test.state === 'failed')) {
                    yield screenCatcher.catchMethodScreen(test.title, currentMethodIndex, iterationIndex);
                    iterationIndex = iterationIndex + 1;
                    yield driverHelper.wait(TestConstants_1.TestConstants.TS_SELENIUM_DELAY_BETWEEN_SCREENSHOTS);
                }
            });
        });
        runner.on('end', function (test) {
            return __awaiter(this, void 0, void 0, function* () {
                // ensure that fired events done
                yield driver.get().sleep(5000);
                // close driver
                yield driver.get().quit();
                // delete screencast folder if conditions matched
                if (deleteScreencast && TestConstants_1.TestConstants.DELETE_SCREENCAST_IF_TEST_PASS) {
                    rm.sync(TestConstants_1.TestConstants.TS_SELENIUM_REPORT_FOLDER);
                }
            });
        });
        runner.on('fail', function (test) {
            return __awaiter(this, void 0, void 0, function* () {
                // raise flag for keeping the screencast
                deleteScreencast = false;
                const testFullTitle = test.fullTitle().replace(/\s/g, '_');
                const testTitle = test.title.replace(/\s/g, '_');
                const testReportDirPath = `${TestConstants_1.TestConstants.TS_SELENIUM_REPORT_FOLDER}/${testFullTitle}`;
                const screenshotFileName = `${testReportDirPath}/screenshot-${testTitle}.png`;
                const pageSourceFileName = `${testReportDirPath}/pagesource-${testTitle}.html`;
                const browserLogsFileName = `${testReportDirPath}/browserlogs-${testTitle}.txt`;
                // create reporter dir if not exist
                const reportDirExists = fs.existsSync(TestConstants_1.TestConstants.TS_SELENIUM_REPORT_FOLDER);
                if (!reportDirExists) {
                    fs.mkdirSync(TestConstants_1.TestConstants.TS_SELENIUM_REPORT_FOLDER);
                }
                // create dir for failed test report if not exist
                const testReportDirExists = fs.existsSync(testReportDirPath);
                if (!testReportDirExists) {
                    fs.mkdirSync(testReportDirPath);
                }
                // take screenshot and write to file
                const screenshot = yield driver.get().takeScreenshot();
                const screenshotStream = fs.createWriteStream(screenshotFileName);
                screenshotStream.write(new Buffer(screenshot, 'base64'));
                screenshotStream.end();
                // take pagesource and write to file
                const pageSource = yield driver.get().getPageSource();
                const pageSourceStream = fs.createWriteStream(pageSourceFileName);
                pageSourceStream.write(new Buffer(pageSource));
                pageSourceStream.end();
                // take browser console logs and write to file
                const browserLogsEntries = yield driverHelper.getDriver().manage().logs().get('browser');
                let browserLogs = '';
                browserLogsEntries.forEach(log => {
                    browserLogs += `\"${log.level}\" \"${log.type}\" \"${log.message}\"\n`;
                });
                const browserLogsStream = fs.createWriteStream(browserLogsFileName);
                browserLogsStream.write(new Buffer(browserLogs));
                browserLogsStream.end();
                // stop and remove running workspace
                if (TestConstants_1.TestConstants.DELETE_WORKSPACE_ON_FAILED_TEST) {
                    console.log('Property DELETE_WORKSPACE_ON_FAILED_TEST se to true - trying to stop and delete running workspace.');
                    let namespace = TestConstants_1.TestConstants.TS_SELENIUM_USERNAME;
                    let workspaceId = yield testWorkspaceUtil.getIdOfRunningWorkspace(namespace);
                    testWorkspaceUtil.stopWorkspaceById(workspaceId);
                    testWorkspaceUtil.removeWorkspaceById(workspaceId);
                }
            });
        });
    }
}
module.exports = CheReporter;
//# sourceMappingURL=CheReporter.js.map