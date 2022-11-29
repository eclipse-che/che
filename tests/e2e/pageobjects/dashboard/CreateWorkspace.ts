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
import { By, Key } from 'selenium-webdriver';
import { Logger } from '../../utils/Logger';
import { TimeoutConstants } from '../../TimeoutConstants';

@injectable()
export class CreateWorkspace {
    static readonly FACTORY_URL_LOCATOR: By = By.xpath(`//input[@id="git-repo-url"]`);

    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) { }

    async waitTitleContains(expectedText: string, timeout: number = TimeoutConstants.TS_COMMON_DASHBOARD_WAIT_TIMEOUT) {
        Logger.debug(`CreateWorkspace.waitTitleContains text: "${expectedText}"`);

        const pageTitleLocator: By = By.xpath(`//h1[contains(text(), '${expectedText}')]`);

        await this.driverHelper.waitVisibility(pageTitleLocator, timeout);
    }

    async waitPage(timeout: number = TimeoutConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT) {
        Logger.debug('CreateWorkspace.waitPage');

        await this.waitTitleContains('Create Workspace', timeout);
    }

    async waitSample(sampleName: string, timeout: number = TimeoutConstants.TS_COMMON_DASHBOARD_WAIT_TIMEOUT) {
        Logger.debug(`CreateWorkspace.waitSample sampleName: "${sampleName}"`);

        const sampleLocator: By = this.getSampleLocator(sampleName);

        await this.driverHelper.waitVisibility(sampleLocator, timeout);
    }

    async clickOnSample(sampleName: string, timeout: number = TimeoutConstants.TS_CLICK_DASHBOARD_ITEM_TIMEOUT) {
        Logger.debug(`CreateWorkspace.clickOnSample sampleName: "${sampleName}"`);

        const sampleLocator: By = this.getSampleLocator(sampleName);

        await this.driverHelper.waitAndClick(sampleLocator, timeout);
    }

    async startWorkspaceUsingFactory(factoryUrl: string, timeout: number = TimeoutConstants.TS_CLICK_DASHBOARD_ITEM_TIMEOUT) {
        Logger.debug(`CreateWorkspace.startWorkspaceUsingFactory factoryUrl: "${factoryUrl}"`);
        await this.driverHelper.waitVisibility(CreateWorkspace.FACTORY_URL_LOCATOR, timeout);
        await this.driverHelper.type(CreateWorkspace.FACTORY_URL_LOCATOR, Key.chord(factoryUrl, Key.ENTER), timeout);
    }

    private getSampleLocator(sampleName: string): By {
        Logger.trace(`CreateWorkspace.getSampleLocator sampleName: ${sampleName}`);

        return By.xpath(`//article[contains(@class, 'sample-card')]//div[text()='${sampleName}']`);
    }

}
