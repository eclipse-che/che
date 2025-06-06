/** *******************************************************************
 * copyright (c) 2025 Red Hat, Inc.
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
import { By, Key, WebElement } from 'selenium-webdriver';
import { DriverHelper } from '../../utils/DriverHelper';

@injectable()
export class CommandPalette {
	private static readonly COMMAND_PALETTE_CONTAINER: By = By.css('.quick-input-widget');
	private static readonly COMMAND_PALETTE_LIST: By = By.css('.monaco-list');
	private static readonly COMMAND_PALETTE_ITEMS: By = By.css('.monaco-list-row');

	constructor(
		@inject(CLASSES.DriverHelper)
		readonly driverHelper: DriverHelper
	) {}

	async openCommandPalette(): Promise<void> {
		await this.driverHelper.getDriver().actions().keyDown(Key.F1).keyUp(Key.F1).perform();

		const paletteVisible: boolean = await this.driverHelper.waitVisibilityBoolean(CommandPalette.COMMAND_PALETTE_CONTAINER, 5, 1000);
		if (!paletteVisible) {
			await this.driverHelper
				.getDriver()
				.actions()
				.keyDown(Key.CONTROL)
				.keyDown(Key.SHIFT)
				.sendKeys('p')
				.keyUp(Key.SHIFT)
				.keyUp(Key.CONTROL)
				.perform();
		}
	}

	async searchForCommand(commandText: string): Promise<void> {
		await this.driverHelper.getDriver().actions().sendKeys(commandText).perform();
	}

	async getAvailableCommands(): Promise<string[]> {
		const listVisible: boolean = await this.driverHelper.waitVisibilityBoolean(CommandPalette.COMMAND_PALETTE_LIST, 5, 1000);
		if (!listVisible) {
			return [];
		}

		const items: WebElement[] = await this.driverHelper.getDriver().findElements(CommandPalette.COMMAND_PALETTE_ITEMS);
		const itemTexts: string[] = [];

		for (const item of items) {
			try {
				const itemText: string = (await item.getAttribute('aria-label')) || '';
				itemTexts.push(itemText);
			} catch (e) {
				// skip items that cannot be read
			}
		}

		return itemTexts;
	}

	async isCommandAvailable(commandText: string): Promise<boolean> {
		const availableCommands: string[] = await this.getAvailableCommands();
		return availableCommands.some((command: string): boolean => command.toLowerCase().includes(commandText.toLowerCase()));
	}

	async closeCommandPalette(): Promise<void> {
		await this.driverHelper.getDriver().actions().sendKeys(Key.ESCAPE).perform();
	}

	async searchAndCheckCommand(commandText: string): Promise<boolean> {
		try {
			await this.openCommandPalette();
			await this.searchForCommand(commandText);
			return await this.isCommandAvailable(commandText);
		} finally {
			await this.closeCommandPalette();
		}
	}
}
