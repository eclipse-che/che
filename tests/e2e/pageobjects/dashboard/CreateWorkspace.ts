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
import { By, error } from 'selenium-webdriver';
import { Logger } from '../../utils/Logger';
import { TimeoutConstants } from '../../TimeoutConstants';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';

@injectable()
export class CreateWorkspace {
    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper,
                @inject(CLASSES.BrowserTabsUtil) private readonly browserTabsUtil: BrowserTabsUtil) { }

    async waitTitleContains(expectedText: string, timeout: number = TimeoutConstants.TS_COMMON_DASHBOARD_WAIT_TIMEOUT) {
        Logger.debug(`CreateWorkspace.waitTitleContains text: "${expectedText}"`);

        const pageTitleLocator: By = By.xpath(`//h1[contains(text(), '${expectedText}')]`);

        await this.driverHelper.waitVisibility(pageTitleLocator, timeout);
    }

    async waitPage(timeout: number = TimeoutConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT) {
        Logger.debug('CreateWorkspace.waitPage');

        await this.waitTitleContains('Getting Started', timeout);
    }

    async waitSample(sampleName: string, timeout: number = TimeoutConstants.TS_COMMON_DASHBOARD_WAIT_TIMEOUT) {
        Logger.debug(`CreateWorkspace.waitSample sampleName: "${sampleName}"`);

        const sampleLocator: By = this.getSampleLocator(sampleName);

        await this.driverHelper.waitVisibility(sampleLocator, timeout);
    }

    async clickOnSample(sampleName: string, timeout: number = TimeoutConstants.TS_CLICK_DASHBOARD_ITEM_TIMEOUT) {
        Logger.debug(`CreateWorkspace.clickOnSample sampleName: "${sampleName}"`);

        const sampleLocator: By = this.getSampleLocator(sampleName);

        try {
            await this.driverHelper.waitAndClick(sampleLocator, timeout);
        } catch (err) {
            if (err instanceof error.StaleElementReferenceError) {
                // for CRW 2.9.x create workspace sometimes fails with StakeElementReferenceError
                // causing the tests to fail to create a workspace, when the dashboard is only
                // partially initialized. Refreshing page and trying to click on workspace again.
                await this.browserTabsUtil.refreshPage();
                await this.waitPage();
                await this.driverHelper.waitAndClick(sampleLocator, timeout);
            }
        }
    }

    private getSampleLocator(sampleName: string): By {
        Logger.trace(`CreateWorkspace.getSampleLocator sampleName: ${sampleName}`);

        return By.xpath(`//article[contains(@class, 'sample-card')]//div[text()='${sampleName}']`);
    }

}
