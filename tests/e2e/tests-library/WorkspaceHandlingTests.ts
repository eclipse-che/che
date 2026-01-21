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
	private static WORKSPACE_STATUS: By = By.xpath('//*/span[@class="pf-c-label__content"]');
	private static WORKSPACE_ALERT_TITLE: By = By.xpath('//h4[@class="pf-c-alert__title"]');
	private static WORKSPACE_ALERT_DESCRIPTION: By = By.xpath('//*/div[@class="pf-c-alert__description"]');
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

	static clearWorkspaceName(): void {
		WorkspaceHandlingTests.workspaceName = 'undefined';
	}

	async createAndOpenWorkspace(stack: string, createNewWorkspace: boolean = true): Promise<void> {
		await this.dashboard.clickWorkspacesButton();
		await this.dashboard.waitPage();
		Logger.debug('fetching user kubernetes namespace, storing auth token by getting workspaces API URL.');
		await this.apiUrlResolver.getWorkspacesApiUrl();
		await this.dashboard.clickCreateWorkspaceButton();
		await this.createWorkspace.waitPage();
		await this.createWorkspace.setCreateNewWorkspaceCheckbox(createNewWorkspace);
		WorkspaceHandlingTests.parentGUID = await this.browserTabsUtil.getCurrentWindowHandle();
		await this.createWorkspace.clickOnSampleNoEditorSelection(stack);
		await this.browserTabsUtil.waitAndSwitchToAnotherWindow(WorkspaceHandlingTests.parentGUID, TIMEOUT_CONSTANTS.TS_IDE_LOAD_TIMEOUT);
	}

	async createAndOpenWorkspaceFromGitRepository(
		factoryUrl: string,
		branchName?: string,
		createNewWorkspace: boolean = true
	): Promise<void> {
		await this.dashboard.waitPage();
		Logger.debug('fetching user kubernetes namespace, storing auth token by getting workspaces API URL.');
		await this.apiUrlResolver.getWorkspacesApiUrl();
		await this.dashboard.clickCreateWorkspaceButton();
		await this.createWorkspace.waitPage();
		await this.createWorkspace.setCreateNewWorkspaceCheckbox(createNewWorkspace);
		WorkspaceHandlingTests.parentGUID = await this.browserTabsUtil.getCurrentWindowHandle();
		await this.createWorkspace.importFromGitUsingUI(factoryUrl, branchName);
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
		Logger.error('failed to obtain workspace name');
		await this.logStartWorkspaceInfo();
		throw new error.InvalidArgumentError('WorkspaceHandlingTests.obtainWorkspaceNameFromStartingPage failed to obtain workspace name.');
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

	async logStartWorkspaceInfo(): Promise<void> {
		const status: string = await this.getWorkspaceStatus();
		const alertTitle: string = await this.getWorkspaceAlertTitle();
		const alertDescription: string = await this.getWorkspaceAlertDescription();

		Logger.info('Start workspace status: ' + status);
		Logger.info('Start workspace progress title: ' + alertTitle);
		Logger.info('Start workspace progress description: ' + alertDescription);
	}

	async createAndOpenWorkspaceWithSpecificEditorAndSample(editor: string, sampleName: string, xPath: string): Promise<void> {
		Logger.debug('Create and open workspace with specific Editor and Sample. Sample ' + editor);
		await this.selectEditor(editor);
		await this.createWorkspace.clickOnSampleNoEditorSelection(sampleName);
		await this.waitForControlXpath(xPath);
	}

	async createAndOpenWorkspaceWithSpecificEditorAndGitUrl(editor: string, sampleUrl: string, xPath: string): Promise<void> {
		Logger.debug('Create and open workspace with specific Editor and URL. Sample ' + editor);
		await this.selectEditor(editor);
		await this.createWorkspace.importFromGitUsingUI(sampleUrl);
		await this.waitForControlXpath(xPath);
	}

	async selectEditor(editor: string): Promise<void> {
		Logger.debug('select Editor. Editor: ' + editor);
		await this.dashboard.openChooseEditorMenu();
		await this.dashboard.chooseEditor(editor);
	}

	async getTextFromUIElementByXpath(xpath: string): Promise<string> {
		Logger.debug('returning text from xPath: ' + xpath);
		return await this.driverHelper.getDriver().findElement(By.xpath(xpath)).getText();
	}

	private async waitForControlXpath(xPathToWait: string): Promise<void> {
		await this.browserTabsUtil.waitAndSwitchToAnotherWindow(WorkspaceHandlingTests.parentGUID, TIMEOUT_CONSTANTS.TS_IDE_LOAD_TIMEOUT);
		await this.obtainWorkspaceNameFromStartingPage();

		await this.driverHelper.waitVisibility(By.xpath(xPathToWait), TIMEOUT_CONSTANTS.TS_SELENIUM_START_WORKSPACE_TIMEOUT);
		await this.driverHelper.waitVisibility(By.xpath(xPathToWait+"asdasdasd"), TIMEOUT_CONSTANTS.TS_COMMON_DASHBOARD_WAIT_TIMEOUT);
	}

	private async getWorkspaceAlertDescription(): Promise<string> {
		try {
			return await this.driverHelper.getDriver().findElement(WorkspaceHandlingTests.WORKSPACE_ALERT_DESCRIPTION).getText();
		} catch (err) {
			return '(unknown)';
		}
	}

	private async getWorkspaceStatus(): Promise<string> {
		try {
			return await this.driverHelper.getDriver().findElement(WorkspaceHandlingTests.WORKSPACE_STATUS).getText();
		} catch (err) {
			return '(unknown)';
		}
	}

	private async getWorkspaceAlertTitle(): Promise<string> {
		try {
			return await this.driverHelper.getDriver().findElement(WorkspaceHandlingTests.WORKSPACE_ALERT_TITLE).getAttribute('innerHTML');
		} catch (err) {
			return '(unknown)';
		}
	}
}
