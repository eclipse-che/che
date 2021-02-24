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
import { Logger } from '../../utils/Logger';
import { TimeoutConstants } from '../../TimeoutConstants';

@injectable()
export class LeftToolBar {
    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) { }

    async waitToolbar(timeout: number = TimeoutConstants.TS_SELENIUM_TOOLBAR_TIMEOUT) {
        Logger.debug('LeftToolBar.waitToolbar');

        await this.driverHelper.waitVisibility(By.css('div.theia-app-left'), timeout);
    }

    async clickOnToolIcon(iconTitle: string, timeout: number = TimeoutConstants.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM) {
        Logger.debug(`LeftToolBar.clickOnToolIcon "${iconTitle}"`);

        const toolIconLocator: By = this.getViewLocator(iconTitle);

        await this.driverHelper.waitAndClick(toolIconLocator, timeout);
    }

    async waitToolIcon(iconTitle: string, timeout: number = TimeoutConstants.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM) {
        Logger.debug(`LeftToolBar.waitToolIcon "${iconTitle}"`);

        const toolIconLocator: By = this.getViewLocator(iconTitle);

        await this.driverHelper.waitVisibility(toolIconLocator, timeout);
    }

    async isViewSelected(toolIconTitle: string): Promise<Boolean> {
        Logger.debug(`LeftToolBar.isViewEnabled "${toolIconTitle}"`);

        const selectedViewLocator: By = this.getSelectedViewLocator(toolIconTitle);

        return await this.driverHelper.isVisible(selectedViewLocator);
    }

    async waitViewSelected(toolIconTitle: string, timeout: number = TimeoutConstants.TS_PROJECT_TREE_TIMEOUT) {
        Logger.debug(`LeftToolBar.waitViewSelected "${toolIconTitle}"`);

        const selectedViewLocator: By = this.getSelectedViewLocator(toolIconTitle);

        return await this.driverHelper.waitVisibility(selectedViewLocator);
    }

    async selectView(toolIconTitle: string, timeout: number = TimeoutConstants.TS_PROJECT_TREE_TIMEOUT) {
        Logger.debug(`LeftToolBar.selectView "${toolIconTitle}"`);

        await this.waitToolIcon(toolIconTitle, timeout);

        if ( await this.isViewSelected(toolIconTitle)) {
            return;
        }

        await this.clickOnToolIcon(toolIconTitle, timeout);
        await this.waitViewSelected(toolIconTitle, timeout);
    }

    private getSelectedViewLocator(viewTitle: string): By {
        return By.css(`div.theia-app-left .p-TabBar-content li.p-mod-current[title='${viewTitle}']`)
    }

    private getViewLocator(viewTitle: string): By {
        return By.css(`div.theia-app-left .p-TabBar-content li[title='${viewTitle}']`);
    }

}
