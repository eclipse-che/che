/// <reference types="Cypress" />


export class Ide{

    private static readonly ROOT_URL: string = Cypress.env("root_url");
    private static readonly LOAD_PAGE_TIMEOUT:number = Cypress.env("load_page_timeout");

    private static readonly TOP_MENU_PANEL:string = "#theia-app-shell #theia-top-panel .p-MenuBar-content";
    private static readonly LEFT_CONTENT_PANEL: string = "#theia-left-content-panel";
    private static readonly FILES_BUTTON: string = "#theia-left-content-panel li[title='Files']";

    private static readonly IDE_IFRAME: string = "iframe[id='ide-application-iframe']";

    openIdeWithoutFrames(workspaceName: string){
        let workspaceUrl: string = `${Ide.ROOT_URL}/che/${workspaceName}`
        it("Open IDE without iframes by workspace straight url", ()=>{
            cy.visit(workspaceUrl);
        })
    }

    waitFilesButton() {
        it("Wait \"Files\" button in the left content panel", ()=>{
            cy.get(Ide.FILES_BUTTON, {timeout: Ide.LOAD_PAGE_TIMEOUT})
            .first()
            .should('be.visible', {timeout: Ide.LOAD_PAGE_TIMEOUT});
        })
    }

    clickOnFilesButton(){
        it("Click on \"Files\" button in the left content panel", ()=>{
            cy.get(Ide.FILES_BUTTON)
            .first()
            .click();
        })
    }

    waitTopMenuPanel(){
        it("Wait top menu panel", ()=>{
            cy.get(Ide.TOP_MENU_PANEL, {timeout: Ide.LOAD_PAGE_TIMEOUT})
            .should('be.visible', {timeout: Ide.LOAD_PAGE_TIMEOUT});
        })
    }

    waitLeftContentPanel(){
        it("Wait left content panel", ()=>{
            cy.get(Ide.LEFT_CONTENT_PANEL, {timeout: Ide.LOAD_PAGE_TIMEOUT})
            .should('be.visible', {timeout: Ide.LOAD_PAGE_TIMEOUT});
        })
    }

    waitIde(){
        this.waitTopMenuPanel();
        this.waitLeftContentPanel();
        this.waitFilesButton();
    }







}
