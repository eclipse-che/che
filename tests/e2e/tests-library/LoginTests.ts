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
import { ICheLoginPage } from '../pageobjects/login/interfaces/ICheLoginPage';
import { BrowserTabsUtil } from '../utils/BrowserTabsUtil';
import { inject, injectable } from 'inversify';
import { Dashboard } from '../pageobjects/dashboard/Dashboard';
import { IOcpLoginPage } from '../pageobjects/login/interfaces/IOcpLoginPage';
import { BaseTestConstants } from '../constants/BaseTestConstants';

@injectable()
export class LoginTests {
  constructor(
    @inject(CLASSES.BrowserTabsUtil) private readonly browserTabsUtil: BrowserTabsUtil,
    @inject(TYPES.CheLogin) private readonly productLoginPage: ICheLoginPage,
    @inject(TYPES.OcpLogin) private readonly ocpLoginPage: IOcpLoginPage,
    @inject(CLASSES.Dashboard) private readonly dashboard: Dashboard) {
  }

  loginIntoChe(): void {
    test('Login', async () => {
      if (!(await this.browserTabsUtil.getCurrentUrl()).includes(BaseTestConstants.TS_SELENIUM_BASE_URL)) {
        await this.browserTabsUtil.navigateTo(BaseTestConstants.TS_SELENIUM_BASE_URL);
      }
      await this.productLoginPage.login();
      await this.browserTabsUtil.maximize();
      await this.dashboard.waitStartingPageLoaderDisappearance();
    });
  }

  loginIntoOcpConsole(): void {
    test('Login into ocp console', async () => {
      const openshiftConsoleUrl: string = BaseTestConstants.TS_SELENIUM_BASE_URL.replace('devspaces', 'console-openshift-console');
      await this.browserTabsUtil.navigateTo(openshiftConsoleUrl);
      await this.ocpLoginPage.login();
      await this.browserTabsUtil.maximize();
    });
  }

  logoutFromChe(): void {
    test('Logout', async () => {
      await this.dashboard.logout();
    });
  }
}
