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
import { ActivityBar, ExtensionsViewItem, ExtensionsViewSection, SideBarView } from 'monaco-page-objects';
import { DriverHelper } from '../../utils/DriverHelper';
import { Logger } from '../../utils/Logger';
import { ViewsMoreActionsButton } from './ViewsMoreActionsButton';

@injectable()
export class ExtensionsView {
	private static readonly EXTENSIONS_VIEW_SELECTOR: By = By.css('.extensions-viewlet');
	private static readonly MENU_ITEM: By = By.css('.context-view .monaco-menu .action-item');

	constructor(
		@inject(CLASSES.DriverHelper)
		private readonly driverHelper: DriverHelper,
		@inject(CLASSES.ViewsMoreActionsButton)
		private readonly viewsMoreActionsButton: ViewsMoreActionsButton
	) {}

	async openExtensionsView(): Promise<void> {
		Logger.debug();

		const viewCtrl: any = await new ActivityBar().getViewControl('Extensions');
		await viewCtrl?.openView();

		const extensionsViewVisible: boolean = await this.driverHelper.waitVisibilityBoolean(
			ExtensionsView.EXTENSIONS_VIEW_SELECTOR,
			5,
			1000
		);

		if (!extensionsViewVisible) {
			throw new Error('Extensions view could not be opened');
		}
	}

	async openMoreActionsMenu(): Promise<void> {
		Logger.debug();

		await this.viewsMoreActionsButton.clickViewsMoreActionsButton();
		await this.viewsMoreActionsButton.waitForContextMenu();
	}

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

	async isMoreActionsMenuItemVisible(menuItemText: string): Promise<boolean> {
		Logger.debug(`"${menuItemText}"`);

		const menuItems: string[] = await this.getMoreActionsMenuItems();
		return menuItems.some((item: string): boolean => item.includes(menuItemText));
	}

	async closeMoreActionsMenu(): Promise<void> {
		Logger.debug();

		await this.driverHelper.getDriver().actions().sendKeys(Key.ESCAPE).perform();
	}

	/**
	 * get installed extension names using @installed filter
	 * Reuses pattern from RecommendedExtensions.spec.ts
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
		await this.driverHelper.wait(2000);

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
	 * search in extensions - reused from RecommendedExtensions.spec.ts pattern
	 */
	private async searchInExtensions(extensionSection: ExtensionsViewSection, searchText: string): Promise<void> {
		Logger.debug(`Searching for: "${searchText}"`);

		const enclosingItem: WebElement = extensionSection.getEnclosingElement();

		try {
			const searchField: WebElement = await enclosingItem.findElement(By.css('input.monaco-inputbox-input'));

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
			await this.driverHelper.wait(1000);
		} catch (err) {
			Logger.debug(`Could not interact with search field: ${err}`);
		}
	}
}
