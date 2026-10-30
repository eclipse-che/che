/** *******************************************************************
 * copyright (c) 2025-2026 Red Hat, Inc.
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
	private static readonly CLONE_REPOSITORY_BUTTON: By = By.xpath('//span[text()="Clone Repository"]');
	private static readonly CONTEXT_MENU_CONTAINER: By = By.css('.monaco-menu-container');
	private static readonly CONTEXT_MENU_ITEMS: By = By.css('.monaco-menu-container .action-item');
	private static readonly EMPTY_EXPLORER: By = By.xpath('//*[contains(@aria-label, "No Folder Opened Section")]');
	private static readonly OPEN_REPOSITORY_BUTTON: By = By.xpath('//div[@class="dialog-buttons"]//a[text()="Open"]');
	private static readonly WORKBENCH_SIDE_BAR: By = By.id('workbench.parts.sidebar');

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
	 * clone a Git repository from the Explorer welcome view and open it in the current window
	 *
	 * @param repositoryUrl Git repository URL to clone
	 * @param destinationDirectory Parent directory where the repository should be cloned
	 */
	async cloneAndOpenRepository(repositoryUrl: string, destinationDirectory: string = '/projects'): Promise<void> {
		Logger.debug(`repositoryUrl: "${repositoryUrl}", destinationDirectory: "${destinationDirectory}"`);

		await this.openExplorerView();
		await this.driverHelper.waitAndClick(ExplorerView.CLONE_REPOSITORY_BUTTON);
		await this.driverHelper.wait(TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING);

		await this.driverHelper.getDriver().actions().sendKeys(repositoryUrl).perform();
		await this.driverHelper.wait(TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING);
		await this.driverHelper.getDriver().actions().sendKeys(Key.ENTER).perform();
		await this.driverHelper.wait(TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING);

		await this.driverHelper.getDriver().actions().keyDown(Key.CONTROL).sendKeys('a').keyUp(Key.CONTROL).perform();
		await this.driverHelper.getDriver().actions().sendKeys(destinationDirectory).perform();
		await this.driverHelper.wait(TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING);
		await this.driverHelper.getDriver().actions().sendKeys(Key.ENTER).perform();

		await this.driverHelper.waitAndClick(ExplorerView.OPEN_REPOSITORY_BUTTON, TIMEOUT_CONSTANTS.TS_COMMON_PLUGIN_TEST_TIMEOUT);
		await this.driverHelper.waitVisibility(ExplorerView.WORKBENCH_SIDE_BAR, TIMEOUT_CONSTANTS.TS_IDE_START_TIMEOUT);
	}

	async waitEmptyExplorer(timeout: number = TIMEOUT_CONSTANTS.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM): Promise<void> {
		Logger.debug();

		await this.openExplorerView();
		await this.driverHelper.waitVisibility(ExplorerView.EMPTY_EXPLORER, timeout);
	}

	async waitWorkbenchSideBarDisappearance(
		attempts: number = TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_ATTEMPTS,
		polling: number = TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING
	): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitDisappearance(ExplorerView.WORKBENCH_SIDE_BAR, attempts, polling);
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
