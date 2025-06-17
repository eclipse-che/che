/** *******************************************************************
 * copyright (c) 2019-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import 'reflect-metadata';
import { inject, injectable } from 'inversify';
import { DriverHelper } from '../../utils/DriverHelper';
import { CLASSES } from '../../configs/inversify.types';
import { By } from 'selenium-webdriver';
import { Logger } from '../../utils/Logger';
import { OcpApplicationPage } from './OcpApplicationPage';
import { e2eContainer } from '../../configs/inversify.config';

@injectable()
export class OcpImportFromGitPage {
	private static readonly GIT_URL_INPUT: By = By.id('form-input-git-url-field');
	private static readonly SHOW_ADVANCED_GIT_OPTIONS_LINK: By = By.xpath('//*[text()="Show advanced Git options"]//ancestor::button');
	private static readonly HIDE_ADVANCED_GIT_OPTIONS: By = By.xpath('//*[text()="Hide advanced Git options"]');
	private static readonly GIT_REFERENCE_INPUT: By = By.id('form-input-git-ref-field');
	private static readonly EDIT_IMPORT_STRATEGY_LINK: By = By.xpath('//*[text()="Edit Import Strategy"]//ancestor::button');
	private static readonly BUILDER_IMAGE_STRATEGY_ITEM: By = By.xpath('//*[text()="Builder Image"]//parent::div//parent::div');
	private static readonly ADD_LABEL_LINK: By = By.xpath('//button[text()="Labels" or .//span[text()="Labels"]]');
	private static readonly ADD_LABEL_INPUT: By = By.id('form-selector-labels-field');
	private static readonly SUBMIT_BUTTON: By = By.xpath('//*[@data-test-id="submit-button"]');

	constructor(
		@inject(CLASSES.DriverHelper)
		private readonly driverHelper: DriverHelper
	) {}

	async enterGitRepoUrl(gitRepoUrl: string): Promise<void> {
		Logger.debug();

		await this.driverHelper.enterValue(OcpImportFromGitPage.GIT_URL_INPUT, gitRepoUrl);
	}

	async clickOnAdvancedOptionsButton(): Promise<void> {
		Logger.debug();

		if (!(await this.driverHelper.isVisible(OcpImportFromGitPage.HIDE_ADVANCED_GIT_OPTIONS))) {
			await this.driverHelper.waitAndClick(OcpImportFromGitPage.SHOW_ADVANCED_GIT_OPTIONS_LINK);
		}
	}

	async enterGitReference(gitReference: string): Promise<void> {
		Logger.debug(`"${gitReference}"`);

		await this.driverHelper.enterValue(OcpImportFromGitPage.GIT_REFERENCE_INPUT, gitReference);
	}

	async selectBuilderImageImportStrategy(): Promise<void> {
		Logger.debug();

		await this.driverHelper.scrollToAndClick(OcpImportFromGitPage.EDIT_IMPORT_STRATEGY_LINK);
		await this.driverHelper.scrollToAndClick(OcpImportFromGitPage.BUILDER_IMAGE_STRATEGY_ITEM);
	}

	async addLabel(label: string): Promise<void> {
		Logger.debug(`"${label}"`);

		await this.driverHelper.scrollToAndClick(OcpImportFromGitPage.ADD_LABEL_LINK);
		await this.driverHelper.scrollToAndEnterValue(OcpImportFromGitPage.ADD_LABEL_INPUT, label);
	}

	async submitConfiguration(): Promise<OcpApplicationPage> {
		Logger.debug();

		await this.driverHelper.waitAndClick(OcpImportFromGitPage.SUBMIT_BUTTON);
		return e2eContainer.get(CLASSES.OcpApplicationPage);
	}

	async fitAndSubmitConfiguration(gitRepoUrl: string, gitReference: string, label: string): Promise<OcpApplicationPage> {
		Logger.debug();

		await this.enterGitRepoUrl(gitRepoUrl);
		await this.clickOnAdvancedOptionsButton();
		await this.enterGitReference(gitReference);
		await this.selectBuilderImageImportStrategy();
		await this.addLabel(label);
		return await this.submitConfiguration();
	}
}
