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

@injectable()
export class RedHatLoginPage {
	private readonly USERNAME_INPUT_ID: string = 'username-verification';
	private readonly PASSWORD_INPUT_ID: string = 'password';
	private readonly NEXT_BUTTON_ID: string = 'login-show-step2';
	private readonly LOGIN_BUTTON_ID: string = 'rh-password-verification-submit-button';

	constructor(
		@inject(CLASSES.DriverHelper)
		private readonly driverHelper: DriverHelper
	) {}

	async waitRedHatLoginWelcomePage(): Promise<void> {
		Logger.debug();
		await this.driverHelper.waitVisibility(By.id(this.USERNAME_INPUT_ID));
	}

	async enterPasswordRedHat(): Promise<void> {
		Logger.debug();
		const passwordFieldLocator: By = By.id(this.PASSWORD_INPUT_ID);
		await this.driverHelper.waitVisibility(passwordFieldLocator, 3000);
		await this.driverHelper.enterValue(passwordFieldLocator, OAUTH_CONSTANTS.TS_SELENIUM_OCP_PASSWORD);
	}

	async clickOnLoginButton(): Promise<void> {
		Logger.debug();
		const loginButtonLocator: By = By.id(this.LOGIN_BUTTON_ID);
		await this.driverHelper.waitAndClick(loginButtonLocator);
	}

	async waitDisappearanceRedHatLoginWelcomePage(): Promise<void> {
		Logger.debug();
		await this.driverHelper.waitDisappearance(By.id(this.LOGIN_BUTTON_ID));
	}

	async enterUserNameRedHat(): Promise<void> {
		Logger.debug();
		const usernameFieldLocator: By = By.id(this.USERNAME_INPUT_ID);
		await this.driverHelper.waitVisibility(usernameFieldLocator, 20000);
		await this.driverHelper.enterValue(usernameFieldLocator, OAUTH_CONSTANTS.TS_SELENIUM_OCP_USERNAME);
	}

	async clickNextButton(): Promise<void> {
		Logger.debug();
		const nextButtonLocator: By = By.id(this.NEXT_BUTTON_ID);
		await this.driverHelper.waitAndClick(nextButtonLocator);
	}
}
