// /*********************************************************************
//  * Copyright (c) 2021 Red Hat, Inc.
//  *
//  * This program and the accompanying materials are made
//  * available under the terms of the Eclipse Public License 2.0
//  * which is available at https://www.eclipse.org/legal/epl-2.0/
//  *
//  * SPDX-License-Identifier: EPL-2.0
//  **********************************************************************/

import { e2eContainer } from '../../inversify.config';
import { CLASSES } from '../../inversify.types';
import { ProjectAndFileTests } from '../../testsLibrary/ProjectAndFileTests';
import CheReporter from '../../driver/CheReporter';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import { WorkspaceNameHandler } from '../../utils/WorkspaceNameHandler';
import { TestConstants } from '../../TestConstants';

const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
const workspaceNameHandler: WorkspaceNameHandler = e2eContainer.get(CLASSES.WorkspaceNameHandler);

// this test checks only workspace created from "web-nodejs-sample" https://github.com/devfile/devworkspace-operator/blob/main/samples/flattened_theia-next.yaml.
suite('Workspace creation via factory url', async () => {

    let factoryUrl : string = `${TestConstants.TS_SELENIUM_DEVWORKSPACE_URL}`;
    const workspaceSampleName: string = 'spring-petclinic';
    const workspaceRootFolderName: string = 'src';

    suite('Open factory URL', async () => {
        test(`Navigating to factory URL`, async () => {
            await browserTabsUtil.navigateTo(factoryUrl);
        });
    });

    suite('Wait workspace readiness', async () => {
        test('Register running workspace', async () => {
            CheReporter.registerRunningWorkspace(await workspaceNameHandler.getNameFromUrl());
        });

        projectAndFileTests.waitWorkspaceReadiness(workspaceSampleName, workspaceRootFolderName);
    });

});
