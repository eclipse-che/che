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
import { CLASSES } from '../../../configs/inversify.types';
import { Logger } from '../../../utils/Logger';
import { By } from 'selenium-webdriver';
import { TIMEOUT_CONSTANTS } from '../../../constants/TIMEOUT_CONSTANTS';
import { DriverHelper } from '../../../utils/DriverHelper';

@injectable()
export class DexLoginPage {
	private static readonly dexPageContentContainer: By = By.className('dex-container');
	private static readonly loginInput: By = By.id('login');
	private static readonly passwordInput: By = By.id('password');
	private static readonly submitButton: By = By.id('submit-login');

	constructor(
		@inject(CLASSES.DriverHelper)
		private readonly driverHelper: DriverHelper
	) {}

	async waitDexLoginPage(): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitVisibility(DexLoginPage.dexPageContentContainer, TIMEOUT_CONSTANTS.TS_SELENIUM_LOAD_PAGE_TIMEOUT);
	}

	async clickOnLoginButton(): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitAndClick(DexLoginPage.submitButton);
	}

	async enterUserNameKubernetes(userName: string): Promise<void> {
		Logger.debug();

		await this.driverHelper.enterValue(DexLoginPage.loginInput, userName);
	}

	async enterPasswordKubernetes(password: string): Promise<void> {
		Logger.debug();

		await this.driverHelper.enterValue(DexLoginPage.passwordInput, password);
	}

	async waitDexLoginPageDisappearance(): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitDisappearance(DexLoginPage.dexPageContentContainer, TIMEOUT_CONSTANTS.TS_SELENIUM_LOAD_PAGE_TIMEOUT);
	}
}
