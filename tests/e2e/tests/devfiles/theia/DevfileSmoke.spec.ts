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
import { PreferencesHandlerTheia } from '../../../utils/theia/PreferencesHandlerTheia';
import { ProjectAndFileTestsTheia } from '../../../testsLibrary/theia/ProjectAndFileTestsTheia';
import { WorkspaceHandlingTestsTheia } from '../../../testsLibrary/theia/WorkspaceHandlingTestsTheia';
import { NavigationBar } from '../../../pageobjects/ide/theia/NavigationBar';
import { Dashboard } from '../../../pageobjects/dashboard/Dashboard';
import CheReporter from '../../../driver/CheReporter';

const workspaceHandlingTests: WorkspaceHandlingTestsTheia = e2eContainer.get(CLASSES.WorkspaceHandlingTestsTheia);
const preferencesHandler: PreferencesHandlerTheia = e2eContainer.get(CLASSES.PreferencesHandlerTheia);
const projectAndFileTests: ProjectAndFileTestsTheia = e2eContainer.get(CLASSES.ProjectAndFileTestsTheia);
const navigationBar: NavigationBar = e2eContainer.get(CLASSES.NavigationBar);
const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);

const workspaceSampleName: string = 'console-java-simple';
const workspaceRootFolderName: string = 'src';
const stack: string = 'Java Maven';

suite(`${stack} test`, async () => {
    suite (`Create ${stack} workspace`, async () => {
        workspaceHandlingTests.createAndOpenWorkspace(stack);
        workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
        test('Register running workspace', async () => {
            CheReporter.registerRunningWorkspace(WorkspaceHandlingTestsTheia.getWorkspaceName());
        });
        projectAndFileTests.waitWorkspaceReadiness(workspaceSampleName, workspaceRootFolderName, false);

        test('Set application.confirmExit user preferences to "never"', async () => {
            await preferencesHandler.setPreferenceUsingUI('application.confirmExit', 'never');
        });
    });

    suite ('Stopping and deleting the workspace', async () => {
        test(`Stop and remowe workspace`, async () => {
            await navigationBar.openNavigationBar();
            await dashboard.stopAndRemoveWorkspaceByUI(WorkspaceHandlingTestsTheia.getWorkspaceName());
        });
    });
});
