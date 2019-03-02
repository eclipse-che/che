
import { LoginPage } from "../pageobjects/dashboard/LoginPage";
import { TestWorkspace } from "../pageobjects/workspace/TestWorkspace";
import { Dashboard } from "../pageobjects/dashboard/Dashboard";
import { Workspaces } from "../pageobjects/dashboard/Workspaces";
import { NewWorkspace } from "../pageobjects/dashboard/NewWorkspace";
import { Ide } from "../pageobjects/ide/Ide";
import { ProjectTree } from "../pageobjects/ide/ProjectTree";
import { Editor } from "../pageobjects/ide/Editor";
import { ProposalWidget } from "../pageobjects/ide/ProposalWidget";

const workspaceName: string = "wksp-new-workspace";

const loginPage: LoginPage = new LoginPage();
// const testWorkspace: TestWorkspace = new TestWorkspace("wksp-test-workspace");
const dashboard: Dashboard = new Dashboard();
const workspaces: Workspaces = new Workspaces();
const newWorkspace: NewWorkspace = new NewWorkspace();
const ide: Ide = new Ide();
const projectTree: ProjectTree = new ProjectTree();
const editor: Editor = new Editor();
const proposalWidget = new ProposalWidget();



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
    let tabTitle: string = "GreetingController.java";

    it("Expand project and open file in editor", () => {
        projectTree.clickOnItem("web-java-spring")

        projectTree.waitItemColapsed("web-java-spring");
        projectTree.clickOnItem("web-java-spring")

        projectTree.waitItemExpanded("web-java-spring");
        projectTree.clickOnItem(filePath);
    })

    it("Open editor tab", () => {
        editor.waitEditorAvailable(filePath, tabTitle);        

        editor.waitTabDisappearance(filePath + "1111");
        editor.clickOnTab(filePath);

        editor.waitEditorAvailable(filePath, tabTitle);
    })

    it("Perform editor checks", ()=>{
        editor.checkTextPresence("if\\s\\(userName\\s!=\\snull\\)");
        editor.checkTextAbsence("return111");
        editor.checkLineTextContains(12, "public\\sModelAndView\\shandleRequest\\(HttpServletRequest\\srequest");
        editor.checkLineTextAbsence(12, "public\\sModelAndView\\shandleRequest\\(HttpServletRequest\\srequest1111");
    })

    // it("Tab focusing", ()=>{
    //     editor.clickOnTab(filePath);
    //     editor.waitTabFocused(filePath);

    // })

    // it("Go to line", ()=>{
    //     editor.setCursorToLine(4);
    // })

    // it("Suggestion", ()=>{
    


    // })

})





















