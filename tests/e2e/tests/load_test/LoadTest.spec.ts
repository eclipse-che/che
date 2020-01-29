/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { e2eContainer } from '../../inversify.config';
import { CLASSES, TYPES } from '../../inversify.types';
import { Ide } from '../../pageobjects/ide/Ide';
import { ProjectTree } from '../../pageobjects/ide/ProjectTree';
import { DriverHelper } from '../../utils/DriverHelper';
import { ICheLoginPage } from '../../pageobjects/login/ICheLoginPage';
import { TestWorkspaceUtil } from '../../utils/workspace/TestWorkspaceUtil';
import { TestConstants } from '../..';
const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
const ide: Ide = e2eContainer.get(CLASSES.Ide);
const projectTree: ProjectTree = e2eContainer.get(CLASSES.ProjectTree);
const factoryUrl: string = `${TestConstants.TS_SELENIUM_BASE_URL}/f?url=https://gist.githubusercontent.com/Katka92/fe747fa93b8275abb2fb9f3803de1f0f/raw`;
const cheLoginPage: ICheLoginPage = e2eContainer.get<ICheLoginPage>(TYPES.CheLogin);
const testWorkspaceUtils: TestWorkspaceUtil = e2eContainer.get<TestWorkspaceUtil>(TYPES.WorkspaceUtil);

suite('Load test suite', async () => {

    suiteTeardown (async function () { await testWorkspaceUtils.cleanUpAllWorkspaces(); });

    test('Login and navigate to factory url', async () => {
        await driverHelper.navigateToUrl(factoryUrl);
        await cheLoginPage.login();
    });

    test('Wait loading workspace and get time', async () => {
        console.log('Waiting for workspace to start.');
        await ide.waitAndSwitchToIdeFrame();
        console.log('Waiting for project to be imported and opened.');
        await projectTree.openProjectTreeContainer();
    });

});


