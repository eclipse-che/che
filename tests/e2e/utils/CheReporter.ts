/** *******************************************************************
 * copyright (c) 2019-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
import * as chromeHar from 'chrome-har';
import * as mocha from 'mocha';
import { CLASSES } from '../configs/inversify.types';
import * as fs from 'fs';
import { WriteStream } from 'fs';
import * as rm from 'rimraf';
import { logging } from 'selenium-webdriver';
import { DriverHelper } from './DriverHelper';
import { ScreenCatcher } from './ScreenCatcher';
import { TIMEOUT_CONSTANTS } from '../constants/TIMEOUT_CONSTANTS';
import { Logger } from './Logger';
import { StringUtil } from './StringUtil';
import { BASE_TEST_CONSTANTS } from '../constants/BASE_TEST_CONSTANTS';
import { CHROME_DRIVER_CONSTANTS } from '../constants/CHROME_DRIVER_CONSTANTS';
import { OAUTH_CONSTANTS } from '../constants/OAUTH_CONSTANTS';
import { REPORTER_CONSTANTS } from '../constants/REPORTER_CONSTANTS';
import { PLUGIN_TEST_CONSTANTS } from '../constants/PLUGIN_TEST_CONSTANTS';
import { injectable } from 'inversify';
import getDecorators from 'inversify-inject-decorators';
import { e2eContainer } from '../configs/inversify.config';

const { lazyInject } = getDecorators(e2eContainer);

@injectable()
class CheReporter extends mocha.reporters.Spec {
	private static methodIndex: number = 0;
	private static deleteScreencast: boolean = true;
	@lazyInject(CLASSES.DriverHelper)
	private readonly driverHelper!: DriverHelper;
	@lazyInject(CLASSES.ScreenCatcher)
	private readonly screenCatcher!: ScreenCatcher;

	constructor(runner: mocha.Runner, options: mocha.MochaOptions) {
		super(runner, options);

		runner.on('start', (): void => {
			let launchInformation: string = `################## Launch Information ##################

      TS_SELENIUM_BASE_URL: ${BASE_TEST_CONSTANTS.TS_SELENIUM_BASE_URL}
      TS_SELENIUM_HEADLESS: ${CHROME_DRIVER_CONSTANTS.TS_SELENIUM_HEADLESS}
      TS_SELENIUM_OCP_USERNAME: ${OAUTH_CONSTANTS.TS_SELENIUM_OCP_USERNAME}
      TS_SELENIUM_EDITOR:   ${BASE_TEST_CONSTANTS.TS_SELENIUM_EDITOR}

      TS_SELENIUM_HAPPY_PATH_WORKSPACE_NAME: ${BASE_TEST_CONSTANTS.TS_SELENIUM_HAPPY_PATH_WORKSPACE_NAME}
      TS_SELENIUM_DELAY_BETWEEN_SCREENSHOTS: ${REPORTER_CONSTANTS.TS_SELENIUM_DELAY_BETWEEN_SCREENSHOTS}
      TS_SELENIUM_REPORT_FOLDER: ${REPORTER_CONSTANTS.TS_SELENIUM_REPORT_FOLDER}
      TS_SELENIUM_EXECUTION_SCREENCAST: ${REPORTER_CONSTANTS.TS_SELENIUM_EXECUTION_SCREENCAST}
      DELETE_SCREENCAST_IF_TEST_PASS: ${REPORTER_CONSTANTS.DELETE_SCREENCAST_IF_TEST_PASS}
      TS_SELENIUM_REMOTE_DRIVER_URL: ${CHROME_DRIVER_CONSTANTS.TS_SELENIUM_REMOTE_DRIVER_URL}
      DELETE_WORKSPACE_ON_FAILED_TEST: ${BASE_TEST_CONSTANTS.DELETE_WORKSPACE_ON_FAILED_TEST}
      TS_SELENIUM_LOG_LEVEL: ${REPORTER_CONSTANTS.TS_SELENIUM_LOG_LEVEL}
      TS_SELENIUM_LAUNCH_FULLSCREEN: ${CHROME_DRIVER_CONSTANTS.TS_SELENIUM_LAUNCH_FULLSCREEN}

      ${process.env.TS_SELENIUM_PROXY_SERVER ? 'TS_SELENIUM_PROXY_SERVER: ' + process.env.TS_SELENIUM_PROXY_SERVER : ''}

      TS_COMMON_DASHBOARD_WAIT_TIMEOUT: ${TIMEOUT_CONSTANTS.TS_COMMON_DASHBOARD_WAIT_TIMEOUT}
      TS_SELENIUM_START_WORKSPACE_TIMEOUT: ${TIMEOUT_CONSTANTS.TS_SELENIUM_START_WORKSPACE_TIMEOUT}
      TS_WAIT_LOADER_PRESENCE_TIMEOUT: ${TIMEOUT_CONSTANTS.TS_WAIT_LOADER_PRESENCE_TIMEOUT}

      TS_SAMPLE_LIST: ${PLUGIN_TEST_CONSTANTS.TS_SAMPLE_LIST}

      ${process.env.MOCHA_DIRECTORY ? 'MOCHA_DIRECTORY: ' + process.env.MOCHA_DIRECTORY : 'MOCHA_DIRECTORY is not set'}
      ${process.env.USERSTORY ? 'USERSTORY: ' + process.env.USERSTORY : 'USERSTORY is not set'}
`;

			if (REPORTER_CONSTANTS.TS_SELENIUM_PRINT_TIMEOUT_VARIABLES) {
				launchInformation += '\n      TS_SELENIUM_PRINT_TIMEOUT_VARIABLES is set to true: \n';
				Object.entries(TIMEOUT_CONSTANTS).forEach(([key, value]): string => (launchInformation += `\n         ${key}: ${value}`));
			} else {
				launchInformation += '\n      to output timeout variables, set TS_SELENIUM_PRINT_TIMEOUT_VARIABLES to true';
			}

			launchInformation += '\n ######################################################## \n';

			console.log(launchInformation);

			rm.sync(REPORTER_CONSTANTS.TS_SELENIUM_REPORT_FOLDER);
		});

		runner.on('test', async (test: mocha.Test): Promise<void> => {
			if (!REPORTER_CONSTANTS.TS_SELENIUM_EXECUTION_SCREENCAST) {
				return;
			}

			CheReporter.methodIndex = CheReporter.methodIndex + 1;
			const currentMethodIndex: number = CheReporter.methodIndex;
			let iterationIndex: number = 1;

			while (!(test.state === 'passed' || test.state === 'failed')) {
				await this.screenCatcher.catchMethodScreen(test.title, currentMethodIndex, iterationIndex);
				iterationIndex = iterationIndex + 1;

				await this.driverHelper.wait(REPORTER_CONSTANTS.TS_SELENIUM_DELAY_BETWEEN_SCREENSHOTS);
			}
		});

		runner.on('pass', (test: mocha.Test): void => {
			if (BASE_TEST_CONSTANTS.TS_LOAD_TESTS) {
				const loadTestReportFolder: string = REPORTER_CONSTANTS.TS_SELENIUM_LOAD_TEST_REPORT_FOLDER;
				const loadTestFilePath: string = loadTestReportFolder + '/load-test-results.txt';
				const report: string = test.title + ': ' + test.duration + '\r';
				if (!fs.existsSync(loadTestReportFolder)) {
					fs.mkdirSync(loadTestReportFolder);
				}
				fs.appendFileSync(loadTestFilePath, report);
			}
		});

		runner.on('end', (): void => {
			// delete screencast folder if conditions matched
			if (CheReporter.deleteScreencast && REPORTER_CONSTANTS.DELETE_SCREENCAST_IF_TEST_PASS) {
				rm.sync(REPORTER_CONSTANTS.TS_SELENIUM_REPORT_FOLDER);
			}
		});

		runner.on('fail', async (test: mocha.Test): Promise<void> => {
			Logger.error(`CheReporter runner.on.fail: ${test.fullTitle()} failed after ${test.duration}ms`);
			// raise flag for keeping the screencast
			CheReporter.deleteScreencast = false;

			Logger.trace(`FullTitle:${test.fullTitle()}`);
			const testFullTitle: string = StringUtil.sanitizeTitle(test.fullTitle());
			Logger.trace(`FullTitleSanitized:${testFullTitle}`);
			Logger.trace(`TestTitle:${test.title}`);
			const testTitle: string = StringUtil.sanitizeTitle(test.title);
			Logger.trace(`TestTitleSanitized:${testTitle}`);

			const testReportDirPath: string = `${REPORTER_CONSTANTS.TS_SELENIUM_REPORT_FOLDER}/${testFullTitle}`;
			const screenshotFileName: string = `${testReportDirPath}/screenshot-${testTitle}.png`;
			const pageSourceFileName: string = `${testReportDirPath}/pagesource-${testTitle}.html`;
			const browserLogsFileName: string = `${testReportDirPath}/browserlogs-${testTitle}.txt`;
			const harFileName: string = `${testReportDirPath}/har-${testTitle}.har`;

			// create reporter dir if not exist
			const reportDirExists: boolean = fs.existsSync(REPORTER_CONSTANTS.TS_SELENIUM_REPORT_FOLDER);

			if (!reportDirExists) {
				fs.mkdirSync(REPORTER_CONSTANTS.TS_SELENIUM_REPORT_FOLDER);
			}

			// create dir for failed test report if not exist
			const testReportDirExists: boolean = fs.existsSync(testReportDirPath);

			if (!testReportDirExists) {
				fs.mkdirSync(testReportDirPath);
			}

			// take screenshot and write to file
			const screenshot: string = await this.driverHelper.getDriver().takeScreenshot();
			const screenshotStream: WriteStream = fs.createWriteStream(screenshotFileName);
			screenshotStream.write(Buffer.from(screenshot, 'base64'), (): void => screenshotStream.end());

			// take page source and write to file
			const pageSource: string = await this.driverHelper.getDriver().getPageSource();
			const pageSourceStream: WriteStream = fs.createWriteStream(pageSourceFileName);
			pageSourceStream.write(Buffer.from(pageSource), (): void => pageSourceStream.end());

			// take browser console logs and write to file
			const browserLogsEntries: logging.Entry[] = await this.driverHelper.getDriver().manage().logs().get('browser');
			let browserLogs: string = '';

			browserLogsEntries.forEach((log): void => {
				browserLogs += `\"${log.level}\" \"${log.type}\" \"${log.message}\"\n`;
			});

			const browserLogsStream: WriteStream = fs.createWriteStream(browserLogsFileName);
			browserLogsStream.write(Buffer.from(browserLogs), (): void => {
				browserLogsStream.end();
			});

			// take networking logs and write to file
			const networkLogsEntries: logging.Entry[] = await this.driverHelper.getDriver().manage().logs().get('performance');
			const events: any[] = networkLogsEntries.map((entry): any[] => JSON.parse(entry.message).message);
			const har: any = chromeHar.harFromMessages(events, { includeTextFromResponseBody: true });
			this.redactHarContent(har);

			const networkLogsStream: WriteStream = fs.createWriteStream(harFileName);
			networkLogsStream.write(Buffer.from(JSON.stringify(har)), (): void => {
				networkLogsStream.end();
			});

			try {
				// Пытаемся проверить наличие алерта
				const driver = this.driverHelper.getDriver();
				try {
				  const alert = await driver.switchTo().alert();
				  const alertText = await alert.getText();
				  
				  // Записываем информацию об алерте
				  const alertFileName: string = `${testReportDirPath}/alert-${testTitle}.txt`;
				  const alertStream: WriteStream = fs.createWriteStream(alertFileName);
				  alertStream.write(Buffer.from(`Alert detected with text: ${alertText}`), (): void => alertStream.end());
				  
				  console.log(`Обнаружен алерт с текстом: "${alertText}"`);
				  
				} catch (noAlertError) {
				  // Алерт не найден, продолжаем обычный процесс
				}
				
				// Продолжаем делать скриншот и другие действия
				const screenshot: string = await driver.takeScreenshot();
				const screenshotStream: WriteStream = fs.createWriteStream(screenshotFileName);
				screenshotStream.write(Buffer.from(screenshot, 'base64'), (): void => screenshotStream.end());
				
			} catch (error) {
				console.error('Ошибка при обработке падения теста:', error);
			}
		});
	}

	redactHarContent(har: any): void {
		har.log?.entries?.forEach((entry: any): void => {
			let text: string | undefined = entry.request?.postData?.text;
			if (text) {
				text = StringUtil.updateUrlQueryValue(text, 'csrf', '<REDACTED>');
				text = StringUtil.updateUrlQueryValue(text, 'username', '<REDACTED>');
				entry.request.postData.text = StringUtil.updateUrlQueryValue(text, 'password', '<REDACTED>');
			}

			const cookies: any = entry.request?.cookies;
			if (cookies) {
				cookies.forEach((cookie: any): void => {
					if (cookie.name?.startsWith('_oauth_proxy')) {
						cookie.value = '<REDACTED>';
					}
				});
			}

			const headers: any = entry.request?.headers;
			if (headers) {
				headers.forEach((header: any): void => {
					if (header.name?.toLowerCase() === 'cookie') {
						header.value = StringUtil.updateCookieValue(header.value, '_oauth_proxy', '<REDACTED>');
						header.value = StringUtil.updateCookieValue(header.value, '_oauth_proxy_csrf', '<REDACTED>');
					}
				});
			}
		});
	}
}

export = CheReporter;
