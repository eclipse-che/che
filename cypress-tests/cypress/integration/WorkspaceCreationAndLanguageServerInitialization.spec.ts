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
import { Dashboard } from "../pageobjects/dashboard/Dashboard";
import { Workspaces } from "../pageobjects/dashboard/Workspaces";
import { NewWorkspace } from "../pageobjects/dashboard/NewWorkspace";
import { Ide } from "../pageobjects/ide/Ide";
import { ProjectTree } from "../pageobjects/ide/ProjectTree";
import { Editor } from "../pageobjects/ide/Editor";
import { NameGenerator } from "../utils/NameGenerator";
import { ILoginPage } from "../pageobjects/dashboard/interfaces/ILoginPage";
import { e2eContainer } from "../inversifyJS/inversify.config";
import { TYPES } from "../inversifyJS/types";
import { WorkspaceDetails } from "../pageobjects/dashboard/workspace-details/WorkspaceDetails";
import { WorkspaceDetailsPlugins } from "../pageobjects/dashboard/workspace-details/WorkspaceDetailsPlugins";

const workspaceName: string = NameGenerator.generate("wksp-test-", 5);
const namespace: string = "che";
const sampleName: string = "console-java-simple";

const loginPage: ILoginPage = e2eContainer.get<ILoginPage>(TYPES.ILoginPage);
const dashboard: Dashboard = new Dashboard();
const workspaces: Workspaces = new Workspaces();
const newWorkspace: NewWorkspace = new NewWorkspace();
const ide: Ide = new Ide();
const projectTree: ProjectTree = new ProjectTree();
const editor: Editor = new Editor();
const workspaceDetails: WorkspaceDetails = new WorkspaceDetails();
const workspaceDetailsPlugins: WorkspaceDetailsPlugins = new WorkspaceDetailsPlugins();


describe("E2E test", () => {

    context("Prepare dashboard", () => {
        it("Open dashboard", () => {
            loginPage.login();
            dashboard.waitLoaderPage();
            dashboard.waitLoaderPageAbcence()
            dashboard.waitDashboard();
        })

    })

    context("Create workspace and open it in IDE", () => {

        it("Go to \"New Workspace\" page", () => {
            dashboard.clickWorkspacesButton();
            workspaces.clickAddWorkspaceButton();
        })

        it(`Create a \"${workspaceName}\" workspace`, () => {
            newWorkspace.typeWorkspaceName(workspaceName);
            newWorkspace.clickOnChe7Stack();
            newWorkspace.waitChe7StackSelected();
            newWorkspace.clickOnAddOrImportProjectButton();
            newWorkspace.enableSampleCheckbox(sampleName);
            newWorkspace.clickOnAddButton();
            newWorkspace.waitProjectAdding(sampleName);

            newWorkspace.selectCreateWorkspaceAndProceedEditing();
        })

        it("Add 'Java Language Support' plugin to workspace", () => {
            const javaPluginName: string = "Language Support for Java(TM)";
            const execPlugin: string = "Che machine-exec Service";

            workspaceDetails.waitPage(workspaceName);
            workspaceDetails.waitTabSelected('Overview')
            workspaceDetails.clickOnTab('Plugins')
            workspaceDetails.waitTabSelected('Plugins')

            workspaceDetailsPlugins.waitPluginEnabling(execPlugin)
            workspaceDetailsPlugins.waitPluginDisabling(javaPluginName)
            workspaceDetailsPlugins.clickOnPluginListItemSwitcher(javaPluginName)
            workspaceDetailsPlugins.waitPluginEnabling(javaPluginName)

            workspaceDetails.waitSaveButton()
            workspaceDetails.clickOnSaveButton()
            workspaceDetails.waitSaveButtonDisappearance()

            workspaceDetails.clickOnOpenButton()
        })


        it("Wait IDE availability", () => {
            ide.waitWorkspaceAndIdeInIframe(namespace, workspaceName);
            ide.openIdeWithoutFrames(workspaceName);
            ide.waitWorkspaceAndIde(namespace, workspaceName);
        })

    })

    context("Work with IDE", () => {
        let fileFolderPath: string = `${sampleName}/src/main/java/org/eclipse/che/examples`;
        let tabTitle: string = "HelloWorld.java";
        let filePath: string = `${fileFolderPath}/${tabTitle}`

        it("Open project tree container", () => {
            projectTree.openProjectTreeContainer();
            projectTree.waitProjectTreeContainer();
            projectTree.waitProjectImported(sampleName, "src")
        })

        it("Expand project and open file in editor", () => {
            projectTree.expandPathAndOpenFile(fileFolderPath, tabTitle);
        })

        //unskip after resolving issue https://github.com/eclipse/che/issues/12904
        it.skip("Check \"Java Language Server\" initialization by statusbar", () => {
            ide.waitStatusBarContains("Starting Java Language Server")
            ide.waitStatusBarContains("100% Starting Java Language Server")
            ide.waitStatusBarTextAbcence("Starting Java Language Server")
        })

        //unskip after resolving issue https://github.com/eclipse/che/issues/12904
        it.skip("Check \"Java Language Server\" initialization by suggestion invoking", () => {
            editor.waitEditorAvailable(filePath, tabTitle);
            editor.clickOnTab(filePath);
            editor.waitEditorAvailable(filePath, tabTitle);

            editor.setCursorToLineAndChar(15, 33);
            editor.performControlSpaceCombination();
            editor.waitSuggestionContainer();
            editor.waitSuggestion("getContentType()");
        })

    })

    context("Stop and remove workspace", () => {
        it("Stop workspace", () => {
            dashboard.openDashboard()
            dashboard.clickWorkspacesButton()
            workspaces.waitPage()
            workspaces.waitWorkspaceListItem(workspaceName)
            workspaces.waitWorkspaceWithRunningStatus(workspaceName)
            workspaces.clickOnStopWorkspaceButton(workspaceName)
            workspaces.waitWorkspaceWithStoppedStatus(workspaceName)
        })

        it("Delete workspace", () => {
            workspaces.waitPage()
            workspaces.waitWorkspaceListItem(workspaceName)
            workspaces.clickWorkspaceListItem(workspaceName);
            workspaces.clickDeleteButtonOnWorkspaceDetails();
            workspaces.clickConfirmDeletionButton();
            workspaces.waitPage()
            workspaces.waitWorkspaceListItemAbcence(workspaceName);
        })
    })

})
