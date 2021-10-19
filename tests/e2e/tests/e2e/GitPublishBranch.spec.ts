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
import { Editor } from '../../pageobjects/ide/Editor';
import { GitPlugin } from '../../pageobjects/ide/plugins/GitPlugin';
import { Ide } from '../../pageobjects/ide/Ide';
import { ProjectTree } from '../../pageobjects/ide/ProjectTree';
import { QuickOpenContainer } from '../../pageobjects/ide/QuickOpenContainer';
import { ICheLoginPage } from '../../pageobjects/login/ICheLoginPage';
import { TestConstants } from '../../TestConstants';
import { DriverHelper } from '../../utils/DriverHelper';
import { TestWorkspaceUtil } from '../../utils/workspace/TestWorkspaceUtil';
import { TopMenu } from '../../pageobjects/ide/TopMenu';
import { WorkspaceNameHandler } from '../../utils/WorkspaceNameHandler';
import { By } from 'selenium-webdriver';
import CheReporter from '../../driver/CheReporter';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';

const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
const ide: Ide = e2eContainer.get(CLASSES.Ide);
const quickOpenContainer: QuickOpenContainer = e2eContainer.get(CLASSES.QuickOpenContainer);
const editor: Editor = e2eContainer.get(CLASSES.Editor);
const topMenu: TopMenu = e2eContainer.get(CLASSES.TopMenu);
const loginPage: ICheLoginPage = e2eContainer.get<ICheLoginPage>(TYPES.CheLogin);
const projectTree: ProjectTree = e2eContainer.get(CLASSES.ProjectTree);
const gitPlugin: GitPlugin = e2eContainer.get(CLASSES.GitPlugin);
const testWorkspaceUtils: TestWorkspaceUtil = e2eContainer.get<TestWorkspaceUtil>(TYPES.WorkspaceUtil);
const workspaceNameHandler: WorkspaceNameHandler = e2eContainer.get(CLASSES.WorkspaceNameHandler);

const workspacePrefixUrl: string = `${TestConstants.TS_SELENIUM_BASE_URL}/dashboard/#/ide/${TestConstants.TS_SELENIUM_USERNAME}/`;
const wsNameGitPublishBranch = workspaceNameHandler.generateWorkspaceName('checkGitPublishBranch-', 5);
const changedFile = 'README.md';
const branchName = workspaceNameHandler.generateWorkspaceName('checkGitPublishBranch', 5);
const file = `https://github.com/${TestConstants.TS_GITHUB_TEST_REPO}/blob/${branchName}/README.md`;

suite('Publish branch in git extension', async () => {
    suiteSetup(async function () {
        const wsConfig = await testWorkspaceUtils.getBaseDevfile();
        wsConfig.metadata!.name = wsNameGitPublishBranch;
        await testWorkspaceUtils.createWsFromDevFile(wsConfig);
    });

    test('Login into workspace', async () => {
        await browserTabsUtil.navigateTo(workspacePrefixUrl + wsNameGitPublishBranch);
        await loginPage.login();
        CheReporter.registerRunningWorkspace(wsNameGitPublishBranch);
        await ide.waitWorkspaceAndIde();
        await projectTree.openProjectTreeContainer();
        await driverHelper.wait(15000);
    });

    test('Create a new branch, change commit and push', async function changeCommitAndPushFunc() {
        const currentDate: string = Date.now().toString();
        const readmeFileContentXpath: string = `//div[@id='readme']//p[contains(text(), '${currentDate}')]`;
        await cloneTestRepo();

        await driverHelper.wait(15000);
        await topMenu.selectOption('View', 'Find Command...');
        await quickOpenContainer.typeAndSelectSuggestion('branch', 'Git: Create Branch...');
        await quickOpenContainer.typeAndSelectSuggestion(branchName, `Please provide a new branch name (Press 'Enter' to confirm your input or 'Escape' to cancel)`);

        await projectTree.expandPathAndOpenFile('Spoon-Knife', changedFile);
        await editor.type(changedFile, currentDate + '\n', 1);
        await gitPlugin.openGitPluginContainer();
        await gitPlugin.waitChangedFileInChagesList(changedFile);
        await gitPlugin.stageAllChanges(changedFile);
        await gitPlugin.waitChangedFileInChagesList(changedFile);
        await gitPlugin.typeCommitMessage(this.test!.title + currentDate);
        await gitPlugin.commitFromCommandMenu();
        await gitPlugin.pushChangesFromCommandMenu();
        await driverHelper.waitAndClick(By.xpath(`//button[@class='theia-button main']`));
        await gitPlugin.waitDataIsSynchronized();
        await testWorkspaceUtils.cleanUpAllWorkspaces();

        await browserTabsUtil.navigateTo(file);
        await driverHelper.waitVisibility(By.xpath(readmeFileContentXpath));
    });

});

suite('Cleanup', async () => {
    test('Remove test workspace', async () => {
        await testWorkspaceUtils.cleanUpAllWorkspaces();
    });
});

async function cloneTestRepo() {
    const sshLinkToRepo: string = 'git@github.com:' + TestConstants.TS_GITHUB_TEST_REPO + '.git';
    const confirmMessage = 'Clone from URL';

    await topMenu.selectOption('View', 'Find Command...');
    await quickOpenContainer.typeAndSelectSuggestion('clone', 'Git: Clone');
    await quickOpenContainer.typeAndSelectSuggestion(sshLinkToRepo, confirmMessage);
    await gitPlugin.clickOnSelectRepositoryButton();

    await ide.waitAndApplyTrustNotification();
}
