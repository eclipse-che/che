/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { assert } from 'chai';
import { e2eContainer } from '../../inversify.config';
import { CLASSES, TYPES } from '../../inversify.types';
import { Editor } from '../../pageobjects/ide/Editor';
import { GitPlugin } from '../../pageobjects/ide/GitPlugin';
import { Ide } from '../../pageobjects/ide/Ide';
import { ProjectTree } from '../../pageobjects/ide/ProjectTree';
import { QuickOpenContainer } from '../../pageobjects/ide/QuickOpenContainer';
import { ICheLoginPage } from '../../pageobjects/login/ICheLoginPage';
import { TestConstants } from '../../TestConstants';
import { DriverHelper } from '../../utils/DriverHelper';
import { WorkspaceNameHandler } from '../../utils/WorkspaceNameHandler';
import { CheGitApi } from '../../utils/VCS/CheGitApi';
import { GitHubUtil } from '../../utils/VCS/github/GitHubUtil';
import { TestWorkspaceUtil } from '../../utils/workspace/TestWorkspaceUtil';
import { TopMenu } from '../../pageobjects/ide/TopMenu';
import { TimeoutConstants } from '../../TimeoutConstants';

const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
const ide: Ide = e2eContainer.get(CLASSES.Ide);
const quickOpenContainer: QuickOpenContainer = e2eContainer.get(CLASSES.QuickOpenContainer);
const editor: Editor = e2eContainer.get(CLASSES.Editor);
const topMenu: TopMenu = e2eContainer.get(CLASSES.TopMenu);
const loginPage: ICheLoginPage = e2eContainer.get<ICheLoginPage>(TYPES.CheLogin);
const gitHubUtils: GitHubUtil = e2eContainer.get<GitHubUtil>(CLASSES.GitHubUtil);
const cheGitAPI: CheGitApi = e2eContainer.get(CLASSES.CheGitApi);
const projectTree: ProjectTree = e2eContainer.get(CLASSES.ProjectTree);
const gitPlugin: GitPlugin = e2eContainer.get(CLASSES.GitPlugin);
const testWorkspaceUtils: TestWorkspaceUtil = e2eContainer.get<TestWorkspaceUtil>(TYPES.WorkspaceUtil);


suite('Git with ssh workflow', async () => {
    const workspacePrefixUrl: string = `${TestConstants.TS_SELENIUM_BASE_URL}/dashboard/#/ide/${TestConstants.TS_SELENIUM_USERNAME}/`;
    const wsNameCheckGeneratingKeys = 'checkGeneraringSsh';
    const wsNameCheckPropagatingKeys = 'checkPropagatingSsh';
    const committedFile = 'README.md';

    suiteSetup(async function () {
        const wsConfig = await testWorkspaceUtils.getBaseDevfile();
        wsConfig.metadata!.name = wsNameCheckGeneratingKeys;
        await testWorkspaceUtils.createWsFromDevFile(wsConfig);
    });

    test('Login into workspace and open tree container', async () => {
        await driverHelper.navigateToUrl(workspacePrefixUrl + wsNameCheckGeneratingKeys);
        await loginPage.login();
        await ide.waitWorkspaceAndIde();
        await projectTree.openProjectTreeContainer(TimeoutConstants.TS_SELENIUM_START_WORKSPACE_TIMEOUT);
        await driverHelper.wait(TimeoutConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT);
    });

    test('Generate a SSH key', async () => {
        await topMenu.selectOption('View', 'Find Command...');
        await quickOpenContainer.typeAndSelectSuggestion('SSH', 'SSH: generate key pair...');
        await ide.waitNotificationAndClickOnButton('Key pair successfully generated, do you want to view the public key', 'View');
        await editor.waitEditorOpened('Untitled-0');
        await editor.waitText('Untitled-0', 'ssh-rsa');
    });


    test('Add a SSH key to GitHub side and clone by ssh link', async () => {
        const sshName: string = WorkspaceNameHandler.generateWorkspaceName('test-SSH-', 5);
        const publicSshKey = await cheGitAPI.getPublicSSHKey();
        await gitHubUtils.addPublicSshKeyToUserAccount(TestConstants.TS_GITHUB_TEST_REPO_ACCESS_TOKEN, sshName, publicSshKey);
        await cloneTestRepo();

    });

    test('Change commit and push', async function changeCommitAndPushFunc() {
        const currentDate: string = Date.now().toString();
        await projectTree.expandPathAndOpenFile('Spoon-Knife', committedFile);
        await editor.type(committedFile, currentDate + '\n', 1);
        await gitPlugin.openGitPluginContainer();
        await gitPlugin.waitChangedFileInChagesList(committedFile);
        await gitPlugin.stageAllChanges(committedFile);
        await gitPlugin.waitChangedFileInChagesList(committedFile);
        await gitPlugin.typeCommitMessage(this.test!.title + currentDate);
        await gitPlugin.commitFromCommandMenu();
        await gitPlugin.pushChangesFromCommandMenu();
        await gitPlugin.waitDataIsSynchronized();
        const rawDataFromFile: string = await gitHubUtils.getRawContentFromFile(TestConstants.TS_GITHUB_TEST_REPO + '/master/' + committedFile);
        assert.isTrue(rawDataFromFile.includes(currentDate));
        await testWorkspaceUtils.cleanUpAllWorkspaces();
    });

    test('Check ssh key in  a new workspace', async () => {
        const data = await testWorkspaceUtils.getBaseDevfile();

        data.metadata!.name = wsNameCheckPropagatingKeys;
        await testWorkspaceUtils.createWsFromDevFile(data);
        await driverHelper.navigateToUrl(workspacePrefixUrl + wsNameCheckPropagatingKeys);
        await ide.waitWorkspaceAndIde();
        await projectTree.openProjectTreeContainer(TimeoutConstants.TS_SELENIUM_START_WORKSPACE_TIMEOUT);
        await driverHelper.wait(TimeoutConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT);
        await cloneTestRepo();
        await projectTree.waitItem('Spoon-Knife');
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
}
