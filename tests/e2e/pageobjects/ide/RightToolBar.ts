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
import { By } from 'selenium-webdriver';
import { Logger } from '../../utils/Logger';
import { TimeoutConstants } from '../../TimeoutConstants';

@injectable()
export class RightToolBar {
    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) { }

    async waitToolbar(timeout: number = TimeoutConstants.TS_SELENIUM_TOOLBAR_TIMEOUT) {
        Logger.debug('RightToolbar.waitToolbar');

        await this.driverHelper.waitVisibility(By.css('div.theia-app-right'), timeout);
    }

    async clickOnToolIcon(iconTitle: string, timeout: number = TimeoutConstants.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM) {
        Logger.debug(`RightToolbar.clickOnToolIcon "${iconTitle}"`);

        const toolIconLocator: By = By.css(`div.theia-app-right .p-TabBar-content li[title='${iconTitle}']`);

        await this.driverHelper.waitAndClick(toolIconLocator, timeout);
    }
}
