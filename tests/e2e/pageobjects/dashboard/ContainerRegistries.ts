/** *******************************************************************
 * copyright (c) 2026 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { inject, injectable } from 'inversify';
import { By } from 'selenium-webdriver';
import { CLASSES } from '../../configs/inversify.types';
import { TIMEOUT_CONSTANTS } from '../../constants/TIMEOUT_CONSTANTS';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import { DriverHelper } from '../../utils/DriverHelper';
import { Logger } from '../../utils/Logger';
import { Dashboard } from './Dashboard';
import { UserPreferences } from './UserPreferences';

@injectable()
export class ContainerRegistries {
	private static readonly OPEN_ADD_REGISTRY_DIALOG_BUTTON: By = By.xpath('//button[@aria-label="add-registry"]');
	private static readonly ADD_REGISTRY_DIALOG: By = By.xpath('//div[@aria-label="edit-registry-info"]');
	private static readonly REGISTRY_URL_INPUT: By = By.xpath('//input[@aria-label="Url input"]');
	private static readonly USERNAME_INPUT: By = By.xpath('//input[@aria-label="Username input"]');
	private static readonly PASSWORD_INPUT: By = By.xpath('//input[@data-testid="registry-password-input"]');
	private static readonly ADD_REGISTRY_BUTTON: By = By.xpath('//button[@data-testid="edit-button"]');
	private static readonly DELETE_REGISTRY_MENU_ITEM: By = By.xpath('//button[@role="menuitem" and normalize-space(.)="Delete registry"]');
	private static readonly DELETE_CONFIRMATION_WINDOW: By = By.css('div[aria-label="warning-info"]');
	private static readonly DELETE_CONFIRMATION_CHECKBOX: By = By.css('[data-testid="warning-info-checkbox"]');
	private static readonly DELETE_REGISTRY_BUTTON_ENABLED: By = By.css('button[data-testid="delete-button"]:not([disabled])');

	constructor(
		@inject(CLASSES.DriverHelper)
		private readonly driverHelper: DriverHelper,
		@inject(CLASSES.BrowserTabsUtil)
		private readonly browserTabsUtil: BrowserTabsUtil,
		@inject(CLASSES.Dashboard)
		private readonly dashboard: Dashboard,
		@inject(CLASSES.UserPreferences)
		private readonly userPreferences: UserPreferences
	) {}

	async openAddContainerRegistryDialog(): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitAndClick(ContainerRegistries.OPEN_ADD_REGISTRY_DIALOG_BUTTON);
		await this.driverHelper.waitVisibility(ContainerRegistries.ADD_REGISTRY_DIALOG);
	}

	async addContainerRegistry(registryUrl: string, username: string, password: string): Promise<void> {
		Logger.debug(`registryUrl: "${registryUrl}", username: "${username}"`);

		await this.userPreferences.openUserPreferencesPage();
		await this.userPreferences.openContainerRegistriesTab();
		await this.deleteContainerRegistryIfExists(registryUrl);
		await this.openAddContainerRegistryDialog();
		await this.driverHelper.enterValue(ContainerRegistries.REGISTRY_URL_INPUT, registryUrl);
		await this.driverHelper.enterValue(ContainerRegistries.USERNAME_INPUT, username);
		await this.driverHelper.enterValue(ContainerRegistries.PASSWORD_INPUT, password);
		await this.driverHelper.waitAndClick(ContainerRegistries.ADD_REGISTRY_BUTTON);

		const attempts: number = Math.ceil(
			TIMEOUT_CONSTANTS.TS_COMMON_DASHBOARD_WAIT_TIMEOUT / TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING
		);
		await this.driverHelper.waitDisappearance(
			ContainerRegistries.ADD_REGISTRY_DIALOG,
			attempts,
			TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING
		);
	}

	async waitContainerRegistryListItem(
		registryUrl: string,
		timeout: number = TIMEOUT_CONSTANTS.TS_COMMON_DASHBOARD_WAIT_TIMEOUT
	): Promise<void> {
		Logger.debug(`"${registryUrl}"`);

		await this.driverHelper.waitVisibility(this.getContainerRegistryListItemLocator(registryUrl), timeout);
	}

	async clickActionsButton(registryUrl: string): Promise<void> {
		Logger.debug(`of the '${registryUrl}' list item`);

		await this.driverHelper.waitAndClick(this.getActionsLocator(registryUrl));
	}

	async clickActionsDeleteButton(registryUrl: string): Promise<void> {
		Logger.debug(`for the '${registryUrl}' list item`);

		await this.driverHelper.waitAndClick(ContainerRegistries.DELETE_REGISTRY_MENU_ITEM);
	}

	async waitDeleteRegistryConfirmationWindow(timeout: number = TIMEOUT_CONSTANTS.TS_COMMON_DASHBOARD_WAIT_TIMEOUT): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitVisibility(ContainerRegistries.DELETE_CONFIRMATION_WINDOW, timeout);
	}

	async clickToDeleteConfirmationCheckbox(timeout: number = TIMEOUT_CONSTANTS.TS_COMMON_DASHBOARD_WAIT_TIMEOUT): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitAndClick(ContainerRegistries.DELETE_CONFIRMATION_CHECKBOX, timeout);
	}

	async waitAndClickEnabledConfirmationWindowDeleteButton(
		timeout: number = TIMEOUT_CONSTANTS.TS_COMMON_DASHBOARD_WAIT_TIMEOUT
	): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitAndClick(ContainerRegistries.DELETE_REGISTRY_BUTTON_ENABLED, timeout);
	}

	async deleteContainerRegistryByActionsButton(
		registryUrl: string,
		timeout: number = TIMEOUT_CONSTANTS.TS_COMMON_DASHBOARD_WAIT_TIMEOUT
	): Promise<void> {
		Logger.debug(`"${registryUrl}"`);

		await this.waitContainerRegistryListItem(registryUrl, timeout);
		await this.clickActionsButton(registryUrl);
		await this.clickActionsDeleteButton(registryUrl);
		await this.waitDeleteRegistryConfirmationWindow(timeout);
		await this.clickToDeleteConfirmationCheckbox(timeout);
		await this.waitAndClickEnabledConfirmationWindowDeleteButton(timeout);
	}

	async deleteContainerRegistry(registryUrl: string): Promise<void> {
		Logger.debug(`"${registryUrl}"`);

		await this.dashboard.openDashboard();
		await this.browserTabsUtil.closeAllTabsExceptCurrent();
		await this.userPreferences.openUserPreferencesPage();
		await this.userPreferences.openContainerRegistriesTab();
		await this.deleteContainerRegistryByActionsButton(registryUrl);
		await this.waitContainerRegistryListItemAbsence(registryUrl);
	}

	async deleteContainerRegistryIfExists(
		registryUrl: string,
		timeout: number = TIMEOUT_CONSTANTS.TS_COMMON_DASHBOARD_WAIT_TIMEOUT
	): Promise<void> {
		Logger.debug(`"${registryUrl}"`);

		const polling: number = TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING;
		const attempts: number = Math.ceil(timeout / polling);
		const isPresent: boolean = await this.driverHelper.waitVisibilityBoolean(
			this.getContainerRegistryListItemLocator(registryUrl),
			attempts,
			polling
		);

		if (!isPresent) {
			Logger.info(`Container registry "${registryUrl}" does not exist`);
			return;
		}

		await this.deleteContainerRegistryByActionsButton(registryUrl, timeout);
		await this.waitContainerRegistryListItemAbsence(registryUrl, timeout);
	}

	async waitContainerRegistryListItemAbsence(
		registryUrl: string,
		timeout: number = TIMEOUT_CONSTANTS.TS_COMMON_DASHBOARD_WAIT_TIMEOUT
	): Promise<void> {
		Logger.debug(`"${registryUrl}"`);

		const polling: number = TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING;
		const attempts: number = Math.ceil(timeout / polling);

		await this.driverHelper.waitDisappearance(this.getContainerRegistryListItemLocator(registryUrl), attempts, polling);
	}

	private getContainerRegistryListItemLocator(registryUrl: string): By {
		return By.xpath(`//table[@aria-label='Container credentials']//tr[td[@data-label='Host' and normalize-space(.)='${registryUrl}']]`);
	}

	private getActionsLocator(registryUrl: string): By {
		return By.xpath(`${this.getContainerRegistryListItemLocator(registryUrl).value}//button[@aria-label='Actions']`);
	}
}
