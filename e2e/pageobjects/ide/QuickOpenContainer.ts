import { injectable, inject } from 'inversify';
import { CLASSES } from '../../inversify.types';
import { DriverHelper } from '../../utils/DriverHelper';
import { TestConstants } from '../../TestConstants';
import { By } from 'selenium-webdriver';

/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

@injectable()
export class QuickOpenContainer {
    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) { }

    public async waitContainer(timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const monacoQuickOpenContainerLocator: By = By.xpath('//div[@class=\'monaco-quick-open-widget\']');
        await this.driverHelper.waitVisibility(monacoQuickOpenContainerLocator, timeout);
    }

    public async waitContainerDisappearance() {
        const monacoQuickOpenContainerLocator: By = By.xpath('//div[@class=\'monaco-quick-open-widget\' and @aria-hidden=\'true\']');
        await this.driverHelper.waitDisappearance(monacoQuickOpenContainerLocator);
    }

    public async clickOnContainerItem(itemText: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const quickContainerItemLocator: By = By.xpath(`//div[@class='quick-open-entry']//span[text()='${itemText}']`);

        await this.waitContainer(timeout);
        await this.driverHelper.waitAndClick(quickContainerItemLocator, timeout);
        await this.waitContainerDisappearance();
    }

    public async type(text: string) {
        await this.driverHelper.enterValue(By.css('.quick-open-input input'), text);
    }

}
