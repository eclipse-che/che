/*********************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { e2eContainer } from '../../../inversify.config';

import { ActivityBar, ViewControl, Workbench } from 'monaco-page-objects';
import { CLASSES, TYPES } from '../../../inversify.types';
import { WorkspaceHandlingTests } from '../../../testsLibrary/WorkspaceHandlingTests';
import { DriverHelper } from '../../../utils/DriverHelper';
import { Logger } from '../../../utils/Logger';

import { IDriver } from '../../../driver/IDriver';
import CheReporter from '../../../driver/CheReporter';

const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
const driver: IDriver = e2eContainer.get(TYPES.Driver);

const stackName: string = 'Empty Workspace';

suite(`${stackName} test`, async () => {
    suite(`Create ${stackName} workspace`, async () => {
        workspaceHandlingTests.createAndOpenWorkspace(stackName);
        workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
        test('Register running workspace', async () => {
            CheReporter.registerRunningWorkspace(WorkspaceHandlingTests.getWorkspaceName());
        });
        test('Wait workspace readiness', async() => {
            await workspaceHandlingTests.waitWorkspaceReadinessForCheCodeEditor();

            const workbench: Workbench = new Workbench();
            const activityBar: ActivityBar = workbench.getActivityBar();
            const activityBarControls: ViewControl[] = await activityBar.getViewControls();

            Logger.debug(`Editor sections:`);
            for (const control of activityBarControls) {
                Logger.debug(`${await control.getTitle()}`);
            }
        });
    });

    suite('Stopping and deleting the workspace', async () => {
        test(`Stop and remove workspace`, async () => {
            await workspaceHandlingTests.stopAndRemoveWorkspace(WorkspaceHandlingTests.getWorkspaceName());
        });
    });
});
