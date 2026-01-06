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
	private static readonly DEX_PAGE_CONTENT_CONTAINER: By = By.className('dex-container');
	private static readonly LOGIN_INPUT: By = By.id('login');
	private static readonly PASSWORD_INPUT: By = By.id('password');
	private static readonly SUBMIT_BUTTON: By = By.id('submit-login');

	constructor(
		@inject(CLASSES.DriverHelper)
		private readonly driverHelper: DriverHelper
	) {}

	async waitDexLoginPage(): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitVisibility(DexLoginPage.DEX_PAGE_CONTENT_CONTAINER, TIMEOUT_CONSTANTS.TS_SELENIUM_LOAD_PAGE_TIMEOUT);
	}

	async clickOnLoginButton(): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitAndClick(DexLoginPage.SUBMIT_BUTTON);
	}

	async enterUserNameKubernetes(userName: string): Promise<void> {
		Logger.debug();

		await this.driverHelper.enterValue(DexLoginPage.LOGIN_INPUT, userName);
	}

	async enterPasswordKubernetes(password: string): Promise<void> {
		Logger.debug();

		await this.driverHelper.enterValue(DexLoginPage.PASSWORD_INPUT, password);
	}

	async waitDexLoginPageDisappearance(): Promise<void> {
		Logger.debug();

		const attempts: number = Math.ceil(TIMEOUT_CONSTANTS.TS_SELENIUM_LOAD_PAGE_TIMEOUT / TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING);
		await this.driverHelper.waitDisappearance(
			DexLoginPage.DEX_PAGE_CONTENT_CONTAINER,
			attempts,
			TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING
		);
	}
}
