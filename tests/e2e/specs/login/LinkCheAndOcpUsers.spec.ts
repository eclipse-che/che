/*********************************************************************
 * Copyright (c) 2019-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { CLASSES, TYPES } from '../../configs/inversify.types';
import { e2eContainer } from '../../configs/inversify.config';
import { IOcpLoginPage } from '../../pageobjects/login/IOcpLoginPage';
import { UpdateAccountInformationPage } from '../../pageobjects/login/UpdateAccountInformationPage';
import { TimeoutConstants } from '../../constants/TimeoutConstants';
import { Dashboard } from '../../pageobjects/dashboard/Dashboard';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import { TestConstants } from '../../constants/TestConstants';

const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
const ocpLogin: IOcpLoginPage = e2eContainer.get<IOcpLoginPage>(TYPES.OcpLogin);
const updateAccountInformation: UpdateAccountInformationPage = e2eContainer.get(CLASSES.UpdateAccountInformationPage);
const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);

const commonTimeout: number = TimeoutConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT * 2;

suite('Link users', async () => {
    test('Login to OCP', async () => {
        await browserTabsUtil.navigateTo(TestConstants.TS_SELENIUM_BASE_URL);

        await ocpLogin.login();
        await updateAccountInformation.clickToAllowSelectedPermissionsButton(commonTimeout);
    });

    test('Update account information', async () => {
        await updateAccountInformation.enterEmail('admin@admin.com', commonTimeout);
        await updateAccountInformation.enterFirstName(TestConstants.TS_SELENIUM_USERNAME, commonTimeout);
        await updateAccountInformation.enterLastName(TestConstants.TS_SELENIUM_USERNAME, commonTimeout);
        await updateAccountInformation.clickConfirmButton(commonTimeout);
        await updateAccountInformation.clickAddToExistingAccountButton(commonTimeout);
    });

    test('Login to Che', async () => {
        await updateAccountInformation.enterPassword(TestConstants.TS_SELENIUM_PASSWORD, commonTimeout);
        await updateAccountInformation.clickLogInButton(commonTimeout);
        await dashboard.waitPage(commonTimeout);
    });

});
