import { TestWorkspaceUtil } from "../../utils/workspace/TestWorkspaceUtil";

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
    private static readonly LOAD_PAGE_TIMEOUT: number = Cypress.env("load_page_timeout");

    private static readonly TOP_MENU_PANEL: string = "#theia-app-shell #theia-top-panel .p-MenuBar-content";
    private static readonly LEFT_CONTENT_PANEL: string = "#theia-left-content-panel";
    public static readonly EXPLORER_BUTTON: string = ".theia-app-left .p-TabBar-content li[title='Explorer']";
    private static readonly PRELOADER: string = ".theia-preload";
    private static readonly IDE_IFRAME: string = "iframe#ide-application-iframe";


    private readonly testWorkspaceUtil: TestWorkspaceUtil = new TestWorkspaceUtil();



    waitNotification(notificationMessage: string) {
        let notificationLocator: string = `div[id='notification-container-3-${notificationMessage}-|']`

        cy.get(notificationLocator, { timeout: Ide.LOAD_PAGE_TIMEOUT })
            .should('be.visible')
    }

    waitNotificationDisappearance(notificationMessage: string) {
        let notificationLocator: string = `div[id='notification-container-3-${notificationMessage}-|']`

        cy.get(notificationLocator, { timeout: Ide.LOAD_PAGE_TIMEOUT })
            .should('not.be.visible')
    }

    waitWorkspaceAndIdeInIframe(workspaceNamespace: string, workspaceName: string) {
        cy.log("**=> Ide.waitWorkspaceAndIdeInIframe**")
            .then(() => {
                cy.log("**Wait until workspace is started**")
            })
            .then(() => {
                this.testWorkspaceUtil.waitWorkspaceRunning(workspaceNamespace, workspaceName)
            })
            .then(() => {
                cy.log("**Wait until defined parts of IDE are visible**")
            })
            .then(() => {
                [Ide.TOP_MENU_PANEL, Ide.LEFT_CONTENT_PANEL, Ide.EXPLORER_BUTTON]
                    .forEach(idePartLocator => {
                        cy.get(Ide.IDE_IFRAME, { timeout: Ide.LOAD_PAGE_TIMEOUT })
                            .should(iframe => {
                                expect(iframe.contents().find(idePartLocator)).to.have.length(1)
                                expect(iframe.contents().find(idePartLocator)).to.be.visible
                            })
                    })
            })
    }

    waitWorkspaceAndIde(workspaceNamespace: string, workspaceName: string) {
        cy.log("**=> Ide.waitWorkspaceAndIde**")
            .then(() => {
                this.testWorkspaceUtil.waitWorkspaceRunning(workspaceNamespace, workspaceName)
            })
            .then(() => {
                [Ide.TOP_MENU_PANEL, Ide.LEFT_CONTENT_PANEL, Ide.EXPLORER_BUTTON]
                    .forEach(idePart => {
                        cy.get(idePart, { timeout: Ide.LOAD_PAGE_TIMEOUT })
                            .should('be.visible')
                    })
            });
    }

    waitIde() {
        [Ide.TOP_MENU_PANEL, Ide.LEFT_CONTENT_PANEL, Ide.EXPLORER_BUTTON]
            .forEach(idePart => {
                cy.get(idePart, { timeout: Ide.LOAD_PAGE_TIMEOUT })
                    .should('be.visible')
            })
    }

    openIdeWithoutFrames(workspaceName: string) {
        cy.log("**=> Ide.openIdeWithoutFrames**")
            .then(() => {
                let workspaceUrl: string = `/che/${workspaceName}`

                cy.visit(workspaceUrl);
            })
    }

    waitExplorerButton() {
        cy.get(Ide.EXPLORER_BUTTON)
            .should('be.visible');
    }

    clickOnExplorerButton() {
        cy.get(Ide.EXPLORER_BUTTON)
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
