/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { Container } from 'inversify';
import { IDriver } from './driver/IDriver';
import { ChromeDriver } from './driver/ChromeDriver';
import { TYPES, CLASSES } from './inversify.types';
import { ITestWorkspaceUtil } from './utils/workspace/ITestWorkspaceUtil';
import { TestWorkspaceUtil } from './utils/workspace/TestWorkspaceUtil';
import { IOcpLoginPage } from './pageobjects/login/IOcpLoginPage';
import { OcpUserLoginPage } from './pageobjects/login/OcpUserLoginPage';
import { TestConstants } from './TestConstants';
import { ICheLoginPage } from './pageobjects/login/ICheLoginPage';
import { RegularUserOcpCheLoginPage } from './pageobjects/login/RegularUserOcpCheLoginPage';
import { MultiUserLoginPage } from './pageobjects/login/MultiUserLoginPage';
import { ContextMenu } from './pageobjects/ide/ContextMenu';
import { DriverHelper } from './utils/DriverHelper';
import { Dashboard } from './pageobjects/dashboard/Dashboard';
import { Workspaces } from './pageobjects/dashboard/Workspaces';
import { WorkspaceDetails } from './pageobjects/dashboard/workspace-details/WorkspaceDetails';
import { WorkspaceDetailsPlugins } from './pageobjects/dashboard/workspace-details/WorkspaceDetailsPlugins';
import { Ide } from './pageobjects/ide/Ide';
import { ProjectTree } from './pageobjects/ide/ProjectTree';
import { Editor } from './pageobjects/ide/Editor';
import { TopMenu } from './pageobjects/ide/TopMenu';
import { QuickOpenContainer } from './pageobjects/ide/QuickOpenContainer';
import { PreviewWidget } from './pageobjects/ide/PreviewWidget';
import { GitPlugin } from './pageobjects/ide/GitPlugin';
import { RightToolBar } from './pageobjects/ide/RightToolBar';
import { Terminal } from './pageobjects/ide/Terminal';
import { DebugView } from './pageobjects/ide/DebugView';
import { DialogWindow } from './pageobjects/ide/DialogWindow';
import { ScreenCatcher } from './utils/ScreenCatcher';
import { OcpLoginPage } from './pageobjects/openshift/OcpLoginPage';
import { OpenWorkspaceWidget } from './pageobjects/ide/OpenWorkspaceWidget';
import { CheLoginPage } from './pageobjects/openshift/CheLoginPage';
import { NotificationCenter } from './pageobjects/ide/NotificationCenter';
import { PreferencesHandler } from './utils/PreferencesHandler';
import { IAuthorizationHeaderHandler } from './utils/requestHandlers/headers/IAuthorizationHeaderHandler';
import { CheMultiuserAuthorizationHeaderHandler } from './utils/requestHandlers/headers/CheMultiuserAuthorizationHeaderHandler';
import { CheMultiuserTokenHandler } from './utils/requestHandlers/tokens/CheMultiuserTokenHandler';
import { ITokenHandler } from './utils/requestHandlers/tokens/ITokenHandler';
import { CheApiRequestHandler } from './utils/requestHandlers/CheApiRequestHandler';
import { CheGitApi } from './utils/VCS/CheGitApi';
import { GitHubUtil } from './utils/VCS/github/GitHubUtil';
import { CreateWorkspace } from './pageobjects/dashboard/CreateWorkspace';
import { OpenshiftPlugin } from './pageobjects/ide/OpenshiftPlugin';
import { OpenDialogWidget } from './pageobjects/ide/OpenDialogWidget';
import { UpdateAccountInformationPage } from './pageobjects/login/UpdateAccountInformationPage';
import { LeftToolBar } from './pageobjects/ide/LeftToolBar';
import { KubernetesPlugin } from './pageobjects/ide/plugins/KubernetesPlugin';
import { BrowserTabsUtil } from './utils/BrowserTabsUtil';
import { PluginsView } from './pageobjects/ide/plugins/PluginsView';
import { LanguageServerTests } from './testsLibrary/LanguageServerTests';
import { CodeExecutionTests } from './testsLibrary/CodeExecutionTests';
import { ProjectAndFileTests } from './testsLibrary/ProjectAndFileTests';
import { WorkspaceHandlingTests } from './testsLibrary/WorkspaceHandlingTests';
import { GitHubPullRequestPlugin } from './pageobjects/ide/plugins/GitHubPullRequestPlugin';
import { GitLoginPage } from './pageobjects/third-parties/GitLoginPage';
import { GitOauthAppsSettings } from './pageobjects/third-parties/GitOauthAppsSettings';
import { AnimationChecker } from './utils/AnimationChecker';
import { WorkspaceNameHandler } from './utils/WorkspaceNameHandler';

const e2eContainer: Container = new Container({ defaultScope: 'Transient' });

e2eContainer.bind<IDriver>(TYPES.Driver).to(ChromeDriver).inSingletonScope();
e2eContainer.bind<ITestWorkspaceUtil>(TYPES.WorkspaceUtil).to(TestWorkspaceUtil);
e2eContainer.bind<IOcpLoginPage>(TYPES.OcpLogin).to(OcpUserLoginPage);

e2eContainer.bind<IAuthorizationHeaderHandler>(TYPES.IAuthorizationHeaderHandler).to(CheMultiuserAuthorizationHeaderHandler);
e2eContainer.bind<ITokenHandler>(TYPES.ITokenHandler).to(CheMultiuserTokenHandler);

if (JSON.parse(TestConstants.TS_SELENIUM_VALUE_OPENSHIFT_OAUTH)) {
    e2eContainer.bind<ICheLoginPage>(TYPES.CheLogin).to(RegularUserOcpCheLoginPage);
} else {
    e2eContainer.bind<ICheLoginPage>(TYPES.CheLogin).to(MultiUserLoginPage);
}

e2eContainer.bind<BrowserTabsUtil>(CLASSES.BrowserTabsUtil).to(BrowserTabsUtil);
e2eContainer.bind<ContextMenu>(CLASSES.ContextMenu).to(ContextMenu);
e2eContainer.bind<DriverHelper>(CLASSES.DriverHelper).to(DriverHelper);
e2eContainer.bind<Dashboard>(CLASSES.Dashboard).to(Dashboard);
e2eContainer.bind<Workspaces>(CLASSES.Workspaces).to(Workspaces);
e2eContainer.bind<WorkspaceDetails>(CLASSES.WorkspaceDetails).to(WorkspaceDetails);
e2eContainer.bind<WorkspaceDetailsPlugins>(CLASSES.WorkspaceDetailsPlugins).to(WorkspaceDetailsPlugins);
e2eContainer.bind<Ide>(CLASSES.Ide).to(Ide);
e2eContainer.bind<ProjectTree>(CLASSES.ProjectTree).to(ProjectTree);
e2eContainer.bind<Editor>(CLASSES.Editor).to(Editor);
e2eContainer.bind<TopMenu>(CLASSES.TopMenu).to(TopMenu);
e2eContainer.bind<QuickOpenContainer>(CLASSES.QuickOpenContainer).to(QuickOpenContainer);
e2eContainer.bind<PreviewWidget>(CLASSES.PreviewWidget).to(PreviewWidget);
e2eContainer.bind<GitPlugin>(CLASSES.GitPlugin).to(GitPlugin);
e2eContainer.bind<RightToolBar>(CLASSES.RightToolBar).to(RightToolBar);
e2eContainer.bind<LeftToolBar>(CLASSES.LeftToolBar).to(LeftToolBar);
e2eContainer.bind<Terminal>(CLASSES.Terminal).to(Terminal);
e2eContainer.bind<DebugView>(CLASSES.DebugView).to(DebugView);
e2eContainer.bind<DialogWindow>(CLASSES.DialogWindow).to(DialogWindow);
e2eContainer.bind<ScreenCatcher>(CLASSES.ScreenCatcher).to(ScreenCatcher);
e2eContainer.bind<OcpLoginPage>(CLASSES.OcpLoginPage).to(OcpLoginPage);
e2eContainer.bind<OpenWorkspaceWidget>(CLASSES.OpenWorkspaceWidget).to(OpenWorkspaceWidget);
e2eContainer.bind<CheLoginPage>(CLASSES.CheLoginPage).to(CheLoginPage);
e2eContainer.bind<NotificationCenter>(CLASSES.NotificationCenter).to(NotificationCenter);
e2eContainer.bind<PreferencesHandler>(CLASSES.PreferencesHandler).to(PreferencesHandler);
e2eContainer.bind<CheApiRequestHandler>(CLASSES.CheApiRequestHandler).to(CheApiRequestHandler);
e2eContainer.bind<CheGitApi>(CLASSES.CheGitApi).to(CheGitApi);
e2eContainer.bind<GitHubUtil>(CLASSES.GitHubUtil).to(GitHubUtil);
e2eContainer.bind<OpenshiftPlugin>(CLASSES.OpenshiftPlugin).to(OpenshiftPlugin);
e2eContainer.bind<CreateWorkspace>(CLASSES.CreateWorkspace).to(CreateWorkspace);
e2eContainer.bind<OpenDialogWidget>(CLASSES.OpenDialogWidget).to(OpenDialogWidget);
e2eContainer.bind<UpdateAccountInformationPage>(CLASSES.UpdateAccountInformationPage).to(UpdateAccountInformationPage);
e2eContainer.bind<KubernetesPlugin>(CLASSES.KubernetesPlugin).to(KubernetesPlugin);
e2eContainer.bind<PluginsView>(CLASSES.PluginsView).to(PluginsView);
e2eContainer.bind<LanguageServerTests>(CLASSES.LanguageServerTests).to(LanguageServerTests);
e2eContainer.bind<CodeExecutionTests>(CLASSES.CodeExecutionTests).to(CodeExecutionTests);
e2eContainer.bind<ProjectAndFileTests>(CLASSES.ProjectAndFileTests).to(ProjectAndFileTests);
e2eContainer.bind<WorkspaceHandlingTests>(CLASSES.WorkspaceHandlingTests).to(WorkspaceHandlingTests);
e2eContainer.bind<WorkspaceNameHandler>(CLASSES.WorkspaceNameHandler).to(WorkspaceNameHandler);
e2eContainer.bind<GitHubPullRequestPlugin>(CLASSES.GitHubPullRequestPlugin).to(GitHubPullRequestPlugin);
e2eContainer.bind<GitLoginPage>(CLASSES.GitLoginPage).to(GitLoginPage);
e2eContainer.bind<GitOauthAppsSettings>(CLASSES.GitOauthAppsSettings).to(GitOauthAppsSettings);
e2eContainer.bind<AnimationChecker>(CLASSES.AnimationChecker).to(AnimationChecker);

export { e2eContainer };
