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
import { By, Key } from 'selenium-webdriver';
import { Ide } from './Ide';


@injectable()
export class DebugView {
    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper,
        @inject(CLASSES.Ide) private readonly ide: Ide) { }

    async clickOnDebugConfigurationDropDown() {
        await this.driverHelper.waitAndClick(By.css('select.debug-configuration'));
    }

    async clickOnDebugConfigurationItem(itemText: string) {
        const configurationItemLocator: By = By.xpath(`//select[@class='debug-configuration']//option[text()=\'${itemText}\']`);

        await this.driverHelper.waitAndClick(configurationItemLocator);
        await this.ide.performKeyCombination(Key.ESCAPE);
    }

    async clickOnRunDebugButton() {
        const runDebugButtonLocator: By = By.xpath('//span[@title=\'Start Debugging\']');

        await this.driverHelper.waitAndClick(runDebugButtonLocator);
    }

}
