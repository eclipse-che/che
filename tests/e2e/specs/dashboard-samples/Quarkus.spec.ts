/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { SideBarView, ViewSection } from 'monaco-page-objects';
import { registerRunningWorkspace } from '../MochaHooks';
import { LoginTests } from '../../tests-library/LoginTests';
import { e2eContainer } from '../../configs/inversify.config';
import { CLASSES } from '../../configs/inversify.types';
import { WorkspaceHandlingTests } from '../../tests-library/WorkspaceHandlingTests';
import { ProjectAndFileTests } from '../../tests-library/ProjectAndFileTests';
import { Logger } from '../../utils/Logger';
import { BaseTestConstants } from '../../constants/BaseTestConstants';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';

const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);

const stackName: string = 'Java 11 with Quarkus';
const projectName: string = 'quarkus-quickstarts';

suite(`The ${stackName} userstory`, async function (): Promise<void> {
    let projectSection: ViewSection;

    loginTests.loginIntoChe();

    workspaceHandlingTests.createAndOpenWorkspace(stackName);

    workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();

    test('Register running workspace', async () => {
        registerRunningWorkspace(WorkspaceHandlingTests.getWorkspaceName());
    });

    test('Wait workspace readiness', async function (): Promise<void> {
        await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
    });

    test('Check a project folder has been created', async function (): Promise<void> {
        projectSection = await new SideBarView().getContent().getSection(projectName);
        Logger.debug(`new SideBarView().getContent().getSection: get ${projectName}`);
    });

    test('Check the project files was imported', async function (): Promise<void> {
        await projectSection.findItem(BaseTestConstants.TS_SELENIUM_PROJECT_ROOT_FILE_NAME);
        Logger.debug(`projectSection.findItem: find ${BaseTestConstants.TS_SELENIUM_PROJECT_ROOT_FILE_NAME}`);
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
