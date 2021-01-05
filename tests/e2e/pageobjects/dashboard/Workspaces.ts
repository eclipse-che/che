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
import { DriverHelper } from '../../utils/DriverHelper';
import { CLASSES } from '../../inversify.types';
import { By } from 'selenium-webdriver';
import { Logger } from '../../utils/Logger';
import { TimeoutConstants } from '../../TimeoutConstants';

export enum WorkspaceStatusUI {
    Running = 'green',
    Stopped = 'grey'
}

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

        const stopWorkspaceButtonLocator: By = By.xpath(`(${this.getWorkspaceListItemLocator(workspaceName)}//td[@data-label='ACTIONS']//span)[1]`);

        await this.driverHelper.waitAndClick(stopWorkspaceButtonLocator, timeout);
    }

    async waitWorkspaceWithRunningStatus(workspaceName: string, timeout: number = TimeoutConstants.TS_COMMON_DASHBOARD_WAIT_TIMEOUT) {
        Logger.debug(`Workspaces.waitWorkspaceWithRunningStatus "${workspaceName}"`);

        const runningStatusLocator: By = this.getWorkspaceStatusLocator(workspaceName, WorkspaceStatusUI.Running);

        await this.driverHelper.waitVisibility(runningStatusLocator, timeout);
    }

    async waitWorkspaceWithStoppedStatus(workspaceName: string, timeout: number = TimeoutConstants.TS_DASHBOARD_WORKSPACE_STOP_TIMEOUT) {
        Logger.debug(`Workspaces.waitWorkspaceWithStoppedStatus "${workspaceName}"`);

        const stoppedStatusLocator: By = this.getWorkspaceStatusLocator(workspaceName, WorkspaceStatusUI.Stopped);

        await this.driverHelper.waitVisibility(stoppedStatusLocator, timeout);
    }

    async clickWorkspaceListItem(workspaceName: string, timeout: number = TimeoutConstants.TS_CLICK_DASHBOARD_ITEM_TIMEOUT) {
        Logger.debug(`Workspaces.clickWorkspaceListItem "${workspaceName}"`);

        const workspaceListItemLocator: By = By.xpath(this.getWorkspaceListItemLocator(workspaceName));

        await this.driverHelper.waitAndClick(workspaceListItemLocator, timeout);
    }

    async clickWorkspaceDeleteButton(workspaceName: string, timeout: number = TimeoutConstants.TS_CLICK_DASHBOARD_ITEM_TIMEOUT) {
        Logger.debug('Workspaces.clickDeleteButtonOnWorkspaceDetails');

        const deleteButtonOnWorkspaceDetailsLocator: By = By.xpath(`(${this.getWorkspaceListItemLocator(workspaceName)}//td[@data-label='ACTIONS']//span)[2]`);

        await this.driverHelper.waitAndClick(deleteButtonOnWorkspaceDetailsLocator, timeout);
    }

    async waitWorkspaceListItemAbcence(workspaceName: string, timeout: number = TimeoutConstants.TS_COMMON_DASHBOARD_WAIT_TIMEOUT) {
        Logger.debug(`Workspaces.waitWorkspaceListItemAbcence "${workspaceName}"`);

        const workspaceListItemLocator: By = By.xpath(this.getWorkspaceListItemLocator(workspaceName));

        await this.driverHelper.waitDisappearance(workspaceListItemLocator, timeout);
    }

    private getWorkspaceListItemLocator(workspaceName: string): string {
        return `//tbody[@class='workspaces-list-table-body']//tr[//td[text()[3]='${workspaceName}']]`;
    }

    private getWorkspaceStatusLocator(workspaceName: string, workspaceStatus: WorkspaceStatusUI): By {
        return By.xpath(`${this.getWorkspaceListItemLocator(workspaceName)}//span[@data-testid='workspace-status-indicator']//*[local-name()='svg' and @fill='${workspaceStatus}']`);
    }

}
