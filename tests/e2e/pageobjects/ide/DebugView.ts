/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { inject, injectable } from 'inversify';
import { CLASSES } from '../../inversify.types';
import { DriverHelper } from '../../utils/DriverHelper';
import { By, Key, WebElement } from 'selenium-webdriver';
import { Ide } from './Ide';
import { Logger } from '../../utils/Logger';
import { TimeoutConstants } from '../../TimeoutConstants';


@injectable()
export class DebugView {
    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper,
        @inject(CLASSES.Ide) private readonly ide: Ide) { }

    async clickOnDebugConfigurationDropDown() {
        Logger.debug('DebugView.clickOnDebugConfigurationDropDown');

        await this.driverHelper.waitAndClick(By.css('select.debug-configuration'));
    }

    async clickOnDebugConfigurationItem(itemText: string) {
        Logger.debug(`DebugView.clickOnDebugConfigurationItem "${itemText}"`);

        const configurationItemLocator: By = By.xpath(`//select[contains(@class,'debug-configuration')]//option[text()=\'${itemText}\']`);

        await this.driverHelper.waitAndClick(configurationItemLocator);
        await this.ide.performKeyCombination(Key.ESCAPE);
    }

    async clickOnRunDebugButton() {
        Logger.debug('DebugView.clickOnRunDebugButton');

        const runDebugButtonLocator: By = By.xpath('//span[@title=\'Start Debugging\']');

        await this.driverHelper.waitAndClick(runDebugButtonLocator, TimeoutConstants.TS_DIALOG_WINDOW_DEFAULT_TIMEOUT);
    }

    /**
     * Waits for number of threads  in "Threads" view to be more than 1 - this should mean that the debugger is connected.
     *
     * @param timeout
     */
    async waitForDebuggerToConnect(timeout: number = TimeoutConstants.TS_DEBUGGER_CONNECTION_TIMEOUT) {
        await this.driverHelper.getDriver().wait(async () => {
            Logger.debug(`Waiting for debugger to connect (threads to appear in "Threads" view)`);

            // collapse "Threads" view to work around che/18034 issue

            const threadElements: WebElement[] = await this.driverHelper.getDriver().findElements(By.xpath(`//div[contains(@class, 'theia-debug-thread')]`));
            if (threadElements.length > 1) {
                return true;
            }
            return false;
        }, timeout);
    }

}
