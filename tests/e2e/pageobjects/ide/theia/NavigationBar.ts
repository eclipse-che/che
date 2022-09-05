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
import { DriverHelper } from '../../../utils/DriverHelper';
import { CLASSES } from '../../../inversify.types';
import { Logger } from '../../../utils/Logger';
import { By } from 'selenium-webdriver';

@injectable()
export class NavigationBar {
    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) { }

    async clickOnShowNavigationBar() {
        Logger.debug('CodereadyNavigationBar.clickOnShowNavigationBar');

        const showNavBarButton: By = By.css(`[title='Show navigation bar']`);
        await this.driverHelper.waitAndClick(showNavBarButton);
    }

    async waitNavigationBar() {
        Logger.debug('CodereadyNavigationBar.waitCodereadyNavigationBar');

        const navigationBar: By = By.id('page-sidebar');
        await this.driverHelper.getDriver().switchTo().defaultContent();
        await this.driverHelper.waitVisibility(navigationBar);
    }

    async openNavigationBar() {
        Logger.debug('CodereadyNavigationBar.openNavigationBar');

        await this.clickOnShowNavigationBar();
        await this.waitNavigationBar();
    }
}
