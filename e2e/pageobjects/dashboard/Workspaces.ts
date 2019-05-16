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


@injectable()
export class Workspaces {
    private static readonly ADD_WORKSPACE_BUTTON_CSS: string = '#add-item-button';

    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) { }

    async waitPage(timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.driverHelper.waitVisibility(By.css(Workspaces.ADD_WORKSPACE_BUTTON_CSS), timeout);
    }

    async clickAddWorkspaceButton(timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.driverHelper.waitAndClick(By.css(Workspaces.ADD_WORKSPACE_BUTTON_CSS), timeout);
    }

    async waitWorkspaceListItem(workspaceName: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const workspaceListItemLocator: By = By.css(this.getWorkspaceListItemLocator(workspaceName));

        await this.driverHelper.waitVisibility(workspaceListItemLocator, timeout);
    }

    async clickOnStopWorkspaceButton(workspaceName: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const stopWorkspaceButtonLocator: By = By.css(`#ws-name-${workspaceName} .workspace-status[uib-tooltip="Stop workspace"]`);

        await this.driverHelper.waitAndClick(stopWorkspaceButtonLocator, timeout);
    }

    async waitWorkspaceWithRunningStatus(workspaceName: string, timeout: number = TestConstants.TS_SELENIUM_START_WORKSPACE_TIMEOUT) {
        const runningStatusLocator: By = By.css(this.getWorkspaceStatusCssLocator(workspaceName, 'RUNNING'));

        await this.driverHelper.waitVisibility(runningStatusLocator, timeout);
    }

    async waitWorkspaceWithStoppedStatus(workspaceName: string, timeout: number = TestConstants.TS_SELENIUM_START_WORKSPACE_TIMEOUT) {
        const stoppedStatusLocator: By = By.css(this.getWorkspaceStatusCssLocator(workspaceName, 'STOPPED'));

        await this.driverHelper.waitVisibility(stoppedStatusLocator, timeout);
    }

    async clickWorkspaceListItem(workspaceName: string, timeout: number = TestConstants.TS_SELENIUM_START_WORKSPACE_TIMEOUT) {
        const workspaceListItemLocator: By = By.css(`div[id='ws-full-name-che/${workspaceName}']`);

        await this.driverHelper.waitAndClick(workspaceListItemLocator, timeout);
    }

    async clickDeleteButtonOnWorkspaceDetails(timeout: number = TestConstants.TS_SELENIUM_START_WORKSPACE_TIMEOUT) {
        const deleteButtonOnWorkspaceDetailsLocator: By = By.css('che-button-danger[che-button-title=\'Delete\']');

        await this.driverHelper.waitAndClick(deleteButtonOnWorkspaceDetailsLocator, timeout);
    }

    async waitWorkspaceListItemAbcence(workspaceName: string, timeout: number = TestConstants.TS_SELENIUM_START_WORKSPACE_TIMEOUT) {
        const workspaceListItemLocator: By = By.css(`div[id='ws-full-name-che/${workspaceName}']`);

        await this.driverHelper.waitDisappearance(workspaceListItemLocator, timeout);
    }

    async clickConfirmDeletionButton(timeout: number = TestConstants.TS_SELENIUM_START_WORKSPACE_TIMEOUT) {
        await this.driverHelper.waitAndClick(By.css('#ok-dialog-button'), timeout);
    }

    private getWorkspaceListItemLocator(workspaceName: string): string {
        return `#ws-name-${workspaceName}`;
    }

    private getWorkspaceStatusCssLocator(workspaceName: string, workspaceStatus: string): string {
        return `#ws-name-${workspaceName}[data-ws-status='${workspaceStatus}']`;
    }

}
