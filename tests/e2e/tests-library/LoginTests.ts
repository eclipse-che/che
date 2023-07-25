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

	loginIntoChe(): void {
		test('Login', async (): Promise<void> => {
			if (!(await this.browserTabsUtil.getCurrentUrl()).includes(BASE_TEST_CONSTANTS.TS_SELENIUM_BASE_URL)) {
				await this.browserTabsUtil.navigateTo(BASE_TEST_CONSTANTS.TS_SELENIUM_BASE_URL);
			}
			await this.productLoginPage.login();
			await this.browserTabsUtil.maximize();
			await this.dashboard.waitStartingPageLoaderDisappearance();
		});
	}

	loginIntoOcpConsole(): void {
		test('Login into ocp console', async (): Promise<void> => {
			const openshiftConsoleUrl: string = BASE_TEST_CONSTANTS.TS_SELENIUM_BASE_URL.replace('devspaces', 'console-openshift-console');
			await this.browserTabsUtil.navigateTo(openshiftConsoleUrl);
			this.ocpLoginPage.login();
			await this.browserTabsUtil.maximize();
		});
	}

	logoutFromChe(): void {
		test('Logout', async (): Promise<void> => {
			await this.dashboard.logout();
		});
	}
}
