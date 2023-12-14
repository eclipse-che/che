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
import { By, Key } from 'selenium-webdriver';
import { Logger } from '../../utils/Logger';
import { TIMEOUT_CONSTANTS } from '../../constants/TIMEOUT_CONSTANTS';

@injectable()
export class WebTerminalPage {
	private static readonly TIMEOUT_BUTTON: By = By.xpath('//button[(text()="Timeout")]');
	private static readonly IMAGE_BUTTON: By = By.xpath('//button[(text()="Image")]');
	private static readonly WEB_TERMINAL_BUTTON: By = By.xpath('//button[@data-quickstart-id="qs-masthead-cloudshell"]');
	private static readonly WEB_TERMINAL_PAGE: By = By.xpath('//*[@class="xterm-helper-textarea"]');
	private static readonly START_WT_COMMAND_LINE_TERMINAL_BUTTON: By = By.css('button[data-test-id="submit-button"]');
	private static readonly WEB_TERMINAL_PROJECT_SELECTION_DROPDOWN: By = By.css('input#form-input-namespace-field');
	private static readonly WEB_TERMINAL_PROJECT_CANCEL_BUTTON: By = By.css('button[data-test-id="reset-button"]');
	constructor(
		@inject(CLASSES.DriverHelper)
		private readonly driverHelper: DriverHelper
	) {}

	async clickOnWebTerminalIcon(): Promise<void> {
		Logger.debug();
		await this.driverHelper.waitAndClick(WebTerminalPage.WEB_TERMINAL_BUTTON, TIMEOUT_CONSTANTS.TS_SELENIUM_WAIT_FOR_URL);
	}
	async waitTerminalIsStarted(): Promise<void> {
		Logger.debug();
		await this.driverHelper.waitPresence(WebTerminalPage.WEB_TERMINAL_PAGE, TIMEOUT_CONSTANTS.TS_WAIT_LOADER_ABSENCE_TIMEOUT);
	}
	async clickOnStartWebTerminalIcon(): Promise<void> {
		Logger.debug();
		await this.driverHelper.waitAndClick(WebTerminalPage.START_WT_COMMAND_LINE_TERMINAL_BUTTON);
	}
	async openWebTerminal(): Promise<void> {
		Logger.debug();
		await this.clickOnWebTerminalIcon();
		await this.clickOnStartWebTerminalIcon();
		await this.waitTerminalIsStarted();
	}
	async waitDisabledProjectFieldAndGetProjectName(): Promise<string> {
		Logger.debug();
		return await this.driverHelper.waitAndGetElementAttribute(WebTerminalPage.WEB_TERMINAL_PROJECT_SELECTION_DROPDOWN, 'value');
	}
	async typeIntoWebTerminal(text: string): Promise<void> {
		Logger.debug();
		await this.driverHelper.waitPresence(WebTerminalPage.WEB_TERMINAL_PAGE, TIMEOUT_CONSTANTS.TS_IDE_LOAD_TIMEOUT);
		await this.driverHelper.typeToInvisible(WebTerminalPage.WEB_TERMINAL_PAGE, text);
	}

	async typeAndEnterIntoWebTerminal(text: string): Promise<void> {
		Logger.debug();
		await this.driverHelper.waitPresence(WebTerminalPage.WEB_TERMINAL_PAGE, TIMEOUT_CONSTANTS.TS_IDE_LOAD_TIMEOUT);
		await this.driverHelper.typeToInvisible(WebTerminalPage.WEB_TERMINAL_PAGE, text + Key.ENTER);
	}
	async clickOnProjectListDropDown(): Promise<void> {
		Logger.debug();
		{
			await this.driverHelper.waitAndClick(WebTerminalPage.WEB_TERMINAL_PROJECT_SELECTION_DROPDOWN);
		}
	}
	async waitTimeoutButton(): Promise<void> {
		Logger.debug();
		{
			await this.driverHelper.waitPresence(WebTerminalPage.TIMEOUT_BUTTON);
		}
	}
	async waitImageButton(): Promise<void> {
		Logger.debug();
		{
			await this.driverHelper.waitPresence(WebTerminalPage.IMAGE_BUTTON);
		}
	}
	async waitStartButton(): Promise<void> {
		Logger.debug();
		{
			await this.driverHelper.waitPresence(WebTerminalPage.START_WT_COMMAND_LINE_TERMINAL_BUTTON);
		}
	}

	async waitCancelButton(): Promise<void> {
		Logger.debug();
		{
			await this.driverHelper.waitPresence(WebTerminalPage.WEB_TERMINAL_PROJECT_CANCEL_BUTTON);
		}
	}
	async waitTerminalWidget(): Promise<void> {
		Logger.debug();
		await this.waitStartButton();
		await this.waitCancelButton();
		await this.waitTimeoutButton();
		await this.waitImageButton();
	}
}
