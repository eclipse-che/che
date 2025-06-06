/** *******************************************************************
 * copyright (c) 2024-2025 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { inject, injectable } from 'inversify';
import 'reflect-metadata';
import { e2eContainer } from '../../configs/inversify.config';
import { CLASSES } from '../../configs/inversify.types';
import { By } from 'selenium-webdriver';
import { DriverHelper } from '../../utils/DriverHelper';
import { Logger } from '../../utils/Logger';
import { TIMEOUT_CONSTANTS } from '../../constants/TIMEOUT_CONSTANTS';
import { CheCodeLocatorLoader } from './CheCodeLocatorLoader';
import { ContextMenu, NewScmView, SingleScmProvider, Locators } from 'monaco-page-objects';

@injectable()
export class ViewsMoreActionsButton {
	private static readonly VIEWS_AND_MORE_ACTIONS_BUTTON: By = By.xpath('//a[@aria-label="Views and More Actions..."]');

	constructor(
		@inject(CLASSES.DriverHelper)
		readonly driverHelper: DriverHelper
	) {}

	async closeSourceControlGraph(): Promise<void> {
		Logger.debug();

		const cheCodeLocatorLoader: CheCodeLocatorLoader = e2eContainer.get(CLASSES.CheCodeLocatorLoader);
		const webCheCodeLocators: Locators = cheCodeLocatorLoader.webCheCodeLocators;

		const scmView: NewScmView = new NewScmView();
		const [scmProvider]: SingleScmProvider[] = await scmView.getProviders();

		Logger.debug('scmProvider.openMoreActions');
		const scmContextMenu: ContextMenu = await scmProvider.openMoreActions();
		await this.driverHelper.waitVisibility(webCheCodeLocators.ContextMenu.contextView);
		Logger.debug('scmContextMenu.select: "Graph"');
		await scmContextMenu.select('Graph');
	}

	async viewsAndMoreActionsButtonIsVisible(timeout: number = TIMEOUT_CONSTANTS.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM): Promise<boolean> {
		Logger.debug();

		const viewsActionsButton: boolean = await this.driverHelper.waitVisibilityBoolean(
			ViewsMoreActionsButton.VIEWS_AND_MORE_ACTIONS_BUTTON,
			timeout
		);

		return viewsActionsButton;
	}

	async clickViewsMoreActionsButton(): Promise<void> {
		Logger.debug();
		await this.driverHelper.waitAndClick(ViewsMoreActionsButton.VIEWS_AND_MORE_ACTIONS_BUTTON);
	}

	async waitForContextMenu(): Promise<void> {
		const cheCodeLocatorLoader: CheCodeLocatorLoader = e2eContainer.get(CLASSES.CheCodeLocatorLoader);
		const webCheCodeLocators: Locators = cheCodeLocatorLoader.webCheCodeLocators;
		await this.driverHelper.waitVisibility(webCheCodeLocators.ContextMenu.contextView);
	}
}
