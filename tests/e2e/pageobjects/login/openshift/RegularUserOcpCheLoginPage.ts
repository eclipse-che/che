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
	private static readonly OPENSHIFT_LOGIN_LANDING_PAGE_BUTTON: By = By.xpath(
		'//div[@class="panel-login"]/div[contains(@class, "panel-content")]/form/button'
	);

	constructor(
		@inject(CLASSES.OcpLoginPage) private readonly ocpLogin: OcpLoginPage,
		@inject(CLASSES.DriverHelper)
		private readonly driverHelper: DriverHelper
	) {}

	async login(
		userName: string = OAUTH_CONSTANTS.TS_SELENIUM_OCP_USERNAME,
		password: string = OAUTH_CONSTANTS.TS_SELENIUM_OCP_PASSWORD
	): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitAndClick(
			RegularUserOcpCheLoginPage.OPENSHIFT_LOGIN_LANDING_PAGE_BUTTON,
			TIMEOUT_CONSTANTS.TS_SELENIUM_LOAD_PAGE_TIMEOUT
		);

		if (await this.ocpLogin.isIdentityProviderLinkVisible()) {
			await this.ocpLogin.waitAndClickOnLoginProviderTitle();
		}

		await this.ocpLogin.waitOpenShiftLoginWelcomePage();
		await this.ocpLogin.enterUserNameOpenShift(userName);
		await this.ocpLogin.enterPasswordOpenShift(password);
		await this.ocpLogin.clickOnLoginButton();
		await this.ocpLogin.waitDisappearanceOpenShiftLoginWelcomePage();

		if (await this.ocpLogin.isAuthorizeOpenShiftIdentityProviderPageVisible()) {
			await this.ocpLogin.clickOnApproveAuthorizeAccessButton();
		}
	}
}
