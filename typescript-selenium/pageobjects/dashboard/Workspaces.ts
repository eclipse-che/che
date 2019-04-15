/*********************************************************************
 * Copyright (c) 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { TestConstants } from "../../TestConstants";
import { injectable, inject } from "inversify";
import { DriverHelper } from "../../utils/DriverHelper";
import { CLASSES } from "../../types";
import { By } from "selenium-webdriver";


@injectable()
export class Workspaces {
    private readonly driverHelper: DriverHelper;
    private static readonly TITLE: string = ".che-toolbar-title-label";
    private static readonly ADD_WORKSPACE_BUTTON_CSS: string = "#add-item-button";
    private static readonly START_STOP_WORKSPACE_TIMEOUT: number = TestConstants.START_STOP_WORKSPACE_TIMEOUT

    constructor(
        @inject(CLASSES.DriverHelper) driverHelper: DriverHelper
    ) {
        this.driverHelper = driverHelper;
    }



    private getWorkspaceListItemLocator(workspaceName: string): string {
        return `#ws-name-${workspaceName}`
    }

    private getWorkspaceStatusLocator(workspaceName: string, workspaceStatus: string){
        return `#ws-name-${workspaceName}[data-ws-status='${workspaceStatus}']`
    }

    async waitPage(timeout = TestConstants.DEFAULT_TIMEOUT) {
        await this.driverHelper.waitVisibility(By.css(Workspaces.ADD_WORKSPACE_BUTTON_CSS), timeout)
    }

    async clickAddWorkspaceButton(timeout = TestConstants.DEFAULT_TIMEOUT) {
        await this.driverHelper.waitAndClick(By.css(Workspaces.ADD_WORKSPACE_BUTTON_CSS), timeout)
    }

    async waitWorkspaceListItem(workspaceName: string, timeout = TestConstants.DEFAULT_TIMEOUT) {
        const workspaceListItemLocator: By =  await By.css(this.getWorkspaceListItemLocator(workspaceName));

        await this.driverHelper.waitVisibility(workspaceListItemLocator, timeout)
    }

    async clickOnStopWorkspaceButton(workspaceName: string, timeout = TestConstants.DEFAULT_TIMEOUT) {
        const stopWorkspaceButtonLocator: By = By.css(`#ws-name-${workspaceName} .workspace-status[uib-tooltip="Stop workspace"]`)

        await this.driverHelper.waitAndClick(stopWorkspaceButtonLocator, timeout)
    }

    async waitWorkspaceWithRunningStatus(workspaceName: string, timeout = TestConstants.START_STOP_WORKSPACE_TIMEOUT) {
        const runningStatusLocator: By = await this.getWorkspaceStatusLocator(workspaceName, 'RUNNING')
        
        await this.driverHelper.waitVisibility(runningStatusLocator, timeout)
    }

    async waitWorkspaceWithStoppedStatus(workspaceName: string, timeout = TestConstants.START_STOP_WORKSPACE_TIMEOUT) {
        const stoppedStatusLocator: By = await this.getWorkspaceStatusLocator(workspaceName, 'STOPPED')
        
        await this.driverHelper.waitVisibility(stoppedStatusLocator, timeout)
    }

    async clickWorkspaceListItem(workspaceName: string, timeout = TestConstants.START_STOP_WORKSPACE_TIMEOUT) {
        const workspaceListItemLocator: By = By.css(`div[id='ws-full-name-che/${workspaceName}']`)
        
        await this.driverHelper.waitAndClick(workspaceListItemLocator, timeout) 
    }

    async clickDeleteButtonOnWorkspaceDetails(timeout = TestConstants.START_STOP_WORKSPACE_TIMEOUT) {
        const deleteButtonOnWorkspaceDetailsLocator: By = By.css("che-button-danger[che-button-title='Delete']")

        await this.driverHelper.waitAndClick(deleteButtonOnWorkspaceDetailsLocator, timeout)
    }

    async waitWorkspaceListItemAbcence(workspaceName: string, timeout = TestConstants.START_STOP_WORKSPACE_TIMEOUT) {
        const workspaceListItemLocator: By = By.css(`div[id='ws-full-name-che/${workspaceName}']`)

        await this.driverHelper.waitDisappearance(workspaceListItemLocator, timeout)
    }

    async clickConfirmDeletionButton(timeout = TestConstants.START_STOP_WORKSPACE_TIMEOUT) {
        await this.driverHelper.waitAndClick(By.css('#ok-dialog-button'), timeout)
    }

}
