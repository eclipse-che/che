/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { injectable, inject } from 'inversify';
import { CLASSES } from '../../inversify.types';
import { DriverHelper } from '../../utils/DriverHelper';
import { Logger } from '../../utils/Logger';
import { By } from 'selenium-webdriver';
import { TimeoutConstants } from '../../TimeoutConstants';

@injectable()
export class NotificationCenter {
    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) { }

    async waitOpening(timeout: number = TimeoutConstants.TS_NOTIFICATION_CENTER_TIMEOUT) {
        Logger.debug('NotificationCenter.waitOpening');

        await this.driverHelper.waitVisibility(By.css('div.theia-notification-center-header'), timeout);
    }

    async clickIconOnStatusBar(timeout: number = TimeoutConstants.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM) {
        Logger.debug('NotificationCenter.clickIconOnStatusBar');

        const statusBarNotificationsLocator: By =
            By.xpath('(//div[@id=\'theia-statusBar\']//div[@class=\'area right\']//div[@title=\'Toggle Bottom Panel\']/preceding-sibling::div)[last()]');

        await this.driverHelper.waitAndClick(statusBarNotificationsLocator, timeout);
    }

    async open() {
        Logger.debug(`NotificationCenter.open`);

        await this.clickIconOnStatusBar(TimeoutConstants.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM);
        await this.waitOpening(TimeoutConstants.TS_NOTIFICATION_CENTER_TIMEOUT);
    }

    async clickCloseAllNotificationsButton(timeout: number = TimeoutConstants.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM) {
        Logger.debug(`NotificationCenter.clickCloseAllNotificationsButton`);

        const closeAllButtonLocator: By = By.css('li.codicon.codicon-clear-all.action-item[title="Clear All"]');

        await this.driverHelper.waitAndClick(closeAllButtonLocator, timeout);
    }

    async waitClosing(timeout: number = TimeoutConstants.TS_NOTIFICATION_CENTER_TIMEOUT) {
        Logger.debug(`NotificationCenter.waitClosing`);

        const notificationCenterClosedLocator: By = By.css('.theia-notification-center.closed');

        await this.driverHelper.waitPresence(notificationCenterClosedLocator, timeout);
    }

    async waitClearNotificationsList(timeout: number = TimeoutConstants.TS_NOTIFICATION_CENTER_TIMEOUT) {
        Logger.debug(`NotificationCenter.waitClearNotificationsList`);

        const notificationLocator: By = By.css('.theia-notification-center .theia-notification-list > *');

        await this.driverHelper.waitDisappearance(notificationLocator, timeout);
    }

    async closeAll(timeout: number = TimeoutConstants.TS_NOTIFICATION_CENTER_TIMEOUT) {
        Logger.debug('NotificationCenter.closeAll');

        await this.clickCloseAllNotificationsButton(timeout);
        await this.waitClearNotificationsList(timeout);
    }

}
