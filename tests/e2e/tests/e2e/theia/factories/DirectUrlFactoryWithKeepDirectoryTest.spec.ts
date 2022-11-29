// /*********************************************************************
//  * Copyright (c) 2022 Red Hat, Inc.
//  *
//  * This program and the accompanying materials are made
//  * available under the terms of the Eclipse Public License 2.0
//  * which is available at https://www.eclipse.org/legal/epl-2.0/
//  *
//  * SPDX-License-Identifier: EPL-2.0
//  **********************************************************************/

import { e2eContainer } from '../../../../inversify.config';
import { CLASSES } from '../../../../inversify.types';
import { TestConstants } from '../../../../TestConstants';
import { ProjectAndFileTestsTheia } from '../../../../testsLibrary/theia/ProjectAndFileTestsTheia';
import { BrowserTabsUtil } from '../../../../utils/BrowserTabsUtil';
import { WorkspaceHandlingTests } from '../../../../testsLibrary/WorkspaceHandlingTests';
import { PreferencesHandlerTheia } from '../../../../utils/theia/PreferencesHandlerTheia';

const preferencesHandler: PreferencesHandlerTheia = e2eContainer.get(CLASSES.PreferencesHandlerTheia);
const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
const projectAndFileTests: ProjectAndFileTestsTheia = e2eContainer.get(CLASSES.ProjectAndFileTestsTheia);
const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);

const factoryUrl : string = `${TestConstants.TS_SELENIUM_BASE_URL}/f?url=https://github.com/che-samples/console-java-simple/tree/master/src`;
const workspaceSampleName: string = 'console-java-simple';
const workspaceRootFolderName: string = 'src';
const fileName: string = 'pom.xml';
const fileFolderPath: string = `${workspaceSampleName}/${workspaceRootFolderName}/main/java/org/eclipse/che/examples`;
const tabTitle: string = 'HelloWorld.java';

suite('Workspace creation via factory url', async () => {
    suite('Open factory URL', async () => {
        test(`Navigating to factory URL`, async () => {
            await browserTabsUtil.navigateTo(factoryUrl);
        });
    });

    suite('Wait workspace readyness', async () => {
        workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
        projectAndFileTests.waitWorkspaceReadiness(workspaceSampleName, workspaceRootFolderName);

        test('Set confirmExit preference to never', async () => {
            await preferencesHandler.setPreferenceUsingUI('application.confirmExit', 'never');
        });
    });

    suite('Check imported project', async () => {
        projectAndFileTests.openFile(fileFolderPath, tabTitle);
        projectAndFileTests.checkFileNotExists(`${workspaceSampleName}/${fileName}`);

        projectAndFileTests.checkProjectBranchName('master');
    });

    suite ('Stopping and deleting the workspace', async () => {
        test('Stop and remove workspace', async () => {
            await workspaceHandlingTests.stopAndRemoveWorkspace(WorkspaceHandlingTests.getWorkspaceName());
        });
    });

});
