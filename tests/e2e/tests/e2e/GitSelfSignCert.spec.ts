/*********************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { e2eContainer } from '../../inversify.config';
import { CLASSES, TYPES } from '../../inversify.types';
import { GitPlugin } from '../../pageobjects/ide/plugins/GitPlugin';
import { Ide } from '../../pageobjects/ide/Ide';
import { ProjectTree } from '../../pageobjects/ide/ProjectTree';
import { QuickOpenContainer } from '../../pageobjects/ide/QuickOpenContainer';
import { ICheLoginPage } from '../../pageobjects/login/ICheLoginPage';
import { TestConstants } from '../../TestConstants';
import { DriverHelper } from '../../utils/DriverHelper';
import { TopMenu } from '../../pageobjects/ide/TopMenu';
import { TimeoutConstants } from '../../TimeoutConstants';
import { Dashboard } from '../../pageobjects/dashboard/Dashboard';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import { WorkspaceHandlingTests } from '../../testsLibrary/WorkspaceHandlingTests';
import { Editor } from '../../pageobjects/ide/Editor';
import { ITestWorkspaceUtil } from '../../utils/workspace/ITestWorkspaceUtil';

const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
const ide: Ide = e2eContainer.get(CLASSES.Ide);
const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
const quickOpenContainer: QuickOpenContainer = e2eContainer.get(CLASSES.QuickOpenContainer);
const topMenu: TopMenu = e2eContainer.get(CLASSES.TopMenu);
const loginPage: ICheLoginPage = e2eContainer.get<ICheLoginPage>(TYPES.CheLogin);
const projectTree: ProjectTree = e2eContainer.get(CLASSES.ProjectTree);
const gitPlugin: GitPlugin = e2eContainer.get(CLASSES.GitPlugin);
const testWorkspaceUtils: ITestWorkspaceUtil = e2eContainer.get<ITestWorkspaceUtil>(TYPES.WorkspaceUtil);
const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
const editor: Editor = e2eContainer.get(CLASSES.Editor);

const workspaceName = 'gitSelfSignCert';

suite('Checking git + self sign cert', async () => {
    const workspacePrefixUrl: string = `${TestConstants.TS_SELENIUM_BASE_URL}/dashboard/#/ide/${TestConstants.TS_SELENIUM_USERNAME}/`;
    const gitProjectUrl = `https://${TestConstants.TS_SELF_HOSTED_GIT_SERVER_URL}/maxura/gitService.git`;
    const committedFile = 'README.md';

    suiteSetup(async function () {
        const wsConfig = await testWorkspaceUtils.getBaseDevfile();
        wsConfig.metadata!.name = workspaceName;
        await browserTabsUtil.navigateTo(TestConstants.TS_SELENIUM_BASE_URL);
        await loginPage.login();
        await testWorkspaceUtils.createWsFromDevFile(wsConfig);
    });

    test('Wait until created workspace is started', async () => {
        await dashboard.waitPage();
        await browserTabsUtil.navigateTo(workspacePrefixUrl + workspaceName);
        await ide.waitWorkspaceAndIde();
        await projectTree.openProjectTreeContainer();
        await driverHelper.wait(TimeoutConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT);
    });


    test('Clone project', async () => {
        await cloneTestRepo(gitProjectUrl);

        await projectTree.waitProjectImportedNoSubfolder('gitService');
        await projectTree.waitItem('gitService/README.md');
    });

    test('Change commit and push', async function changeCommitAndPushFunc() {
        const currentDate: string = Date.now().toString();
        await projectTree.expandPathAndOpenFile('gitService', committedFile);
        await editor.type(committedFile, currentDate + '\n', 1);
        await gitPlugin.openGitPluginContainer();
        await gitPlugin.waitChangedFileInChagesList(committedFile);
        await gitPlugin.stageAllChanges(committedFile);
        await gitPlugin.waitChangedFileInChagesList(committedFile);
        await gitPlugin.typeCommitMessage(this.test!.title + currentDate);
        await gitPlugin.commitFromCommandMenu();
        await gitPlugin.pushChangesFromCommandMenu();
        await quickOpenContainer.typeAndSelectSuggestion('git-admin', 'Git: ' + `https://${TestConstants.TS_SELF_HOSTED_GIT_SERVER_URL}` + ' (Press \'Enter\' to confirm your input or \'Escape\' to cancel)');
        await quickOpenContainer.typeAndSelectSuggestion('admin', 'Git: https://git-admin@' + `${TestConstants.TS_SELF_HOSTED_GIT_SERVER_URL}` + ' (Press \'Enter\' to confirm your input or \'Escape\' to cancel)');
        await gitPlugin.waitDataIsSynchronized();
    });

});

suite('Stopping and deleting the workspace', async () => {
    test(`Stop and remove workspace`, async () => {
        await workspaceHandlingTests.stopAndRemoveWorkspace(workspaceName);
    });
});

async function cloneTestRepo(linkToRepo: string) {
    const confirmMessage = 'Clone from URL';

    await topMenu.selectOption('View', 'Find Command...');
    // workaround - reopen 'Find Command' container - https://github.com/eclipse/che/issues/19793
    await topMenu.selectOption('View', 'Find Command...');
    await quickOpenContainer.typeAndSelectSuggestion('clone', 'Git: Clone');
    await quickOpenContainer.typeAndSelectSuggestion(linkToRepo, confirmMessage);
    await gitPlugin.clickOnSelectRepositoryButton();

    await ide.waitAndApplyTrustNotification();
}
