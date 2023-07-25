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
import { ICheLoginPage } from '../interfaces/ICheLoginPage';
import { OcpLoginPage } from './OcpLoginPage';
import { inject, injectable } from 'inversify';
import { CLASSES } from '../../../configs/inversify.types';
import { Logger } from '../../../utils/Logger';
import { TIMEOUT_CONSTANTS } from '../../../constants/TIMEOUT_CONSTANTS';
import { By } from 'selenium-webdriver';
import { DriverHelper } from '../../../utils/DriverHelper';
import { OAUTH_CONSTANTS } from '../../../constants/OAUTH_CONSTANTS';

@injectable()
export class RegularUserOcpCheLoginPage implements ICheLoginPage {
	private readonly OPEN_SHIFT_LOGIN_LANDING_PAGE_LOCATOR: string = '//div[@class="panel-login"]';
	private readonly OPEN_SHIFT_LOGIN_LANDING_PAGE_BUTTON_LOCATOR: string = `${this.OPEN_SHIFT_LOGIN_LANDING_PAGE_LOCATOR}/div[contains(@class, 'panel-content')]/form/button`;

	constructor(
		@inject(CLASSES.OcpLoginPage) private readonly ocpLogin: OcpLoginPage,
		@inject(CLASSES.DriverHelper)
		private readonly driverHelper: DriverHelper
	) {}

	async login(): Promise<void> {
		Logger.debug();

		Logger.debug('wait for LogInWithOpenShift page and click button');
		await this.driverHelper.waitPresence(
			By.xpath(this.OPEN_SHIFT_LOGIN_LANDING_PAGE_LOCATOR),
			TIMEOUT_CONSTANTS.TS_SELENIUM_LOAD_PAGE_TIMEOUT
		);
		await this.driverHelper.waitAndClick(By.xpath(this.OPEN_SHIFT_LOGIN_LANDING_PAGE_BUTTON_LOCATOR));

		if (await this.ocpLogin.isIdentityProviderLinkVisible()) {
			await this.ocpLogin.clickOnLoginProviderTitle();
		}

		await this.ocpLogin.waitOpenShiftLoginWelcomePage();
		await this.ocpLogin.enterUserNameOpenShift(OAUTH_CONSTANTS.TS_SELENIUM_OCP_USERNAME);
		await this.ocpLogin.enterPasswordOpenShift(OAUTH_CONSTANTS.TS_SELENIUM_OCP_PASSWORD);
		await this.ocpLogin.clickOnLoginButton();
		await this.ocpLogin.waitDisappearanceOpenShiftLoginWelcomePage();

		if (await this.ocpLogin.isAuthorizeOpenShiftIdentityProviderPageVisible()) {
			await this.ocpLogin.waitAuthorizeOpenShiftIdentityProviderPage();
			await this.ocpLogin.clickOnApproveAuthorizeAccessButton();
		}
	}
}
