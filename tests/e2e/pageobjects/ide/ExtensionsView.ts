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
import { ActivityBar, ExtensionsViewItem, ExtensionsViewSection, SideBarView, ViewControl } from 'monaco-page-objects';
import { DriverHelper } from '../../utils/DriverHelper';
import { Logger } from '../../utils/Logger';
import { ViewsMoreActionsButton } from './ViewsMoreActionsButton';
import { TIMEOUT_CONSTANTS } from '../../constants/TIMEOUT_CONSTANTS';

@injectable()
export class ExtensionsView {
	private static readonly EXTENSIONS_VIEW: By = By.css('.extensions-viewlet');
	private static readonly MENU_ITEM: By = By.css('.context-view .monaco-menu .action-item');
	private static readonly SEARCH_BOX: By = By.css('input.monaco-inputbox-input');

	constructor(
		@inject(CLASSES.DriverHelper)
		private readonly driverHelper: DriverHelper,
		@inject(CLASSES.ViewsMoreActionsButton)
		private readonly viewsMoreActionsButton: ViewsMoreActionsButton
	) {}

	async openExtensionsView(): Promise<void> {
		Logger.debug();

		const viewCtrl: ViewControl | undefined = await new ActivityBar().getViewControl('Extensions');
		await viewCtrl?.openView();

		const extensionsViewVisible: boolean = await this.driverHelper.waitVisibilityBoolean(ExtensionsView.EXTENSIONS_VIEW);

		if (!extensionsViewVisible) {
			throw new Error('Extensions view could not be opened');
		}
	}

	async openMoreActionsMenu(): Promise<void> {
		Logger.debug();

		await this.viewsMoreActionsButton.clickViewsMoreActionsButton();
		await this.viewsMoreActionsButton.waitForContextMenu();
	}

	/**
	 * get all items in the More Actions menu
	 *
	 * @returns Array of menu item texts
	 */
	async getMoreActionsMenuItems(): Promise<string[]> {
		Logger.debug();

		const menuItems: WebElement[] = await this.driverHelper.getDriver().findElements(ExtensionsView.MENU_ITEM);
		const menuTexts: string[] = [];

		for (const item of menuItems) {
			try {
				const text: string = await item.getText();
				menuTexts.push(text);
			} catch (err) {
				// skip items that cannot be read
			}
		}

		return menuTexts;
	}

	/**
	 * check if a specific item is visible in the More Actions menu
	 *
	 * @param menuItemText Text to check for
	 * @returns True if item is visible
	 */
	async isMoreActionsMenuItemVisible(menuItemText: string): Promise<boolean> {
		Logger.debug(`"${menuItemText}"`);

		const menuItems: string[] = await this.getMoreActionsMenuItems();
		return menuItems.some((item: string): boolean => item.includes(menuItemText));
	}

	async closeMoreActionsMenu(): Promise<void> {
		Logger.debug();

		await this.driverHelper.getDriver().actions().sendKeys(Key.ESCAPE).perform();
		await this.driverHelper.wait(TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING);
	}

	/**
	 * get names of all installed extensions
	 */
	async getInstalledExtensionNames(): Promise<string[]> {
		Logger.debug();

		await this.openExtensionsView();

		const extensionsView: SideBarView | undefined = await (await new ActivityBar().getViewControl('Extensions'))?.openView();
		if (!extensionsView) {
			throw new Error('Could not open Extensions view');
		}

		const [extensionSection]: ExtensionsViewSection[] = (await extensionsView.getContent().getSections()) as ExtensionsViewSection[];
		if (!extensionSection) {
			throw new Error('Could not find Extensions section');
		}

		// search for installed extensions
		await this.searchInExtensions(extensionSection, '@installed');

		// wait for results to load
		await this.driverHelper.wait(TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING);

		const installedItems: ExtensionsViewItem[] = await extensionSection.getVisibleItems();
		const extensionNames: string[] = [];

		for (const item of installedItems) {
			try {
				const title: string = await item.getTitle();
				extensionNames.push(title);
			} catch (err) {
				// skip items that cannot be read
			}
		}

		Logger.debug(`Found ${extensionNames.length} installed extensions: ${extensionNames.join(', ')}`);
		return extensionNames;
	}

	/**
	 * search for extensions using a search term
	 *
	 * @param extensionSection The ExtensionsViewSection to search in
	 * @param searchText Text to search for
	 */
	private async searchInExtensions(extensionSection: ExtensionsViewSection, searchText: string): Promise<void> {
		Logger.debug(`Searching for: "${searchText}"`);

		const enclosingItem: WebElement = extensionSection.getEnclosingElement();

		try {
			const searchField: WebElement = await enclosingItem.findElement(ExtensionsView.SEARCH_BOX);

			// clear existing search
			await this.driverHelper.getDriver().actions().click(searchField).perform();
			await this.driverHelper
				.getDriver()
				.actions()
				.keyDown(Key.CONTROL)
				.sendKeys('a')
				.keyUp(Key.CONTROL)
				.sendKeys(Key.DELETE)
				.perform();

			// enter new search text
			await this.driverHelper.getDriver().actions().sendKeys(searchText).perform();
			await this.driverHelper.wait(TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING);
		} catch (err) {
			Logger.debug(`Could not interact with search field: ${err}`);
		}
	}
}
