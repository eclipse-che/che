/*********************************************************************
 * Copyright (c) 2022 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { e2eContainer } from '../../inversify.config';
import { CLASSES } from '../../inversify.types';
import { TestConstants } from '../../TestConstants';
import { WorkspaceHandlingTests } from '../../testsLibrary/WorkspaceHandlingTests';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import { ProjectAndFileTests } from '../../testsLibrary/ProjectAndFileTests';

const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);

const devfileUrl: string = 'https://github.com/che-samples/web-nodejs-sample/tree/devfilev2';
const factoryUrl: string = `${TestConstants.TS_SELENIUM_BASE_URL}/f?url=${devfileUrl}`;
const projectName: string = 'web-nodejs-sample';
const subRootFolder: string = 'app';
let workspaceName: string = 'nodejs-web-app';

suite('Load test suite', async () => {
    suite('Create workspace', async () => {
        test('Create workspace using factory', async () => {
            await browserTabsUtil.navigateTo(factoryUrl);
        });

        workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();

        projectAndFileTests.waitWorkspaceReadiness(projectName, subRootFolder);
    });

    suite ('Stopping and deleting the workspace', async () => {
        test('Stop and remove workspace', async () => {
            await workspaceHandlingTests.stopAndRemoveWorkspace(workspaceName);
        });
    });

});
