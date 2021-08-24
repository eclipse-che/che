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
import { By, error, Key } from 'selenium-webdriver';
import { TestConstants } from '../../TestConstants';
import { Ide } from './Ide';
import { Logger } from '../../utils/Logger';
import { TimeoutConstants } from '../../TimeoutConstants';

@injectable()
export class PreviewWidget {
    private static readonly WIDGET_LOCATOR: By = By.css('div.theia-mini-browser');
    private static readonly WIDGET_IFRAME_LOCATOR: By = By.css('div.theia-mini-browser iframe');
    private static readonly WIDGET_URL_LOCATOR: By = By.css('div.theia-mini-browser input');
    private static readonly WIDGET_REFRESH_BUTTON_LOCATOR: By = By.css('div.theia-mini-browser-refresh.theia-mini-browser-button');

    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper,
        @inject(CLASSES.Ide) private readonly ide: Ide) { }

    async waitUrl(expectedUrl: string, timeout: number = TimeoutConstants.TS_WAIT_URL_TIMEOUT) {
        Logger.debug(`PreviewWidget.waitUrl ${expectedUrl}`);

        await this.driverHelper.waitAttributeValue(PreviewWidget.WIDGET_URL_LOCATOR, 'value', expectedUrl, timeout);
    }

    async getUrl(timeout: number = TimeoutConstants.TS_WAIT_URL_TIMEOUT): Promise<string> {
        Logger.debug(`PreviewWidget.getUrl`);

        return await this.driverHelper.waitAndGetElementAttribute(PreviewWidget.WIDGET_URL_LOCATOR, 'value', timeout);
    }

    async typeUrl(url: string, timeout: number = TimeoutConstants.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM) {
        Logger.debug(`PreviewWidget.typeUrl ${url}`);

        await this.driverHelper.type(PreviewWidget.WIDGET_URL_LOCATOR, url, timeout);
    }

    async clearUrl() {
        Logger.debug('PreviewWidget.clearUrl');

        await this.typeUrl(Key.chord(Key.CONTROL, 'a', Key.DELETE));
        await this.waitUrl('');
    }

    async typeAndApplyUrl(url: string, timeout: number = TimeoutConstants.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM) {
        Logger.debug(`PreviewWidget.typeAndApplyUrl ${url}`);

        await this.clearUrl();
        await this.typeUrl(Key.chord(url, Key.ENTER), timeout);
    }

    async waitApplicationOpened(expectedUrl: string, timeout: number) {
        Logger.debug(`PreviewWidget.waitApplicationOpened ${expectedUrl}`);

        await this.driverHelper.getDriver().wait(async () => {
            try {
                await this.waitUrl(expectedUrl, timeout / 5);
                return true;
            } catch (err) {
                if (!(err instanceof error.TimeoutError)) {
                    throw err;
                }

                await this.typeAndApplyUrl(expectedUrl, timeout);
            }
        }, timeout);
    }

    async waitAndSwitchToWidgetFrame() {
        Logger.debug('PreviewWidget.waitAndSwitchToWidgetFrame');

        await this.driverHelper.waitAndSwitchToFrame(PreviewWidget.WIDGET_IFRAME_LOCATOR, TimeoutConstants.TS_SELENIUM_PREVIEW_WIDGET_DEFAULT_TIMEOUT);
    }

    async waitPreviewWidgetAbsence() {
        Logger.debug('PreviewWidget.waitPreviewWidgetAbsence');

        await this.driverHelper.waitDisappearance(PreviewWidget.WIDGET_LOCATOR);
    }

    async waitContentAvailable(contentLocator: By,
        timeout: number,
        polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING * 5) {

        Logger.debug(`PreviewWidget.waitContentAvailable ${contentLocator}`);

        await this.waitAndSwitchToWidgetFrame();
        await this.driverHelper.getDriver().wait(async () => {
            const isApplicationTitleVisible: boolean = await this.driverHelper.isVisible(contentLocator);
            if (isApplicationTitleVisible) {
                await this.driverHelper.getDriver().switchTo().defaultContent();
                await this.ide.waitAndSwitchToIdeFrame();
                return true;
            }

            await this.driverHelper.wait(polling);
        }, timeout);
    }

    async waitVisibility(element: By, timeout: number) {
        Logger.debug(`PreviewWidget.waitVisibility ${element}`);

        await this.driverHelper.waitVisibility(element, timeout);
    }

    async waitAndClick(element: By, timeout: number = TimeoutConstants.TS_SELENIUM_PREVIEW_WIDGET_DEFAULT_TIMEOUT) {
        Logger.debug(`PreviewWidget.waitAndClick ${element}`);

        await this.driverHelper.waitAndClick(element, timeout);
    }

    async refreshPage() {
        Logger.debug('PreviewWidget.refreshPage');

        await this.driverHelper.waitAndClick(PreviewWidget.WIDGET_REFRESH_BUTTON_LOCATOR);
    }

    async switchBackToIdeFrame() {
        Logger.debug('PreviewWidget.switchBackToIdeFrame');

        await this.driverHelper.getDriver().switchTo().defaultContent();
        await this.ide.waitAndSwitchToIdeFrame();
    }

}
