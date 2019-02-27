
import { LoginPage } from "../pageobjects/dashboard/LoginPage";
import { TestWorkspace } from "../pageobjects/workspace/TestWorkspace";
import { Dashboard } from "../pageobjects/dashboard/Dashboard";
import { Workspaces } from "../pageobjects/dashboard/Workspaces";
import { NewWorkspace } from "../pageobjects/dashboard/NewWorkspace";
import { Ide } from "../pageobjects/ide/Ide";
import { ProjectTree } from "../pageobjects/ide/ProjectTree";
import { Editor } from "../pageobjects/ide/Editor";

const workspaceName: string = "wksp-new-workspace";

const loginPage: LoginPage = new LoginPage();
// const testWorkspace: TestWorkspace = new TestWorkspace("wksp-test-workspace");
const dashboard: Dashboard = new Dashboard();
const workspaces: Workspaces = new Workspaces();
const newWorkspace: NewWorkspace = new NewWorkspace();
const ide: Ide = new Ide();
const projectTree: ProjectTree = new ProjectTree();
const editor: Editor = new Editor();

// describe("Prepare dashboard", ()=>{
//     it("Open dashboard", ()=>{
//         dashboard.openDashboard();
//     })

// })

// describe("Create workspace and open it in IDE", ()=>{

//     it("Go to \"New Workspace\" page", ()=>{
//         dashboard.clickWorkspacesButton(); 
//         workspaces.clickAddWorkspaceButton();
//     })

//     it(`Create a \"${workspaceName}\" workspace`, ()=>{
//         newWorkspace.typeWorkspaceName(workspaceName);
//         newWorkspace.clickOnChe7Stack();
//         newWorkspace.waitChe7StackSelected();
//         newWorkspace.clickOnAddOrImportProjectButton();
//         newWorkspace.enableWebJavaSpringCheckbox();
//         newWorkspace.clickOnAddButton();
//         newWorkspace.clickOnCreateAndOpenButton();    
//     })

// })

describe("Perform IDE checkings", () => {
    it("Open workspace", () => {
        ide.openIdeWithoutFrames(workspaceName);
        ide.waitIde();
        cy.wait(3000);
    })

    it("Open project tree container", () => {
        ide.clickOnFilesButton();
        projectTree.waitProjectTreeContainerClosed();

        ide.clickOnFilesButton();
        projectTree.waitProjectTreeContainer();
    })

})





describe("Work with IDE", () => {
    let filePath: string = "web-java-spring/src/main/java/org/eclipse/che/examples/GreetingController.java";

    it("Expand project and open file in editor", () => {
        projectTree.clickOnItem("web-java-spring")

        projectTree.waitItemColapsed("web-java-spring");
        projectTree.clickOnItem("web-java-spring")

        projectTree.waitItemExpanded("web-java-spring");
        projectTree.clickOnItem(filePath);
    })

    it("Work with editor", () => {
        editor.waitTab(filePath, "GreetingController.java");
        editor.waitTabDisappearance(filePath + "1111");


        editor.clickOnTab(filePath);

        editor.checkTextPresence("if\\s\\(userName\\s!=\\snull\\)");
        editor.checkTextAbsence("return111");
    })

})





















