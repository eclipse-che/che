/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import 'reflect-metadata';
import { inject, injectable } from 'inversify';
import { CLASSES } from '../inversify.types';
import { Dashboard } from '../pageobjects/dashboard/Dashboard';
import { CreateWorkspace } from '../pageobjects/dashboard/CreateWorkspace';
import { Workspaces } from '../pageobjects/dashboard/Workspaces';
import { BrowserTabsUtil } from '../utils/BrowserTabsUtil';
import { Logger } from '../utils/Logger';
import { Editor } from '../pageobjects/ide/Editor';
import { TimeoutConstants } from '../TimeoutConstants';
import { ProjectAndFileTests } from './ProjectAndFileTests';
import CheReporter from '../driver/CheReporter';
import { DriverHelper } from '../utils/DriverHelper';
import { Ide } from '../pageobjects/ide/Ide';
import { error } from 'selenium-webdriver';

@injectable()
export class WorkspaceHandlingTests {

    private static workspaceName: string = 'undefined';
    private static parentGUID: string;

    public static getWorkspaceName(): string {
        return WorkspaceHandlingTests.workspaceName;
    }

    public static setWindowHandle(guid: string) {
        WorkspaceHandlingTests.parentGUID = guid;
    }

    public static getWindowHandle(): string {
        return WorkspaceHandlingTests.parentGUID;
    }

    constructor(
        @inject(CLASSES.Dashboard) private readonly dashboard: Dashboard,
        @inject(CLASSES.CreateWorkspace) private readonly createWorkspace: CreateWorkspace,
        @inject(CLASSES.Workspaces) private readonly workspaces: Workspaces,
        @inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper,
        @inject(CLASSES.BrowserTabsUtil) private readonly browserTabsUtil: BrowserTabsUtil,
        @inject(CLASSES.Editor) private readonly editor: Editor,
        @inject(CLASSES.Ide) private readonly ide: Ide,
        @inject(CLASSES.ProjectAndFileTests) private readonly projectAndFileTests: ProjectAndFileTests) {}

    public createAndOpenWorkspace(stack: string) {
        test(`Create and open new workspace, stack:${stack}`, async () => {
            await this.dashboard.waitPage();
            await this.dashboard.clickCreateWorkspaceButton();
            await this.createWorkspace.waitPage();
            WorkspaceHandlingTests.parentGUID = await this.browserTabsUtil.getCurrentWindowHandle();
            await this.createWorkspace.clickOnSample(stack);
            await this.browserTabsUtil.waitAndSwitchToAnotherWindow(WorkspaceHandlingTests.parentGUID, TimeoutConstants.TS_IDE_LOAD_TIMEOUT);
        });
    }

    public openExistingWorkspace(workspaceName: string) {
        test('Open and start existing workspace', async () => {
            await this.dashboard.waitPage();
            await this.dashboard.clickWorkspacesButton();
            await this.workspaces.waitPage();
            await this.workspaces.clickOpenButton(workspaceName);
        });
    }

    public obtainWorkspaceNameFromApplicationYaml(yamlPath: string, fileName: string, editorLine: number) {
        this.projectAndFileTests.openFile(yamlPath, fileName);
        test(`Obtain workspace name from ${yamlPath}/${fileName}`, async() => {
            let workspaceNameLine: string = await this.editor.getLineText(fileName, editorLine);
            Logger.info(`Obtained name from application.yaml: ${workspaceNameLine}`);
            WorkspaceHandlingTests.workspaceName = workspaceNameLine.split(`: `)[1];
            CheReporter.registerRunningWorkspace(WorkspaceHandlingTests.workspaceName);
        });
    }

    public switchBackToFirstOpenIdeTabFromLeftToRight() {
        test('WorkspaceHandlingTests.switchBackToIdeTab', async () => {
            let tabs = await this.driverHelper.getDriver().getAllWindowHandles();
            Logger.trace(`WorkspaceHandlingTests.switchBackToIdeTab Found ${tabs.length} window handles, iterating...`);
            for (let i = 0; i < tabs.length; i++) {
                await this.browserTabsUtil.switchToWindow(tabs[i]);
                try {
                    await this.ide.waitIde(TimeoutConstants.TS_IDE_LOAD_TIMEOUT);
                    Logger.info(`WorkspaceHandlingTests.switchBackToIdeTab located and switched to IDE tab`);
                    return;
                } catch (err) {
                    if (err instanceof error.TimeoutError) {
                        Logger.warn(`WorkspaceHandlingTests.switchBackToIdeTab Locator timed out, trying with another window handle.`);
                        continue;
                    }
                    Logger.error(`WorkspaceHandlingTests.switchBackToIdeTab Received unexpected exception while trying to locate IDE tab:${err}`);
                    throw err;
                }
            }
            Logger.error(`WorkspaceHandlingTests.switchBackToIdeTab Failed to locate IDE tab, out of window handles.`);
        });
    }

    public async stopWorkspace(workspaceName: string) {
        await this.dashboard.openDashboard();
        await this.dashboard.stopWorkspaceByUI(workspaceName);
    }

    public async removeWorkspace(workspaceName: string) {
        await this.dashboard.openDashboard();
        await this.dashboard.deleteStoppedWorkspaceByUI(workspaceName);
    }

    public async stopAndRemoveWorkspace(workspaceName: string) {
        await this.dashboard.openDashboard();
        await this.dashboard.stopAndRemoveWorkspaceByUI(workspaceName);
    }
}
