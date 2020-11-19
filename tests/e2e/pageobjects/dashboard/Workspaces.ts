/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { TestConstants } from '../../TestConstants';
import { injectable, inject } from 'inversify';
import { DriverHelper } from '../../utils/DriverHelper';
import { CLASSES } from '../../inversify.types';
import { By } from 'selenium-webdriver';
import { Logger } from '../../utils/Logger';
import { TimeoutConstants } from '../../TimeoutConstants';


@injectable()
export class Workspaces {
    private static readonly ADD_WORKSPACE_BUTTON_XPATH: string = `//button[text()='Add Workspace']`;

    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) { }

    async waitPage(timeout: number = TimeoutConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT) {
        Logger.debug('Workspaces.waitPage');

        await this.driverHelper.waitVisibility(By.xpath(Workspaces.ADD_WORKSPACE_BUTTON_XPATH), timeout);
    }

    async clickAddWorkspaceButton(timeout: number = TimeoutConstants.TS_CLICK_DASHBOARD_ITEM_TIMEOUT) {
        Logger.debug('Workspaces.clickAddWorkspaceButton');

        await this.driverHelper.waitAndClick(By.xpath(Workspaces.ADD_WORKSPACE_BUTTON_XPATH), timeout);
    }

    async waitWorkspaceListItem(workspaceName: string, timeout: number = TimeoutConstants.TS_COMMON_DASHBOARD_WAIT_TIMEOUT) {
        Logger.debug(`Workspaces.waitWorkspaceListItem "${workspaceName}"`);

        const workspaceListItemLocator: By = By.xpath(this.getWorkspaceListItemLocator(workspaceName));

        await this.driverHelper.waitVisibility(workspaceListItemLocator, timeout);
    }

    async clickOnStopWorkspaceButton(workspaceName: string, timeout: number = TimeoutConstants.TS_CLICK_DASHBOARD_ITEM_TIMEOUT) {
        Logger.debug(`Workspaces.clickOnStopWorkspaceButton "${workspaceName}"`);

        const stopWorkspaceButtonLocator: By = By.xpath(`(//tbody[@class='workspaces-list-table-body' and //td[text()[3]='${workspaceName}']]//td[@data-label='ACTIONS']//span)[1]`);

        await this.driverHelper.waitAndClick(stopWorkspaceButtonLocator, timeout);
    }

    async waitWorkspaceWithRunningStatus(workspaceName: string, timeout: number = TimeoutConstants.TS_COMMON_DASHBOARD_WAIT_TIMEOUT) {
        Logger.debug(`Workspaces.waitWorkspaceWithRunningStatus "${workspaceName}"`);

        const runningStatusLocator: By = By.css(this.getWorkspaceStatusCssLocator(workspaceName, 'RUNNING'));

        await this.driverHelper.waitVisibility(runningStatusLocator, timeout);
    }

    async waitWorkspaceWithStoppedStatus(workspaceName: string, timeout: number = TimeoutConstants.TS_COMMON_DASHBOARD_WAIT_TIMEOUT) {
        Logger.debug(`Workspaces.waitWorkspaceWithStoppedStatus "${workspaceName}"`);

        const stoppedStatusLocator: By = By.css(this.getWorkspaceStatusCssLocator(workspaceName, 'STOPPED'));

        await this.driverHelper.waitVisibility(stoppedStatusLocator, timeout);
    }

    async clickWorkspaceListItem(workspaceName: string, timeout: number = TimeoutConstants.TS_CLICK_DASHBOARD_ITEM_TIMEOUT) {
        Logger.debug(`Workspaces.clickWorkspaceListItem "${workspaceName}"`);

        const workspaceListItemLocator: By = By.xpath(this.getWorkspaceListItemLocator(workspaceName));

        await this.driverHelper.waitAndClick(workspaceListItemLocator, timeout);
    }

    async clickDeleteButtonOnWorkspaceDetails(timeout: number = TimeoutConstants.TS_CLICK_DASHBOARD_ITEM_TIMEOUT) {
        Logger.debug('Workspaces.clickDeleteButtonOnWorkspaceDetails');

        const deleteButtonOnWorkspaceDetailsLocator: By = By.css('che-button-danger[che-button-title=\'Delete\']');

        await this.driverHelper.waitAndClick(deleteButtonOnWorkspaceDetailsLocator, timeout);
    }

    async waitWorkspaceListItemAbcence(workspaceName: string, timeout: number = TimeoutConstants.TS_COMMON_DASHBOARD_WAIT_TIMEOUT) {
        Logger.debug(`Workspaces.waitWorkspaceListItemAbcence "${workspaceName}"`);

        const workspaceListItemLocator: By = By.xpath(this.getWorkspaceListItemLocator(workspaceName));

        await this.driverHelper.waitDisappearance(workspaceListItemLocator, timeout);
    }

    async confirmWorkspaceDeletion(timeout: number = TimeoutConstants.TS_CLICK_DASHBOARD_ITEM_TIMEOUT) {
        Logger.debug('Workspaces.confirmWorkspaceDeletion');

        const checkbox: By = By.xpath(`//che-popup//input[@id='enable-button' and contains(@class, 'ng-empty')]`);
        const checkbox_checked: By = By.xpath(`//che-popup//input[@id='enable-button' and contains(@class, 'ng-not-empty')]`);
        const deleteButton: By = By.xpath('//che-popup//che-button-danger');

        await this.driverHelper.waitAndClick(checkbox, 5000);
        try {
            await this.driverHelper.waitVisibility(checkbox_checked, 3000);
        } catch (err) {
            Logger.info('The checkbox is not checked. Trying again.');
            await this.driverHelper.waitAndClick(checkbox, 5000);

            try {
                await this.driverHelper.waitVisibility(checkbox_checked, 3000);
            } catch (err) {
                Logger.error('Test was not able to select the checkbox during a workspace deletion.');
                throw err;
            }
        }

        await this.driverHelper.waitAndClick(deleteButton, 10000);
    }

    private getWorkspaceListItemLocator(workspaceName: string): string {
        return `//tbody[@class='workspaces-list-table-body']//td[text()[3]='${workspaceName}']`;
    }

    private getWorkspaceStatusCssLocator(workspaceName: string, workspaceStatus: string): string {
        return `#ws-name-${workspaceName}[data-ws-status='${workspaceStatus}']`;
    }

}
