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
import { CLASSES } from '../../configs/inversify.types';
import { DriverHelper } from '../../utils/DriverHelper';
import { By, Key, WebElement } from 'selenium-webdriver';
import { Logger } from '../../utils/Logger';
import { TIMEOUT_CONSTANTS } from '../../constants/TIMEOUT_CONSTANTS';
import { BASE_TEST_CONSTANTS } from '../../constants/BASE_TEST_CONSTANTS';
import { TrustAuthorPopup } from './TrustAuthorPopup';

@injectable()
export class CreateWorkspace {
	private static readonly FACTORY_URL: By = By.id('git-repo-url');
	private static readonly GIT_REPO_OPTIONS: By = By.xpath('//span[text()="Git Repo Options"]');

	private static readonly GIT_BRANCH_SELECT_FIELD: By = By.xpath('//span[text()="Select the branch of the Git Repository"]');
	private static readonly GIT_FILTER_BRANCHES: By = By.css('input[placeholder="Filter branches"]');
	private static readonly CREATE_AND_OPEN_BUTTON: By = By.id('create-and-open-button');
	private static readonly CREATE_NEW_WORKPACE_CHECKBOX: By = By.css('label[for="create-new-if-exist-switch"]');
	private static readonly CREATE_NEW_WORKPACE_CHECKBOX_VALUE: By = By.id('create-new-if-exist-switch');

	private static readonly AI_PROVIDER_SELECTOR_HEADING: By = By.xpath('//h3[text()="AI Provider Selector"]');
	private static readonly AI_PROVIDER_CHOOSE_TOGGLE: By = By.id('accordion-item-ai-selector');
	private static readonly AI_PROVIDER_KEY_CONFIGURED_BADGE: By = By.xpath('//span[text()=" Key configured"]');

	constructor(
		@inject(CLASSES.DriverHelper)
		private readonly driverHelper: DriverHelper,

		@inject(CLASSES.TrustAuthorPopup)
		private readonly trustAuthorPopup: TrustAuthorPopup
	) {}

	async waitTitleContains(expectedText: string, timeout: number = TIMEOUT_CONSTANTS.TS_COMMON_DASHBOARD_WAIT_TIMEOUT): Promise<void> {
		Logger.debug(`text: "${expectedText}"`);

		const pageTitleLocator: By = By.xpath(`//h1[contains(text(), '${expectedText}')]`);

		await this.driverHelper.waitVisibility(pageTitleLocator, timeout);
	}

	async waitPage(timeout: number = TIMEOUT_CONSTANTS.TS_SELENIUM_LOAD_PAGE_TIMEOUT): Promise<void> {
		Logger.debug();

		await this.waitTitleContains('Create Workspace', timeout);
	}

	async clickOnSampleNoEditorSelection(
		sampleName: string,
		timeout: number = TIMEOUT_CONSTANTS.TS_CLICK_DASHBOARD_ITEM_TIMEOUT
	): Promise<void> {
		Logger.debug(`sampleName: "${sampleName}"`);

		const sampleLocator: By = this.getSampleLocator(sampleName);

		await this.driverHelper.waitAndClick(sampleLocator, timeout);
	}

	async clickOnSampleForSpecificEditor(
		sampleName: string,
		timeout: number = TIMEOUT_CONSTANTS.TS_CLICK_DASHBOARD_ITEM_TIMEOUT
	): Promise<void> {
		await this.clickOnEditorsDropdownListButton(sampleName, timeout);

		Logger.debug(`sampleName: "${sampleName}"`);

		const sampleLocator: By = this.getSampleWithSpecificEditorLocator(sampleName);
		await this.driverHelper.waitAndClick(sampleLocator, timeout);
	}

	async importFromGitUsingUI(
		factoryUrl: string,
		branchName?: string,
		timeout: number = TIMEOUT_CONSTANTS.TS_COMMON_DASHBOARD_WAIT_TIMEOUT
	): Promise<void> {
		Logger.debug(`factoryUrl: "${factoryUrl}"`);

		await this.driverHelper.waitVisibility(CreateWorkspace.FACTORY_URL, timeout);
		await this.driverHelper.type(CreateWorkspace.FACTORY_URL, Key.chord(factoryUrl), timeout);

		if (branchName) {
			await this.driverHelper.waitAndClick(CreateWorkspace.GIT_REPO_OPTIONS, timeout);
			await this.driverHelper.waitAndClick(CreateWorkspace.GIT_BRANCH_SELECT_FIELD, timeout);
			await this.driverHelper.waitAndClick(CreateWorkspace.GIT_FILTER_BRANCHES, timeout);
			await this.driverHelper.type(CreateWorkspace.GIT_FILTER_BRANCHES, Key.chord(branchName), timeout);
			await this.driverHelper.waitAndClick(this.getGitBranchListItemLocator(branchName), timeout);
		}

		await this.driverHelper.waitAndClick(CreateWorkspace.CREATE_AND_OPEN_BUTTON, timeout);
		await this.performTrustAuthorPopup();
	}

	async setGitRepositoryUrl(factoryUrl: string, timeout: number = TIMEOUT_CONSTANTS.TS_CLICK_DASHBOARD_ITEM_TIMEOUT): Promise<void> {
		Logger.debug(`factoryUrl: "${factoryUrl}"`);
		await this.driverHelper.waitVisibility(CreateWorkspace.FACTORY_URL, timeout);
		await this.driverHelper.type(CreateWorkspace.FACTORY_URL, factoryUrl, timeout);

		const actualFactoryUrl: string = await this.getGitRepositoryUrl(timeout);
		Logger.info(`[INFO] Git repository URL set to "${actualFactoryUrl}"`);
	}

	async getGitRepositoryUrl(timeout: number = TIMEOUT_CONSTANTS.TS_CLICK_DASHBOARD_ITEM_TIMEOUT): Promise<string> {
		Logger.debug();
		return await this.driverHelper.waitAndGetValue(CreateWorkspace.FACTORY_URL, timeout);
	}

	async clickOnCreateAndOpenButton(timeout: number = TIMEOUT_CONSTANTS.TS_CLICK_DASHBOARD_ITEM_TIMEOUT): Promise<void> {
		Logger.debug();
		await this.driverHelper.waitAndClick(CreateWorkspace.CREATE_AND_OPEN_BUTTON, timeout);
	}

	async clickOnEditorsDropdownListButton(sampleName: string, timeout: number): Promise<void> {
		Logger.debug(`sampleName: "${sampleName}, editor ${BASE_TEST_CONSTANTS.TS_SELENIUM_EDITOR}"`);

		const editorDropdownListLocator: By = this.getEditorsDropdownListLocator(sampleName);
		await this.driverHelper.waitAndClick(editorDropdownListLocator, timeout);
	}

	async performTrustAuthorPopup(): Promise<void> {
		Logger.debug();

		try {
			await this.trustAuthorPopup.clickContinue();
		} catch (e) {
			Logger.info('"Trust author" popup was not shown');
		}
	}

	async isCreateNewWorkspaceCheckboxChecked(timeout: number = TIMEOUT_CONSTANTS.TS_SELENIUM_WAIT_FOR_URL): Promise<boolean> {
		Logger.debug();

		const element: WebElement = await this.driverHelper.waitPresence(CreateWorkspace.CREATE_NEW_WORKPACE_CHECKBOX_VALUE, timeout);
		return await element.isSelected();
	}

	async waitForCheckboxState(
		expectedState: boolean,
		timeout: number = TIMEOUT_CONSTANTS.TS_COMMON_DASHBOARD_WAIT_TIMEOUT
	): Promise<void> {
		Logger.debug(`waiting for checkbox to be ${expectedState ? 'checked' : 'unchecked'}`);

		const polling: number = 500;
		const attempts: number = Math.ceil(timeout / polling);

		for (let i: number = 0; i < attempts; i++) {
			const currentState: boolean = await this.isCreateNewWorkspaceCheckboxChecked();
			if (currentState === expectedState) {
				Logger.debug(`Checkbox reached expected state: ${expectedState}`);
				return;
			}
			await this.driverHelper.wait(polling);
		}

		throw new Error(`Checkbox did not reach expected state ${expectedState} within ${timeout}ms`);
	}

	async clickOnCreateNewWorkspaceCheckbox(timeout: number = TIMEOUT_CONSTANTS.TS_SELENIUM_WAIT_FOR_URL): Promise<void> {
		Logger.debug();

		await this.driverHelper.scrollToAndClick(CreateWorkspace.CREATE_NEW_WORKPACE_CHECKBOX, timeout);
	}

	async setCreateNewWorkspaceCheckbox(
		checked: boolean = true,
		timeout: number = TIMEOUT_CONSTANTS.TS_SELENIUM_WAIT_FOR_URL
	): Promise<void> {
		Logger.debug(`checked: ${checked}`);

		// check current state
		const isCurrentlyChecked: boolean = await this.isCreateNewWorkspaceCheckboxChecked(timeout);

		// if already in desired state, do nothing
		if (isCurrentlyChecked === checked) {
			Logger.debug(`Checkbox is already ${checked ? 'set' : 'unset'}, no action needed`);
			return;
		}

		// click to change state
		Logger.debug(`Checkbox is ${isCurrentlyChecked ? 'set' : 'unset'}, ${checked ? 'setting' : 'unsetting'} it now`);
		await this.driverHelper.wait(TIMEOUT_CONSTANTS.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM); // wait for any potential UI updates before clicking
		await this.driverHelper.scrollToAndClick(CreateWorkspace.CREATE_NEW_WORKPACE_CHECKBOX, timeout);
	}

	async waitAiProviderSectionVisible(timeout: number = TIMEOUT_CONSTANTS.TS_COMMON_DASHBOARD_WAIT_TIMEOUT): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitVisibility(CreateWorkspace.AI_PROVIDER_SELECTOR_HEADING, timeout);
	}

	async expandChooseAiProviderSection(): Promise<void> {
		Logger.debug();

		const toggle: By = CreateWorkspace.AI_PROVIDER_CHOOSE_TOGGLE;
		await this.driverHelper.waitAndClick(toggle);
		await this.driverHelper.waitAttributeValue(toggle, 'aria-expanded', 'true', TIMEOUT_CONSTANTS.TS_COMMON_DASHBOARD_WAIT_TIMEOUT);
	}

	async isAiProviderSectionVisible(): Promise<boolean> {
		Logger.debug();

		return await this.driverHelper.isVisible(CreateWorkspace.AI_PROVIDER_SELECTOR_HEADING);
	}

	async isAiProviderCardVisible(providerId: string): Promise<boolean> {
		Logger.debug(`providerId: "${providerId}"`);

		return await this.driverHelper.isVisible(this.getAiProviderCardLocator(providerId));
	}

	async waitAiProviderCardVisible(
		providerId: string,
		timeout: number = TIMEOUT_CONSTANTS.TS_COMMON_DASHBOARD_WAIT_TIMEOUT
	): Promise<void> {
		Logger.debug(`providerId: "${providerId}"`);

		await this.driverHelper.waitVisibility(this.getAiProviderCardLocator(providerId), timeout);
	}

	async isAiProviderKeyConfiguredBadgeVisible(): Promise<boolean> {
		Logger.debug();

		return await this.driverHelper.isVisible(CreateWorkspace.AI_PROVIDER_KEY_CONFIGURED_BADGE);
	}

	private getEditorsDropdownListLocator(sampleName: string): By {
		return By.xpath(`//div[text()=\'${sampleName}\']//parent::article//button`);
	}

	private getSampleWithSpecificEditorLocator(sampleName: string): By {
		let editor: string = '';
		switch (process.env.TS_SELENIUM_EDITOR) {
			case 'che-code':
				editor = 'code';
				break;
			default:
				throw new Error(`Unsupported editor ${process.env.TS_SELENIUM_EDITOR}`);
		}

		Logger.trace(`sampleName: ${sampleName}, editor "${editor}"`);

		return By.xpath(`//div[text()='${sampleName}']//parent::article//span[text()[
                contains(
                translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),
                '${editor}')]
            ]//parent::a`);
	}

	private getSampleLocator(sampleName: string): By {
		Logger.trace(`sampleName: ${sampleName}, used default editor`);

		return By.xpath(`//div[contains(@id, 'sample-card') and text()='${sampleName}']`);
	}

	private getAiProviderCardLocator(providerId: string): By {
		const cardId: string = providerId.replace(/\//g, '-');
		return By.id(`ai-provider-card-${cardId}`);
	}

	private getGitBranchListItemLocator(branchName: string): By {
		return By.xpath(`//ul[@role="listbox"]//span[text()="${branchName}"]`);
	}
}
