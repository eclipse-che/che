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
import { By, Key } from 'selenium-webdriver';
import { ActivityBar, ViewControl, ViewItem, ViewSection } from 'monaco-page-objects';
import { DriverHelper } from '../../utils/DriverHelper';
import { Logger } from '../../utils/Logger';
import { ProjectAndFileTests } from '../../tests-library/ProjectAndFileTests';
import { TIMEOUT_CONSTANTS } from '../../constants/TIMEOUT_CONSTANTS';

@injectable()
export class ExplorerView {
	private static readonly CONTEXT_MENU_CONTAINER: By = By.css('.monaco-menu-container');
	private static readonly CONTEXT_MENU_ITEMS: By = By.css('.monaco-menu-container .action-item');

	constructor(
		@inject(CLASSES.DriverHelper)
		private readonly driverHelper: DriverHelper,
		@inject(CLASSES.ProjectAndFileTests)
		private readonly projectAndFileTests: ProjectAndFileTests
	) {}

	async openExplorerView(): Promise<void> {
		Logger.debug();

		const explorerCtrl: ViewControl | undefined = await new ActivityBar().getViewControl('Explorer');
		await explorerCtrl?.openView();
	}

	/**
	 * open the context menu for a file in the Explorer view
	 *
	 * @param fileName Name of the file to open context menu for
	 */
	async openFileContextMenu(fileName: string): Promise<void> {
		Logger.debug(`"${fileName}"`);

		await this.openExplorerView();

		const projectSection: ViewSection = await this.projectAndFileTests.getProjectViewSession();
		const fileItem: ViewItem | undefined = await this.projectAndFileTests.getProjectTreeItem(projectSection, fileName);

		if (!fileItem) {
			throw new Error(`Could not find ${fileName} file in explorer`);
		}

		await this.driverHelper.wait(TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING);

		try {
			await fileItem.openContextMenu();
			await this.waitContextMenuVisible();
		} catch (error) {
			Logger.error(`Context menu failed for "${fileName}": ${error instanceof Error ? error.message : 'Unknown error'}`);
			throw new Error(`Context menu failed to open for "${fileName}"`);
		}
	}

	/**
	 * check if a specific item is visible in the context menu
	 *
	 * @param menuItemAriaLabel Aria label of the menu item to check for
	 * @returns True if the item is visible
	 */
	async isContextMenuItemVisible(menuItemAriaLabel: string): Promise<boolean> {
		Logger.debug(`"${menuItemAriaLabel}"`);

		await this.driverHelper.wait(TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING);

		const contextMenuItemLocator: By = By.css(`.monaco-menu-container [aria-label="${menuItemAriaLabel}"]`);
		return await this.driverHelper.waitVisibilityBoolean(contextMenuItemLocator);
	}

	async closeContextMenu(): Promise<void> {
		Logger.debug();

		await this.driverHelper.getDriver().actions().sendKeys(Key.ESCAPE).perform();
		await this.driverHelper.wait(TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING);
		await this.driverHelper.waitDisappearance(ExplorerView.CONTEXT_MENU_CONTAINER);
	}

	private async waitContextMenuVisible(): Promise<void> {
		Logger.debug();

		const containerVisible: boolean = await this.driverHelper.waitVisibilityBoolean(ExplorerView.CONTEXT_MENU_CONTAINER);

		if (!containerVisible) {
			throw new Error('Context menu container did not appear');
		}

		await this.driverHelper.wait(TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING);

		const menuItemsVisible: boolean = await this.driverHelper.waitVisibilityBoolean(ExplorerView.CONTEXT_MENU_ITEMS);

		if (!menuItemsVisible) {
			throw new Error('Context menu items did not load properly');
		}
	}
}
