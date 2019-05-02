/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { e2eContainer } from "../inversify.config";
import { Driver } from "../driver/Driver";
import { TYPES, CLASSES } from "../inversify.types";
import { DriverHelper } from "../utils/DriverHelper";
import { By, WebElementCondition, Condition } from "selenium-webdriver";
import { describe, after, test } from "mocha";
import { LoginPage } from "../pageobjects/login/LoginPage";
import { Dashboard } from "../pageobjects/dashboard/Dashboard";
import { expect, assert } from 'chai'
import { Workspaces } from "../pageobjects/dashboard/Workspaces";
import { NameGenerator } from "../utils/NameGenerator";
import { NewWorkspace } from "../pageobjects/dashboard/NewWorkspace";
import { WorkspaceDetails } from "../pageobjects/dashboard/workspace-details/WorkspaceDetails";
import { WorkspaceDetailsPlugins } from "../pageobjects/dashboard/workspace-details/WorkspaceDetailsPlugins";
import { Request, post, get } from "selenium-webdriver/http";
import { TestWorkspaceUtil } from "../utils/workspace/TestWorkspaceUtil";
import { Ide } from "../pageobjects/ide/Ide";
import { ProjectTree } from "../pageobjects/ide/ProjectTree";
import { Editor } from "../pageobjects/ide/Editor";
import { TestConstants } from "../TestConstants";
import * as mocha from 'mocha'

const workspaceName: string = NameGenerator.generate("wksp-test-", 5);
const namespace: string = "che";
const sampleName: string = "console-java-simple";
const pluginId: string = "org.eclipse.che.vscode-redhat.java";
const javaPluginName: string = "Language Support for Java(TM)";

const driver: Driver = e2eContainer.get<Driver>(TYPES.Driver);
const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
const loginPage: LoginPage = e2eContainer.get<LoginPage>(TYPES.LoginPage);
const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
const workspaces: Workspaces = e2eContainer.get(CLASSES.Workspaces);
const newWorkspace: NewWorkspace = e2eContainer.get(CLASSES.NewWorkspace);
const workspaceDetails: WorkspaceDetails = e2eContainer.get(CLASSES.WorkspaceDetails);
const workspaceDetailsPlugins: WorkspaceDetailsPlugins = e2eContainer.get(CLASSES.WorkspaceDetailsPlugins)
const ide: Ide = e2eContainer.get(CLASSES.Ide)
const projectTree: ProjectTree = e2eContainer.get(CLASSES.ProjectTree)
const editor: Editor = e2eContainer.get(CLASSES.Editor)
const testWorkspaceUtil: TestWorkspaceUtil = e2eContainer.get(CLASSES.TestWorkspaceUtil)


suite("E2E", async () => {

    suite("Login and wait dashboard", async () => {
        test("login", async () => {
            await loginPage.login()
        })
    })

    suite("Create workspace and add plugin", async () => {
        test("Open 'New Workspace' page", async () => {
            await newWorkspace.openPageByUI();
        })

        test(`Create a '${workspaceName}' workspace and proceed editing`, async () => {
            await newWorkspace.createWorkspaceAndProceedEditing(workspaceName, 'che7-preview', sampleName)
        })

        test("Add 'Java Language Support' plugin to workspace", async () => {
            await workspaceDetailsPlugins.addPluginAndOpenWorkspace(namespace, workspaceName, javaPluginName, pluginId)
        })

    })

    suite("Work with IDE", async () => {
        let fileFolderPath: string = `${sampleName}/src/main/java/org/eclipse/che/examples`;
        let tabTitle: string = "HelloWorld.java";
        let filePath: string = `${fileFolderPath}/${tabTitle}`

        test("Wait IDE availability", async () => {
            await ide.waitWorkspaceAndIde(namespace, workspaceName)
        })

        test("Open project tree container", async () => {
            await projectTree.openProjectTreeContainer();
        })

        test("Wait project imported", async () => {
            await projectTree.waitProjectImported(sampleName, "src")
        })

        test("Expand project and open file in editor", async () => {
            await projectTree.expandPathAndOpenFile(fileFolderPath, tabTitle);
        })

        // unskip after resolving issue https://github.com/eclipse/che/issues/12904
        test.skip("Check \"Java Language Server\" initialization by statusbar", async () => {
            await ide.waitStatusBarContains("Starting Java Language Server")
            await ide.waitStatusBarContains("100% Starting Java Language Server")
            await ide.waitStatusBarTextAbcence("Starting Java Language Server")
        })

        // unskip after resolving issue https://github.com/eclipse/che/issues/12904
        test.skip("Check \"Java Language Server\" initialization by suggestion invoking", async () => {
            await editor.waitEditorAvailable(tabTitle);
            await editor.clickOnTab(tabTitle);
            await editor.waitEditorAvailable(tabTitle);
        })

    })

    suite("Stop and remove workspace", async () => {
        test("Stop workspace", async () => {
            await dashboard.stopWorkspaceByUI(workspaceName)
        })

        test("Delete workspace", async () => {
            await dashboard.deleteWorkspaceByUI(workspaceName)
        })

    })

})
