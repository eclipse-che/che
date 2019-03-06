
import { LoginPage } from "../pageobjects/dashboard/LoginPage";
import { TestWorkspace } from "../pageobjects/workspace/TestWorkspace";
import { Dashboard } from "../pageobjects/dashboard/Dashboard";
import { Workspaces } from "../pageobjects/dashboard/Workspaces";
import { NewWorkspace } from "../pageobjects/dashboard/NewWorkspace";
import { Ide } from "../pageobjects/ide/Ide";
import { ProjectTree } from "../pageobjects/ide/ProjectTree";
import { Editor } from "../pageobjects/ide/Editor";
import { ProposalWidget } from "../pageobjects/ide/ProposalWidget";
import { NameGenerator } from "../pageobjects/workspace/NameGenerator";

const workspaceName: string = NameGenerator.generate("wksp-test-", 5);

const loginPage: LoginPage = new LoginPage();
const dashboard: Dashboard = new Dashboard();
const workspaces: Workspaces = new Workspaces();
const newWorkspace: NewWorkspace = new NewWorkspace();
const ide: Ide = new Ide();
const projectTree: ProjectTree = new ProjectTree();
const editor: Editor = new Editor();
const proposalWidget = new ProposalWidget();


describe("E2E test", () => {

    context("Prepare dashboard", () => {
        it("Open dashboard", () => {
            dashboard.openDashboard();
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
            newWorkspace.enableWebJavaSpringCheckbox();
            newWorkspace.clickOnAddButton();
            newWorkspace.clickOnCreateAndOpenButton();
        })

        it("Wait IDE availability", () => {
            ide.openIdeWithoutFrames(workspaceName);
            ide.waitIde();
        })

    })

    context("Work with IDE", () => {
        let fileFolderPath: string = "web-java-spring/src/main/java/org/eclipse/che/examples";
        let tabTitle: string = "GreetingController.java";
        let filePath: string = `${fileFolderPath}/${tabTitle}`

        it("Open project tree container", () => {
            projectTree.openProjectTreeContainer();
            projectTree.waitProjectTreeContainer();
        })

        it("Expand project and open file in editor", () => {
            projectTree.expandPathAndOpenFile(fileFolderPath, tabTitle);
        })

        it("Check \"Java Language Server\" initialization by statusbar", () => {
            ide.waitStatusBarContains("Starting Java Language Server")
            ide.waitStatusBarContains("100% Starting Java Language Server")
            ide.waitStatusBarTextAbcence("Starting Java Language Server")
        })

        it("Check \"Java Language Server\" initialization by suggestion invoking", () => {
            editor.waitEditorAvailable(filePath, tabTitle);
            editor.clickOnTab(filePath);
            editor.waitEditorAvailable(filePath, tabTitle);

            editor.setCursorToLineAndChar(15, 33);
            editor.performControlSpaceCombination();
            editor.waitSuggestionContainer();
            editor.waitSuggestion("getContentType()");
        })

    })

    context("Delete workspace", () => {
        it("Delete workspace", () => {
            dashboard.openDashboard()
            dashboard.clickWorkspacesButton()
            workspaces.waitPage()
            workspaces.clickWorkspaceListItem(workspaceName);
            workspaces.clickDeleteButtonOnWorkspaceDetails();
            workspaces.clickConfirmDeletionButton();
            workspaces.waitPage()
            workspaces.waitWorkspaceListItemAbcence(workspaceName);
        })
    })


})






















