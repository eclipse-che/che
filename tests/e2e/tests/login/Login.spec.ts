/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { TestConstants, CLASSES, ICheLoginPage, TYPES, Logger } from '../..';
import { e2eContainer } from '../../inversify.config';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';

const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
const loginPage: ICheLoginPage = e2eContainer.get<ICheLoginPage>(TYPES.CheLogin);

suite('Login test', async () => {
    test('Login', async () => {
        await browserTabsUtil.navigateTo(TestConstants.TS_SELENIUM_BASE_URL);
        await loginPage.login();
        if (TestConstants.TS_SELENIUM_LAUNCH_FULLSCREEN) {
            Logger.debug(`TS_SELENIUM_LAUNCH_FULLSCREEN is set to true, maximizing window.`);
            await browserTabsUtil.maximize();
        }
    });
});
