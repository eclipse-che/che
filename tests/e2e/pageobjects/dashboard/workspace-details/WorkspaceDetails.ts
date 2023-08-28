/** *******************************************************************
 * copyright (c) 2019-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { DriverHelper } from '../../../utils/DriverHelper';
import { inject, injectable } from 'inversify';
import { CLASSES, TYPES } from '../../../configs/inversify.types';
import 'reflect-metadata';
import { By } from 'selenium-webdriver';
import { WorkspaceStatus } from '../../../utils/workspace/WorkspaceStatus';
import { Logger } from '../../../utils/Logger';
import { TIMEOUT_CONSTANTS } from '../../../constants/TIMEOUT_CONSTANTS';
import { ITestWorkspaceUtil } from '../../../utils/workspace/ITestWorkspaceUtil';
import { ProjectAndFileTests } from '../../../tests-library/ProjectAndFileTests';

@injectable()
export class WorkspaceDetails {
	private static readonly RUN_BUTTON: By = By.css('#run-workspace-button[che-button-title="Run"]');
	private static readonly OPEN_BUTTON: By = By.css('#open-in-ide-button[che-button-title="Open"]');
	private static readonly SAVE_BUTTON: By = By.css('button[name="save-button"]');
	private static readonly ENABLED_SAVE_BUTTON: By = By.css('button[name="save-button"][aria-disabled="false"]');
	private static readonly WORKSPACE_DETAILS_LOADER: By = By.css('workspace-details-overview md-progress-linear');

	constructor(
		@inject(CLASSES.DriverHelper)
		private readonly driverHelper: DriverHelper,
		@inject(TYPES.WorkspaceUtil)
		private readonly testWorkspaceUtil: ITestWorkspaceUtil,
		@inject(CLASSES.ProjectAndFileTests)
		private readonly testProjectAndFileCheCode: ProjectAndFileTests
	) {}

	async waitLoaderDisappearance(
		attempts: number = TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_ATTEMPTS,
		polling: number = TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING
	): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitDisappearance(WorkspaceDetails.WORKSPACE_DETAILS_LOADER, attempts, polling);
	}

	async saveChanges(): Promise<void> {
		Logger.debug();

		await this.waitSaveButton();
		await this.clickOnSaveButton();
		await this.waitSaveButtonDisappearance();
	}

	async waitPage(workspaceName: string, timeout: number = TIMEOUT_CONSTANTS.TS_SELENIUM_LOAD_PAGE_TIMEOUT): Promise<void> {
		Logger.debug(`workspace: "${workspaceName}"`);

		await this.waitWorkspaceTitle(workspaceName, timeout);
		await this.waitOpenButton(timeout);
		await this.waitRunButton(timeout);
		await this.waitTabsPresence(timeout);
		await this.waitLoaderDisappearance(timeout);
	}

	async waitWorkspaceTitle(workspaceName: string, timeout: number = TIMEOUT_CONSTANTS.TS_COMMON_DASHBOARD_WAIT_TIMEOUT): Promise<void> {
		Logger.debug(`title: "${workspaceName}"`);

		const workspaceTitleLocator: By = this.getWorkspaceTitleLocator(workspaceName);

		await this.driverHelper.waitVisibility(workspaceTitleLocator, timeout);
	}

	async waitRunButton(timeout: number = TIMEOUT_CONSTANTS.TS_COMMON_DASHBOARD_WAIT_TIMEOUT): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitVisibility(WorkspaceDetails.RUN_BUTTON, timeout);
	}

	async clickOnRunButton(timeout: number = TIMEOUT_CONSTANTS.TS_CLICK_DASHBOARD_ITEM_TIMEOUT): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitAndClick(WorkspaceDetails.RUN_BUTTON, timeout);
	}

	async waitOpenButton(timeout: number = TIMEOUT_CONSTANTS.TS_COMMON_DASHBOARD_WAIT_TIMEOUT): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitVisibility(WorkspaceDetails.OPEN_BUTTON, timeout);
	}

	async openWorkspace(
		namespace: string,
		workspaceName: string,
		timeout: number = TIMEOUT_CONSTANTS.TS_SELENIUM_LOAD_PAGE_TIMEOUT
	): Promise<void> {
		Logger.debug(`"${namespace}/${workspaceName}"`);

		await this.clickOnOpenButton(timeout);
		await this.testProjectAndFileCheCode.waitWorkspaceReadinessForCheCodeEditor();
		this.testWorkspaceUtil.waitWorkspaceStatus(namespace, workspaceName, WorkspaceStatus.STARTING);
	}

	async waitTabsPresence(timeout: number = TIMEOUT_CONSTANTS.TS_COMMON_DASHBOARD_WAIT_TIMEOUT): Promise<void> {
		Logger.debug('WorkspaceDetails.waitTabsPresence');

		const workspaceDetailsTabs: Array<string> = [
			'Overview',
			'Projects',
			'Containers',
			'Servers',
			'Env Variables',
			'Volumes',
			'Config',
			'SSH',
			'Plugins',
			'Editors'
		];

		for (const tabTitle of workspaceDetailsTabs) {
			const workspaceDetailsTabLocator: By = this.getTabLocator(tabTitle);

			await this.driverHelper.waitVisibility(workspaceDetailsTabLocator, timeout);
		}
	}

	async selectTab(tabTitle: string, timeout: number = TIMEOUT_CONSTANTS.TS_COMMON_DASHBOARD_WAIT_TIMEOUT): Promise<void> {
		Logger.debug(`WorkspaceDetails.selectTab ${tabTitle}`);

		await this.clickOnTab(tabTitle, timeout);
		await this.waitTabSelected(tabTitle, timeout);
	}

	private getWorkspaceTitleLocator(workspaceName: string): By {
		return By.css(`che-row-toolbar[che-title='${workspaceName}']`);
	}

	private getTabLocator(tabTitle: string): By {
		return By.xpath(`//md-tabs-canvas//md-tab-item//span[text()='${tabTitle}']`);
	}

	private getSelectedTabLocator(tabTitle: string): By {
		return By.xpath(`//md-tabs-canvas[@role='tablist']//md-tab-item[@aria-selected='true']//span[text()='${tabTitle}']`);
	}

	private async waitSaveButton(timeout: number = TIMEOUT_CONSTANTS.TS_COMMON_DASHBOARD_WAIT_TIMEOUT): Promise<void> {
		await this.driverHelper.waitVisibility(WorkspaceDetails.ENABLED_SAVE_BUTTON, timeout);
	}

	private async waitSaveButtonDisappearance(
		attempts: number = TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_ATTEMPTS,
		polling: number = TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING
	): Promise<void> {
		await this.driverHelper.waitDisappearance(WorkspaceDetails.SAVE_BUTTON, attempts, polling);
	}

	private async clickOnSaveButton(timeout: number = TIMEOUT_CONSTANTS.TS_CLICK_DASHBOARD_ITEM_TIMEOUT): Promise<void> {
		await this.driverHelper.waitAndClick(WorkspaceDetails.ENABLED_SAVE_BUTTON, timeout);
	}

	private async clickOnOpenButton(timeout: number = TIMEOUT_CONSTANTS.TS_CLICK_DASHBOARD_ITEM_TIMEOUT): Promise<void> {
		await this.driverHelper.waitAndClick(WorkspaceDetails.OPEN_BUTTON, timeout);
	}

	private async clickOnTab(tabTitle: string, timeout: number = TIMEOUT_CONSTANTS.TS_CLICK_DASHBOARD_ITEM_TIMEOUT): Promise<void> {
		const workspaceDetailsTabLocator: By = this.getTabLocator(tabTitle);
		await this.driverHelper.waitAndClick(workspaceDetailsTabLocator, timeout);
	}

	private async waitTabSelected(tabTitle: string, timeout: number = TIMEOUT_CONSTANTS.TS_COMMON_DASHBOARD_WAIT_TIMEOUT): Promise<void> {
		const selectedTabLocator: By = this.getSelectedTabLocator(tabTitle);
		await this.driverHelper.waitVisibility(selectedTabLocator, timeout);
	}
}
