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
import { CLASSES } from '../../configs/inversify.types';
import { DriverHelper } from '../../utils/DriverHelper';
import { By, Key } from 'selenium-webdriver';
import { Logger } from '../../utils/Logger';
import { TIMEOUT_CONSTANTS } from '../../constants/TIMEOUT_CONSTANTS';
import { BASE_TEST_CONSTANTS } from '../../constants/BASE_TEST_CONSTANTS';
import { TrustAuthorPopup } from './TrustAuthorPopup';

@injectable()
export class CreateWorkspace {
	private static readonly FACTORY_URL: By = By.xpath('//input[@id="git-repo-url"]');
	private static readonly GIT_REPO_OPTIONS: By = By.xpath('//span[text()="Git Repo Options"]');
	private static readonly GIT_BRANCH_NAME: By = By.xpath('//input[@aria-label="Git Branch"]');
	private static readonly PATH_TO_DEVFILE: By = By.xpath('//input[@aria-label="Path to Devfile"]');
	private static readonly CREATE_AND_OPEN_BUTTON: By = By.xpath('//button[@id="create-and-open-button"]');

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
		timeout: number = TIMEOUT_CONSTANTS.TS_CLICK_DASHBOARD_ITEM_TIMEOUT
	): Promise<void> {
		Logger.debug(`factoryUrl: "${factoryUrl}"`);

		await this.driverHelper.waitVisibility(CreateWorkspace.FACTORY_URL, timeout);
		await this.driverHelper.type(CreateWorkspace.FACTORY_URL, Key.chord(factoryUrl), timeout);

		if (branchName) {
			await this.driverHelper.waitAndClick(CreateWorkspace.GIT_REPO_OPTIONS, timeout);

			await this.driverHelper.waitVisibility(CreateWorkspace.GIT_BRANCH_NAME, timeout);
			await this.driverHelper.type(CreateWorkspace.GIT_BRANCH_NAME, Key.chord(branchName, Key.ENTER), timeout);
		}

		await this.driverHelper.waitAndClick(CreateWorkspace.CREATE_AND_OPEN_BUTTON, timeout);

		await this.performTrustAuthorPopup();
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

		return By.xpath(`//article[contains(@class, 'sample-card')]//div[text()='${sampleName}']`);
	}
}
