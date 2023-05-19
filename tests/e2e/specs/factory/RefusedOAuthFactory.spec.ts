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
import { e2eContainer } from '../../configs/inversify.config';
import {
    ActivityBar,
    ContextMenu,
    EditorView,
    error,
    InputBox,
    Locators,
    ModalDialog,
    NewScmView,
    SideBarView,
    SingleScmProvider,
    TextEditor,
    ViewControl,
    ViewItem,
    ViewSection
} from 'monaco-page-objects';
import { expect } from 'chai';
import WebDriverError = error.WebDriverError;
import { registerRunningWorkspace } from '../MochaHooks';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import { CLASSES } from '../../configs/inversify.types';
import { WorkspaceHandlingTests } from '../../tests-library/WorkspaceHandlingTests';
import { CheCodeLocatorLoader } from '../../pageobjects/ide/CheCodeLocatorLoader';
import { ProjectAndFileTests } from '../../tests-library/ProjectAndFileTests';
import { DriverHelper } from '../../utils/DriverHelper';
import { TestConstants } from '../../constants/TestConstants';
import { OauthPage } from '../../pageobjects/git-providers/OauthPage';
import { GitUtil } from '../../utils/vsc/GitUtil';
import { Logger } from '../../utils/Logger';
import { TimeoutConstants } from '../../constants/TimeoutConstants';
import { LoginTests } from '../../tests-library/LoginTests';

const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
const webCheCodeLocators: Locators = new CheCodeLocatorLoader().webCheCodeLocators;
const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);

suite(`Create a workspace via launching a factory from the ${TestConstants.TS_SELENIUM_FACTORY_GIT_PROVIDER} repository and deny the access`, async function (): Promise<void> {
    const oauthPage: OauthPage = new OauthPage(driverHelper);

    let projectSection: ViewSection;
    let scmProvider: SingleScmProvider;
    let rest: SingleScmProvider[];
    let scmContextMenu: ContextMenu;

    // test specific data
    const timeToRefresh: number = 1500;
    const changesToCommit: string = (new Date()).getTime().toString();
    const fileToChange: string = 'Date.txt';
    const commitChangesButtonLabel: string = `Commit Changes on "${TestConstants.TS_SELENIUM_FACTORY_GIT_REPO_BRANCH}"`;
    const refreshButtonLabel: string = 'Refresh';
    const pushItemLabel: string = 'Push';
    const label: string = TestConstants.TS_SELENIUM_PROJECT_ROOT_FILE_NAME;
    let testRepoProjectName: string;
    const isPrivateRepo: string = TestConstants.TS_SELENIUM_IS_PRIVATE_FACTORY_GIT_REPO ? 'private' : 'public';

    loginTests.loginIntoChe();

    test(`Navigate to the ${isPrivateRepo} repository factory URL`, async function (): Promise<void> {
        await browserTabsUtil.navigateTo(TestConstants.TS_SELENIUM_FACTORY_URL());
    });

    if (TestConstants.TS_SELENIUM_GIT_PROVIDER_OAUTH) {
        test(`Authorize with a ${TestConstants.TS_SELENIUM_FACTORY_GIT_PROVIDER} OAuth and deny access`, async function (): Promise<void> {
            await oauthPage.login();
            await oauthPage.waitOauthPage();
            await oauthPage.denyAccess();
        });
    }

    workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();

    test('The workspace starts with access deny flag in the url', async function (): Promise<void> {
        expect(await driverHelper.getDriver().getCurrentUrl()).contains('&error_code=access_denied');
    });

    test('Registering the running workspace', async function (): Promise<void> {
        registerRunningWorkspace(WorkspaceHandlingTests.getWorkspaceName());
    });

    test('Wait the workspace readiness', async function (): Promise<void> {
        await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
    });

    test('Check if a project folder has been created', async function (): Promise<void> {
        testRepoProjectName = GitUtil.getProjectNameFromGitUrl(TestConstants.TS_SELENIUM_FACTORY_GIT_REPO_URL);
        Logger.debug(`new SideBarView().getContent().getSection: get ${testRepoProjectName}`);
        projectSection = await new SideBarView().getContent().getSection(testRepoProjectName);
    });

    test('Accept the project as a trusted one', async function (): Promise<void> {
        // click somewhere to trigger "Welcome Content" dialog
        try {
            await driverHelper.waitAndClick(webCheCodeLocators.Workbench.notificationItem);
        } catch (e) {
            Logger.info(`Click on ${webCheCodeLocators.Workbench.notificationItem} to get "Welcome Content" dialog ${e as string}`);
        }
        // "Welcome Content" dialog can be shown before of after dialog with an error for private repo
        try {
            const buttonYesITrustTheAuthors: string = `Yes, I trust the authors`;
            await driverHelper.waitVisibility(webCheCodeLocators.WelcomeContent.text, TimeoutConstants.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM);
            const welcomeContentDialog: ModalDialog = new ModalDialog();
            Logger.debug(`trustedProjectDialog.pushButton: "${buttonYesITrustTheAuthors}"`);
            await welcomeContentDialog.pushButton(buttonYesITrustTheAuthors);
            await driverHelper.waitDisappearance(webCheCodeLocators.WelcomeContent.text);
        } catch (e) {
            Logger.info(`"Accept the project as a trusted one" dialog was not shown firstly for "${isPrivateRepo}"`);
            if (!TestConstants.TS_SELENIUM_IS_PRIVATE_FACTORY_GIT_REPO) {
                throw new WebDriverError(e as string);
            }
        }
    });

    if (TestConstants.TS_SELENIUM_IS_PRIVATE_FACTORY_GIT_REPO) {
        test('Check that project can not be cloned', async function (): Promise<void> {
            await driverHelper.waitVisibility(webCheCodeLocators.Dialog.message);
            const workspaceDoesNotExistDialog: ModalDialog = new ModalDialog();
            const message: string = await workspaceDoesNotExistDialog.getMessage();
            expect(message).contains('space does not exist');
        });

        test('Check that project files were not imported', async function (): Promise<void> {
            const isFileImported: ViewItem | undefined = await projectSection.findItem(label);
            expect(isFileImported).eqls(undefined);
        });

    } else {
        test('Check if the project files were imported', async function (): Promise<void> {
            Logger.debug(`projectSection.findItem: find ${label}`);
            const isFileImported: ViewItem | undefined = await projectSection.findItem(label);
            // projectSection.findItem(label) can return undefined but test will goes on
            expect(isFileImported).not.eqls(undefined);
        });

        test('Make changes to the file', async function (): Promise<void> {
            Logger.debug(`projectSection.openItem: "${fileToChange}"`);
            await projectSection.openItem(fileToChange);
            const editor: TextEditor = await new EditorView().openEditor(fileToChange) as TextEditor;
            await driverHelper.waitVisibility(webCheCodeLocators.Editor.inputArea);
            Logger.debug(`editor.clearText`);
            await editor.clearText();
            Logger.debug(`editor.typeTextAt: "${changesToCommit}"`);
            await editor.typeTextAt(1, 1, changesToCommit);
        });

        test('Open a source control manager', async function (): Promise<void> {
            const viewSourceControl: string = `Source Control`;
            const sourceControl: ViewControl = await new ActivityBar().getViewControl(viewSourceControl) as ViewControl;
            Logger.debug(`sourceControl.openView: "${viewSourceControl}"`);
            await sourceControl.openView();
            const scmView: NewScmView = new NewScmView();
            await driverHelper.waitVisibility(webCheCodeLocators.ScmView.actionConstructor(commitChangesButtonLabel));
            [scmProvider, ...rest] = await scmView.getProviders();
            Logger.debug(`scmView.getProviders: "${scmProvider}, ${scmProvider}"`);
        });

        test('Check if the changes are displayed in the source control manager', async function (): Promise<void> {
            await driverHelper.waitVisibility(webCheCodeLocators.ScmView.more);
            await driverHelper.wait(timeToRefresh);
            Logger.debug(`scmProvider.takeAction: "${refreshButtonLabel}"`);
            await scmProvider.takeAction(refreshButtonLabel);
            // wait while changes counter will be refreshed
            await driverHelper.wait(timeToRefresh);
            const changes: number = await scmProvider.getChangeCount();
            Logger.debug(`scmProvider.getChangeCount: number of changes is "${changes}"`);
            expect(changes).eql(1);
        });

        test('Stage the changes', async function (): Promise<void> {
            await driverHelper.waitVisibility(webCheCodeLocators.ScmView.more);
            Logger.debug(`scmProvider.openMoreActions`);
            scmContextMenu = await scmProvider.openMoreActions();
            await driverHelper.waitVisibility(webCheCodeLocators.ContextMenu.contextView);
            Logger.debug(`scmContextMenu.select: "Changes" -> "Stage All Changes"`);
            await scmContextMenu.select('Changes', 'Stage All Changes');
        });

        test('Commit the changes', async function (): Promise<void> {
            await driverHelper.waitVisibility(webCheCodeLocators.ScmView.actionConstructor(commitChangesButtonLabel));
            Logger.debug(`scmProvider.commitChanges: commit name "Commit ${changesToCommit}"`);
            await scmProvider.commitChanges('Commit ' + changesToCommit);
            await driverHelper.waitVisibility(webCheCodeLocators.ScmView.more);
            await driverHelper.wait(timeToRefresh);
            Logger.debug(`scmProvider.takeAction: "${refreshButtonLabel}"`);
            await scmProvider.takeAction(refreshButtonLabel);
            // wait while changes counter will be refreshed
            await driverHelper.wait(timeToRefresh);
            const changes: number = await scmProvider.getChangeCount();
            Logger.debug(`scmProvider.getChangeCount: number of changes is "${changes}"`);
            expect(changes).eql(0);
        });

        test('Push the changes', async function (): Promise<void> {
            await driverHelper.waitVisibility(webCheCodeLocators.ScmView.actionConstructor(`Push 1 commits to origin/${TestConstants.TS_SELENIUM_FACTORY_GIT_REPO_BRANCH}`));
            await driverHelper.waitVisibility(webCheCodeLocators.ScmView.more);
            Logger.debug(`scmProvider.openMoreActions`);
            scmContextMenu = await scmProvider.openMoreActions();
            await driverHelper.waitVisibility(webCheCodeLocators.ContextMenu.itemConstructor(pushItemLabel));
            Logger.debug(`scmContextMenu.select: "${pushItemLabel}"`);
            await scmContextMenu.select(pushItemLabel);
        });

        test('Insert git credentials which were asked after push', async function (): Promise<void> {
            await driverHelper.waitVisibility(webCheCodeLocators.ScmView.more);

            try {
                await driverHelper.waitVisibility(webCheCodeLocators.Input.inputBox);
            } catch (e) {
                Logger.info(`Workspace did not ask credentials before push - ${e};
                Known issue for github.com - https://issues.redhat.com/browse/CRW-4066`);
            }
            const input: InputBox = new InputBox();
            await input.setText(TestConstants.TS_SELENIUM_GIT_PROVIDER_USERNAME);
            await input.confirm();
            await driverHelper.wait(timeToRefresh);
            await input.setText(TestConstants.TS_SELENIUM_GIT_PROVIDER_PASSWORD);
            await input.confirm();
            await driverHelper.wait(timeToRefresh);
        });

        test('Check if the changes were pushed', async function (): Promise<void> {
            try {
                Logger.debug(`scmProvider.takeAction: "${refreshButtonLabel}"`);
                await scmProvider.takeAction(refreshButtonLabel);
            } catch (e) {
                Logger.info('Check you use correct credentials.' +
                    'For bitbucket.org ensure you use an app password: https://support.atlassian.com/bitbucket-cloud/docs/using-app-passwords/;' +
                    'For github.com - personal access token instead of password.');
            }
            const isCommitButtonDisabled: string = await driverHelper.waitAndGetElementAttribute(webCheCodeLocators.ScmView.actionConstructor(commitChangesButtonLabel), 'aria-disabled');
            expect(isCommitButtonDisabled).eql('true');
        });
    }

    test(`Stop and remove the workspace`, async function (): Promise<void> {
        await workspaceHandlingTests.stopAndRemoveWorkspace(WorkspaceHandlingTests.getWorkspaceName());
    });

    loginTests.logoutFromChe();

    suiteTeardown('Close the browser', async function (): Promise<void> {
        if (!TestConstants.TS_DEBUG_MODE) {
            await driverHelper.getDriver().close();
        }
    });
});
