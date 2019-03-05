/// <reference types="Cypress" />


export class Ide {

    private static readonly ROOT_URL: string = Cypress.env("root_url");
    private static readonly LOAD_PAGE_TIMEOUT: number = Cypress.env("load_page_timeout");
    private static readonly LANGUAGE_SERVER_INITIALIZATION_TIMEOUT: number = Cypress.env("language_server_initialization_timeout");

    private static readonly TOP_MENU_PANEL: string = "#theia-app-shell #theia-top-panel .p-MenuBar-content";
    private static readonly LEFT_CONTENT_PANEL: string = "#theia-left-content-panel";
    private static readonly FILES_BUTTON: string = ".theia-app-left .p-TabBar-content li[title='Files']";
    private static readonly PRELOADER: string = ".theia-preload";

    private static readonly IDE_IFRAME: string = "iframe[id='ide-application-iframe']";

    openIdeWithoutFrames(workspaceName: string) {
        let workspaceUrl: string = `${Ide.ROOT_URL}/che/${workspaceName}`

        cy.visit(workspaceUrl);
    }

    waitFilesButton() {
        cy.get(Ide.FILES_BUTTON, { timeout: Ide.LOAD_PAGE_TIMEOUT })
            .should('be.visible');
    }

    clickOnFilesButton() {
        cy.get(Ide.FILES_BUTTON)
            .first()
            .click();
    }

    waitTopMenuPanel() {
        cy.get(Ide.TOP_MENU_PANEL, { timeout: Ide.LOAD_PAGE_TIMEOUT })
            .should('be.visible');
    }

    waitLeftContentPanel() {
        cy.get(Ide.LEFT_CONTENT_PANEL, { timeout: Ide.LOAD_PAGE_TIMEOUT })
            .should('be.visible');
    }

    waitPreloaderAbsent() {
        cy.get(Ide.PRELOADER, { timeout: Ide.LOAD_PAGE_TIMEOUT })
            .should('not.be.visible');
    }

    waitIde() {
        this.waitTopMenuPanel();
        this.waitLeftContentPanel();
        this.waitFilesButton();
        this.waitPreloaderAbsent();
    }

    waitStatusBarContains(expectedText: string) {
        cy.get("div[id='theia-statusBar']").invoke('text').should(text => {
                let elementText: string = "" + text;

                console.log("= yes ==>>>>  ", elementText)

                expect(elementText).contain(expectedText);
            })

        // .should('contain', expectedText, {timeout: Ide.LANGUAGE_SERVER_INITIALIZATION_TIMEOUT})
    }

    waitStatusBarTextAbcence(expectedText: string) {
        cy.get("div[id='theia-statusBar']").invoke('text', {timeout: Ide.LANGUAGE_SERVER_INITIALIZATION_TIMEOUT}).should(text => {
            let elementText: string = "" + text;

            console.log("= no ==>>>>  ", elementText)

            expect(elementText).not.contain(expectedText);
        })
    }





}
