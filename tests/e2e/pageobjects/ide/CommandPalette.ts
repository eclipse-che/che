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
import { CLASSES } from '../../configs/inversify.types';
import { By, Key, WebElement } from 'selenium-webdriver';
import { DriverHelper } from '../../utils/DriverHelper';
import { Logger } from '../../utils/Logger';
import { TIMEOUT_CONSTANTS } from '../../constants/TIMEOUT_CONSTANTS';

@injectable()
export class CommandPalette {
	private static readonly COMMAND_PALETTE_CONTAINER: By = By.css('.quick-input-widget');
	private static readonly COMMAND_PALETTE_LIST: By = By.css('#quickInput_list');
	private static readonly COMMAND_PALETTE_ITEMS: By = By.css('#quickInput_list [role="option"]');

	constructor(
		@inject(CLASSES.DriverHelper)
		private readonly driverHelper: DriverHelper
	) {}

	/**
	 * open the Command Palette using keyboard shortcut
	 * tries F1 first, then Ctrl+Shift+P if needed
	 */
	async openCommandPalette(): Promise<void> {
		Logger.debug();

		await this.driverHelper.getDriver().actions().keyDown(Key.F1).keyUp(Key.F1).perform();

		const paletteVisible: boolean = await this.driverHelper.waitVisibilityBoolean(CommandPalette.COMMAND_PALETTE_CONTAINER);

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

		await this.driverHelper.waitVisibility(CommandPalette.COMMAND_PALETTE_LIST);
	}

	/**
	 * search for a command in the Command Palette
	 *
	 * @param commandText Text to search for
	 */
	async searchCommand(commandText: string): Promise<void> {
		Logger.debug(`"${commandText}"`);

		await this.driverHelper.wait(TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING);
		await this.driverHelper.getDriver().actions().sendKeys(commandText).perform();
		await this.driverHelper.wait(TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING);
	}

	/**
	 * get all visible commands in the Command Palette
	 *
	 * @returns Array of command texts
	 */
	async getVisibleCommands(): Promise<string[]> {
		Logger.debug();

		const listVisible: boolean = await this.driverHelper.waitVisibilityBoolean(CommandPalette.COMMAND_PALETTE_LIST);

		if (!listVisible) {
			return [];
		}

		await this.driverHelper.wait(TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING);
		const items: WebElement[] = await this.driverHelper.getDriver().findElements(CommandPalette.COMMAND_PALETTE_ITEMS);
		const itemTexts: string[] = [];

		for (const item of items) {
			try {
				const ariaLabel: string = await item.getAttribute('aria-label');
				if (ariaLabel) {
					itemTexts.push(ariaLabel);
				}
			} catch (err) {
				// skip items that cannot be read
			}
		}

		return itemTexts;
	}

	async isCommandVisible(commandText: string): Promise<boolean> {
		Logger.debug(`"${commandText}"`);
		await this.driverHelper.wait(TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING);

		const availableCommands: string[] = await this.getVisibleCommands();
		Logger.debug(`Available commands: ${availableCommands.join(', ')}`);
		return availableCommands.some((command: string): boolean => command.toLowerCase().includes(commandText.toLowerCase()));
	}

	async closeCommandPalette(): Promise<void> {
		Logger.debug();

		await this.driverHelper.getDriver().actions().sendKeys(Key.ESCAPE).perform();
		await this.driverHelper.waitDisappearance(CommandPalette.COMMAND_PALETTE_CONTAINER);
	}
}
