/*********************************************************************
 * Copyright (c) 2019-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { CLASSES, TYPES } from '../configs/inversify.types';
import { ICheLoginPage } from '../pageobjects/login/ICheLoginPage';
import { TestConstants } from '../constants/TestConstants';
import { BrowserTabsUtil } from '../utils/BrowserTabsUtil';
import { Logger } from '../utils/Logger';
import { inject, injectable } from 'inversify';
import { Dashboard } from '../pageobjects/dashboard/Dashboard';

@injectable()
export class LoginTests {
    constructor(
        @inject(CLASSES.BrowserTabsUtil) private readonly browserTabsUtil: BrowserTabsUtil,
        @inject(TYPES.CheLogin) private readonly loginPage: ICheLoginPage,
        @inject(CLASSES.Dashboard) private readonly dashboard: Dashboard) {
    }

    public loginIntoChe(): void {
        test('Login', async () => {
            await this.browserTabsUtil.navigateTo(TestConstants.TS_SELENIUM_BASE_URL);
            await this.loginPage.login();
            if (TestConstants.TS_SELENIUM_LAUNCH_FULLSCREEN) {
                Logger.debug(`TS_SELENIUM_LAUNCH_FULLSCREEN is set to true, maximizing window.`);
                await this.browserTabsUtil.maximize();
                await this.dashboard.waitPage();
            }
        });
    }

    public logoutFromChe(): void {
        test('Logout', async () => {
            await this.dashboard.logout();
        });
    }
}
