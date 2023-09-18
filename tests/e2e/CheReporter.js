"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __importStar = (this && this.__importStar) || function (mod) {
    if (mod && mod.__esModule) return mod;
    var result = {};
    if (mod != null) for (var k in mod) if (k !== "default" && Object.prototype.hasOwnProperty.call(mod, k)) __createBinding(result, mod, k);
    __setModuleDefault(result, mod);
    return result;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
var __param = (this && this.__param) || function (paramIndex, decorator) {
    return function (target, key) { decorator(target, key, paramIndex); }
};
var CheReporter_1;
/** *******************************************************************
 * copyright (c) 2019-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
const mocha = __importStar(require("mocha"));
const inversify_types_1 = require("../configs/inversify.types");
const fs = __importStar(require("fs"));
const rm = __importStar(require("rimraf"));
const DriverHelper_1 = require("./DriverHelper");
const ScreenCatcher_1 = require("./ScreenCatcher");
const TIMEOUT_CONSTANTS_1 = require("../constants/TIMEOUT_CONSTANTS");
const Logger_1 = require("./Logger");
const StringUtil_1 = require("./StringUtil");
const BASE_TEST_CONSTANTS_1 = require("../constants/BASE_TEST_CONSTANTS");
const CHROME_DRIVER_CONSTANTS_1 = require("../constants/CHROME_DRIVER_CONSTANTS");
const OAUTH_CONSTANTS_1 = require("../constants/OAUTH_CONSTANTS");
const REPORTER_CONSTANTS_1 = require("../constants/REPORTER_CONSTANTS");
const PLUGIN_TEST_CONSTANTS_1 = require("../constants/PLUGIN_TEST_CONSTANTS");
const inversify_1 = require("inversify");
let CheReporter = CheReporter_1 = class CheReporter extends mocha.reporters.Spec {
    constructor(runner, options, driverHelper, screenCatcher) {
        super(runner, options);
        this.driverHelper = driverHelper;
        this.screenCatcher = screenCatcher;
        CheReporter_1.helper = driverHelper;
        runner.on('start', () => {
            let launchInformation = `################## Launch Information ##################

      TS_SELENIUM_BASE_URL: ${BASE_TEST_CONSTANTS_1.BASE_TEST_CONSTANTS.TS_SELENIUM_BASE_URL}
      TS_SELENIUM_HEADLESS: ${CHROME_DRIVER_CONSTANTS_1.CHROME_DRIVER_CONSTANTS.TS_SELENIUM_HEADLESS}
      TS_SELENIUM_OCP_USERNAME: ${OAUTH_CONSTANTS_1.OAUTH_CONSTANTS.TS_SELENIUM_OCP_USERNAME}
      TS_SELENIUM_EDITOR:   ${BASE_TEST_CONSTANTS_1.BASE_TEST_CONSTANTS.TS_SELENIUM_EDITOR}

      TS_SELENIUM_HAPPY_PATH_WORKSPACE_NAME: ${BASE_TEST_CONSTANTS_1.BASE_TEST_CONSTANTS.TS_SELENIUM_HAPPY_PATH_WORKSPACE_NAME}
      TS_SELENIUM_DELAY_BETWEEN_SCREENSHOTS: ${REPORTER_CONSTANTS_1.REPORTER_CONSTANTS.TS_SELENIUM_DELAY_BETWEEN_SCREENSHOTS}
      TS_SELENIUM_REPORT_FOLDER: ${REPORTER_CONSTANTS_1.REPORTER_CONSTANTS.TS_SELENIUM_REPORT_FOLDER}
      TS_SELENIUM_EXECUTION_SCREENCAST: ${REPORTER_CONSTANTS_1.REPORTER_CONSTANTS.TS_SELENIUM_EXECUTION_SCREENCAST}
      DELETE_SCREENCAST_IF_TEST_PASS: ${REPORTER_CONSTANTS_1.REPORTER_CONSTANTS.DELETE_SCREENCAST_IF_TEST_PASS}
      TS_SELENIUM_REMOTE_DRIVER_URL: ${CHROME_DRIVER_CONSTANTS_1.CHROME_DRIVER_CONSTANTS.TS_SELENIUM_REMOTE_DRIVER_URL}
      DELETE_WORKSPACE_ON_FAILED_TEST: ${BASE_TEST_CONSTANTS_1.BASE_TEST_CONSTANTS.DELETE_WORKSPACE_ON_FAILED_TEST}
      TS_SELENIUM_LOG_LEVEL: ${REPORTER_CONSTANTS_1.REPORTER_CONSTANTS.TS_SELENIUM_LOG_LEVEL}
      TS_SELENIUM_LAUNCH_FULLSCREEN: ${CHROME_DRIVER_CONSTANTS_1.CHROME_DRIVER_CONSTANTS.TS_SELENIUM_LAUNCH_FULLSCREEN}

      TS_COMMON_DASHBOARD_WAIT_TIMEOUT: ${TIMEOUT_CONSTANTS_1.TIMEOUT_CONSTANTS.TS_COMMON_DASHBOARD_WAIT_TIMEOUT}
      TS_SELENIUM_START_WORKSPACE_TIMEOUT: ${TIMEOUT_CONSTANTS_1.TIMEOUT_CONSTANTS.TS_SELENIUM_START_WORKSPACE_TIMEOUT}
      TS_WAIT_LOADER_PRESENCE_TIMEOUT: ${TIMEOUT_CONSTANTS_1.TIMEOUT_CONSTANTS.TS_WAIT_LOADER_PRESENCE_TIMEOUT}

      TS_SAMPLE_LIST: ${PLUGIN_TEST_CONSTANTS_1.PLUGIN_TEST_CONSTANTS.TS_SAMPLE_LIST}

      ${process.env.MOCHA_DIRECTORY ? 'MOCHA_DIRECTORY: ' + process.env.MOCHA_DIRECTORY : 'MOCHA_DRIRECTORY is not set'}
      ${process.env.USERSTORY ? 'USERSTORY: ' + process.env.USERSTORY : 'USERSTORY is not set'}
`;
            if (REPORTER_CONSTANTS_1.REPORTER_CONSTANTS.TS_SELENIUM_PRINT_TIMEOUT_VARIABLES) {
                launchInformation += '\n      TS_SELENIUM_PRINT_TIMEOUT_VARIABLES is set to true: \n';
                Object.entries(TIMEOUT_CONSTANTS_1.TIMEOUT_CONSTANTS).forEach(([key, value]) => (launchInformation += `\n         ${key}: ${value}`));
            }
            else {
                launchInformation += '\n      to output timeout variables, set TS_SELENIUM_PRINT_TIMEOUT_VARIABLES to true';
            }
            launchInformation += '\n ######################################################## \n';
            console.log(launchInformation);
            rm.sync(REPORTER_CONSTANTS_1.REPORTER_CONSTANTS.TS_SELENIUM_REPORT_FOLDER);
        });
        runner.on('test', async (test) => {
            if (!REPORTER_CONSTANTS_1.REPORTER_CONSTANTS.TS_SELENIUM_EXECUTION_SCREENCAST) {
                return;
            }
            CheReporter_1.methodIndex = CheReporter_1.methodIndex + 1;
            const currentMethodIndex = CheReporter_1.methodIndex;
            let iterationIndex = 1;
            while (!(test.state === 'passed' || test.state === 'failed')) {
                await this.screenCatcher.catchMethodScreen(test.title, currentMethodIndex, iterationIndex);
                iterationIndex = iterationIndex + 1;
                await this.driverHelper.wait(REPORTER_CONSTANTS_1.REPORTER_CONSTANTS.TS_SELENIUM_DELAY_BETWEEN_SCREENSHOTS);
            }
        });
        runner.on('pass', (test) => {
            if (BASE_TEST_CONSTANTS_1.BASE_TEST_CONSTANTS.TS_LOAD_TESTS) {
                const loadTestReportFolder = REPORTER_CONSTANTS_1.REPORTER_CONSTANTS.TS_SELENIUM_LOAD_TEST_REPORT_FOLDER;
                const loadTestFilePath = loadTestReportFolder + '/load-test-results.txt';
                const report = test.title + ': ' + test.duration + '\r';
                if (!fs.existsSync(loadTestReportFolder)) {
                    fs.mkdirSync(loadTestReportFolder);
                }
                fs.appendFileSync(loadTestFilePath, report);
            }
        });
        runner.on('end', async () => {
            // ensure that fired events done
            await this.driverHelper.wait(5000);
            // close driver
            await this.driverHelper.getDriver().quit();
            // delete screencast folder if conditions matched
            if (CheReporter_1.deleteScreencast && REPORTER_CONSTANTS_1.REPORTER_CONSTANTS.DELETE_SCREENCAST_IF_TEST_PASS) {
                rm.sync(REPORTER_CONSTANTS_1.REPORTER_CONSTANTS.TS_SELENIUM_REPORT_FOLDER);
            }
        });
        runner.on('fail', async (test) => {
            Logger_1.Logger.error(`CheReporter runner.on.fail: ${test.fullTitle()} failed after ${test.duration}ms`);
            // raise flag for keeping the screencast
            CheReporter_1.deleteScreencast = false;
            Logger_1.Logger.trace(`FullTitle:${test.fullTitle()}`);
            const testFullTitle = StringUtil_1.StringUtil.sanitizeTitle(test.fullTitle());
            Logger_1.Logger.trace(`FullTitleSanitized:${testFullTitle}`);
            Logger_1.Logger.trace(`TestTitle:${test.title}`);
            const testTitle = StringUtil_1.StringUtil.sanitizeTitle(test.title);
            Logger_1.Logger.trace(`TestTitleSanitized:${testTitle}`);
            const testReportDirPath = `${REPORTER_CONSTANTS_1.REPORTER_CONSTANTS.TS_SELENIUM_REPORT_FOLDER}/${testFullTitle}`;
            const screenshotFileName = `${testReportDirPath}/screenshot-${testTitle}.png`;
            const pageSourceFileName = `${testReportDirPath}/pagesource-${testTitle}.html`;
            const browserLogsFileName = `${testReportDirPath}/browserlogs-${testTitle}.txt`;
            // create reporter dir if not exist
            const reportDirExists = fs.existsSync(REPORTER_CONSTANTS_1.REPORTER_CONSTANTS.TS_SELENIUM_REPORT_FOLDER);
            if (!reportDirExists) {
                fs.mkdirSync(REPORTER_CONSTANTS_1.REPORTER_CONSTANTS.TS_SELENIUM_REPORT_FOLDER);
            }
            // create dir for failed test report if not exist
            const testReportDirExists = fs.existsSync(testReportDirPath);
            if (!testReportDirExists) {
                fs.mkdirSync(testReportDirPath);
            }
            // take screenshot and write to file
            try {
                const screenshot = await CheReporter_1.helper.getDriver().takeScreenshot();
                const screenshotStream = fs.createWriteStream(screenshotFileName);
                screenshotStream.write(Buffer.from(screenshot, 'base64'));
                screenshotStream.end();
            }
            catch (e) {
                console.log('asdf');
            }
            // take page source and write to file
            const pageSource = await this.driverHelper.getDriver().getPageSource();
            const pageSourceStream = fs.createWriteStream(pageSourceFileName);
            pageSourceStream.write(Buffer.from(pageSource));
            pageSourceStream.end();
            // take browser console logs and write to file
            const browserLogsEntries = await this.driverHelper.getDriver().manage().logs().get('browser');
            let browserLogs = '';
            browserLogsEntries.forEach((log) => {
                browserLogs += `\"${log.level}\" \"${log.type}\" \"${log.message}\"\n`;
            });
            const browserLogsStream = fs.createWriteStream(browserLogsFileName);
            browserLogsStream.write(Buffer.from(browserLogs));
            browserLogsStream.end();
        });
    }
};
CheReporter.methodIndex = 0;
CheReporter.deleteScreencast = true;
CheReporter = CheReporter_1 = __decorate([
    (0, inversify_1.injectable)(),
    __param(2, (0, inversify_1.inject)(inversify_types_1.CLASSES.DriverHelper)),
    __param(3, (0, inversify_1.inject)(inversify_types_1.CLASSES.ScreenCatcher)),
    __metadata("design:paramtypes", [mocha.Runner, Object, DriverHelper_1.DriverHelper,
        ScreenCatcher_1.ScreenCatcher])
], CheReporter);
module.exports = CheReporter;
//# sourceMappingURL=CheReporter.js.map