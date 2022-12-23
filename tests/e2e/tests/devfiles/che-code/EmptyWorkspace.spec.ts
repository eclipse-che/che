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
import { CLASSES } from '../../../inversify.types';
import { WorkspaceHandlingTests } from '../../../testsLibrary/WorkspaceHandlingTests';
import { Logger } from '../../../utils/Logger';
import CheReporter from '../../../driver/CheReporter';
import { ProjectAndFileTestsCheCode } from '../../../testsLibrary/che-code/ProjectAndFileTestsCheCode';

const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
const projectAndFileTests: ProjectAndFileTestsCheCode = e2eContainer.get(CLASSES.ProjectAndFileTestsCheCode);
const stackName: string = 'Empty Workspace';

suite(`${stackName} test`, async () => {
    suite(`Create ${stackName} workspace`, async () => {
        workspaceHandlingTests.createAndOpenWorkspace(stackName);
        workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
        test('Register running workspace', async () => {
            CheReporter.registerRunningWorkspace(WorkspaceHandlingTests.getWorkspaceName());
        });
        test('Wait workspace readiness', async() => {
            await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();

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
