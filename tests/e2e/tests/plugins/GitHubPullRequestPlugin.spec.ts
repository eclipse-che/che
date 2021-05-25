/*********************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import 'reflect-metadata';
import { e2eContainer } from '../../inversify.config';
import { CLASSES, TYPES } from '../../inversify.types';
import { Ide } from '../../pageobjects/ide/Ide';
import { TimeoutConstants } from '../../TimeoutConstants';
import { TestConstants } from '../../TestConstants';
import { ProjectTree } from '../../pageobjects/ide/ProjectTree';
import { WorkspaceHandlingTests } from '../../testsLibrary/WorkspaceHandlingTests';
import { Logger } from '../../utils/Logger';
import { DriverHelper } from '../../utils/DriverHelper';
import { GitHubPullRequestPlugin } from '../../pageobjects/ide/plugins/GitHubPullRequestPlugin';
import { GitLoginPage } from '../../pageobjects/third-parties/GitLoginPage';
import { GitOauthAppsSettings } from '../../pageobjects/third-parties/GitOauthAppsSettings';
import { WorkspaceNameHandler } from '../../utils/WorkspaceNameHandler';

const ide: Ide = e2eContainer.get(CLASSES.Ide);
const projectTree: ProjectTree = e2eContainer.get(CLASSES.ProjectTree);
const workspaceHandling: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
const gitHubPullRequestPlugin: GitHubPullRequestPlugin = e2eContainer.get(CLASSES.GitHubPullRequestPlugin);
const githubLoginPage: GitLoginPage = e2eContainer.get(CLASSES.GitLoginPage);
const gitOauthAppsSettings: GitOauthAppsSettings = e2eContainer.get(CLASSES.GitOauthAppsSettings);

const devfileUrl: string = `https://gist.githubusercontent.com/Ohrimenko1988/244ad55483c717201ee6f71d68d43c87/raw/8fec64dcb57084f9e201a35b12c464d3fd73eee3/GithubPullRequestPlugin.yaml`;
const factoryUrl: string = `${TestConstants.TS_SELENIUM_BASE_URL}/f?url=${devfileUrl}`;
const projectName: string = 'Spoon-Knife'
const oAuthAppName: string = 'iokhrime-che';


suite(`The 'GitHubPullRequestPlugin' test`, async () => {
    let workspaceName: string = '';

    suite('Setup github', async () => {
        test('Login to github', async () => {
            await gitOauthAppsSettings.openPage();
            await githubLoginPage.login();
        });

        test('Open application settings', async () => {
            await gitOauthAppsSettings.openOauthApp(oAuthAppName);
        });

        test('Configure oauth application', async () => {
            await gitOauthAppsSettings.scrollToUpdateApplicationButton();

            await gitOauthAppsSettings.typeHomePageUrl(TestConstants.TS_SELENIUM_BASE_URL);
            await gitOauthAppsSettings.typeCallbackUrl(`${TestConstants.TS_SELENIUM_KEYCLOAK_URL}/realms/che/broker/github/endpoint`)
            await gitOauthAppsSettings.clickUpdateApplicationButton()
        });
    });

    suite('Test', async () => {
        test('Create workspace using factory', async () => {
            await driverHelper.navigateToUrl(factoryUrl);
        });

        test('Wait until created workspace is started', async () => {
            await ide.waitAndSwitchToIdeFrame();
            await ide.waitIde(TimeoutConstants.TS_SELENIUM_START_WORKSPACE_TIMEOUT);

            await gitHubPullRequestPlugin.waitViewIcon();
            await projectTree.openProjectTreeContainer();
            await projectTree.waitProjectImported(projectName, 'README.md');

            workspaceName = await WorkspaceNameHandler.getNameFromUrl();
        });

        test('Open GH PR plugin', async () => {
            await gitHubPullRequestPlugin.openView();
        });

        test('Login to GitHub', async () => {
            const loginNotificationText: string = `wants to sign in using GitHub.`;
            const buttonText: string = 'Allow';

            await gitHubPullRequestPlugin.clickSignInButton();
            await ide.waitNotificationAndClickOnButton(loginNotificationText, buttonText);
        });

        test('Check PR plugin connected to PR', async () => {
            await gitHubPullRequestPlugin.expandTreeItem('Created By Me');
            await gitHubPullRequestPlugin.waitTreeItem('iokhrime-pr-test (#1) by @chepullreq4');
        });
    });


    suite('Stopping and deleting the workspace', async () => {
        test(`Stop and remove workspace`, async () => {
            if (TestConstants.TS_DELETE_PLUGINS_TEST_WORKSPACE === 'true') {
                await workspaceHandling.stopAndRemoveWorkspace(workspaceName);
                return;
            }

            Logger.info(`As far as the "TS_DELETE_PLUGINS_TEST_WORKSPACE" value is "false the workspace deletion is skipped"`);
        });
    });

});
