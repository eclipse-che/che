/*********************************************************************
 * Copyright (c) 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

/// <reference types="Cypress" />


export class Ide {

    private static readonly START_WORKSPACE_TIMEOUT: number = Cypress.env("start_workspace_timeout");
    private static readonly LANGUAGE_SERVER_INITIALIZATION_TIMEOUT: number = Cypress.env("language_server_initialization_timeout");

    private static readonly TOP_MENU_PANEL: string = "#theia-app-shell #theia-top-panel .p-MenuBar-content";
    private static readonly LEFT_CONTENT_PANEL: string = "#theia-left-content-panel";
    public static readonly FILES_BUTTON: string = ".theia-app-left .p-TabBar-content li[title='Files']";
    private static readonly PRELOADER: string = ".theia-preload";

    private static readonly IDE_IFRAME: string = "iframe[id='ide-application-iframe']";

    openIdeWithoutFrames(workspaceName: string) {
        let workspaceUrl: string = `/che/${workspaceName}`

        cy.visit(workspaceUrl);
    }

    waitFilesButton() {
        cy.get(Ide.FILES_BUTTON)
            .should('be.visible');
    }

    clickOnFilesButton() {
        cy.get(Ide.FILES_BUTTON)
            .first()
            .click();
    }

    waitTopMenuPanel() {
        cy.get(Ide.TOP_MENU_PANEL)
            .should('be.visible');
    }

    waitLeftContentPanel() {
        cy.get(Ide.LEFT_CONTENT_PANEL)
            .should('be.visible');
    }

    waitPreloaderAbsent() {
        cy.get(Ide.PRELOADER)
            .should('not.be.visible');
    }

    waitIde() {
        [Ide.TOP_MENU_PANEL, Ide.LEFT_CONTENT_PANEL, Ide.FILES_BUTTON, Ide.PRELOADER]
            .forEach(idePart => {
                cy.get(idePart, { timeout: Ide.START_WORKSPACE_TIMEOUT })
                    .should('be.visible')
            })
    }

    waitStatusBarContains(expectedText: string) {
        cy.get("div[id='theia-statusBar']", { timeout: Ide.LANGUAGE_SERVER_INITIALIZATION_TIMEOUT })
            .should(elem => {
                let elementText: string = elem[0].innerText.toString();

                expect(elementText).contain(expectedText);
            })

    }

    waitStatusBarTextAbcence(expectedText: string) {
        cy.get("div[id='theia-statusBar']", { timeout: Ide.LANGUAGE_SERVER_INITIALIZATION_TIMEOUT })
            .should(elem => {
                let elementText: string = elem[0].innerText.toString();

                expect(elementText).not.contain(expectedText);
            })
    }

}
