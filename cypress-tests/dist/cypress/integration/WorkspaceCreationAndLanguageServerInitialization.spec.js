/*********************************************************************
 * Copyright (c) 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
/**
 * In this testcase is implemented next scenario:
 *
 * 1. Open dashboard
 * 2. Create new workspace using dashboard and open it
 * 3. Wait IDE and open file
 * 4. Check Language Server initialization
 * 5. Delete workspace using dashboard
 */
import { LoginPage } from "../pageobjects/dashboard/LoginPage";
import { Dashboard } from "../pageobjects/dashboard/Dashboard";
import { Workspaces } from "../pageobjects/dashboard/Workspaces";
import { NewWorkspace } from "../pageobjects/dashboard/NewWorkspace";
import { Ide } from "../pageobjects/ide/Ide";
import { ProjectTree } from "../pageobjects/ide/ProjectTree";
import { Editor } from "../pageobjects/ide/Editor";
import { NameGenerator } from "../utils/NameGenerator";
var workspaceName = NameGenerator.generate("wksp-test-", 5);
var namespace = "che";
var sampleName = "console-java-simple";
var loginPage = new LoginPage();
var dashboard = new Dashboard();
var workspaces = new Workspaces();
var newWorkspace = new NewWorkspace();
var ide = new Ide();
var projectTree = new ProjectTree();
var editor = new Editor();
describe("E2E test", function () {
    context("Prepare dashboard", function () {
        it("Open dashboard", function () {
            dashboard.openDashboard();
            dashboard.waitLoaderPage();
            dashboard.waitLoaderPageAbcence();
            dashboard.waitDashboard();
        });
    });
    context("Create workspace and open it in IDE", function () {
        it("Go to \"New Workspace\" page", function () {
            dashboard.clickWorkspacesButton();
            workspaces.clickAddWorkspaceButton();
        });
        it("Create a \"" + workspaceName + "\" workspace", function () {
            var javaPluginName = "Language Support for Java(TM)";
            newWorkspace.typeWorkspaceName(workspaceName);
            newWorkspace.clickOnChe7Stack();
            newWorkspace.waitChe7StackSelected();
            newWorkspace.clickOnAddOrImportProjectButton();
            newWorkspace.enableSampleCheckbox(sampleName);
            newWorkspace.clickOnAddButton();
            newWorkspace.waitProjectAdding(sampleName);
            newWorkspace.waitPluginListItem(javaPluginName);
            newWorkspace.waitPluginDisabling(javaPluginName);
            newWorkspace.clickOnPluginListItemSwitcher(javaPluginName);
            newWorkspace.waitPluginEnabling(javaPluginName);
            newWorkspace.clickOnCreateAndOpenButton();
        });
        it("Wait IDE availability", function () {
            ide.waitWorkspaceAndIdeInIframe(namespace, workspaceName);
            ide.openIdeWithoutFrames(workspaceName);
            ide.waitWorkspaceAndIde(namespace, workspaceName);
        });
    });
    context("Work with IDE", function () {
        var fileFolderPath = sampleName + "/src/main/java/org/eclipse/che/examples";
        var tabTitle = "HelloWorld.java";
        var filePath = fileFolderPath + "/" + tabTitle;
        it("Open project tree container", function () {
            projectTree.openProjectTreeContainer();
            projectTree.waitProjectTreeContainer();
            projectTree.waitProjectImported(sampleName, "src");
        });
        it("Expand project and open file in editor", function () {
            projectTree.expandPathAndOpenFile(fileFolderPath, tabTitle);
        });
        //unskip after resolving issue https://github.com/eclipse/che/issues/12904
        it.skip("Check \"Java Language Server\" initialization by statusbar", function () {
            ide.waitStatusBarContains("Starting Java Language Server");
            ide.waitStatusBarContains("100% Starting Java Language Server");
            ide.waitStatusBarTextAbcence("Starting Java Language Server");
        });
        //unskip after resolving issue https://github.com/eclipse/che/issues/12904
        it.skip("Check \"Java Language Server\" initialization by suggestion invoking", function () {
            editor.waitEditorAvailable(filePath, tabTitle);
            editor.clickOnTab(filePath);
            editor.waitEditorAvailable(filePath, tabTitle);
            editor.setCursorToLineAndChar(15, 33);
            editor.performControlSpaceCombination();
            editor.waitSuggestionContainer();
            editor.waitSuggestion("getContentType()");
        });
    });
    context("Stop and remove workspace", function () {
        it("Stop workspace", function () {
            dashboard.openDashboard();
            dashboard.clickWorkspacesButton();
            workspaces.waitPage();
            workspaces.waitWorkspaceListItem(workspaceName);
            workspaces.waitWorkspaceWithRunningStatus(workspaceName);
            workspaces.clickOnStopWorkspaceButton(workspaceName);
            workspaces.waitWorkspaceWithStoppedStatus(workspaceName);
        });
        it("Delete workspace", function () {
            workspaces.waitPage();
            workspaces.waitWorkspaceListItem(workspaceName);
            workspaces.clickWorkspaceListItem(workspaceName);
            workspaces.clickDeleteButtonOnWorkspaceDetails();
            workspaces.clickConfirmDeletionButton();
            workspaces.waitPage();
            workspaces.waitWorkspaceListItemAbcence(workspaceName);
        });
    });
});
