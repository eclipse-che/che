/*********************************************************************
 * Copyright (c) 2020-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { e2eContainer } from '../../configs/inversify.config';
import { ActivityBar, ViewControl, Workbench } from 'monaco-page-objects';
import { CLASSES } from '../../configs/inversify.types';
import { WorkspaceHandlingTests } from '../../tests-library/WorkspaceHandlingTests';
import { Logger } from '../../utils/Logger';
import { ProjectAndFileTests } from '../../tests-library/ProjectAndFileTests';
import { LoginTests } from '../../tests-library/LoginTests';
import { registerRunningWorkspace } from '../MochaHooks';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';

const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);

const stackName: string = 'Empty Workspace';

suite(`${stackName} test`, async () => {
    loginTests.loginIntoChe();

    workspaceHandlingTests.createAndOpenWorkspace(stackName);

    workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();

    test('Register running workspace', async () => {
        registerRunningWorkspace(WorkspaceHandlingTests.getWorkspaceName());
    });

    test('Wait workspace readiness', async () => {
        await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();

        const workbench: Workbench = new Workbench();
        const activityBar: ActivityBar = workbench.getActivityBar();
        const activityBarControls: ViewControl[] = await activityBar.getViewControls();

        Logger.debug(`Editor sections:`);
        for (const control of activityBarControls) {
            Logger.debug(`${await control.getTitle()}`);
        }
    });

    test('Stop the workspace', async function (): Promise<void> {
        await workspaceHandlingTests.stopWorkspace(WorkspaceHandlingTests.getWorkspaceName());
        await browserTabsUtil.closeAllTabsExceptCurrent();
    });

    test('Delete the workspace', async function (): Promise<void> {
        await workspaceHandlingTests.removeWorkspace(WorkspaceHandlingTests.getWorkspaceName());
    });

    loginTests.logoutFromChe();
});
