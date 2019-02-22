/// <reference types="Cypress" />


import { LoginPage } from "../pageobjects/LoginPage";
import { TestWorkspace } from "../pageobjects/workspace/TestWorkspace";
import { Dashboard } from "../pageobjects/dashboard/Dashboard";
import { Workspaces } from "../pageobjects/dashboard/Workspaces";
import { NewWorkspace } from "../pageobjects/dashboard/NewWorkspace";

const loginPage = new LoginPage();
// const testWorkspace: TestWorkspace = new TestWorkspace("wksp-test-workspace");
const dashboard: Dashboard = new Dashboard();
const workspaces: Workspaces = new Workspaces();
const newWorkspace: NewWorkspace = new NewWorkspace();


describe("Prepare dashboard", ()=>{
    dashboard.openDashboard();

})

describe("Create workspace and open it in IDE", ()=>{

    dashboard.clickWorkspacesButton(); 
    workspaces.clickAddWorkspaceButton();
    newWorkspace.clickOnChe7Stack();
    newWorkspace.waitChe7StackSelected();
    newWorkspace.clickOnCreateAndOpenButton();

})



