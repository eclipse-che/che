"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
const inversify_1 = require("inversify");
const inversify_types_1 = require("../inversify.types");
const ChromeDriver_1 = require("./ChromeDriver");
const DriverHelper_1 = require("../utils/DriverHelper");
const SingleUserLoginPage_1 = require("../pageobjects/login/SingleUserLoginPage");
const Dashboard_1 = require("../pageobjects/dashboard/Dashboard");
const Workspaces_1 = require("../pageobjects/dashboard/Workspaces");
const NewWorkspace_1 = require("../pageobjects/dashboard/NewWorkspace");
const WorkspaceDetails_1 = require("../pageobjects/dashboard/workspace-details/WorkspaceDetails");
const WorkspaceDetailsPlugins_1 = require("../pageobjects/dashboard/workspace-details/WorkspaceDetailsPlugins");
const Ide_1 = require("../pageobjects/ide/Ide");
const TestWorkspaceUtil_1 = require("../utils/workspace/TestWorkspaceUtil");
const ProjectTree_1 = require("../pageobjects/ide/ProjectTree");
const Editor_1 = require("../pageobjects/ide/Editor");
const TopMenu_1 = require("../pageobjects/ide/TopMenu");
const QuickOpenContainer_1 = require("../pageobjects/ide/QuickOpenContainer");
const PreviewWidget_1 = require("../pageobjects/ide/PreviewWidget");
const GitHubPlugin_1 = require("../pageobjects/ide/GitHubPlugin");
const RightToolbar_1 = require("../pageobjects/ide/RightToolbar");
const Terminal_1 = require("../pageobjects/ide/Terminal");
const DebugView_1 = require("../pageobjects/ide/DebugView");
const DialogWindow_1 = require("../pageobjects/ide/DialogWindow");
const ScreenCatcher_1 = require("../utils/ScreenCatcher");
const MultiUserLoginPage_1 = require("../pageobjects/login/MultiUserLoginPage");
const TestConstants_1 = require("../TestConstants");
const OcpLoginPage_1 = require("../pageobjects/openshift/OcpLoginPage");
const OcpWebConsolePage_1 = require("../pageobjects/openshift/OcpWebConsolePage");
const OcpLoginByTempAdmin_1 = require("../pageobjects/login/OcpLoginByTempAdmin");
const OpenWorkspaceWidget_1 = require("../pageobjects/ide/OpenWorkspaceWidget");
const ContextMenu_1 = require("../pageobjects/ide/ContextMenu");
function getContainer() {
    const e2eContainer = new inversify_1.Container();
    e2eContainer.bind(inversify_types_1.TYPES.Driver).to(ChromeDriver_1.ChromeDriver).inSingletonScope();
    e2eContainer.bind(inversify_types_1.TYPES.WorkspaceUtil).to(TestWorkspaceUtil_1.TestWorkspaceUtil).inSingletonScope();
    e2eContainer.bind(inversify_types_1.TYPES.OcpLogin).to(OcpLoginByTempAdmin_1.OcpLoginByTempAdmin).inSingletonScope();
    if (TestConstants_1.TestConstants.TS_SELENIUM_MULTIUSER) {
        e2eContainer.bind(inversify_types_1.TYPES.CheLogin).to(MultiUserLoginPage_1.MultiUserLoginPage).inSingletonScope();
    }
    else {
        e2eContainer.bind(inversify_types_1.TYPES.CheLogin).to(SingleUserLoginPage_1.SingleUserLoginPage).inSingletonScope();
    }
    e2eContainer.bind(inversify_types_1.CLASSES.ContextMenu).to(ContextMenu_1.ContextMenu).inSingletonScope();
    e2eContainer.bind(inversify_types_1.CLASSES.DriverHelper).to(DriverHelper_1.DriverHelper).inSingletonScope();
    e2eContainer.bind(inversify_types_1.CLASSES.Dashboard).to(Dashboard_1.Dashboard).inSingletonScope();
    e2eContainer.bind(inversify_types_1.CLASSES.Workspaces).to(Workspaces_1.Workspaces).inSingletonScope();
    e2eContainer.bind(inversify_types_1.CLASSES.NewWorkspace).to(NewWorkspace_1.NewWorkspace).inSingletonScope();
    e2eContainer.bind(inversify_types_1.CLASSES.WorkspaceDetails).to(WorkspaceDetails_1.WorkspaceDetails).inSingletonScope();
    e2eContainer.bind(inversify_types_1.CLASSES.WorkspaceDetailsPlugins).to(WorkspaceDetailsPlugins_1.WorkspaceDetailsPlugins).inSingletonScope();
    e2eContainer.bind(inversify_types_1.CLASSES.Ide).to(Ide_1.Ide).inSingletonScope();
    e2eContainer.bind(inversify_types_1.CLASSES.ProjectTree).to(ProjectTree_1.ProjectTree).inSingletonScope();
    e2eContainer.bind(inversify_types_1.CLASSES.Editor).to(Editor_1.Editor).inSingletonScope();
    e2eContainer.bind(inversify_types_1.CLASSES.TopMenu).to(TopMenu_1.TopMenu).inSingletonScope();
    e2eContainer.bind(inversify_types_1.CLASSES.QuickOpenContainer).to(QuickOpenContainer_1.QuickOpenContainer).inSingletonScope();
    e2eContainer.bind(inversify_types_1.CLASSES.PreviewWidget).to(PreviewWidget_1.PreviewWidget).inSingletonScope();
    e2eContainer.bind(inversify_types_1.CLASSES.GitHubPlugin).to(GitHubPlugin_1.GitHubPlugin).inSingletonScope();
    e2eContainer.bind(inversify_types_1.CLASSES.RightToolbar).to(RightToolbar_1.RightToolbar).inSingletonScope();
    e2eContainer.bind(inversify_types_1.CLASSES.Terminal).to(Terminal_1.Terminal).inSingletonScope();
    e2eContainer.bind(inversify_types_1.CLASSES.DebugView).to(DebugView_1.DebugView).inSingletonScope();
    e2eContainer.bind(inversify_types_1.CLASSES.WarningDialog).to(DialogWindow_1.DialogWindow).inSingletonScope();
    e2eContainer.bind(inversify_types_1.CLASSES.ScreenCatcher).to(ScreenCatcher_1.ScreenCatcher).inSingletonScope();
    e2eContainer.bind(inversify_types_1.CLASSES.OcpLoginPage).to(OcpLoginPage_1.OcpLoginPage).inSingletonScope();
    e2eContainer.bind(inversify_types_1.CLASSES.OcpWebConsolePage).to(OcpWebConsolePage_1.OcpWebConsolePage).inSingletonScope();
    e2eContainer.bind(inversify_types_1.CLASSES.OpenWorkspaceWidget).to(OpenWorkspaceWidget_1.OpenWorkspaceWidget).inSingletonScope();
    return e2eContainer;
}
exports.getContainer = getContainer;
//# sourceMappingURL=ContainerInitializer.js.map