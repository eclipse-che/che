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
export class GetStarted {
    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) { }

    async waitTitleContains(expectedText: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug(`GetStarted.waitTitleContains text: "${expectedText}"`);

        const pageTitleLocator: By = By.xpath(`//div[contains(@title, '${expectedText}')]`);

        await this.driverHelper.waitVisibility(pageTitleLocator, timeout);
    }

    async waitPage(timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug('GetStarted.waitPage');

        await this.waitTitleContains('Getting Started', timeout);
    }

    async waitSample(sampleName: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug(`GetStarted.waitSample sampleName: "${sampleName}"`);

        const sampleLocator: By = this.getSampleLocator(sampleName);

        await this.driverHelper.waitVisibility(sampleLocator, timeout);
    }

    async clickOnSample(sampleName: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug(`GetStarted.clickOnSample sampleName: "${sampleName}"`);

        const sampleLocator: By = this.getSampleLocator(sampleName);

        await this.driverHelper.waitAndClick(sampleLocator, timeout);
    }

    async waitSampleSelected(sampleName: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug(`GetStarted.waitSampleSelected sampleName: "${sampleName}"`);

        const selectedSampleLocator: By =
            By.xpath(`//div[contains(@class, 'get-started-template') and contains(@class, 'selected')]//span[text()='${sampleName}']`);

        await this.driverHelper.waitVisibility(selectedSampleLocator, timeout);
    }

    async waitSampleUnselected(sampleName: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug(`GetStarted.waitSampleUnselected sampleName: "${sampleName}"`);

        const unselectedSampleLocator: By =
            By.xpath(`//div[contains(@class, 'get-started-template') and not(contains(@class, 'selected'))]//span[text()='${sampleName}']`);

        await this.driverHelper.waitVisibility(unselectedSampleLocator, timeout);
    }

    async clickCreateAndOpenButton(timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug('GetStarted.clickCreateAndOpenButton');

        const createAndOpenButtonLocator: By =
            By.xpath('(//che-button-save-flat[@che-button-title=\'Create & Open\'][@aria-disabled=\'false\']/button)[1]');

        await this.driverHelper.waitAndClick(createAndOpenButtonLocator, timeout);
    }

    private getSampleLocator(sampleName: string): By {
        Logger.trace(`GetStarted.getSampleLocator sampleName: ${sampleName}`);

        return By.xpath(`//div[contains(@devfile, 'devfile')]/div/b[contains(text(), '${sampleName}')]`);
    }

}
