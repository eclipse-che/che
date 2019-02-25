
import { LoginPage } from "../pageobjects/LoginPage";
import { TestWorkspace } from "../pageobjects/workspace/TestWorkspace";
import { Dashboard } from "../pageobjects/dashboard/Dashboard";
import { Workspaces } from "../pageobjects/dashboard/Workspaces";
import { NewWorkspace } from "../pageobjects/dashboard/NewWorkspace";
import { Ide } from "../pageobjects/ide/Ide";
import { ProjectTree } from "../pageobjects/ide/ProjectTree";

const workspaceName: string = "wksp-new-workspace";

const loginPage: LoginPage = new LoginPage();
// const testWorkspace: TestWorkspace = new TestWorkspace("wksp-test-workspace");
const dashboard: Dashboard = new Dashboard();
const workspaces: Workspaces = new Workspaces();
const newWorkspace: NewWorkspace = new NewWorkspace();
const ide: Ide = new Ide();
const projectTree: ProjectTree = new ProjectTree();

// describe("Prepare dashboard", ()=>{
//     dashboard.openDashboard();

// })

// describe("Create workspace and open it in IDE", ()=>{

//     dashboard.clickWorkspacesButton(); 
//     workspaces.clickAddWorkspaceButton();
//     newWorkspace.typeWorkspaceName(workspaceName);
//     newWorkspace.clickOnChe7Stack();
//     newWorkspace.waitChe7StackSelected();
//     newWorkspace.clickOnAddOrImportProjectButton();
//     newWorkspace.enableWebJavaSpringCheckbox();
//     newWorkspace.clickOnAddButton();
//     newWorkspace.clickOnCreateAndOpenButton();


//     ide.openIdeWithoutFrames(workspaceName);
//     ide.waitIde();

//     ide.clickOnFilesButton();
//     projectTree.waitProjectTreeContainer();

// })

describe("Work with IDE", ()=>{
    
    it ("Open workspace", ()=>{
        cy.visit("http://routegrstamky-eclipse-che.172.19.20.205.nip.io/#/projects")
    })


    ide.waitIde();
    ide.clickOnFilesButton();
    projectTree.waitProjectTreeContainer();


    // projectTree.clickOnItem("web-java-spring")
    projectTree.waitItemExpanded("web-java-spring");
    projectTree.waitItemColapsed("web-java-spring/src");

    projectTree.clickOnItem("web-java-spring/src")
    projectTree.waitItemExpanded("web-java-spring/src");
    projectTree.waitItemExpanded("web-java-spring/src/main");
    projectTree.waitItemColapsed("web-java-spring/src/main/java");


    projectTree.clickOnItem("web-java-spring/src/main/java")
    projectTree.waitItemExpanded("web-java-spring/src/main/java");
    projectTree.waitItemExpanded("web-java-spring/src/main/java/org");
    projectTree.waitItemExpanded("web-java-spring/src/main/java/org/eclipse");
    projectTree.waitItemExpanded("web-java-spring/src/main/java/org/eclipse/che");
    projectTree.waitItemExpanded("web-java-spring/src/main/java/org/eclipse/che/examples");


    projectTree.clickOnItem("web-java-spring/src/main/java/org/eclipse/che/examples/GreetingController.java")

})













