
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
// const testWorkspace: TestWorkspace = new TestWorkspace("wksp-test-workspace");
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

    })

    context("Perform IDE checkings", () => {
        it("Open workspace", () => {
            ide.openIdeWithoutFrames(workspaceName);
            ide.waitIde();
        })

        it("Open project tree container", () => {
            ide.clickOnFilesButton();
            projectTree.waitProjectTreeContainer();
        })

    })


    context("Work with IDE", () => {
        let filePath: string = "web-java-spring/src/main/java/org/eclipse/che/examples/GreetingController.java";
        let tabTitle: string = "GreetingController.java";

        it("Expand project and open file in editor", () => {
            projectTree.clickOnItem("web-java-spring")
            projectTree.waitItemExpanded("web-java-spring");

            projectTree.clickOnItem("web-java-spring/src");
            projectTree.waitItemExpanded("web-java-spring/src");

            projectTree.clickOnItem("web-java-spring/src/main/java");
            projectTree.waitItemExpanded("web-java-spring/src/main/java");

            projectTree.clickOnItem(filePath);
        })

        it("statusbar", () => {
            ide.waitStatusBarContains("Starting Java Language Server")
            ide.waitStatusBarContains("100% Starting Java Language Server")
            ide.waitStatusBarTextAbcence("Starting Java Language Server")
        })

        it("Open editor tab", () => {
            editor.waitEditorAvailable(filePath, tabTitle);

            editor.waitTabDisappearance(filePath + "1111");
            editor.clickOnTab(filePath);

            editor.waitEditorAvailable(filePath, tabTitle);
        })

        it("Perform editor checks", () => {
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






















