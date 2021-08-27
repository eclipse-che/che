/*********************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { injectable, inject } from 'inversify';
import { CLASSES } from '../../../inversify.types';
import { DriverHelper } from '../../../utils/DriverHelper';
import { Logger } from '../../../utils/Logger';
import { TimeoutConstants } from '../../../TimeoutConstants';
import { By } from 'selenium-webdriver';

@injectable()
export class WorkspacePlugin {

    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) {}

    async isTrustAuthorsNotificationVisible(timeout: number = TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT): Promise<boolean> {
        Logger.debug(`WorkspacePlugin.waitTrustAuthorsNotification`);

        const locator = By.xpath(`//div[@class='theia-notification-list-item']` +
            `//div[@class='theia-notification-message']` +
            `/span[starts-with(text(), 'Do you trust the authors of ')]`);

        return this.driverHelper.waitVisibilityBoolean(locator, 5, timeout / 5);
    }

    async confirmTrustAuthors(timeout: number = TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT): Promise<void> {
        Logger.debug(`WorkspacePlugin.confirmTrustAuthors`);

        const locator = By.xpath(`//div[@class='theia-notification-list-item']` +
            `//div[@class='theia-notification-buttons']` +
            `/button[@data-action='Yes, I trust']`);

        await this.driverHelper.waitAndClick(locator, timeout);
    }

}
