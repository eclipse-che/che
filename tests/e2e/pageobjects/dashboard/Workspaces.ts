/*********************************************************************
 * Copyright (c) 2019-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { injectable, inject } from 'inversify';
import { DriverHelper } from '../../utils/DriverHelper';
import { CLASSES } from '../../configs/inversify.types';
import { By, WebElement } from 'selenium-webdriver';
import { Logger } from '../../utils/Logger';
import { TimeoutConstants } from '../../constants/TimeoutConstants';

export enum WorkspaceStatusUI {
    Running = 'green',
    Stopped = 'grey'
}

@injectable()
export class Workspaces {
    private static readonly ADD_WORKSPACE_BUTTON_XPATH: string = `//button[text()='Add Workspace']`;
    private static readonly WORKSPACE_ITEM_TABLE_NAME_SECTION_XPATH: string = `//td[@data-label="Name"]/span/a`;

    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) {
    }

    async waitPage(timeout: number = TimeoutConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT): Promise<void> {
        Logger.debug('Workspaces.waitPage');

        await this.driverHelper.waitVisibility(By.xpath(Workspaces.ADD_WORKSPACE_BUTTON_XPATH), timeout);
    }

    async clickAddWorkspaceButton(timeout: number = TimeoutConstants.TS_CLICK_DASHBOARD_ITEM_TIMEOUT): Promise<void> {
        Logger.debug('Workspaces.clickAddWorkspaceButton');

        await this.driverHelper.waitAndClick(By.xpath(Workspaces.ADD_WORKSPACE_BUTTON_XPATH), timeout);
    }

    async clickOpenButton(workspaceName: string, timeout: number = TimeoutConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT): Promise<void> {
        Logger.debug('Workspaces.clickOpenButton');

        await this.driverHelper.waitAndClick(this.getOpenButtonLocator(workspaceName), timeout);
    }

    async waitWorkspaceListItem(workspaceName: string, timeout: number = TimeoutConstants.TS_COMMON_DASHBOARD_WAIT_TIMEOUT): Promise<void> {
        Logger.debug(`Workspaces.waitWorkspaceListItem "${workspaceName}"`);

        const workspaceListItemLocator: By = By.xpath(this.getWorkspaceListItemLocator(workspaceName));

        await this.driverHelper.waitVisibility(workspaceListItemLocator, timeout);
    }

    async waitWorkspaceWithRunningStatus(workspaceName: string, timeout: number = TimeoutConstants.TS_COMMON_DASHBOARD_WAIT_TIMEOUT): Promise<void> {
        Logger.debug(`Workspaces.waitWorkspaceWithRunningStatus "${workspaceName}"`);

        const runningStatusLocator: By = this.getWorkspaceStatusLocator(workspaceName, WorkspaceStatusUI.Running);

        await this.driverHelper.waitVisibility(runningStatusLocator, timeout);
    }

    async waitWorkspaceWithStoppedStatus(workspaceName: string, timeout: number = TimeoutConstants.TS_DASHBOARD_WORKSPACE_STOP_TIMEOUT): Promise<void> {
        Logger.debug(`Workspaces.waitWorkspaceWithStoppedStatus "${workspaceName}"`);

        const stoppedStatusLocator: By = this.getWorkspaceStatusLocator(workspaceName, WorkspaceStatusUI.Stopped);

        await this.driverHelper.waitVisibility(stoppedStatusLocator, timeout);
    }

    async clickWorkspaceListItem(workspaceName: string, timeout: number = TimeoutConstants.TS_CLICK_DASHBOARD_ITEM_TIMEOUT): Promise<void> {
        Logger.debug(`Workspaces.clickWorkspaceListItem "${workspaceName}"`);

        const workspaceListItemLocator: By = By.xpath(this.getWorkspaceListItemLocator(workspaceName));

        await this.driverHelper.waitAndClick(workspaceListItemLocator, timeout);
    }

    async clickActionsButton(workspaceName: string): Promise<void> {
        Logger.debug(`Workspaces.clickActionsButton of the '${workspaceName}' list item`);

        await this.driverHelper.waitAndClick(this.getActionsLocator(workspaceName));
    }

    async waitActionsPopup(workspaceName: string, timeout: number = TimeoutConstants.TS_CONTEXT_MENU_TIMEOUT): Promise<void> {
        Logger.debug(`Workspaces.waitActionsPopup of the '${workspaceName}' list item`);

        await this.driverHelper.waitVisibility(this.getExpandedActionsLocator(workspaceName), timeout);
    }

    async openActionsPopup(workspaceName: string, timeout: number = TimeoutConstants.TS_CONTEXT_MENU_TIMEOUT): Promise<void> {
        Logger.debug(`Workspaces.openActionsPopup for the '${workspaceName}' list item`);
        await this.clickActionsButton(workspaceName);
        await this.waitActionsPopup(workspaceName, timeout);
    }

    async clickActionsDeleteButton(workspaceName: string): Promise<void> {
        Logger.debug(`Workspaces.clickActionsDeleteButton for the '${workspaceName}' list item`);

        await this.driverHelper.waitAndClick(this.getActionsPopupButtonLocator(workspaceName, 'Delete Workspace'));
    }

    async clickActionsStopWorkspaceButton(workspaceName: string): Promise<void> {
        Logger.debug(`Workspaces.clickActionsStopWorkspaceButton for the '${workspaceName}' list item`);
        // todo: workaround because of issue CRW-3649
        try {
            await this.driverHelper.waitAndClick(this.getActionsPopupButtonLocator(workspaceName, 'Stop Workspace'));
        } catch (e) {
            Logger.warn(`Workspaces.clickActionsStopWorkspaceButton for the '${workspaceName}' list item - popup was missed, try to click one more time (issue CRW-3649).`);

            await this.driverHelper.waitAndClick(this.getActionsLocator(workspaceName));
            await this.driverHelper.waitAndClick(this.getActionsPopupButtonLocator(workspaceName, 'Stop Workspace'));
        }
    }

    async waitDeleteWorkspaceConfirmationWindow(timeout: number = TimeoutConstants.TS_DASHBOARD_WORKSPACE_STOP_TIMEOUT): Promise<void> {
        Logger.debug(`Workspaces.waitDeleteWorkspaceConfirmationWindow`);

        const confirmationWindowLocator: By = By.xpath(`//div[@aria-label='Delete workspaces confirmation window']`);

        await this.driverHelper.waitVisibility(confirmationWindowLocator, timeout);
    }


    async clickToDeleteConfirmationCheckbox(timeout: number = TimeoutConstants.TS_DASHBOARD_WORKSPACE_STOP_TIMEOUT): Promise<void> {
        Logger.debug(`Workspaces.clickToDeleteConfirmationCheckbox`);

        const deleteConfirmationCheckboxLocator: By = By.xpath(`//input[@data-testid='confirmation-checkbox']`);

        await this.driverHelper.waitAndClick(deleteConfirmationCheckboxLocator, timeout);
    }

    async waitAndClickEnabledConfirmationWindowDeleteButton(timeout: number = TimeoutConstants.TS_DASHBOARD_WORKSPACE_STOP_TIMEOUT): Promise<void> {
        Logger.debug(`Workspaces.waitEnabledConfirmationWindowDeleteButton`);

        const enabledConfirmationWindowDeleteButton: By = By.xpath(`//button[@data-testid='delete-workspace-button' and not(@disabled)]`);

        await this.driverHelper.waitAndClick(enabledConfirmationWindowDeleteButton, timeout);
    }

    async deleteWorkspaceByActionsButton(workspaceName: string, timeout: number = TimeoutConstants.TS_DASHBOARD_WORKSPACE_STOP_TIMEOUT): Promise<void> {
        Logger.debug('Workspaces.deleteWorkspaceByActionsButton');

        await this.waitWorkspaceListItem(workspaceName, timeout);
        await this.openActionsPopup(workspaceName, timeout);
        await this.clickActionsDeleteButton(workspaceName);
        await this.waitDeleteWorkspaceConfirmationWindow(timeout);
        await this.clickToDeleteConfirmationCheckbox(timeout);
        await this.waitAndClickEnabledConfirmationWindowDeleteButton(timeout);
    }

    async stopWorkspaceByActionsButton(workspaceName: string, timeout: number = TimeoutConstants.TS_DASHBOARD_WORKSPACE_STOP_TIMEOUT): Promise<void> {
        Logger.debug('Workspaces.stopWorkspaceByActionsButton');

        await this.waitWorkspaceListItem(workspaceName, timeout);
        await this.openActionsPopup(workspaceName, timeout);
        await this.clickActionsStopWorkspaceButton(workspaceName);
    }

    async waitWorkspaceListItemAbsence(workspaceName: string, timeout: number = TimeoutConstants.TS_DASHBOARD_WORKSPACE_STOP_TIMEOUT): Promise<void> {
        Logger.debug(`Workspaces.waitWorkspaceListItemAbsence "${workspaceName}"`);

        const polling: number = TimeoutConstants.TS_SELENIUM_DEFAULT_POLLING;
        const attempts: number = Math.ceil(timeout / polling);

        const workspaceListItemLocator: By = By.xpath(this.getWorkspaceListItemLocator(workspaceName));

        await this.driverHelper.waitDisappearance(workspaceListItemLocator, attempts, polling);
    }

    async getAllCreatedWorkspacesNames(timeout: number = TimeoutConstants.TS_COMMON_DASHBOARD_WAIT_TIMEOUT): Promise<string[]> {
        Logger.debug('Workspaces.getAllCreatedWorkspacesNames');

        const workspaceNames: string[] = [];
        try {
            const workspaceItems: WebElement[] = await this.driverHelper.waitAllPresence(By.xpath(Workspaces.WORKSPACE_ITEM_TABLE_NAME_SECTION_XPATH), timeout);
            for (let item of workspaceItems) {
                Logger.debug(`Workspaces.getAllCreatedWorkspacesNames - try to get ${workspaceItems.indexOf(item)} items name`);
                workspaceNames.push(await item.getText());
                Logger.debug(`Workspaces.getAllCreatedWorkspacesNames - workspace name is "${workspaceNames[workspaceNames.length - 1]}"`);
            }
        } catch (e) {
            Logger.debug(`Workspaces.getAllCreatedWorkspacesNames - ${e}`);
        }

        Logger.debug(`Workspaces.getAllCreatedWorkspacesNames - ${workspaceNames.length} workspaces have been created in DevSpaces`);
        return workspaceNames;
    }

    private getWorkspaceListItemLocator(workspaceName: string): string {
        return `//tr[td/span/a[text()='${workspaceName}']]`;
    }

    private getWorkspaceStatusLocator(workspaceName: string, workspaceStatus: WorkspaceStatusUI): By {
        return By.xpath(`${this.getWorkspaceListItemLocator(workspaceName)}//span[@data-testid='workspace-status-indicator']//*[local-name()='svg' and @fill='${workspaceStatus}']`);
    }

    private getActionsLocator(workspaceName: string): By {
        return By.xpath(`${this.getWorkspaceListItemLocator(workspaceName)}/td/div/button[@aria-label='Actions']`);
    }

    private getExpandedActionsLocator(workspaceName: string): By {
        return By.xpath(`${this.getWorkspaceListItemLocator(workspaceName)}//button[@aria-label='Actions' and @aria-expanded='true']`);
    }

    private getActionsPopupButtonLocator(workspaceName: string, buttonText: string): By {
        return By.xpath(`${this.getWorkspaceListItemLocator(workspaceName)}//button[text()='${buttonText}']`);
    }

    private getOpenButtonLocator(workspaceName: string): By {
        return By.xpath(`${this.getWorkspaceListItemLocator(workspaceName)}//td[@data-key=5]//a[text()='Open']`);
    }
}
