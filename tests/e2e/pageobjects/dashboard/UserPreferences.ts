/** *******************************************************************
 * copyright (c) 2019-2023 Red Hat, Inc.
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
import { By } from 'selenium-webdriver';
import { DriverHelper } from '../../utils/DriverHelper';
import { Logger } from '../../utils/Logger';

@injectable()
export class UserPreferences {
	private static readonly USER_SETTINGS_DROPDOWN: By = By.xpath('//header//button/span[text()!=""]//parent::button');
	private static readonly USER_PREFERENCES_BUTTON: By = By.xpath('//button[text()="User Preferences"]');
	private static readonly USER_PREFERENCES_PAGE: By = By.xpath('//h1[text()="User Preferences"]');

    private static readonly CONTAINER_REGISTRIES_TAB: By = By.xpath('//button[text()="Container Registries"]');
    private static readonly GIT_SERVICES_TAB: By = By.xpath('//button[text()="Git Services"]');

    private static readonly PAT_TAB: By = By.xpath('//button[text()="Personal Access Tokens"]');
    private static readonly ADD_NEW_PAT_BUTTON: By = By.xpath('//button[text()="Add Personal Access Token"]');

    private static readonly GIT_CONFIG_PAGE: By = By.xpath('//button[text()="Gitconfig"]');

    private static readonly SSH_KEY_TAB: By = By.xpath('//button[text()="SSH Keys"]');
    private static readonly ADD_NEW_SSH_KEY_BUTTON: By = By.xpath('//button[text()="Add SSH Key"]');

	constructor(
		@inject(CLASSES.DriverHelper)
		readonly driverHelper: DriverHelper
	) {}

	async openUserPreferencesPage(): Promise<void> {
        Logger.debug();

		await this.driverHelper.waitAndClick(UserPreferences.USER_SETTINGS_DROPDOWN);
		await this.driverHelper.waitAndClick(UserPreferences.USER_PREFERENCES_BUTTON);

		await this.driverHelper.waitVisibility(UserPreferences.USER_PREFERENCES_PAGE);
	}

    async checkTabsAvailability(): Promise<void> {
        Logger.debug();

        await this.openContainerRegistriesTab();
        await this.openGitServicesTab();
        await this.openPatTab();
        await this.openGitConfigPage();
        await this.openSshKeyTab();
    }

    async openContainerRegistriesTab(): Promise<void> {
        Logger.debug();

        await this.driverHelper.waitAndClick(UserPreferences.CONTAINER_REGISTRIES_TAB);
    }

    async openGitServicesTab(): Promise<void> {
        Logger.debug();

        await this.driverHelper.waitAndClick(UserPreferences.GIT_SERVICES_TAB);
    }

    async openPatTab(): Promise<void> {
        Logger.debug();

        await this.driverHelper.waitAndClick(UserPreferences.PAT_TAB);
        await this.driverHelper.waitVisibility(UserPreferences.ADD_NEW_PAT_BUTTON);
    }

    async openGitConfigPage(): Promise<void> {
        Logger.debug();

        await this.driverHelper.waitAndClick(UserPreferences.GIT_CONFIG_PAGE);
    }

    async openSshKeyTab(): Promise<void> {
        Logger.debug();

        await this.driverHelper.waitAndClick(UserPreferences.SSH_KEY_TAB);
        await this.driverHelper.waitVisibility(UserPreferences.ADD_NEW_SSH_KEY_BUTTON);
    }
}
