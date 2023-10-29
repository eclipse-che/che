/** *******************************************************************
 * copyright (c) 2019-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { CLASSES, TYPES } from '../configs/inversify.types';
import { ICheLoginPage } from '../pageobjects/login/interfaces/ICheLoginPage';
import { BrowserTabsUtil } from '../utils/BrowserTabsUtil';
import { inject, injectable } from 'inversify';
import { Dashboard } from '../pageobjects/dashboard/Dashboard';
import { IOcpLoginPage } from '../pageobjects/login/interfaces/IOcpLoginPage';
import { BASE_TEST_CONSTANTS } from '../constants/BASE_TEST_CONSTANTS';
import { TIMEOUT_CONSTANTS } from '../constants/TIMEOUT_CONSTANTS';
import { Logger } from '../utils/Logger';

@injectable()
export class LoginTests {
	constructor(
		@inject(CLASSES.BrowserTabsUtil)
		private readonly browserTabsUtil: BrowserTabsUtil,
		@inject(TYPES.CheLogin)
		private readonly productLoginPage: ICheLoginPage,
		@inject(TYPES.OcpLogin) private readonly ocpLoginPage: IOcpLoginPage,
		@inject(CLASSES.Dashboard) private readonly dashboard: Dashboard
	) {}

	async loginIntoChe(
		userName?: string,
		password?: string,
		timeout: number = TIMEOUT_CONSTANTS.TS_COMMON_DASHBOARD_WAIT_TIMEOUT
	): Promise<void> {
		Logger.debug();
		try {
			if (!(await this.browserTabsUtil.getCurrentUrl()).includes(BASE_TEST_CONSTANTS.TS_SELENIUM_BASE_URL)) {
				await this.browserTabsUtil.navigateTo(BASE_TEST_CONSTANTS.TS_SELENIUM_BASE_URL);
			}
			await this.dashboard.waitPage(timeout);
			Logger.debug('user already logged in');
		} catch (e) {
			Logger.debug('try to login into application');
			await this.productLoginPage.login(userName, password);
			await this.browserTabsUtil.maximize();
			await this.dashboard.waitStartingPageLoaderDisappearance();
		}
	}

	loginIntoOcpConsole(): void {
		suiteSetup('Login into ocp console', async (): Promise<void> => {
			const openshiftConsoleUrl: string = BASE_TEST_CONSTANTS.TS_SELENIUM_BASE_URL.replace(
				BASE_TEST_CONSTANTS.TESTING_APPLICATION_NAME(),
				'console-openshift-console'
			);
			await this.browserTabsUtil.navigateTo(openshiftConsoleUrl);
			await this.ocpLoginPage.login();
			await this.browserTabsUtil.maximize();
		});
	}

	async logoutFromChe(): Promise<void> {
		await this.dashboard.logout();
	}
}
