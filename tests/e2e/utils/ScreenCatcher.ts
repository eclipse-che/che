/** *******************************************************************
 * copyright (c) 2021-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import * as fs from 'fs';
import { WriteStream } from 'fs';
import { inject, injectable } from 'inversify';
import { CLASSES } from '../configs/inversify.types';
import { DriverHelper } from './DriverHelper';
import { error } from 'selenium-webdriver';
import { StringUtil } from './StringUtil';
import { REPORTER_CONSTANTS } from '../constants/REPORTER_CONSTANTS';

@injectable()
export class ScreenCatcher {
	constructor(
		@inject(CLASSES.DriverHelper)
		private readonly driverHelper: DriverHelper
	) {}

	async catchMethodScreen(methodName: string, methodIndex: number, screenshotIndex: number): Promise<void> {
		const executionScreenCastDir: string = `${REPORTER_CONSTANTS.TS_SELENIUM_REPORT_FOLDER}/executionScreencast`;
		const executionScreenCastErrorsDir: string = `${REPORTER_CONSTANTS.TS_SELENIUM_REPORT_FOLDER}/executionScreencastErrors`;
		const formattedMethodIndex: string = new Intl.NumberFormat('en-us', {
			minimumIntegerDigits: 3
		}).format(methodIndex);
		const formattedScreenshotIndex: string = new Intl.NumberFormat('en-us', { minimumIntegerDigits: 5 })
			.format(screenshotIndex)
			.replace(/,/g, '');

		if (!fs.existsSync(REPORTER_CONSTANTS.TS_SELENIUM_REPORT_FOLDER)) {
			fs.mkdirSync(REPORTER_CONSTANTS.TS_SELENIUM_REPORT_FOLDER);
		}

		if (!fs.existsSync(executionScreenCastDir)) {
			fs.mkdirSync(executionScreenCastDir);
		}

		const date: Date = new Date();
		const timeStr: string =
			date.toLocaleTimeString('en-us', { hour12: false }) +
			'.' +
			new Intl.NumberFormat('en-us', { minimumIntegerDigits: 3 }).format(date.getMilliseconds());

		const screenshotPath: string = `${executionScreenCastDir}/${formattedMethodIndex}-${formattedScreenshotIndex}--(${StringUtil.sanitizeTitle(
			timeStr
		)})_${StringUtil.sanitizeTitle(methodName)}.png`;

		try {
			await this.catchScreen(screenshotPath);
		} catch (err) {
			if (!fs.existsSync(executionScreenCastErrorsDir)) {
				fs.mkdirSync(executionScreenCastErrorsDir);
			}

			let errorLogFilePath: string = screenshotPath.replace('.png', '.txt');
			errorLogFilePath = errorLogFilePath.replace(executionScreenCastDir, executionScreenCastErrorsDir);
			if (err instanceof error.IError) {
				this.writeErrorLog(errorLogFilePath, err);
			}
		}
	}

	async catchScreen(screenshotPath: string): Promise<void> {
		const screenshot: string = await this.driverHelper.getDriver().takeScreenshot();
		const screenshotStream: WriteStream = fs.createWriteStream(screenshotPath);
		screenshotStream.write(Buffer.from(screenshot, 'base64'));
		screenshotStream.end();
	}

	writeErrorLog(errorLogPath: string, err: error.IError): void {
		console.log(`Failed to save screenshot, additional information in the ${errorLogPath}`);

		if (err.stack) {
			const screenshotStream: WriteStream = fs.createWriteStream(errorLogPath);
			screenshotStream.write(Buffer.from(err.stack, 'utf8'));
			screenshotStream.end();
		}
	}
}
