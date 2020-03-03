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
import { runTaskWithDialogShellAndClose } from '../../testsLibrary/CodeExecutionTests';

@injectable()
export class GetStarted {
    private static readonly CREATE_AND_OPEN_BUTTON_XPATH: string = '(//che-button-save-flat[@che-button-title=\'Create & Open\'][@aria-disabled=\'false\']/button)[1]';

    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) { }

    async waitTitleContains(expectedText: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const pageTitleLocator: By = By.xpath(`//div[contains(@che-title, '${expectedText}')]`);

        await this.driverHelper.waitVisibility(pageTitleLocator, timeout);
    }

    async waitPage(timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.waitTitleContains('Getting Started', timeout);
    }

    async waitSample(sampleName: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const sampleLocator: By = this.getSampleLocator(sampleName);

        await this.driverHelper.waitVisibility(sampleLocator, timeout);
    }

    async clickOnSample(sampleName: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const sampleLocator: By = this.getSampleLocator(sampleName);

        await this.driverHelper.waitAndClick(sampleLocator, timeout);
    }

    async selectSample(sampleName: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.clickOnSample(sampleName, timeout);
        await this.waitSampleSelected(sampleName, timeout);
    }

    async waitSampleSelected(sampleName: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const selectedSampleLocator: By =
            By.xpath(`//div[contains(@class, 'get-started-template') and contains(@class, 'selected')]//span[text()='${sampleName}']`);

        await this.driverHelper.waitVisibility(selectedSampleLocator, timeout);
    }

    async waitSampleUnselected(sampleName: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const unselectedSampleLocator: By =
            By.xpath(`//div[contains(@class, 'get-started-template') and not(contains(@class, 'selected'))]//span[text()='${sampleName}']`);

        await this.driverHelper.waitVisibility(unselectedSampleLocator, timeout);
    }

    async clickCreateAndOpenButton(timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const createAndOpenButtonXpath: string = '(//che-button-save-flat[@che-button-title=\'Create & Open\'][@aria-disabled=\'false\']/button)[1]';

        await this.driverHelper.waitAndClick(createAndOpenButtonXpath, timeout);
    }

    private getSampleLocator(sampleName: string): By {
        return By.xpath(`//div[contains(@class, 'get-started-template')]//span[text()='${sampleName}']`);
    }

}
