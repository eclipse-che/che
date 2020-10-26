/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { TestConstants, DriverHelper, CLASSES, ICheLoginPage, TYPES } from '../..';
import { e2eContainer } from '../../inversify.config';
import { IOcpLoginPage } from '../../pageobjects/login/IOcpLoginPage';
import { UpdateAccountInformationPage } from '../../pageobjects/login/UpdateAccountInformationPage';
import { TimeoutConstants } from '../../TimeoutConstants';
import { Dashboard } from '../../pageobjects/dashboard/Dashboard';

const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
const ocpLogin: IOcpLoginPage = e2eContainer.get<IOcpLoginPage>(TYPES.OcpLogin);
const updateAccountInformation: UpdateAccountInformationPage = e2eContainer.get(CLASSES.UpdateAccountInformationPage);
const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);

suite('Link users', async () => {
    test('Login to OCP', async () => {
        await driverHelper.navigateToUrl(TestConstants.TS_SELENIUM_BASE_URL);

        await ocpLogin.login();
    });

    test('Update account information', async () => {
        await updateAccountInformation.enterEmail('admin@admin.com', TimeoutConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT * 2);
        await updateAccountInformation.enterFirstName(TestConstants.TS_SELENIUM_USERNAME, TimeoutConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT * 2);
        await updateAccountInformation.enterLastName(TestConstants.TS_SELENIUM_USERNAME, TimeoutConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT * 2);
        await updateAccountInformation.clickConfirmButton(TimeoutConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT * 2);
        await updateAccountInformation.clickAddToExistingAccountButton(TimeoutConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT * 2);
    });

    test('Login to Che', async () => {
        await updateAccountInformation.enterPassword(TestConstants.TS_SELENIUM_PASSWORD, TimeoutConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT * 2);
        await updateAccountInformation.clickLogInButton(TimeoutConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT * 2);
        await dashboard.waitPage(TimeoutConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT * 2);
    });

});
