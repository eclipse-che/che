/** *******************************************************************
 * copyright (c) 2019-2025 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { inject, injectable } from 'inversify';
import 'reflect-metadata';
import * as fs from 'fs';
import * as path from 'path';
import { CLASSES } from '../../configs/inversify.types';
import { By } from 'selenium-webdriver';
import { DriverHelper } from '../../utils/DriverHelper';
import { Logger } from '../../utils/Logger';
import { GitProviderType } from '../../constants/FACTORY_TEST_CONSTANTS';
import { TIMEOUT_CONSTANTS } from '../../constants/TIMEOUT_CONSTANTS';

@injectable()
export class UserPreferences {
	private static readonly USER_SETTINGS_DROPDOWN: By = By.xpath('//header//button/span[text()!=""]//parent::button');
	private static readonly USER_PREFERENCES_BUTTON: By = By.xpath('//button[text()="User Preferences"]');
	private static readonly USER_PREFERENCES_PAGE: By = By.xpath('//h1[text()="User Preferences"]');

	private static readonly CONTAINER_REGISTRIES_TAB: By = By.xpath('//button[text()="Container Registries"]');

	private static readonly GIT_SERVICES_TAB: By = By.xpath('//button[text()="Git Services"]');
	private static readonly GIT_SERVICES_REVOKE_BUTTON: By = By.xpath('//button[text()="Revoke"]');

	private static readonly PAT_TAB: By = By.xpath('//button[text()="Personal Access Tokens"]');
	private static readonly ADD_NEW_PAT_BUTTON: By = By.xpath('//button[text()="Add Personal Access Token"]');

	private static readonly GIT_CONFIG_PAGE: By = By.xpath('//button[text()="Gitconfig"]');
	private static readonly GIT_CONFIG_USER_NAME: By = By.id('gitconfig-user-name');
	private static readonly GIT_CONFIG_USER_EMAIL: By = By.id('gitconfig-user-email');
	private static readonly GIT_CONFIG_SAVE_BUTTON: By = By.css('[data-testid="button-save"]');

	private static readonly SSH_KEY_TAB: By = By.xpath('//button[text()="SSH Keys"]');
	private static readonly ADD_NEW_SSH_KEY_BUTTON: By = By.xpath('//button[text()="Add SSH Key"]');
	private static readonly ADD_SSH_KEYS_POPUP: By = By.xpath('//span[text()="Add SSH Keys"]');
	private static readonly PASTE_PRIVATE_SSH_KEY_FIELD: By = By.css('textarea[name="ssh-private-key"]');
	private static readonly PASTE_PUBLIC_SSH_KEY_FIELD: By = By.css('textarea[name="ssh-public-key"]');
	private static readonly ADD_SSH_KEYS_BUTTON: By = By.css('.pf-c-button.pf-m-primary');
	private static readonly GIT_SSH_KEY_NAME: By = By.css('[data-testid="title"]');
	private static readonly GIT_SSH_KEY_ACTIONS_BUTTON: By = By.css('section[id*="SshKeys-user-preferences"] button[aria-label="Actions"]');
	private static readonly DELETE_BUTTON: By = By.xpath('//button[text()="Delete"]');
	private static readonly CONFIRM_DELETE_SSH_KEYS_POPUP: By = By.css('div[id^="pf-modal-part"][role="dialog"]');
	private static readonly CONFIRM_DELETE_SSH_KEYS_CHECKBOX: By = By.id('delete-ssh-keys-warning-checkbox');

	private static readonly CONFIRMATION_WINDOW: By = By.xpath('//span[text()="Revoke Git Services"]');
	private static readonly DELETE_CONFIRMATION_CHECKBOX: By = By.xpath('//input[@data-testid="warning-info-checkbox"]');
	private static readonly DELETE_ITEM_BUTTON_ENABLED: By = By.xpath('//button[@data-testid="revoke-button" and not(@disabled)]');

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
		await this.checkAddSshKeyButtonAvailability();
	}

	async openContainerRegistriesTab(): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitAndClick(UserPreferences.CONTAINER_REGISTRIES_TAB);
	}

	async openGitServicesTab(): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitAndClick(UserPreferences.GIT_SERVICES_TAB);
	}

	async revokeGitService(servicesName: string): Promise<void> {
		Logger.debug();

		await this.selectListItem(servicesName);
		await this.driverHelper.waitAndClick(UserPreferences.GIT_SERVICES_REVOKE_BUTTON);

		await this.driverHelper.waitVisibility(UserPreferences.CONFIRMATION_WINDOW);
		await this.driverHelper.waitAndClick(UserPreferences.DELETE_CONFIRMATION_CHECKBOX);
		await this.driverHelper.waitAndClick(UserPreferences.DELETE_ITEM_BUTTON_ENABLED);

		await this.driverHelper.waitAttributeValue(
			this.getServicesListItemLocator(servicesName),
			'disabled',
			'true',
			TIMEOUT_CONSTANTS.TS_COMMON_DASHBOARD_WAIT_TIMEOUT
		);
	}

	async selectListItem(servicesName: string): Promise<void> {
		Logger.debug(`of the '${servicesName}' list item`);

		await this.driverHelper.waitAndClick(this.getServicesListItemLocator(servicesName));
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

	async enterGitConfigUserName(userName: string): Promise<void> {
		Logger.debug(`"${userName}"`);

		await this.driverHelper.enterValue(UserPreferences.GIT_CONFIG_USER_NAME, userName);
	}

	async enterGitConfigUserEmail(userEmail: string): Promise<void> {
		Logger.debug(`"${userEmail}"`);

		await this.driverHelper.enterValue(UserPreferences.GIT_CONFIG_USER_EMAIL, userEmail);
	}

	async clickOnGitConfigSaveButton(): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitAndClick(UserPreferences.GIT_CONFIG_SAVE_BUTTON);
	}

	async waitGitConfigSaveButtonIsDisabled(): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitAttributeValue(
			UserPreferences.GIT_CONFIG_SAVE_BUTTON,
			'aria-disabled',
			'true',
			TIMEOUT_CONSTANTS.TS_COMMON_DASHBOARD_WAIT_TIMEOUT
		);
	}

	async openSshKeyTab(): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitAndClick(UserPreferences.SSH_KEY_TAB);
	}

	async checkAddSshKeyButtonAvailability(): Promise<void> {
		Logger.debug();

		await this.openSshKeyTab();
		await this.driverHelper.waitVisibility(UserPreferences.ADD_NEW_SSH_KEY_BUTTON);
	}

	async addSshKeys(privateSshKeyPath: string, publicSshKeyPath: string): Promise<void> {
		Logger.debug();

		Logger.info('Adding new SSH keys');
		await this.driverHelper.waitAndClick(UserPreferences.ADD_NEW_SSH_KEY_BUTTON);
		await this.driverHelper.waitVisibility(UserPreferences.ADD_SSH_KEYS_POPUP);
		await this.uploadSshKeys(privateSshKeyPath, publicSshKeyPath);
		await this.driverHelper.waitAndClick(UserPreferences.ADD_SSH_KEYS_BUTTON);
		await this.driverHelper.waitVisibility(UserPreferences.GIT_SSH_KEY_NAME);
		Logger.info('SSH keys have been added');
	}

	async uploadSshKeys(privateSshKeyPath: string, publicSshKeyPath: string): Promise<void> {
		Logger.debug();
		const privateSshKey: string = Buffer.from(fs.readFileSync(path.resolve(privateSshKeyPath), 'utf-8'), 'base64').toString('utf-8');
		const publicSshKey: string = Buffer.from(fs.readFileSync(path.resolve(publicSshKeyPath), 'utf-8'), 'base64').toString('utf-8');

		Logger.info('Pasting private SSH key');
		await this.driverHelper.waitAndClick(UserPreferences.PASTE_PRIVATE_SSH_KEY_FIELD);
		await this.driverHelper.getAction().sendKeys(privateSshKey).perform();

		Logger.info('Pasting public SSH key');
		await this.driverHelper.waitAndClick(UserPreferences.PASTE_PUBLIC_SSH_KEY_FIELD);
		await this.driverHelper.getAction().sendKeys(publicSshKey).perform();
	}

	async isSshKeyPresent(): Promise<boolean> {
		Logger.debug();

		return this.driverHelper.isVisible(UserPreferences.GIT_SSH_KEY_NAME);
	}

	async deleteSshKeys(): Promise<void> {
		Logger.debug();

		Logger.info('Deleting SSH keys');
		await this.openSshKeyTab();
		await this.driverHelper.waitAndClick(
			UserPreferences.GIT_SSH_KEY_ACTIONS_BUTTON,
			TIMEOUT_CONSTANTS.TS_COMMON_DASHBOARD_WAIT_TIMEOUT
		);
		await this.driverHelper.waitAndClick(UserPreferences.DELETE_BUTTON);
		await this.driverHelper.waitVisibility(UserPreferences.CONFIRM_DELETE_SSH_KEYS_POPUP);
		await this.driverHelper.waitAndClick(UserPreferences.CONFIRM_DELETE_SSH_KEYS_CHECKBOX);
		await this.driverHelper.waitAndClick(UserPreferences.DELETE_BUTTON);
		await this.driverHelper.waitDisappearance(UserPreferences.GIT_SSH_KEY_NAME);
		Logger.info('SSH keys have been deleted');
	}

	getServiceConfig(service: string): string {
		const gitService: { [key: string]: string } = {
			[GitProviderType.GITHUB]: 'GitHub',
			[GitProviderType.GITLAB]: 'GitLab',
			[GitProviderType.AZURE_DEVOPS]: 'Microsoft Azure DevOps',
			[GitProviderType.BITBUCKET_CLOUD_OAUTH2]: 'Bitbucket Cloud',
			[GitProviderType.BITBUCKET_SERVER_OAUTH1]: 'Bitbucket Server',
			[GitProviderType.BITBUCKET_SERVER_OAUTH2]: 'Bitbucket Server'
		};

		return gitService[service];
	}

	private getServicesListItemLocator(servicesName: string): By {
		return By.xpath(`//tr[td[text()='${servicesName}']]//input`);
	}
}
