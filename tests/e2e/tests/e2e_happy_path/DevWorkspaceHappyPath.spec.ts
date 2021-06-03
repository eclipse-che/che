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
import { DriverHelper } from '../../utils/DriverHelper';
import { TestConstants, WorkspaceNameHandler } from '../..';
import { ProjectAndFileTests } from '../../testsLibrary/ProjectAndFileTests';
import CheReporter from '../../driver/CheReporter';

const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);

// this test checks only workspace created from "web-nodejs-sample" https://github.com/devfile/devworkspace-operator/blob/main/samples/flattened_theia-next.yaml.
suite('Workspace creation via factory url', async () => {

    let factoryUrl : string = `${TestConstants.TS_SELENIUM_DEVWORKSPACE_URL}`;
    const workspaceSampleName: string = 'spring-petclinic';
    const workspaceRootFolderName: string = 'src';

    suite('Open factory URL', async () => {
        test(`Navigating to factory URL`, async () => {
            await driverHelper.navigateToUrl(factoryUrl);
        });
    });

    suite('Wait workspace readiness', async () => {
        test('Register running workspace', async () => {
            CheReporter.registerRunningWorkspace(await WorkspaceNameHandler.getNameFromUrl());
        });

        projectAndFileTests.waitWorkspaceReadiness(workspaceSampleName, workspaceRootFolderName);
    });

});
