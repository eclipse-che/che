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
import { ICheLoginPage } from '../../pageobjects/login/ICheLoginPage';
import { TestWorkspaceUtil } from '../../utils/workspace/TestWorkspaceUtil';
import { TestConstants, WorkspaceNameHandler } from '../..';
import CheReporter from '../../driver/CheReporter';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';

const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
const ide: Ide = e2eContainer.get(CLASSES.Ide);
const projectTree: ProjectTree = e2eContainer.get(CLASSES.ProjectTree);
const cheLoginPage: ICheLoginPage = e2eContainer.get<ICheLoginPage>(TYPES.CheLogin);
const testWorkspaceUtils: TestWorkspaceUtil = e2eContainer.get<TestWorkspaceUtil>(CLASSES.WorkspaceUtil);
const workspaceNameHandler: WorkspaceNameHandler = e2eContainer.get(CLASSES.WorkspaceNameHandler);

const workspaceName: string = workspaceNameHandler.generateWorkspaceName('wksp-test-', 5);
const workspacePrefixUrl: string = `${TestConstants.TS_SELENIUM_BASE_URL}/dashboard/#/ide/${TestConstants.TS_SELENIUM_USERNAME}/`;

suite('Load test suite', async () => {

    suiteTeardown (async function () { await testWorkspaceUtils.cleanUpAllWorkspaces(); });

    suiteSetup(async function () {
        const wsConfig = await testWorkspaceUtils.getBaseDevfile();
        wsConfig.metadata!.name = workspaceName;
        await testWorkspaceUtils.createWsFromDevFile(wsConfig);
    });

    test('Login into workspace and open tree container', async () => {
        await browserTabsUtil.navigateTo(workspacePrefixUrl + workspaceName);
        await cheLoginPage.login();
    });

    test('Wait loading workspace and get time', async () => {
        CheReporter.registerRunningWorkspace(workspaceName);
        await ide.waitWorkspaceAndIde();
        await projectTree.openProjectTreeContainer();
    });

});


