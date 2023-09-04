/** *******************************************************************
 * copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import 'reflect-metadata';
import { inject, injectable } from 'inversify';
import { By } from 'selenium-webdriver';
import { CLASSES } from '../../../configs/inversify.types';
import { DriverHelper } from '../../../utils/DriverHelper';
import { Logger } from '../../../utils/Logger';
import { OAUTH_CONSTANTS } from '../../../constants/OAUTH_CONSTANTS';
import { TIMEOUT_CONSTANTS } from '../../../constants/TIMEOUT_CONSTANTS';

@injectable()
export class RedHatLoginPage {
	private static readonly USERNAME_INPUT: By = By.id('username-verification');
	private static readonly PASSWORD_INPUT: By = By.id('password');
	private static readonly NEXT_BUTTON: By = By.id('login-show-step2');
	private static readonly LOGIN_BUTTON: By = By.id('rh-password-verification-submit-button');

	constructor(
		@inject(CLASSES.DriverHelper)
		private readonly driverHelper: DriverHelper
	) {}

	async waitRedHatLoginWelcomePage(): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitVisibility(RedHatLoginPage.USERNAME_INPUT, TIMEOUT_CONSTANTS.TS_SELENIUM_LOAD_PAGE_TIMEOUT);
	}

	async enterPasswordRedHat(): Promise<void> {
		Logger.debug();

		await this.driverHelper.enterValue(RedHatLoginPage.PASSWORD_INPUT, OAUTH_CONSTANTS.TS_SELENIUM_OCP_PASSWORD);
	}

	async clickOnLoginButton(): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitAndClick(RedHatLoginPage.LOGIN_BUTTON);
	}

	async waitDisappearanceRedHatLoginWelcomePage(): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitDisappearance(RedHatLoginPage.LOGIN_BUTTON);
	}

	async enterUserNameRedHat(): Promise<void> {
		Logger.debug();

		await this.driverHelper.enterValue(RedHatLoginPage.USERNAME_INPUT, OAUTH_CONSTANTS.TS_SELENIUM_OCP_USERNAME);
	}

	async clickNextButton(): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitAndClick(RedHatLoginPage.NEXT_BUTTON);
	}
}
