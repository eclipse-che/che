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

export enum TimeUnits {
	Seconds = 'Seconds',
	Minutes = 'Minutes',
	Hours = 'Hours'
}
@injectable()
export class WebTerminalPage {
	private static readonly TIMEOUT_BUTTON: By = By.xpath('//button[(text()="Timeout")]');
	private static readonly IMAGE_BUTTON: By = By.xpath('//button[(text()="Image")]');
	private static readonly WEB_TERMINAL_BUTTON: By = By.xpath('//button[@data-quickstart-id="qs-masthead-cloudshell"]');
	private static readonly WEB_TERMINAL_PAGE: By = By.xpath('//*[@class="xterm-helper-textarea"]');
	private static readonly START_WT_COMMAND_LINE_TERMINAL_BUTTON: By = By.css('button[data-test-id="submit-button"]');
	private static readonly WEB_TERMINAL_PROJECT_SELECTION_DROPDOWN: By = By.css('input#form-input-namespace-field');
	private static readonly WEB_TERMINAL_PROJECT_CANCEL_BUTTON: By = By.css('button[data-test-id="reset-button"]');
	private static readonly TERMINAL_INACTIVITY_MESS: By = By.xpath('//div[contains(text(),"The terminal connection has closed")]');
	private static readonly RESTART_BUTTON: By = By.xpath('//button[text()="Restart terminal"]');
	private static readonly PROJECT_NAMESPACE_DROP_DAWN: By = By.css('button#form-ns-dropdown-namespace-field');
	private static readonly PROJECT_SELECTION_FIELD: By = By.css('input[data-test-id="dropdown-text-filter"]');
	private static readonly PROJECT_NAME_FIELD: By = By.css('input#form-input-newNamespace-field');
	private static readonly TIMEOUT_INPUT: By = By.css(
		'input[aria-describedby="form-resource-limit-advancedOptions-timeout-limit-field-helper"]'
	);
	private static readonly INCREMENT_TIMEOUT_BTN: By = By.css('button[data-test-id="Decrement"]');
	private static readonly DECREMENT_TIMEOUT_BTN: By = By.css('button[data-test-id="Increment');
	private static readonly TIME_UNIT_DROP_DAWN: By = By.css('div.request-size-input__unit button');

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
	async clickOnStartWebTerminalButton(): Promise<void> {
		Logger.debug();
		await this.driverHelper.waitAndClick(WebTerminalPage.START_WT_COMMAND_LINE_TERMINAL_BUTTON);
	}
	async getAdminProjectName(): Promise<string> {
		Logger.debug();
		return await this.driverHelper.waitAndGetValue(
			By.css('input#form-input-namespace-field'),
			TIMEOUT_CONSTANTS.TS_SELENIUM_WAIT_FOR_URL
		);
	}
	async openWebTerminal(): Promise<void> {
		Logger.debug();
		await this.clickOnWebTerminalIcon();
		await this.clickOnStartWebTerminalButton();
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
			await this.driverHelper.waitPresence(
				WebTerminalPage.WEB_TERMINAL_PROJECT_SELECTION_DROPDOWN,
				TIMEOUT_CONSTANTS.TS_COMMON_DASHBOARD_WAIT_TIMEOUT
			);
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
	async waitTerminalInactivity(customTimeout?: number): Promise<void> {
		await this.driverHelper.waitVisibility(
			WebTerminalPage.TERMINAL_INACTIVITY_MESS,
			TIMEOUT_CONSTANTS.TS_COMMON_PLUGIN_TEST_TIMEOUT || customTimeout
		);

		await this.driverHelper.waitVisibility(WebTerminalPage.RESTART_BUTTON);
	}
	async waitWebTerminalProjectNameField(): Promise<void> {
		await this.driverHelper.waitPresence(WebTerminalPage.PROJECT_NAMESPACE_DROP_DAWN);
	}
	async typeProjectName(projectName: string): Promise<void> {
		await this.waitWebTerminalProjectNameField();
		await this.driverHelper.type(WebTerminalPage.PROJECT_NAME_FIELD, projectName, TIMEOUT_CONSTANTS.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM);
	}
	async openProjectDropDawn(): Promise<void> {
		await this.driverHelper.waitAndClick(WebTerminalPage.PROJECT_NAMESPACE_DROP_DAWN);
	}
	async typeProjectNameForSelecting(projectName: string): Promise<void> {
		await this.driverHelper.type(WebTerminalPage.PROJECT_SELECTION_FIELD, projectName);
	}

	async selectProjectFromDropDawnList(projectName: string): Promise<void> {
		await this.driverHelper.waitAndClick(By.xpath(`//span[@class="pf-v5-c-menu__item-text" and text()="${projectName}"]`));
	}

	async findAndSelectProject(projectName: string): Promise<void> {
		await this.openProjectDropDawn();
		await this.typeProjectNameForSelecting(projectName);
		await this.selectProjectFromDropDawnList(projectName);
	}
	async clickOnTimeoutButton(): Promise<void> {
		await this.driverHelper.waitAndClick(WebTerminalPage.TIMEOUT_BUTTON);
	}
	async setTimeoutByEntering(timeValue: number): Promise<void> {
		await this.driverHelper.type(WebTerminalPage.TIMEOUT_INPUT, timeValue.toString());
	}
	async clickOnPlusBtn(): Promise<void> {
		await this.driverHelper.waitAndClick(WebTerminalPage.INCREMENT_TIMEOUT_BTN);
	}

	async clickOnMinutesBtn(): Promise<void> {
		await this.driverHelper.waitAndClick(WebTerminalPage.DECREMENT_TIMEOUT_BTN);
	}

	async clickOnTimeUnitDropDown(): Promise<void> {
		await this.driverHelper.waitAndClick(WebTerminalPage.TIME_UNIT_DROP_DAWN);
	}
	async selectTimeUnit(timeUnits: TimeUnits): Promise<void> {
		await this.driverHelper.waitAndClick(By.xpath(`//button[@data-test-id='dropdown-menu' and text()='${timeUnits}']`));
	}
}
