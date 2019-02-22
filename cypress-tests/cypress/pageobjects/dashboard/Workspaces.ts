/// <reference types="Cypress" />

export class Workspaces{
    private static readonly TITLE: string = ".che-toolbar-title-label";
    private static readonly ADD_WORKSPACE_BUTTON: string = "#add-item-button";



    clickAddWorkspaceButton(){
        it("Click on \"Add Workspace\" button on \"Workspaces\" page", ()=>{
            cy.get(Workspaces.ADD_WORKSPACE_BUTTON).click();
        })
    }

}
