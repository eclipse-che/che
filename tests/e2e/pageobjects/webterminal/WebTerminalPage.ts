/** *******************************************************************
 * copyright (c) 2019-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import 'reflect-metadata';
import { inject, injectable } from 'inversify';
import { DriverHelper } from '../../utils/DriverHelper';
import { CLASSES } from '../../configs/inversify.types';
import { Logger } from '../../utils/Logger';
import { TIMEOUT_CONSTANTS } from '../../constants/TIMEOUT_CONSTANTS';
import { ScreenCatcher } from '../../utils/ScreenCatcher';
import * as Tesseract from 'tesseract.js';

import sharp from 'sharp';
import { By } from 'selenium-webdriver';

@injectable()
export class WebTerminalPage {
	private static readonly LOGIN_PAGE_WELCOME_MESSAGE: By = By.xpath('//*[contains(text(), "Welcome")]');
	private static readonly WEB_TERMINAL_BUTTON: By = By.xpath('//button[@data-quickstart-id="qs-masthead-cloudshell"]');
	private static readonly WEB_TERMINAL_PAGE: By = By.xpath('//*[@class="xterm-helper-textarea"]');
	private static readonly START_WT_COMMAND_LINE_TERMINAL_BUTTON: By = By.xpath('//*[@data-test-id="submit-button"]');
	private static readonly WEB_TERMINAL_PROJECT_SELECTION_DROPDOWN: By = By.css('input#form-input-namespace-field');

	constructor(
		@inject(CLASSES.DriverHelper)
		private readonly driverHelper: DriverHelper,
		@inject(CLASSES.ScreenCatcher)
		private readonly screenCatcher: ScreenCatcher
	) {}

	async openWebTerminal(): Promise<void> {
		Logger.debug();
		await this.driverHelper.waitAndClick(WebTerminalPage.WEB_TERMINAL_BUTTON);
		await this.driverHelper.waitAndClick(WebTerminalPage.START_WT_COMMAND_LINE_TERMINAL_BUTTON);
		await this.driverHelper.waitPresence(WebTerminalPage.WEB_TERMINAL_PAGE, TIMEOUT_CONSTANTS.TS_WAIT_LOADER_ABSENCE_TIMEOUT);
	}

	async clickOnWebTerminalIcon(): Promise<void> {
		Logger.debug();
		await this.driverHelper.waitAndClick(WebTerminalPage.WEB_TERMINAL_BUTTON, TIMEOUT_CONSTANTS.TS_SELENIUM_WAIT_FOR_URL);
	}

	async waitDisabledProjectFieldAndGetProjectName(): Promise<string> {
		Logger.debug();
		return await this.driverHelper.waitAndGetElementAttribute(WebTerminalPage.WEB_TERMINAL_PROJECT_SELECTION_DROPDOWN, 'value');
	}

	async typeToWebTerminal(text: string): Promise<void> {
		Logger.debug();
		await this.driverHelper.waitPresence(WebTerminalPage.WEB_TERMINAL_PAGE, TIMEOUT_CONSTANTS.TS_IDE_LOAD_TIMEOUT);
		await this.driverHelper.typeToInvisible(WebTerminalPage.WEB_TERMINAL_PAGE, text);
	}

	async clickOnProjectListDropDown(): Promise<void> {
		Logger.debug();
		{
			await this.driverHelper.waitAndClick(WebTerminalPage.WEB_TERMINAL_PROJECT_SELECTION_DROPDOWN);
		}
	}

	async cropScreen(): Promise<void> {
		const image: string = '/tmp/screen1.jpg';
		const resultFile: string = '/tmp/screen2.jpg';
		//	await this.screenCatcher.catchScreen(image);
		const imageSize: sharp.Sharp = await sharp(image);
		const metaData: sharp.Metadata = await imageSize.metadata();
		console.log('------------------------', metaData.width, metaData.height);
		await sharp(image).extract({ width: 1920, height: 264, left: 0, top: 618 }).toFile(resultFile);
		const worker: Tesseract.Worker = await Tesseract.createWorker('eng', Tesseract.OEM.DEFAULT);
		const result: Tesseract.RecognizeResult = await worker.recognize('/tmp/screen2.jpg');
		console.log('--------------------', result.data.text);
	}
}
