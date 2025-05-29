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
import { By, Key } from 'selenium-webdriver';
import { ActivityBar } from 'monaco-page-objects';
import { DriverHelper } from '../../utils/DriverHelper';
import { Logger } from '../../utils/Logger';
import { CheCodeLocatorLoader } from './CheCodeLocatorLoader';
import { ProjectAndFileTests } from '../../tests-library/ProjectAndFileTests';

@injectable()
export class ExplorerView {
	constructor(
		@inject(CLASSES.DriverHelper)
		readonly driverHelper: DriverHelper,
		@inject(CLASSES.CheCodeLocatorLoader)
		readonly cheCodeLocatorLoader: CheCodeLocatorLoader,
		@inject(CLASSES.ProjectAndFileTests)
		readonly projectAndFileTests: ProjectAndFileTests
	) {}

	async checkFileContextMenuItem(fileName: string, menuItemAriaLabel: string): Promise<boolean> {
		try {
			const explorerCtrl: any = await new ActivityBar().getViewControl('Explorer');
			await explorerCtrl?.openView();

			const projectSection: any = await this.projectAndFileTests.getProjectViewSession();
			if (!projectSection) {
				throw new Error('Failed to get project tree section');
			}

			const vsixFileItem: any = await projectSection.findItem(fileName);
			if (!vsixFileItem) {
				Logger.warn(`Could not find ${fileName} file in explorer`);
				return false;
			}

			await vsixFileItem.openContextMenu();

			const menuVisible: boolean = await this.driverHelper.waitVisibilityBoolean(
				this.cheCodeLocatorLoader.webCheCodeLocators.ContextMenu.contextView,
				5,
				1000
			);
			if (!menuVisible) {
				throw new Error('Context menu not visible after right-click');
			}

			const vsixContextMenuItemLocator: By = By.xpath(`//span[@aria-label="${menuItemAriaLabel}"]`);
			return await this.driverHelper.waitVisibilityBoolean(vsixContextMenuItemLocator, 5, 1000);
		} catch (error) {
			Logger.error(`Error in context menu test: ${error}`);
			throw error;
		} finally {
			await this.driverHelper.getDriver().actions().sendKeys(Key.ESCAPE).perform();
			await this.driverHelper.wait(500);
		}
	}
}
