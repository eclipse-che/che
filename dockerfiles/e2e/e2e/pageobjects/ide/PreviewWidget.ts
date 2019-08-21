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
import { TestConstants } from '../../TestConstants';
import { Ide } from './Ide';

@injectable()
export class PreviewWidget {
    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper,
        @inject(CLASSES.Ide) private readonly ide: Ide) { }

    async waitAndSwitchToWidgetFrame() {
        const iframeLocator: By = By.css('.theia-mini-browser iframe');

        await this.driverHelper.waitAndSwitchToFrame(iframeLocator);
    }

    async waitPreviewWidget(timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.driverHelper.waitVisibility(By.css('div.theia-mini-browser'), timeout);
    }

    async waitPreviewWidgetAbsence() {
        await this.driverHelper.waitDisappearance(By.css('div.theia-mini-browser'));
    }

    async waitContentAvailable(contentLocator: By,
        timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT,
        polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING * 5) {

        await this.waitAndSwitchToWidgetFrame();

        await this.driverHelper.getDriver().wait(async () => {
            const isApplicationTitleVisible: boolean = await this.driverHelper.isVisible(contentLocator);

            if (isApplicationTitleVisible) {
                await this.driverHelper.getDriver().switchTo().defaultContent();
                await this.ide.waitAndSwitchToIdeFrame();

                return true;
            }

            await this.switchBackToIdeFrame();
            await this.refreshPage();
            await this.waitAndSwitchToWidgetFrame();
            await this.driverHelper.wait(polling);
        }, timeout);
    }

    async waitVisibility(element: By, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.driverHelper.waitVisibility(element, timeout);
    }

    async waitAndClick(element: By, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.driverHelper.waitAndClick(element, timeout);
    }

    async refreshPage() {
        const refreshButtonLocator: By = By.css('.theia-mini-browser .theia-mini-browser-refresh');
        await this.driverHelper.waitAndClick(refreshButtonLocator);
    }

    async switchBackToIdeFrame() {
        await this.driverHelper.getDriver().switchTo().defaultContent();
        await this.ide.waitAndSwitchToIdeFrame();
    }

}
