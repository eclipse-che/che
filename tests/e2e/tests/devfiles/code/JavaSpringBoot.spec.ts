/*********************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import 'reflect-metadata';
import { CLASSES } from '../../../inversify.types';
import { e2eContainer } from '../../../inversify.config';
import { WorkspaceHandlingTests } from '../../../testsLibrary/WorkspaceHandlingTests';
import CheReporter from '../../../driver/CheReporter';
import { CreateWorkspace } from '../../../pageobjects/dashboard/CreateWorkspace';
import { Logger } from '../../../utils/Logger';
import { ApiUrlResolver } from '../../../utils/workspace/ApiUrlResolver';
import { BrowserTabsUtil } from '../../../utils/BrowserTabsUtil';
import { TimeoutConstants } from '../../../TimeoutConstants';
import { Dashboard } from '../../../pageobjects/dashboard/Dashboard';
import { DriverHelper } from '../../../utils/DriverHelper';
import { By, until } from 'selenium-webdriver';
import { Workbench } from 'monaco-page-objects';

const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
const createWorkspace: CreateWorkspace = e2eContainer.get(CLASSES.CreateWorkspace);
const apiUrlResolver: ApiUrlResolver = e2eContainer.get(CLASSES.ApiUrlResolver);
const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);

const stack: string = 'Java Spring Boot';

suite(`${stack} test`, async () => {
    suite(`Create ${stack} workspace`, async () => {
        // workspaceHandlingTests.createAndOpenWorkspace(stack);
        test('Start Maven workspace using factory URL and vscode editor', async() => {
            await dashboard.waitPage();
            Logger.debug(`Fetching user kubernetes namespace, storing auth token by getting workspaces API URL.`);
            await apiUrlResolver.getWorkspacesApiUrl();
            await dashboard.clickCreateWorkspaceButton();
            await createWorkspace.waitPage();
            workspaceHandlingTests.setWindowHandle(await browserTabsUtil.getCurrentWindowHandle());
            await createWorkspace.startWorkspaceUsingFactory(`https://github.com/che-samples/web-java-spring-boot/tree/master?che-editor=che-incubator/che-code/insiders&storageType=persistent`);
            await browserTabsUtil.waitAndSwitchToAnotherWindow(workspaceHandlingTests.getWindowHandle(), TimeoutConstants.TS_IDE_LOAD_TIMEOUT);
        });
        workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
        test('Register running workspace', async () => {
            CheReporter.registerRunningWorkspace(WorkspaceHandlingTests.getWorkspaceName());
        });
        test('Wait workspace readiness', async() => {
            try {
                await driverHelper.getDriver().wait(until.elementLocated(By.className('monaco-workbench')));
            } catch (err) {
                if ((err as Error).name === 'WebDriverError') {
                    await new Promise(res => setTimeout(res, 3000));
                } else {
                    throw err;
                }
            }
            let workbench = new Workbench();
            let activityBar = workbench.getActivityBar();
            let activityBarControls = await activityBar.getViewControls();
            Logger.debug(`Editor sections:`);
            activityBarControls.forEach(async control => {
                Logger.debug(`${await control.getTitle()}`);
            });
        });
        // projectAndFileTests.waitWorkspaceReadiness(workspaceSampleName, workspaceRootFolderName, false);
    });

    suite('Stopping and deleting the workspace', async () => {
        test(`Stop and remowe workspace`, async () => {
            await workspaceHandlingTests.stopAndRemoveWorkspace(WorkspaceHandlingTests.getWorkspaceName());
        });
    });
});
