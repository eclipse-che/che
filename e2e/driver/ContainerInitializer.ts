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
import { IDriver } from './IDriver';
import { TYPES, CLASSES } from '../inversify.types';
import { ChromeDriver } from './ChromeDriver';
import { DriverHelper } from '../utils/DriverHelper';
import { ICheLoginPage } from '../pageobjects/login/ICheLoginPage';
import { IOcpLoginPage } from '../pageobjects/login/IOcpLoginPage';
import { SingleUserLoginPage } from '../pageobjects/login/SingleUserLoginPage';
import { Dashboard } from '../pageobjects/dashboard/Dashboard';
import { Workspaces } from '../pageobjects/dashboard/Workspaces';
import { NewWorkspace } from '../pageobjects/dashboard/NewWorkspace';
import { WorkspaceDetails } from '../pageobjects/dashboard/workspace-details/WorkspaceDetails';
import { WorkspaceDetailsPlugins } from '../pageobjects/dashboard/workspace-details/WorkspaceDetailsPlugins';
import { Ide } from '../pageobjects/ide/Ide';
import { TestWorkspaceUtil } from '../utils/workspace/TestWorkspaceUtil';
import { ProjectTree } from '../pageobjects/ide/ProjectTree';
import { Editor } from '../pageobjects/ide/Editor';
import { TopMenu } from '../pageobjects/ide/TopMenu';
import { QuickOpenContainer } from '../pageobjects/ide/QuickOpenContainer';
import { PreviewWidget } from '../pageobjects/ide/PreviewWidget';
import { GitHubPlugin } from '../pageobjects/ide/GitHubPlugin';
import { RightToolbar } from '../pageobjects/ide/RightToolbar';
import { Terminal } from '../pageobjects/ide/Terminal';
import { DebugView } from '../pageobjects/ide/DebugView';
import { WarningDialog } from '../pageobjects/ide/WarningDialog';
import { ScreenCatcher } from '../utils/ScreenCatcher';
import { MultiUserLoginPage } from '../pageobjects/login/MultiUserLoginPage';
import { TestConstants } from '../TestConstants';
import { OcpLoginPage } from '../pageobjects/openshift/OcpLoginPage';
import { OcpWebConsolePage } from '../pageobjects/openshift/OcpWebConsolePage';
import { OcpLoginByTempAdmin } from '../pageobjects/login/OcpLoginByTempAdmin';
import { OpenWorkspaceWidget } from '../pageobjects/ide/OpenWorkspaceWidget';
import { ITestWorkspaceUtil } from '..';

export function getContainer(): Container {
    const e2eContainer = new Container();

    e2eContainer.bind<IDriver>(TYPES.Driver).to(ChromeDriver).inSingletonScope();
    e2eContainer.bind<ITestWorkspaceUtil>(TYPES.WorkspaceUtil).to(TestWorkspaceUtil).inSingletonScope();
    e2eContainer.bind<IOcpLoginPage>(TYPES.OcpLogin).to(OcpLoginByTempAdmin).inSingletonScope();

    if (TestConstants.TS_SELENIUM_MULTIUSER) {
        e2eContainer.bind<ICheLoginPage>(TYPES.CheLogin).to(MultiUserLoginPage).inSingletonScope();
    } else {
        e2eContainer.bind<ICheLoginPage>(TYPES.CheLogin).to(SingleUserLoginPage).inSingletonScope();
    }

    e2eContainer.bind<DriverHelper>(CLASSES.DriverHelper).to(DriverHelper).inSingletonScope();
    e2eContainer.bind<Dashboard>(CLASSES.Dashboard).to(Dashboard).inSingletonScope();
    e2eContainer.bind<Workspaces>(CLASSES.Workspaces).to(Workspaces).inSingletonScope();
    e2eContainer.bind<NewWorkspace>(CLASSES.NewWorkspace).to(NewWorkspace).inSingletonScope();
    e2eContainer.bind<WorkspaceDetails>(CLASSES.WorkspaceDetails).to(WorkspaceDetails).inSingletonScope();
    e2eContainer.bind<WorkspaceDetailsPlugins>(CLASSES.WorkspaceDetailsPlugins).to(WorkspaceDetailsPlugins).inSingletonScope();
    e2eContainer.bind<Ide>(CLASSES.Ide).to(Ide).inSingletonScope();
    e2eContainer.bind<ProjectTree>(CLASSES.ProjectTree).to(ProjectTree).inSingletonScope();
    e2eContainer.bind<Editor>(CLASSES.Editor).to(Editor).inSingletonScope();
    e2eContainer.bind<TopMenu>(CLASSES.TopMenu).to(TopMenu).inSingletonScope();
    e2eContainer.bind<QuickOpenContainer>(CLASSES.QuickOpenContainer).to(QuickOpenContainer).inSingletonScope();
    e2eContainer.bind<PreviewWidget>(CLASSES.PreviewWidget).to(PreviewWidget).inSingletonScope();
    e2eContainer.bind<GitHubPlugin>(CLASSES.GitHubPlugin).to(GitHubPlugin).inSingletonScope();
    e2eContainer.bind<RightToolbar>(CLASSES.RightToolbar).to(RightToolbar).inSingletonScope();
    e2eContainer.bind<Terminal>(CLASSES.Terminal).to(Terminal).inSingletonScope();
    e2eContainer.bind<DebugView>(CLASSES.DebugView).to(DebugView).inSingletonScope();
    e2eContainer.bind<WarningDialog>(CLASSES.WarningDialog).to(WarningDialog).inSingletonScope();
    e2eContainer.bind<ScreenCatcher>(CLASSES.ScreenCatcher).to(ScreenCatcher).inSingletonScope();
    e2eContainer.bind<OcpLoginPage>(CLASSES.OcpLoginPage).to(OcpLoginPage).inSingletonScope();
    e2eContainer.bind<OcpWebConsolePage>(CLASSES.OcpWebConsolePage).to(OcpWebConsolePage).inSingletonScope();
    e2eContainer.bind<OpenWorkspaceWidget>(CLASSES.OpenWorkspaceWidget).to(OpenWorkspaceWidget).inSingletonScope();
    return e2eContainer;
}
