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
import { ActivityBar } from 'monaco-page-objects';
import { DriverHelper } from '../../utils/DriverHelper';
import { ViewsMoreActionsButton } from './ViewsMoreActionsButton';

@injectable()
export class ExtensionsView {
	private static readonly EXTENSIONS_VIEW_SELECTOR: By = By.css('.extensions-viewlet');

	constructor(
		@inject(CLASSES.DriverHelper)
		readonly driverHelper: DriverHelper,
		@inject(CLASSES.ViewsMoreActionsButton)
		readonly viewsMoreActionsButton: ViewsMoreActionsButton
	) {}

	async openExtensionsView(): Promise<void> {
		const viewCtrl: any = await new ActivityBar().getViewControl('Extensions');
		await viewCtrl?.openView();

		const extensionsViewVisible: boolean = await this.driverHelper.waitVisibilityBoolean(
			ExtensionsView.EXTENSIONS_VIEW_SELECTOR,
			10,
			1000
		);
		if (!extensionsViewVisible) {
			throw new Error('Extensions view could not be opened');
		}
	}

	async openMoreActionsMenu(): Promise<void> {
		await this.viewsMoreActionsButton.clickViewsMoreActionsButton();
		await this.viewsMoreActionsButton.waitForContextMenu();
	}

	async getMoreActionsMenuItems(): Promise<string[]> {
		const menuItems: WebElement[] = await this.driverHelper.getDriver().findElements(By.css('.monaco-menu .action-item'));
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

	async isMenuItemAvailable(menuItemText: string): Promise<boolean> {
		const menuItems: string[] = await this.getMoreActionsMenuItems();
		return menuItems.some((item: string): boolean => item.includes(menuItemText));
	}

	async closeMoreActionsMenu(): Promise<void> {
		await this.driverHelper.getDriver().actions().sendKeys(Key.ESCAPE).perform();
	}

	async checkForMenuItem(menuItemText: string): Promise<boolean> {
		try {
			await this.openExtensionsView();
			await this.openMoreActionsMenu();
			return await this.isMenuItemAvailable(menuItemText);
		} finally {
			await this.closeMoreActionsMenu();
		}
	}
}
