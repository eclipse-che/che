/*********************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { SideBarView, ViewSection } from 'monaco-page-objects';
import { ProjectAndFileTests } from '../../tests-library/ProjectAndFileTests';
import { CLASSES } from '../../configs/inversify.types';
import { e2eContainer } from '../../configs/inversify.config';
import { WorkspaceHandlingTests } from '../../tests-library/WorkspaceHandlingTests';
import { registerRunningWorkspace } from '../MochaHooks';
import { Logger } from '../../utils/Logger';
import { LoginTests } from '../../tests-library/LoginTests';

const stackName: string = 'Quarkus REST API';
const projectName: string = 'quarkus-quickstarts';
const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);

suite(`The ${stackName} userstory`, async function () {
    let projectSection: ViewSection;
    suite(`Create workspace from ${stackName} sample`, async function () {
        loginTests.loginIntoChe();
        workspaceHandlingTests.createAndOpenWorkspace(stackName);
        workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
        test('Register running workspace', async () => {
            registerRunningWorkspace(WorkspaceHandlingTests.getWorkspaceName());
        });
        test('Wait workspace readiness', async function () {
            await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
        });
        test('Check a project folder has been created', async function () {
            projectSection = await new SideBarView().getContent().getSection(projectName);
            Logger.debug(`new SideBarView().getContent().getSection: get ${projectName}`);
        });
        test('Check the project files was imported', async function () {
            const label: string = 'devfile.yaml';
            await projectSection.findItem(label);
            Logger.debug(`projectSection.findItem: find ${label}`);
        });
        test('Stopping and deleting the workspace', async function () {
            await workspaceHandlingTests.stopAndRemoveWorkspace(WorkspaceHandlingTests.getWorkspaceName());
        });
    });
});
