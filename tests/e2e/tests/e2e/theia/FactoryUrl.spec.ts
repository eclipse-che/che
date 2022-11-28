// /*********************************************************************
//  * Copyright (c) 2020 Red Hat, Inc.
//  *
//  * This program and the accompanying materials are made
//  * available under the terms of the Eclipse Public License 2.0
//  * which is available at https://www.eclipse.org/legal/epl-2.0/
//  *
//  * SPDX-License-Identifier: EPL-2.0
//  **********************************************************************/

import { e2eContainer } from '../../../inversify.config';
import { CLASSES, TYPES } from '../../../inversify.types';
import { TestConstants } from '../../../TestConstants';
import { ProjectAndFileTestsTheia } from '../../../testsLibrary/theia/ProjectAndFileTestsTheia';
import { BrowserTabsUtil } from '../../../utils/BrowserTabsUtil';
import { ITestWorkspaceUtil } from '../../../utils/workspace/ITestWorkspaceUtil';
import { WorkspaceHandlingTests } from '../../../testsLibrary/WorkspaceHandlingTests';

const projectAndFileTests: ProjectAndFileTestsTheia = e2eContainer.get(CLASSES.ProjectAndFileTestsTheia);
const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
const testWorkspaceUtils: ITestWorkspaceUtil = e2eContainer.get<ITestWorkspaceUtil>(TYPES.WorkspaceUtil);
const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);

const factoryUrl : string = `${TestConstants.TS_SELENIUM_BASE_URL}/f?url=https://raw.githubusercontent.com/eclipse/che-devfile-registry/master/devfiles/java-maven/devfile.yaml`;
const workspaceSampleName: string = 'console-java-simple';
const workspaceRootFolderName: string = 'src';

// the suite expect user to be logged in
suite('Workspace creation via factory url', async () => {
    suite('Open factory URL', async () => {
        test(`Navigating to factory URL`, async () => {
            await browserTabsUtil.navigateTo(factoryUrl);
        });
    });

    suite('Wait workspace readyness', async () => {
        workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
        projectAndFileTests.waitWorkspaceReadiness(workspaceSampleName, workspaceRootFolderName);
    });

    suite ('Stopping and deleting the workspace', async () => {
        test('Stop and remove workspace', async () => {
            await testWorkspaceUtils.cleanUpRunningWorkspace(WorkspaceHandlingTests.getWorkspaceName());
        });
    });

});
