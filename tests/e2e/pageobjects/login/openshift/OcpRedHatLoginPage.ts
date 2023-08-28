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
import { RedHatLoginPage } from './RedHatLoginPage';
import { CLASSES } from '../../../configs/inversify.types';
import { By } from 'selenium-webdriver';
import { TIMEOUT_CONSTANTS } from '../../../constants/TIMEOUT_CONSTANTS';
import { ICheLoginPage } from '../interfaces/ICheLoginPage';
import { OcpLoginPage } from './OcpLoginPage';
import { DriverHelper } from '../../../utils/DriverHelper';
import { Logger } from '../../../utils/Logger';

@injectable()
export class OcpRedHatLoginPage implements ICheLoginPage {
	private static readonly OPENSHIFT_LOGIN_LANDING_PAGE_BUTTON: By = By.xpath(
		'//div[@class="panel-login"]/div[contains(@class, "panel-content")]/form/button'
	);

	constructor(
		@inject(CLASSES.OcpLoginPage) private readonly ocpLogin: OcpLoginPage,
		@inject(CLASSES.RedHatLoginPage)
		private readonly redHatLogin: RedHatLoginPage,
		@inject(CLASSES.DriverHelper)
		private readonly driverHelper: DriverHelper
	) {}

	async login(): Promise<void> {
		Logger.debug();
		await this.driverHelper.waitAndClick(
			OcpRedHatLoginPage.OPENSHIFT_LOGIN_LANDING_PAGE_BUTTON,
			TIMEOUT_CONSTANTS.TS_SELENIUM_LOAD_PAGE_TIMEOUT
		);
		await this.ocpLogin.waitAndClickOnLoginProviderTitle();
		await this.redHatLogin.waitRedHatLoginWelcomePage();
		await this.redHatLogin.enterUserNameRedHat();
		await this.redHatLogin.clickNextButton();
		await this.redHatLogin.enterPasswordRedHat();
		await this.redHatLogin.clickOnLoginButton();
		await this.redHatLogin.waitDisappearanceRedHatLoginWelcomePage();
	}
}
