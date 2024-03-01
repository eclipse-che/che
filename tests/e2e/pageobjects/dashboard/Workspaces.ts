/** *******************************************************************
 * copyright (c) 2019-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { inject, injectable } from 'inversify';
import { DriverHelper } from '../../utils/DriverHelper';
import { CLASSES } from '../../configs/inversify.types';
import { By, WebElement } from 'selenium-webdriver';
import { Logger } from '../../utils/Logger';
import { TIMEOUT_CONSTANTS } from '../../constants/TIMEOUT_CONSTANTS';

export enum WorkspaceStatusUI {
	Running = 'Workspace status is Running',
	Stopped = 'Workspace status is Stopped'
}

@injectable()
export class Workspaces {
	private static readonly ADD_WORKSPACE_BUTTON: By = By.xpath('//button[text()="Add Workspace"]');
	private static readonly WORKSPACE_ITEM_TABLE_NAME_SECTION: By = By.xpath('//td[@data-label="Name"]/span/a');
	private static readonly DELETE_WORKSPACE_BUTTON_ENABLED: By = By.xpath(
		'//button[@data-testid="delete-workspace-button" and not(@disabled)]'
	);
	private static readonly DELETE_CONFIRMATION_CHECKBOX: By = By.xpath('//input[@data-testid="confirmation-checkbox"]');
	private static readonly CONFIRMATION_WINDOW: By = By.xpath('//div[@aria-label="Delete workspaces confirmation window"]');
	private static readonly LEARN_MORE_DOC_LINK: By = By.xpath('//div/p/a');

	constructor(
		@inject(CLASSES.DriverHelper)
		private readonly driverHelper: DriverHelper
	) {}

	async waitPage(timeout: number = TIMEOUT_CONSTANTS.TS_SELENIUM_LOAD_PAGE_TIMEOUT): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitVisibility(Workspaces.ADD_WORKSPACE_BUTTON, timeout);
	}

	async clickAddWorkspaceButton(timeout: number = TIMEOUT_CONSTANTS.TS_CLICK_DASHBOARD_ITEM_TIMEOUT): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitAndClick(Workspaces.ADD_WORKSPACE_BUTTON, timeout);
	}

	async clickOpenButton(workspaceName: string, timeout: number = TIMEOUT_CONSTANTS.TS_SELENIUM_LOAD_PAGE_TIMEOUT): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitAndClick(this.getOpenButtonLocator(workspaceName), timeout);
	}

	async waitWorkspaceListItem(
		workspaceName: string,
		timeout: number = TIMEOUT_CONSTANTS.TS_COMMON_DASHBOARD_WAIT_TIMEOUT
	): Promise<void> {
		Logger.debug(`"${workspaceName}"`);

		await this.driverHelper.waitVisibility(this.getWorkspaceListItemLocator(workspaceName), timeout);
	}

	async waitWorkspaceWithRunningStatus(
		workspaceName: string,
		timeout: number = TIMEOUT_CONSTANTS.TS_COMMON_DASHBOARD_WAIT_TIMEOUT
	): Promise<void> {
		Logger.debug(`"${workspaceName}"`);

		await this.driverHelper.waitVisibility(this.getWorkspaceStatusLocator(workspaceName, WorkspaceStatusUI.Running), timeout);
	}

	async waitWorkspaceWithStoppedStatus(
		workspaceName: string,
		timeout: number = TIMEOUT_CONSTANTS.TS_DASHBOARD_WORKSPACE_STOP_TIMEOUT
	): Promise<void> {
		Logger.debug(`"${workspaceName}"`);

		await this.driverHelper.waitVisibility(this.getWorkspaceStatusLocator(workspaceName, WorkspaceStatusUI.Stopped), timeout);
	}

	async clickWorkspaceListItemLink(
		workspaceName: string,
		timeout: number = TIMEOUT_CONSTANTS.TS_CLICK_DASHBOARD_ITEM_TIMEOUT
	): Promise<void> {
		Logger.debug(`"${workspaceName}"`);

		await this.driverHelper.waitAndClick(this.getOpenWorkspaceDetailsLinkLocator(workspaceName), timeout);
	}

	async clickActionsButton(workspaceName: string): Promise<void> {
		Logger.debug(`of the '${workspaceName}' list item`);

		await this.driverHelper.waitAndClick(this.getActionsLocator(workspaceName));
	}

	async waitActionsPopup(workspaceName: string, timeout: number = TIMEOUT_CONSTANTS.TS_COMMON_DASHBOARD_WAIT_TIMEOUT): Promise<void> {
		Logger.debug(`of the '${workspaceName}' list item`);

		await this.driverHelper.waitVisibility(this.getExpandedActionsLocator(workspaceName), timeout);
	}

	async openActionsPopup(workspaceName: string, timeout: number = TIMEOUT_CONSTANTS.TS_COMMON_DASHBOARD_WAIT_TIMEOUT): Promise<void> {
		Logger.debug(`for the '${workspaceName}' list item`);

		await this.clickActionsButton(workspaceName);
		await this.waitActionsPopup(workspaceName, timeout);
	}

	async clickActionsDeleteButton(workspaceName: string): Promise<void> {
		Logger.debug(`for the '${workspaceName}' list item`);

		await this.driverHelper.waitAndClick(this.getActionsPopupButtonLocator(workspaceName, 'Delete Workspace'));
	}

	async clickActionsStopWorkspaceButton(workspaceName: string): Promise<void> {
		Logger.debug(`for the '${workspaceName}' list item`);
		// todo: workaround because of issue CRW-3649
		try {
			await this.driverHelper.waitAndClick(this.getActionsPopupButtonLocator(workspaceName, 'Stop Workspace'));
		} catch (e) {
			Logger.warn(`for the '${workspaceName}' list item - popup was missed, try to click one more time (issue CRW-3649).`);

			await this.driverHelper.waitAndClick(this.getActionsLocator(workspaceName));
			await this.driverHelper.waitAndClick(this.getActionsPopupButtonLocator(workspaceName, 'Stop Workspace'));
		}
	}

	async waitDeleteWorkspaceConfirmationWindow(timeout: number = TIMEOUT_CONSTANTS.TS_DASHBOARD_WORKSPACE_STOP_TIMEOUT): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitVisibility(Workspaces.CONFIRMATION_WINDOW, timeout);
	}

	async clickToDeleteConfirmationCheckbox(timeout: number = TIMEOUT_CONSTANTS.TS_DASHBOARD_WORKSPACE_STOP_TIMEOUT): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitAndClick(Workspaces.DELETE_CONFIRMATION_CHECKBOX, timeout);
	}

	async waitAndClickEnabledConfirmationWindowDeleteButton(
		timeout: number = TIMEOUT_CONSTANTS.TS_DASHBOARD_WORKSPACE_STOP_TIMEOUT
	): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitAndClick(Workspaces.DELETE_WORKSPACE_BUTTON_ENABLED, timeout);
	}

	async deleteWorkspaceByActionsButton(
		workspaceName: string,
		timeout: number = TIMEOUT_CONSTANTS.TS_DASHBOARD_WORKSPACE_STOP_TIMEOUT
	): Promise<void> {
		Logger.debug();

		await this.waitWorkspaceListItem(workspaceName, timeout);
		await this.openActionsPopup(workspaceName, timeout);
		await this.clickActionsDeleteButton(workspaceName);
		await this.waitDeleteWorkspaceConfirmationWindow(timeout);
		await this.clickToDeleteConfirmationCheckbox(timeout);
		await this.waitAndClickEnabledConfirmationWindowDeleteButton(timeout);
	}

	async stopWorkspaceByActionsButton(
		workspaceName: string,
		timeout: number = TIMEOUT_CONSTANTS.TS_DASHBOARD_WORKSPACE_STOP_TIMEOUT
	): Promise<void> {
		Logger.debug();

		await this.waitWorkspaceListItem(workspaceName, timeout);
		await this.openActionsPopup(workspaceName, timeout);
		await this.clickActionsStopWorkspaceButton(workspaceName);
	}

	async waitWorkspaceListItemAbsence(
		workspaceName: string,
		timeout: number = TIMEOUT_CONSTANTS.TS_DASHBOARD_WORKSPACE_STOP_TIMEOUT
	): Promise<void> {
		Logger.debug(`"${workspaceName}"`);

		const polling: number = TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING;
		const attempts: number = Math.ceil(timeout / polling);

		await this.driverHelper.waitDisappearance(this.getWorkspaceListItemLocator(workspaceName), attempts, polling);
	}

	async getAllCreatedWorkspacesNames(timeout: number = TIMEOUT_CONSTANTS.TS_COMMON_DASHBOARD_WAIT_TIMEOUT): Promise<string[]> {
		Logger.debug();

		const workspaceNames: string[] = [];
		try {
			const workspaceItems: WebElement[] = await this.driverHelper.waitAllPresence(
				Workspaces.WORKSPACE_ITEM_TABLE_NAME_SECTION,
				timeout
			);
			for (const item of workspaceItems) {
				Logger.debug(`try to get ${workspaceItems.indexOf(item)} items name`);
				workspaceNames.push(await item.getText());
				Logger.debug(`workspace name is "${workspaceNames[workspaceNames.length - 1]}"`);
			}
		} catch (e) {
			Logger.debug(`${e}`);
		}

		Logger.debug(`${workspaceNames.length} workspaces have been created in DevSpaces`);
		return workspaceNames;
	}

	async getLearnMoreDocumentationLink(): Promise<string> {
		Logger.debug();

		return await this.driverHelper.waitAndGetElementAttribute(Workspaces.LEARN_MORE_DOC_LINK, 'href');
	}

	private getWorkspaceListItemLocator(workspaceName: string): By {
		return By.xpath(`//tr[td//a[text()='${workspaceName}']]`);
	}

	private getWorkspaceStatusLocator(workspaceName: string, workspaceStatus: WorkspaceStatusUI): By {
		return By.xpath(
			`${
				this.getWorkspaceListItemLocator(workspaceName).value
			}//span[@data-testid='workspace-status-indicator' and @aria-label='${workspaceStatus}']`
		);
	}

	private getActionsLocator(workspaceName: string): By {
		return By.xpath(`${this.getWorkspaceListItemLocator(workspaceName).value}/td/div/button[@aria-label='Actions']`);
	}

	private getExpandedActionsLocator(workspaceName: string): By {
		return By.xpath(
			`${this.getWorkspaceListItemLocator(workspaceName).value}//button[@aria-label='Actions' and @aria-expanded='true']`
		);
	}

	private getActionsPopupButtonLocator(workspaceName: string, buttonText: string): By {
		return By.xpath(`${this.getWorkspaceListItemLocator(workspaceName).value}//button[text()='${buttonText}']`);
	}

	private getOpenButtonLocator(workspaceName: string): By {
		return By.xpath(`${this.getWorkspaceListItemLocator(workspaceName).value}//td[@data-key=5]//a[text()='Open']`);
	}

	private getOpenWorkspaceDetailsLinkLocator(workspaceName: string): By {
		return By.xpath(`${this.getWorkspaceListItemLocator(workspaceName).value}//a[text()='${workspaceName}']`);
	}
}
