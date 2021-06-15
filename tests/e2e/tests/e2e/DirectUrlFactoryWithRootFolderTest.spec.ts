// /*********************************************************************
//  * Copyright (c) 2020 Red Hat, Inc.
//  *
//  * This program and the accompanying materials are made
//  * available under the terms of the Eclipse Public License 2.0
//  * which is available at https://www.eclipse.org/legal/epl-2.0/
//  *
//  * SPDX-License-Identifier: EPL-2.0
//  **********************************************************************/

import { e2eContainer } from '../../inversify.config';
import { CLASSES } from '../../inversify.types';
import { TestConstants } from '../../TestConstants';
import { ProjectAndFileTests } from '../../testsLibrary/ProjectAndFileTests';
import { WorkspaceHandlingTests } from '../../testsLibrary/WorkspaceHandlingTests';
import CheReporter from '../../driver/CheReporter';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import { Dashboard } from '../../pageobjects/dashboard/Dashboard';
import { WorkspaceNameHandler } from '../../utils/WorkspaceNameHandler';

const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
const workspaceNameHandler: WorkspaceNameHandler = e2eContainer.get(CLASSES.WorkspaceNameHandler);

const factoryUrl : string = `${TestConstants.TS_SELENIUM_BASE_URL}/f?url=https://github.com/che-samples/console-java-simple`;
const workspaceSampleName: string = 'console-java-simple';
const workspaceRootFolderName: string = 'src';
const fileFolderPath: string = `${workspaceSampleName}/${workspaceRootFolderName}/main/java/org/eclipse/che/examples`;
const tabTitle: string = 'HelloWorld.java';
let workspaceName: string;

// the suite expect user to be logged in
suite('Workspace creation via factory url', async () => {
    suite('Open factory URL', async () => {
        test(`Navigating to factory URL`, async () => {
            await browserTabsUtil.navigateTo(factoryUrl);
        });
    });

    suite('Wait workspace readyness', async () => {
        test('Register running workspace', async () => {
            await dashboard.waitWorkspaceStartingPage();
            workspaceName = await workspaceNameHandler.getNameFromUrl();
            CheReporter.registerRunningWorkspace(workspaceName);
        });

        projectAndFileTests.waitWorkspaceReadiness(workspaceSampleName, workspaceRootFolderName);
    });

    suite('Check imported project', async () => {
        projectAndFileTests.openFile(fileFolderPath, tabTitle);

        projectAndFileTests.checkProjectBranchName('master');
    });

    suite ('Stopping and deleting the workspace', async () => {
        test (`Stop workspace`, async () => {
            await workspaceHandlingTests.stopWorkspace(workspaceName);
        });
        test (`Remove workspace`, async () => {
            await workspaceHandlingTests.removeWorkspace(workspaceName);
        });
    });

});
