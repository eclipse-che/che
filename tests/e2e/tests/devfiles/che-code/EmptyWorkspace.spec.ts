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
import { Logger } from '../../../utils/Logger';
import { DriverHelper } from '../../../utils/DriverHelper';
import { By, until } from 'selenium-webdriver';
import { Workbench } from 'monaco-page-objects';

const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);

const stackName: string = 'Empty Workspace';

suite(`${stackName} test`, async () => {
    suite(`Create ${stackName} workspace`, async () => {
        workspaceHandlingTests.createAndOpenWorkspace(stackName);
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
