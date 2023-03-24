/*********************************************************************
 * Copyright (c) 2019-2023 Red Hat, Inc.
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
import { TYPES, CLASSES } from './inversify.types';
import { TestWorkspaceUtil } from '../utils/workspace/TestWorkspaceUtil';
import { IOcpLoginPage } from '../pageobjects/login/IOcpLoginPage';
import { OcpUserLoginPage } from '../pageobjects/login/OcpUserLoginPage';
import { TestConstants } from '../constants/TestConstants';
import { ICheLoginPage } from '../pageobjects/login/ICheLoginPage';
import { RegularUserOcpCheLoginPage } from '../pageobjects/login/RegularUserOcpCheLoginPage';
import { DriverHelper } from '../utils/DriverHelper';
import { Dashboard } from '../pageobjects/dashboard/Dashboard';
import { Workspaces } from '../pageobjects/dashboard/Workspaces';
import { WorkspaceDetails } from '../pageobjects/dashboard/workspace-details/WorkspaceDetails';
import { ScreenCatcher } from '../utils/ScreenCatcher';
import { OcpLoginPage } from '../pageobjects/openshift/OcpLoginPage';
import { CheLoginPage } from '../pageobjects/openshift/CheLoginPage';
import { IAuthorizationHeaderHandler } from '../utils/request-handlers/headers/IAuthorizationHeaderHandler';
import { CheMultiuserAuthorizationHeaderHandler } from '../utils/request-handlers/headers/CheMultiuserAuthorizationHeaderHandler';
import { CheApiRequestHandler } from '../utils/request-handlers/CheApiRequestHandler';
import { CreateWorkspace } from '../pageobjects/dashboard/CreateWorkspace';
import { BrowserTabsUtil } from '../utils/BrowserTabsUtil';
import { WorkspaceHandlingTests } from '../tests-library/WorkspaceHandlingTests';
import { Sanitizer } from '../utils/Sanitizer';
import { ApiUrlResolver } from '../utils/workspace/ApiUrlResolver';
import { ITestWorkspaceUtil } from '../utils/workspace/ITestWorkspaceUtil';
import { ProjectAndFileTests } from '../tests-library/ProjectAndFileTests';
import { LoginTests } from '../tests-library/LoginTests';
import { RedHatLoginPage } from '../pageobjects/login/RedHatLoginPage';
import { OcpRedHatLoginPage } from '../pageobjects/login/OcpRedHatLoginPage';

const e2eContainer: Container = new Container({ defaultScope: 'Transient' });

e2eContainer.bind<IDriver>(TYPES.Driver).to(ChromeDriver).inSingletonScope();
e2eContainer.bind<ITestWorkspaceUtil>(TYPES.WorkspaceUtil).to(TestWorkspaceUtil);
e2eContainer.bind<IOcpLoginPage>(TYPES.OcpLogin).to(OcpUserLoginPage);
e2eContainer.bind<IAuthorizationHeaderHandler>(TYPES.IAuthorizationHeaderHandler).to(CheMultiuserAuthorizationHeaderHandler);

if (JSON.parse(TestConstants.TS_SELENIUM_VALUE_OPENSHIFT_OAUTH)) {
    e2eContainer.bind<ICheLoginPage>(TYPES.CheLogin).to(RegularUserOcpCheLoginPage);
}

e2eContainer.bind<BrowserTabsUtil>(CLASSES.BrowserTabsUtil).to(BrowserTabsUtil);
e2eContainer.bind<DriverHelper>(CLASSES.DriverHelper).to(DriverHelper);
e2eContainer.bind<Dashboard>(CLASSES.Dashboard).to(Dashboard);
e2eContainer.bind<Workspaces>(CLASSES.Workspaces).to(Workspaces);
e2eContainer.bind<WorkspaceDetails>(CLASSES.WorkspaceDetails).to(WorkspaceDetails);
e2eContainer.bind<ScreenCatcher>(CLASSES.ScreenCatcher).to(ScreenCatcher);
e2eContainer.bind<OcpLoginPage>(CLASSES.OcpLoginPage).to(OcpLoginPage);
e2eContainer.bind<CheLoginPage>(CLASSES.CheLoginPage).to(CheLoginPage);
e2eContainer.bind<CheApiRequestHandler>(CLASSES.CheApiRequestHandler).to(CheApiRequestHandler);
e2eContainer.bind<CreateWorkspace>(CLASSES.CreateWorkspace).to(CreateWorkspace);
e2eContainer.bind<ProjectAndFileTests>(CLASSES.ProjectAndFileTests).to(ProjectAndFileTests);
e2eContainer.bind<LoginTests>(CLASSES.LoginTests).to(LoginTests);
e2eContainer.bind<Sanitizer>(CLASSES.Sanitizer).to(Sanitizer);
e2eContainer.bind<ApiUrlResolver>(CLASSES.ApiUrlResolver).to(ApiUrlResolver);
e2eContainer.bind<WorkspaceHandlingTests>(CLASSES.WorkspaceHandlingTests).to(WorkspaceHandlingTests);

if (TestConstants.TS_OCP_LOGIN_PAGE_PROVIDER_TITLE === 'DevSandbox') {
    e2eContainer.bind<RedHatLoginPage>(CLASSES.RedHatLoginPage).to(RedHatLoginPage);
    e2eContainer.rebind<ICheLoginPage>(TYPES.CheLogin).to(OcpRedHatLoginPage);
}

export { e2eContainer };
