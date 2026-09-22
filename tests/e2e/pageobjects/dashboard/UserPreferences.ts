/** *******************************************************************
 * copyright (c) 2019-2026 Red Hat, Inc.
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
	private static readonly USER_PREFERENCES_BUTTON: By = By.xpath('//span[text()="User Preferences"]');
	private static readonly USER_PREFERENCES_PAGE: By = By.xpath('//h1[text()="User Preferences"]');

	private static readonly CONTAINER_REGISTRIES_TAB: By = By.xpath('//button[text()="Container Registries"]');

	private static readonly GIT_SERVICES_TAB: By = By.id('pf-tab-GitServices-user-preferences-tabs');
	private static readonly GIT_SERVICES_REVOKE_BUTTON: By = By.css('button[data-testid="bulk-revoke-button"]');

	private static readonly PAT_TAB: By = By.xpath('//button[text()="Personal Access Tokens"]');
	private static readonly ADD_NEW_PAT_BUTTON: By = By.xpath('//button[text()="Add Personal Access Token"]');

	private static readonly GIT_CONFIG_PAGE: By = By.xpath('//button[text()="Gitconfig"]');
	private static readonly GIT_CONFIG_USER_NAME: By = By.id('gitconfig-user-name');
	private static readonly GIT_CONFIG_USER_EMAIL: By = By.id('gitconfig-user-email');
	private static readonly GIT_CONFIG_SAVE_BUTTON: By = By.css('[data-testid="button-save"]');

	private static readonly SSH_KEY_TAB: By = By.xpath('//button[text()="SSH Keys"]');
	private static readonly ADD_NEW_SSH_KEY_BUTTON: By = By.css('button[aria-label="Add SSH Key"]');
	private static readonly ADD_SSH_KEYS_POPUP: By = By.xpath('//span[text()="Add SSH Keys"]');
	private static readonly PASTE_PRIVATE_SSH_KEY_FIELD: By = By.id('ssh-private-key');
	private static readonly PASTE_PUBLIC_SSH_KEY_FIELD: By = By.id('ssh-public-key');
	private static readonly PASTE_SSH_KEY_PASSPHRASE_FIELD: By = By.css('input[placeholder="Enter passphrase (optional)"]');
	private static readonly ADD_SSH_KEYS_BUTTON: By = By.xpath('//span[text()="Add"]');
	private static readonly GIT_SSH_KEY_NAME: By = By.css('[data-testid="title"]');
	private static readonly GIT_SSH_KEY_ACTIONS_BUTTON: By = By.css('section[id*="SshKeys-user-preferences"] button[aria-label="Actions"]');
	private static readonly DELETE_BUTTON: By = By.xpath('//span[text()="Delete"]');
	private static readonly CONFIRM_DELETE_SSH_KEYS_POPUP: By = By.css('div[id^="pf-modal-part"][role="dialog"]');
	private static readonly CONFIRM_DELETE_SSH_KEYS_CHECKBOX: By = By.id('delete-ssh-keys-warning-checkbox');

	private static readonly CONFIRMATION_WINDOW: By = By.xpath('//span[text()="Revoke Git Service"]');
	private static readonly DELETE_CONFIRMATION_CHECKBOX: By = By.id('revoke-warning-info-check');
	private static readonly DELETE_ITEM_BUTTON_ENABLED: By = By.css('button[data-testid="revoke-button"]:not([disabled])');

	private static readonly AI_PROVIDER_KEYS_TAB: By = By.xpath('//button[text()="AI Providers Keys"]');
	private static readonly AI_ADD_KEY_EMPTY_STATE_BUTTON: By = By.css('button[aria-label="Add AI Provider Key"]');
	private static readonly AI_ADD_EDIT_MODAL: By = By.css('div[aria-label="add-edit-ai-provider-key"]');
	private static readonly AI_PROVIDER_SELECT_TOGGLE: By = By.css('div[aria-label="add-edit-ai-provider-key"] button.pf-v6-c-menu-toggle');
	private static readonly AI_API_KEY_INPUT: By = By.id('ai-provider-api-key');
	private static readonly AI_SAVE_BUTTON: By = By.css('button[data-testid="save-button"]');
	private static readonly AI_BULK_DELETE_BUTTON: By = By.css('button[data-testid="bulk-delete-ai-key-button"]');
	private static readonly AI_PROVIDER_TABLE: By = By.css('table[aria-label="AI Provider Keys"]');
	private static readonly AI_DELETE_CONFIRM_CHECKBOX: By = By.id('delete-ai-key-warning-checkbox');

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

	async enterGitConfigUsernameAndEmail(userName: string, userEmail: string): Promise<void> {
		Logger.debug(`"${userName}"`);
		Logger.debug(`"${userEmail}"`);

		await this.driverHelper.enterValue(UserPreferences.GIT_CONFIG_USER_NAME, userName);
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
			'disabled',
			'true',
			TIMEOUT_CONSTANTS.TS_COMMON_DASHBOARD_WAIT_TIMEOUT
		);
	}

	async ensureGitConfig(userName: string, userEmail: string): Promise<void> {
		await this.openUserPreferencesPage();
		await this.openGitConfigPage();

		// check if the values are already set correctly
		const currentUserName: string = await this.driverHelper.waitAndGetElementAttribute(UserPreferences.GIT_CONFIG_USER_NAME, 'value');
		const currentEmail: string = await this.driverHelper.waitAndGetElementAttribute(UserPreferences.GIT_CONFIG_USER_EMAIL, 'value');

		if (currentUserName === userName && currentEmail === userEmail) {
			Logger.info(`Git config already set correctly: name="${userName}", email="${userEmail}"`);
			return;
		}

		Logger.info(`Setting git config: name="${userName}", email="${userEmail}"`);
		await this.enterGitConfigUsernameAndEmail(userName, userEmail);
		await this.clickOnGitConfigSaveButton();
		await this.waitGitConfigSaveButtonIsDisabled();
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

	async addSshKeysFromFiles(privateSshKeyPath: string, publicSshKeyPath: string): Promise<void> {
		Logger.debug();

		Logger.info('Adding new SSH keys from files');
		await this.driverHelper.waitAndClick(UserPreferences.ADD_NEW_SSH_KEY_BUTTON);
		await this.driverHelper.waitVisibility(UserPreferences.ADD_SSH_KEYS_POPUP);
		await this.uploadSshKeys(privateSshKeyPath, publicSshKeyPath);
		await this.driverHelper.waitAndClick(UserPreferences.ADD_SSH_KEYS_BUTTON);
		await this.driverHelper.waitVisibility(UserPreferences.GIT_SSH_KEY_NAME);
		Logger.info('SSH keys have been added');
	}

	async addSshKeysFromStrings(privateSshKey: string, publicSshKey: string, passphrase?: string): Promise<void> {
		Logger.debug();

		if (!privateSshKey || privateSshKey === '') {
			throw new Error('Private SSH key is empty or not provided');
		}
		if (!publicSshKey || publicSshKey === '') {
			throw new Error('Public SSH key is empty or not provided');
		}

		Logger.info('Adding new SSH keys from strings');
		await this.driverHelper.waitAndClick(UserPreferences.ADD_NEW_SSH_KEY_BUTTON);
		await this.driverHelper.waitVisibility(UserPreferences.ADD_SSH_KEYS_POPUP);

		Logger.info('Pasting private SSH key');
		await this.driverHelper.waitAndClick(UserPreferences.PASTE_PRIVATE_SSH_KEY_FIELD);
		await this.driverHelper.type(UserPreferences.PASTE_PRIVATE_SSH_KEY_FIELD, privateSshKey);

		// verify private SSH key was correctly set
		const enteredPrivateKey: string = await this.driverHelper.waitAndGetValue(
			UserPreferences.PASTE_PRIVATE_SSH_KEY_FIELD,
			TIMEOUT_CONSTANTS.TS_COMMON_DASHBOARD_WAIT_TIMEOUT
		);
		if (enteredPrivateKey !== privateSshKey) {
			throw new Error('Private SSH key was not correctly set in the field');
		}
		Logger.info('Private SSH key verified successfully');

		Logger.info('Pasting public SSH key');
		await this.driverHelper.waitAndClick(UserPreferences.PASTE_PUBLIC_SSH_KEY_FIELD);
		await this.driverHelper.type(UserPreferences.PASTE_PUBLIC_SSH_KEY_FIELD, publicSshKey);

		// verify public SSH key was correctly set
		const enteredPublicKey: string = await this.driverHelper.waitAndGetValue(
			UserPreferences.PASTE_PUBLIC_SSH_KEY_FIELD,
			TIMEOUT_CONSTANTS.TS_COMMON_DASHBOARD_WAIT_TIMEOUT
		);
		if (enteredPublicKey !== publicSshKey) {
			throw new Error('Public SSH key was not correctly set in the field');
		}
		Logger.info('Public SSH key verified successfully');

		if (passphrase) {
			Logger.info('Pasting SSH key passphrase');
			await this.driverHelper.waitAndClick(UserPreferences.PASTE_SSH_KEY_PASSPHRASE_FIELD);
			await this.driverHelper.getAction().sendKeys(passphrase).perform();
		}

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

	async openAiProviderKeysTab(): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitAndClick(UserPreferences.AI_PROVIDER_KEYS_TAB);
	}

	async waitAiProviderKeysTab(timeout: number = TIMEOUT_CONSTANTS.TS_COMMON_DASHBOARD_WAIT_TIMEOUT): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitVisibility(UserPreferences.AI_PROVIDER_KEYS_TAB, timeout);
	}

	async isAiProviderKeyPresent(providerId: string): Promise<boolean> {
		Logger.debug(`providerId: "${providerId}"`);

		return await this.driverHelper.isVisible(this.getAiProviderRowLocator(providerId));
	}

	async waitAiProviderKeyPresent(
		providerId: string,
		timeout: number = TIMEOUT_CONSTANTS.TS_COMMON_DASHBOARD_WAIT_TIMEOUT
	): Promise<void> {
		Logger.debug(`providerId: "${providerId}"`);

		await this.driverHelper.waitVisibility(this.getAiProviderRowLocator(providerId), timeout);
	}

	async addAiProviderKey(providerName: string, apiKey: string): Promise<void> {
		Logger.debug(`providerName: "${providerName}"`);

		await this.driverHelper.waitAndClick(UserPreferences.AI_ADD_KEY_EMPTY_STATE_BUTTON);
		await this.driverHelper.waitVisibility(UserPreferences.AI_ADD_EDIT_MODAL);
		await this.selectAiProvider(providerName);
		await this.driverHelper.waitVisibility(UserPreferences.AI_API_KEY_INPUT);
		await this.driverHelper.enterValue(UserPreferences.AI_API_KEY_INPUT, apiKey);
		await this.driverHelper.waitAndClick(UserPreferences.AI_SAVE_BUTTON);

		const attempts: number = Math.ceil(
			TIMEOUT_CONSTANTS.TS_COMMON_DASHBOARD_WAIT_TIMEOUT / TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING
		);
		await this.driverHelper.waitDisappearance(
			UserPreferences.AI_ADD_EDIT_MODAL,
			attempts,
			TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING
		);
	}

	async selectAiProvider(providerName: string): Promise<void> {
		Logger.debug(`providerName: "${providerName}"`);

		await this.driverHelper.waitAndClick(UserPreferences.AI_PROVIDER_SELECT_TOGGLE);
		await this.driverHelper.waitAndClick(this.getAiProviderSelectOptionLocator(providerName));
	}

	async getAiProviderEnvVarName(providerId: string): Promise<string> {
		Logger.debug(`providerId: "${providerId}"`);

		return await this.driverHelper.waitAndGetText(this.getAiProviderEnvVarLocator(providerId));
	}

	async deleteAiProviderKeys(providerName: string): Promise<void> {
		Logger.debug(`providerName: "${providerName}"`);

		Logger.info('Deleting AI Provider keys');
		const rows: By = By.css('table[aria-label="AI Provider Keys"] tbody tr input[type="checkbox"]');
		const hasRows: boolean = await this.driverHelper.isVisible(rows);

		if (!hasRows) {
			Logger.info('No AI Provider keys to delete');
			return;
		}

		const selectAllCheckbox: By = By.css('table[aria-label="AI Provider Keys"] thead input[type="checkbox"]');
		await this.driverHelper.waitAndClick(selectAllCheckbox);
		await this.driverHelper.waitAndClick(UserPreferences.AI_BULK_DELETE_BUTTON);
		await this.driverHelper.waitAndClick(UserPreferences.AI_DELETE_CONFIRM_CHECKBOX);
		await this.driverHelper.waitAndClick(this.getAiDeleteConfirmButtonLocator(providerName));
		await this.driverHelper.waitDisappearance(rows);
		Logger.info('AI Provider keys have been deleted');
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

	private getAiProviderRowLocator(providerId: string): By {
		return By.css(`tr[data-testid="${providerId}"]`);
	}

	private getAiProviderSelectOptionLocator(providerName: string): By {
		return By.xpath(`//button[@role="option"]//span[text()="${providerName}"]`);
	}

	private getAiProviderEnvVarLocator(providerId: string): By {
		return By.xpath(`//tr[@data-testid="${providerId}"]//td[@data-label="Environment Variable"]//code`);
	}

	private getAiDeleteConfirmButtonLocator(providerName: string): By {
		return By.xpath(`//div[@aria-label="Delete ${providerName} API Key"]//span[text()="Delete"]`);
	}
}
