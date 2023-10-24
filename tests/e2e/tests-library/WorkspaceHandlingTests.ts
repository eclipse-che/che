/** *******************************************************************
 * copyright (c) 2019-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import 'reflect-metadata';
import { inject, injectable } from 'inversify';
import { CLASSES } from '../configs/inversify.types';
import { Dashboard } from '../pageobjects/dashboard/Dashboard';
import { CreateWorkspace } from '../pageobjects/dashboard/CreateWorkspace';
import { BrowserTabsUtil } from '../utils/BrowserTabsUtil';
import { Logger } from '../utils/Logger';
import { ApiUrlResolver } from '../utils/workspace/ApiUrlResolver';
import { TIMEOUT_CONSTANTS } from '../constants/TIMEOUT_CONSTANTS';
import { DriverHelper } from '../utils/DriverHelper';
import { By, error } from 'selenium-webdriver';

@injectable()
export class WorkspaceHandlingTests {
	private static WORKSPACE_NAME: By = By.xpath('//h1[contains(.,"Starting workspace ")]');
	private static workspaceName: string = 'undefined';
	private static parentGUID: string;

	constructor(
		@inject(CLASSES.Dashboard) private readonly dashboard: Dashboard,
		@inject(CLASSES.CreateWorkspace)
		private readonly createWorkspace: CreateWorkspace,
		@inject(CLASSES.BrowserTabsUtil)
		private readonly browserTabsUtil: BrowserTabsUtil,
		@inject(CLASSES.ApiUrlResolver)
		private readonly apiUrlResolver: ApiUrlResolver,
		@inject(CLASSES.DriverHelper)
		private readonly driverHelper: DriverHelper
	) {}

	static getWorkspaceName(): string {
		return WorkspaceHandlingTests.workspaceName;
	}

	async createAndOpenWorkspace(stack: string): Promise<void> {
		await this.dashboard.clickWorkspacesButton();
		await this.dashboard.waitPage();
		Logger.debug('fetching user kubernetes namespace, storing auth token by getting workspaces API URL.');
		await this.apiUrlResolver.getWorkspacesApiUrl();
		await this.dashboard.clickCreateWorkspaceButton();
		await this.createWorkspace.waitPage();
		WorkspaceHandlingTests.parentGUID = await this.browserTabsUtil.getCurrentWindowHandle();
		await this.createWorkspace.clickOnSampleNoEditorSelection(stack);
		await this.browserTabsUtil.waitAndSwitchToAnotherWindow(WorkspaceHandlingTests.parentGUID, TIMEOUT_CONSTANTS.TS_IDE_LOAD_TIMEOUT);
	}

	async createAndOpenWorkspaceFromGitRepository(factoryUrl: string): Promise<void> {
		await this.dashboard.waitPage();
		Logger.debug('fetching user kubernetes namespace, storing auth token by getting workspaces API URL.');
		await this.apiUrlResolver.getWorkspacesApiUrl();
		await this.dashboard.clickCreateWorkspaceButton();
		await this.createWorkspace.waitPage();
		WorkspaceHandlingTests.parentGUID = await this.browserTabsUtil.getCurrentWindowHandle();
		await this.createWorkspace.importFromGitUsingUI(factoryUrl);
		await this.browserTabsUtil.waitAndSwitchToAnotherWindow(WorkspaceHandlingTests.parentGUID, TIMEOUT_CONSTANTS.TS_IDE_LOAD_TIMEOUT);
	}

	async createAndOpenWorkspaceWithExistedWorkspaceName(stack: string): Promise<void> {
		Logger.debug('create and open workspace with existed workspace name.');
		await this.createAndOpenWorkspace(stack);
		await this.dashboard.waitExistingWorkspaceFoundAlert();
		await this.dashboard.clickOnCreateNewWorkspaceButton();
	}

	async obtainWorkspaceNameFromStartingPage(): Promise<void> {
		const timeout: number = TIMEOUT_CONSTANTS.TS_SELENIUM_START_WORKSPACE_TIMEOUT;
		const polling: number = TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING;
		const attempts: number = Math.ceil(timeout / polling);

		for (let i: number = 0; i < attempts; i++) {
			try {
				const startingWorkspaceLineContent: string = await this.driverHelper
					.getDriver()
					.findElement(WorkspaceHandlingTests.WORKSPACE_NAME)
					.getText();
				Logger.trace(`obtained starting workspace getText():${startingWorkspaceLineContent}`);
				// cutting away leading text
				WorkspaceHandlingTests.workspaceName = startingWorkspaceLineContent.substring('Starting workspace '.length).trim();
				Logger.trace(`trimmed workspace name from getText():${WorkspaceHandlingTests.workspaceName}`);
				break;
			} catch (err) {
				if (err instanceof error.StaleElementReferenceError) {
					Logger.trace('failed to obtain name from workspace start page, element possibly detached from DOM. Retrying.');
					await this.driverHelper.wait(polling);
					continue;
				}
				if (err instanceof error.NoSuchElementError) {
					Logger.trace('failed to obtain name from workspace start page, element not visible yet. Retrying.');
					await this.driverHelper.wait(polling);
					continue;
				}
				Logger.error(`obtaining workspace name failed with an unexpected error:${err}`);
				throw err;
			}
		}
		if (WorkspaceHandlingTests.workspaceName !== '' && WorkspaceHandlingTests.workspaceName !== 'undefined') {
			Logger.info(`obtained workspace name from workspace loader page: ${WorkspaceHandlingTests.workspaceName}`);
			return;
		}
		Logger.error(`failed to obtain workspace name:${WorkspaceHandlingTests.workspaceName}`);
		throw new error.InvalidArgumentError(
			`WorkspaceHandlingTests.obtainWorkspaceNameFromStartingPage failed to obtain workspace name:${WorkspaceHandlingTests.workspaceName}`
		);
	}

	async stopWorkspace(workspaceName: string): Promise<void> {
		await this.dashboard.openDashboard();
		await this.dashboard.stopWorkspaceByUI(workspaceName);
	}

	async removeWorkspace(workspaceName: string): Promise<void> {
		await this.dashboard.openDashboard();
		await this.dashboard.deleteStoppedWorkspaceByUI(workspaceName);
	}

	async stopAndRemoveWorkspace(workspaceName: string): Promise<void> {
		await this.dashboard.openDashboard();
		await this.dashboard.stopAndRemoveWorkspaceByUI(workspaceName);
	}
}
