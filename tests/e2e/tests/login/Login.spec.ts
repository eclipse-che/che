/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { TestConstants, DriverHelper, CLASSES, ICheLoginPage, TYPES, Logger } from '../..';
import { e2eContainer } from '../../inversify.config';

const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
const loginPage: ICheLoginPage = e2eContainer.get<ICheLoginPage>(TYPES.CheLogin);

suite('Login test', async () => {
    test('Login', async () => {
        await driverHelper.navigateToUrl(TestConstants.TS_SELENIUM_BASE_URL);
        await loginPage.login();
        if (TestConstants.TS_SELENIUM_LAUNCH_FULLSCREEN) {
            Logger.debug(`TS_SELENIUM_LAUNCH_FULLSCREEN is set to true, maximizing window.`);
            await driverHelper.maximize();
        }
    });
});
