/** *******************************************************************
 * copyright (c) 2024 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { inject, injectable } from 'inversify';
import 'reflect-metadata';
import { CLASSES } from '../../configs/inversify.types';
import { By, WebElement } from 'selenium-webdriver';
import { DriverHelper } from '../../utils/DriverHelper';
import { Logger } from '../../utils/Logger';
import { TIMEOUT_CONSTANTS } from '../../constants/TIMEOUT_CONSTANTS';
import { ContextMenu } from 'monaco-page-objects';

@injectable()
export class MoreActionsButton {
	private static readonly SOURCE_CONTROL_SECTION: By = By.xpath('//div[@aria-label="Source Control Management"]');
	private static readonly MORE_ACTIONS_BUTTON: By = By.xpath('//a[@role="button" and @aria-label="More Actions..."]');
	private static readonly SCM_CONTEXT_MENU: By = By.className('context-view');

	constructor(
		@inject(CLASSES.DriverHelper)
		readonly driverHelper: DriverHelper
	) {}

	async openMoreActions(timeout: number = TIMEOUT_CONSTANTS.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM): Promise<ContextMenu> {
		Logger.debug();

		await this.waitMoreActionsButton();
		await this.driverHelper.waitAndClick(MoreActionsButton.MORE_ACTIONS_BUTTON, timeout);

		let menuElement: WebElement;

		try {
			Logger.debug('Attempting to find SCM context menu element');
			
			menuElement = await this.driverHelper.getDriver().findElement(MoreActionsButton.SCM_CONTEXT_MENU);
		
			const elementText = await menuElement.getText();
			Logger.debug(`SCM context menu element text: ${elementText}`);
		} catch (error) {
			Logger.error(`Error finding SCM context menu element:', ${error}`);
			throw error
		}
		
		console.log('>>> 1')
		const scmContextMenu: ContextMenu = new ContextMenu(menuElement);
		console.log('>>> 2')

		// check: invoke of the metod from 'ContextMenu' class
		const items = await scmContextMenu.getItems();
		console.log('>>> 3')

		Logger.debug(`Menu has ${items.length} items`);
		console.log('>>> 4')

		return scmContextMenu;
	}

	async waitMoreActionsButton(timeout: number = TIMEOUT_CONSTANTS.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitAndClick(MoreActionsButton.SOURCE_CONTROL_SECTION, timeout);
		await this.driverHelper.waitVisibility(MoreActionsButton.MORE_ACTIONS_BUTTON, timeout);
	}
}
