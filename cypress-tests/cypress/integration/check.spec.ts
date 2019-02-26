
import { LoginPage } from "../pageobjects/LoginPage";
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

describe("Prepare dashboard", ()=>{
    it("Open dashboard", ()=>{
        dashboard.openDashboard();
    })

})

describe("Create workspace and open it in IDE", ()=>{

    it("Go to \"New Workspace\" page", ()=>{
        dashboard.clickWorkspacesButton(); 
        workspaces.clickAddWorkspaceButton();
    })
    
    it(`Create a \"${workspaceName}\" workspace`, ()=>{
        newWorkspace.typeWorkspaceName(workspaceName);
        newWorkspace.clickOnChe7Stack();
        newWorkspace.waitChe7StackSelected();
        newWorkspace.clickOnAddOrImportProjectButton();
        newWorkspace.enableWebJavaSpringCheckbox();
        newWorkspace.clickOnAddButton();
        newWorkspace.clickOnCreateAndOpenButton();    
    })


//     ide.openIdeWithoutFrames(workspaceName);
//     ide.waitIde();

//     ide.clickOnFilesButton();
//     projectTree.waitProjectTreeContainer();

// })

// describe("Work with IDE", ()=>{
    
//     it ("Open workspace", ()=>{
//         cy.visit("http://route24jd46nt-eclipse-che.172.19.20.205.nip.io/#/projects")
//     })


//     ide.waitIde();
//     ide.clickOnFilesButton();
//     projectTree.waitProjectTreeContainer();


//     // projectTree.clickOnItem("web-java-spring")
//     projectTree.waitItemExpanded("web-java-spring");
//     projectTree.waitItemColapsed("web-java-spring/src");

//     projectTree.clickOnItem("web-java-spring/src")
//     projectTree.waitItemExpanded("web-java-spring/src");
//     projectTree.waitItemExpanded("web-java-spring/src/main");
//     projectTree.waitItemColapsed("web-java-spring/src/main/java");


//     projectTree.clickOnItem("web-java-spring/src/main/java")
//     projectTree.waitItemExpanded("web-java-spring/src/main/java");
//     projectTree.waitItemExpanded("web-java-spring/src/main/java/org");
//     projectTree.waitItemExpanded("web-java-spring/src/main/java/org/eclipse");
//     projectTree.waitItemExpanded("web-java-spring/src/main/java/org/eclipse/che");
//     projectTree.waitItemExpanded("web-java-spring/src/main/java/org/eclipse/che/examples");


//     let filePath: string = "web-java-spring/src/main/java/org/eclipse/che/examples/GreetingController.java";



//     projectTree.clickOnItem(filePath);
//     editor.waitTab(filePath, "GreetingController.java");
//     editor.waitTabDisappearance(filePath + "1111");


//     editor.clickOnTab(filePath);
//     editor.checkText("public class");
    // editor.waitTabFocused(filePath);


    // editor.closeTab(filePath);
    // editor.waitTabDisappearance(filePath);

})



















