/** *******************************************************************
 * copyright (c) 2019-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import 'reflect-metadata';
import { Container } from 'inversify';
import { IDriver } from '../driver/IDriver';
import { ChromeDriver } from '../driver/ChromeDriver';
import { CLASSES, EXTERNAL_CLASSES, TYPES } from './inversify.types';
import { TestWorkspaceUtil } from '../utils/workspace/TestWorkspaceUtil';
import { IOcpLoginPage } from '../pageobjects/login/interfaces/IOcpLoginPage';
import { OcpUserLoginPage } from '../pageobjects/login/openshift/OcpUserLoginPage';
import { ICheLoginPage } from '../pageobjects/login/interfaces/ICheLoginPage';
import { RegularUserOcpCheLoginPage } from '../pageobjects/login/openshift/RegularUserOcpCheLoginPage';
import { DriverHelper } from '../utils/DriverHelper';
import { Dashboard } from '../pageobjects/dashboard/Dashboard';
import { Workspaces } from '../pageobjects/dashboard/Workspaces';
import { WorkspaceDetails } from '../pageobjects/dashboard/workspace-details/WorkspaceDetails';
import { ScreenCatcher } from '../utils/ScreenCatcher';
import { OcpLoginPage } from '../pageobjects/login/openshift/OcpLoginPage';
import { IAuthorizationHeaderHandler } from '../utils/request-handlers/headers/IAuthorizationHeaderHandler';
import { CheMultiuserAuthorizationHeaderHandler } from '../utils/request-handlers/headers/CheMultiuserAuthorizationHeaderHandler';
import { CheApiRequestHandler } from '../utils/request-handlers/CheApiRequestHandler';
import { CreateWorkspace } from '../pageobjects/dashboard/CreateWorkspace';
import { BrowserTabsUtil } from '../utils/BrowserTabsUtil';
import { WorkspaceHandlingTests } from '../tests-library/WorkspaceHandlingTests';
import { ApiUrlResolver } from '../utils/workspace/ApiUrlResolver';
import { ITestWorkspaceUtil } from '../utils/workspace/ITestWorkspaceUtil';
import { ProjectAndFileTests } from '../tests-library/ProjectAndFileTests';
import { LoginTests } from '../tests-library/LoginTests';
import { RedHatLoginPage } from '../pageobjects/login/openshift/RedHatLoginPage';
import { OcpRedHatLoginPage } from '../pageobjects/login/openshift/OcpRedHatLoginPage';
import { OcpMainPage } from '../pageobjects/openshift/OcpMainPage';
import { OcpImportFromGitPage } from '../pageobjects/openshift/OcpImportFromGitPage';
import { OcpApplicationPage } from '../pageobjects/openshift/OcpApplicationPage';
import { StringUtil } from '../utils/StringUtil';
import { KubernetesLoginPage } from '../pageobjects/login/kubernetes/KubernetesLoginPage';
import { DexLoginPage } from '../pageobjects/login/kubernetes/DexLoginPage';
import { OAUTH_CONSTANTS } from '../constants/OAUTH_CONSTANTS';
import { BASE_TEST_CONSTANTS, Platform } from '../constants/BASE_TEST_CONSTANTS';
import { CheCodeLocatorLoader } from '../pageobjects/ide/CheCodeLocatorLoader';
import { LocatorLoader } from 'monaco-page-objects/out/locators/loader';
import { OauthPage } from '../pageobjects/git-providers/OauthPage';
import { DevfilesHelper } from '../utils/DevfilesHelper';
import { Main as Generator } from '@eclipse-che/che-devworkspace-generator/lib/main';
import { ContainerTerminal, KubernetesCommandLineToolsExecutor } from '../utils/KubernetesCommandLineToolsExecutor';
import { ShellExecutor } from '../utils/ShellExecutor';
import { UserPreferences } from '../pageobjects/dashboard/UserPreferences';
import { WebTerminalPage } from '../pageobjects/webterminal/WebTerminalPage';
import { TrustAuthorPopup } from '../pageobjects/dashboard/TrustAuthorPopup';
import { ViewsMoreActionsButton } from '../pageobjects/ide/ViewsMoreActionsButton';
import { RestrictedModeButton } from '../pageobjects/ide/RestrictedModeButton';

const e2eContainer: Container = new Container({ defaultScope: 'Transient', skipBaseClassChecks: true });

e2eContainer.bind<IDriver>(TYPES.Driver).to(ChromeDriver).inSingletonScope();
e2eContainer.bind<ITestWorkspaceUtil>(TYPES.WorkspaceUtil).to(TestWorkspaceUtil);
e2eContainer.bind<IOcpLoginPage>(TYPES.OcpLogin).to(OcpUserLoginPage);
e2eContainer.bind<IAuthorizationHeaderHandler>(TYPES.IAuthorizationHeaderHandler).to(CheMultiuserAuthorizationHeaderHandler);
e2eContainer.bind<BrowserTabsUtil>(CLASSES.BrowserTabsUtil).to(BrowserTabsUtil);
e2eContainer.bind<DriverHelper>(CLASSES.DriverHelper).to(DriverHelper);
e2eContainer.bind<Dashboard>(CLASSES.Dashboard).to(Dashboard);
e2eContainer.bind<Workspaces>(CLASSES.Workspaces).to(Workspaces);
e2eContainer.bind<WorkspaceDetails>(CLASSES.WorkspaceDetails).to(WorkspaceDetails);
e2eContainer.bind<ScreenCatcher>(CLASSES.ScreenCatcher).to(ScreenCatcher);
e2eContainer.bind<OcpLoginPage>(CLASSES.OcpLoginPage).to(OcpLoginPage);
e2eContainer.bind<DexLoginPage>(CLASSES.DexLoginPage).to(DexLoginPage);
e2eContainer.bind<CheCodeLocatorLoader>(CLASSES.CheCodeLocatorLoader).to(CheCodeLocatorLoader);
e2eContainer.bind<LocatorLoader>(CLASSES.LocatorLoader).to(LocatorLoader);
e2eContainer.bind<OauthPage>(CLASSES.OauthPage).to(OauthPage);
e2eContainer.bind<OcpMainPage>(CLASSES.OcpMainPage).to(OcpMainPage);
e2eContainer.bind<OcpImportFromGitPage>(CLASSES.OcpImportFromGitPage).to(OcpImportFromGitPage);
e2eContainer.bind<OcpApplicationPage>(CLASSES.OcpApplicationPage).to(OcpApplicationPage);
e2eContainer.bind<CheApiRequestHandler>(CLASSES.CheApiRequestHandler).to(CheApiRequestHandler);
e2eContainer.bind<CreateWorkspace>(CLASSES.CreateWorkspace).to(CreateWorkspace);
e2eContainer.bind<ProjectAndFileTests>(CLASSES.ProjectAndFileTests).to(ProjectAndFileTests);
e2eContainer.bind<LoginTests>(CLASSES.LoginTests).to(LoginTests);
e2eContainer.bind<StringUtil>(CLASSES.StringUtil).to(StringUtil);
e2eContainer.bind<ApiUrlResolver>(CLASSES.ApiUrlResolver).to(ApiUrlResolver);
e2eContainer.bind<WorkspaceHandlingTests>(CLASSES.WorkspaceHandlingTests).to(WorkspaceHandlingTests);
e2eContainer.bind<RedHatLoginPage>(CLASSES.RedHatLoginPage).to(RedHatLoginPage);
e2eContainer.bind<DevfilesHelper>(CLASSES.DevfilesRegistryHelper).to(DevfilesHelper);
e2eContainer.bind<KubernetesCommandLineToolsExecutor>(CLASSES.KubernetesCommandLineToolsExecutor).to(KubernetesCommandLineToolsExecutor);
e2eContainer.bind<ShellExecutor>(CLASSES.ShellExecutor).to(ShellExecutor);
e2eContainer.bind<ContainerTerminal>(CLASSES.ContainerTerminal).to(ContainerTerminal);
e2eContainer.bind<WebTerminalPage>(CLASSES.WebTerminalPage).to(WebTerminalPage);
e2eContainer.bind<UserPreferences>(CLASSES.UserPreferences).to(UserPreferences);
e2eContainer.bind<Generator>(EXTERNAL_CLASSES.Generator).to(Generator);
e2eContainer.bind<LocatorLoader>(EXTERNAL_CLASSES.LocatorLoader).to(LocatorLoader);
e2eContainer.bind<LocatorLoader>(EXTERNAL_CLASSES.LocatorLoader).to(LocatorLoader);
e2eContainer.bind<TrustAuthorPopup>(CLASSES.TrustAuthorPopup).to(TrustAuthorPopup);
e2eContainer.bind<ViewsMoreActionsButton>(CLASSES.ViewsMoreActionsButton).to(ViewsMoreActionsButton);
e2eContainer.bind<RestrictedModeButton>(CLASSES.RestrictedModeButton).to(RestrictedModeButton);

if (BASE_TEST_CONSTANTS.TS_PLATFORM === Platform.OPENSHIFT) {
	if (OAUTH_CONSTANTS.TS_SELENIUM_VALUE_OPENSHIFT_OAUTH) {
		e2eContainer.bind<ICheLoginPage>(TYPES.CheLogin).to(RegularUserOcpCheLoginPage);
	} else {
		e2eContainer.bind<ICheLoginPage>(TYPES.CheLogin).to(OcpRedHatLoginPage);
	}
} else {
	e2eContainer.bind<ICheLoginPage>(TYPES.CheLogin).to(KubernetesLoginPage);
}

export { e2eContainer };
