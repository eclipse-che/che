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
import { DriverHelper } from '../../../utils/DriverHelper';
import { CLASSES } from '../../../configs/inversify.types';
import { By } from 'selenium-webdriver';
import { Logger } from '../../../utils/Logger';
import { TIMEOUT_CONSTANTS } from '../../../constants/TIMEOUT_CONSTANTS';
import { OAUTH_CONSTANTS } from '../../../constants/OAUTH_CONSTANTS';

@injectable()
export class OcpLoginPage {
	private static readonly LOGIN_PAGE_WELCOME_MESSAGE: By = By.xpath('//*[contains(text(), "Welcome")]');
	private static readonly LOGIN_BUTTON: By = By.css('button[type=submit]');
	private static readonly LOGIN_PROVIDER_BUTTON: By = By.xpath(`//a[text()="${OAUTH_CONSTANTS.TS_OCP_LOGIN_PAGE_PROVIDER_TITLE}"]`);
	private static readonly AUTHORIZE_OPENSHIFT_ACCESS_HEADER: By = By.xpath('//h1[text()="Authorize Access"]');
	private static readonly APPROVE_ACCESS_BUTTON: By = By.css('input[name="approve"]');
	private static readonly USERNAME_INPUT: By = By.id('inputUsername');
	private static readonly PASSWORD_INPUT: By = By.id('inputPassword');

	constructor(
		@inject(CLASSES.DriverHelper)
		private readonly driverHelper: DriverHelper
	) {}

	async waitOpenShiftLoginWelcomePage(): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitVisibility(OcpLoginPage.LOGIN_PAGE_WELCOME_MESSAGE, TIMEOUT_CONSTANTS.TS_SELENIUM_LOAD_PAGE_TIMEOUT);
	}

	async waitAndClickOnLoginProviderTitle(): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitAndClick(OcpLoginPage.LOGIN_PROVIDER_BUTTON, TIMEOUT_CONSTANTS.TS_SELENIUM_WAIT_FOR_URL);
	}

	async isIdentityProviderLinkVisible(): Promise<boolean> {
		Logger.debug();

		return await this.driverHelper.waitVisibilityBoolean(OcpLoginPage.LOGIN_PROVIDER_BUTTON, 3, 5000);
	}

	async isAuthorizeOpenShiftIdentityProviderPageVisible(): Promise<boolean> {
		Logger.debug();

		return await this.driverHelper.isVisible(OcpLoginPage.AUTHORIZE_OPENSHIFT_ACCESS_HEADER);
	}

	async clickOnApproveAuthorizeAccessButton(): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitAndClick(OcpLoginPage.APPROVE_ACCESS_BUTTON);
	}

	async enterUserNameOpenShift(userName: string): Promise<void> {
		Logger.debug(`"${userName}"`);

		await this.driverHelper.enterValue(OcpLoginPage.USERNAME_INPUT, userName);
	}

	async enterPasswordOpenShift(userPassword: string): Promise<void> {
		Logger.debug();

		await this.driverHelper.enterValue(OcpLoginPage.PASSWORD_INPUT, userPassword);
	}

	async clickOnLoginButton(): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitAndClick(OcpLoginPage.LOGIN_BUTTON);
	}

	async waitDisappearanceOpenShiftLoginWelcomePage(): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitDisappearance(OcpLoginPage.LOGIN_PAGE_WELCOME_MESSAGE);
	}
}
