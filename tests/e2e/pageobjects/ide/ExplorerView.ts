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
import { ActivityBar, ViewItem, ViewSection } from 'monaco-page-objects';
import { DriverHelper } from '../../utils/DriverHelper';
import { Logger } from '../../utils/Logger';
import { CheCodeLocatorLoader } from './CheCodeLocatorLoader';
import { ProjectAndFileTests } from '../../tests-library/ProjectAndFileTests';

@injectable()
export class ExplorerView {
	constructor(
		@inject(CLASSES.DriverHelper)
		private readonly driverHelper: DriverHelper,
		@inject(CLASSES.CheCodeLocatorLoader)
		private readonly cheCodeLocatorLoader: CheCodeLocatorLoader,
		@inject(CLASSES.ProjectAndFileTests)
		private readonly projectAndFileTests: ProjectAndFileTests
	) {}

	async openExplorerView(): Promise<void> {
		Logger.debug();

		const explorerCtrl: any = await new ActivityBar().getViewControl('Explorer');
		await explorerCtrl?.openView();
	}

	async openFileContextMenu(fileName: string): Promise<void> {
		Logger.debug(`"${fileName}"`);

		await this.openExplorerView();

		const projectSection: ViewSection = await this.projectAndFileTests.getProjectViewSession();
		const fileItem: ViewItem | undefined = await this.projectAndFileTests.getProjectTreeItem(projectSection, fileName);

		if (!fileItem) {
			throw new Error(`Could not find ${fileName} file in explorer`);
		}

		await fileItem.openContextMenu();
		await this.waitContextMenuVisible();
	}

	async isContextMenuItemVisible(menuItemAriaLabel: string): Promise<boolean> {
		Logger.debug(`"${menuItemAriaLabel}"`);

		// add a small delay to ensure menu is stable
		await this.driverHelper.wait(500);

		const contextMenuItemLocator: By = By.css(`.monaco-menu-container [aria-label="${menuItemAriaLabel}"]`);
		return await this.driverHelper.waitVisibilityBoolean(contextMenuItemLocator, 5, 1000);
	}

	async closeContextMenu(): Promise<void> {
		Logger.debug();

		await this.driverHelper.getDriver().actions().sendKeys(Key.ESCAPE).perform();
		await this.driverHelper.wait(500);
	}

	private async waitContextMenuVisible(): Promise<void> {
		Logger.debug();

		// wait for container with increased timeout
		const containerVisible: boolean = await this.driverHelper.waitVisibilityBoolean(
			this.cheCodeLocatorLoader.webCheCodeLocators.ContextMenu.contextView,
			10,
			1000
		);

		if (!containerVisible) {
			throw new Error('Context menu container did not appear');
		}

		// brief pause to allow menu items to render
		await this.driverHelper.wait(800);

		// verify menu items are present
		const menuItemsLocator: By = By.css('.monaco-menu-container .action-item');
		const menuItemsVisible: boolean = await this.driverHelper.waitVisibilityBoolean(menuItemsLocator, 5, 1000);

		if (!menuItemsVisible) {
			throw new Error('Context menu items did not load properly');
		}
	}
}
