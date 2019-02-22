/// <reference types="Cypress" />

export class NewWorkspace{
    private static readonly CHE_7_STACK: string = "div[data-stack-id='che7-preview']";
    private static readonly SELECTED_CHE_7_STACK: string = ".stack-selector-item-selected[data-stack-id='che7-preview']"
    private static readonly CREATE_AND_OPEN_BUTTON: string = "che-button-save-flat[che-button-title='Create & Open']>button"

    clickOnChe7Stack(){
        it("Click on \"Che 7\" stack on the \"New Workspace\" page", ()=>{
            cy.get(NewWorkspace.CHE_7_STACK).click();
        })
    }

    waitChe7StackSelected(){
        it("Wait until \"Che 7\" stack on the \"New Workspace\" page is selected", ()=>{
            cy.get(NewWorkspace.SELECTED_CHE_7_STACK);    
        })
    }

    clickOnCreateAndOpenButton(){
        it("Click on \"CREATE & OPEN\" button on the \"New Workspace\" page", ()=>{
            cy.get(NewWorkspace.CREATE_AND_OPEN_BUTTON).first().click();
        })
    }









}