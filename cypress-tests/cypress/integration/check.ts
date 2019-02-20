/// <reference types="Cypress" />


import { LoginPage } from "../pageobjects/LoginPage";
import { TestWorkspace } from "../pageobjects/workspace/TestWorkspace";

const loginPage = new LoginPage();
const testWorkspace: TestWorkspace = new TestWorkspace("wksp-test-workspace");


describe("Go to workspace IDE", ()=>{

   
    // loginPage.visitLoginPage();
    // loginPage.typeUsername("admin");
    // loginPage.typePassword("admin");
    // loginPage.clickOnLoginButton();

    testWorkspace.openWorkspaceIde();



})



