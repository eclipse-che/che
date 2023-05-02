/*********************************************************************
 * Copyright (c) 2019-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import 'reflect-metadata';
import { inject, injectable } from 'inversify';
import { DriverHelper } from '../../utils/DriverHelper';
import { CLASSES } from '../../configs/inversify.types';
import { By } from 'selenium-webdriver';
import { Logger } from '../../utils/Logger';
import { TimeoutConstants } from '../../constants/TimeoutConstants';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';

@injectable()
export class OcpApplicationPage {

    private static readonly APPLICATION_ICON_LOCATOR: By = By.xpath('//*[@data-test-id="base-node-handler"]');
    private static readonly EDIT_SOURCE_CODE_ICON_LOCATOR: By = By.xpath('//*[@aria-label="Edit source code"]');

    constructor(
        @inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper,
        @inject(CLASSES.BrowserTabsUtil) private readonly browserTabsUtil: BrowserTabsUtil) {
    }

    async waitApplicationIcon(): Promise<void> {
        Logger.debug(`${this.constructor.name}.${this.waitApplicationIcon.name}`);

        await this.driverHelper.waitPresence(OcpApplicationPage.APPLICATION_ICON_LOCATOR, TimeoutConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT);
    }

    async waitAndOpenEditSourceCodeIcon(): Promise<void> {
        Logger.debug(`${this.constructor.name}.${this.waitAndOpenEditSourceCodeIcon.name}`);
        const parentGUID: string = await this.browserTabsUtil.getCurrentWindowHandle();
        await this.driverHelper.waitAndClick(OcpApplicationPage.EDIT_SOURCE_CODE_ICON_LOCATOR);
        await this.browserTabsUtil.waitAndSwitchToAnotherWindow(parentGUID, TimeoutConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT);
    }
}
