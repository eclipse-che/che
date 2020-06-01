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
import { TestConstants } from '../../TestConstants';
import { By } from 'selenium-webdriver';
import { Logger } from '../../utils/Logger';



@injectable()
export class LeftToolbar {
    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) { }

    async waitToolbar(timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug('RightToolbar.waitToolbar');

        await this.driverHelper.waitVisibility(By.css('div.theia-app-right'), timeout);
    }

    async clickOnToolIcon(iconTitle: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug(`RightToolbar.clickOnToolIcon "${iconTitle}"`);

        const toolIconLocator: By = By.css(`div.theia-app-right .p-TabBar-content li[title='${iconTitle}']`);

        await this.driverHelper.waitAndClick(toolIconLocator, timeout);
    }

}
