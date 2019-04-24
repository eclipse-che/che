/*********************************************************************
 * Copyright (c) 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/



export class WorkspaceDetails {

    private static readonly RUN_BUTTON: string = "#run-workspace-button[che-button-title='Run']";
    private static readonly OPEN_BUTTON: string = "#open-in-ide-button[che-button-title='Open']";
    private static readonly TAB_BUTTONS: string = "md-tabs-canvas[role='tablist'] md-tab-item";
    private static readonly SELECTED_TAB_BUTTON: string = "md-tabs-canvas[role='tablist'] md-tab-item[aria-selected='true']";
    private static readonly SAVE_BUTTON: string = "button[name='save-button']";
    private static readonly ENABLED_SAVE_BUTTON: string = "button[name='save-button'][aria-disabled='false']";
    private static readonly WORKSPACE_DETAILS_LOADER: string = "workspace-details-overview md-progress-linear";
    private static readonly LOAD_PAGE_TIMEOUT: number = Cypress.env("load_page_timeout");

    private getWorkspaceTitleLocator(workspaceName: string): string {
        return `che-row-toolbar[che-title='${workspaceName}']`
    }

    waitLoaderDisappearance() {
        cy.get(WorkspaceDetails.WORKSPACE_DETAILS_LOADER, { timeout: WorkspaceDetails.LOAD_PAGE_TIMEOUT })
            .should('not.be.visible')
    }

    waitSaveButton() {
        cy.get(WorkspaceDetails.ENABLED_SAVE_BUTTON)
            .should('be.visible')
    }

    waitSaveButtonDisappearance() {
        cy.get(WorkspaceDetails.SAVE_BUTTON)
            .should('not.be.visible')
    }

    clickOnSaveButton() {
        cy.get(WorkspaceDetails.ENABLED_SAVE_BUTTON)
            .should('be.visible')
            .click()
    }

    waitPage(workspaceName: string) {
        cy.log("**wait page**").then(() => {
            this.waitWorkspaceTitle(workspaceName);
            this.waitOpenButton();
            this.waitRunButton();
            this.waitTabsPresence();
            this.waitLoaderDisappearance();
        })

    }

    waitWorkspaceTitle(workspaceName: string) {
        cy.get(this.getWorkspaceTitleLocator(workspaceName), { timeout: WorkspaceDetails.LOAD_PAGE_TIMEOUT })
            .should('be.visible')
    }

    waitRunButton() {
        cy.get(WorkspaceDetails.RUN_BUTTON, { timeout: WorkspaceDetails.LOAD_PAGE_TIMEOUT })
            .should('be.visible')
    }

    clickOnRunButton() {
        cy.get(WorkspaceDetails.RUN_BUTTON, { timeout: WorkspaceDetails.LOAD_PAGE_TIMEOUT })
            .should('be.visible')
            .click()
    }

    waitOpenButton() {
        cy.get(WorkspaceDetails.OPEN_BUTTON, { timeout: WorkspaceDetails.LOAD_PAGE_TIMEOUT })
            .should('be.visible')
    }

    clickOnOpenButton() {
        cy.get(WorkspaceDetails.OPEN_BUTTON, { timeout: WorkspaceDetails.LOAD_PAGE_TIMEOUT })
            .should('be.visible')
            .click()
    }

    waitTabsPresence() {
        ["Overview", "Projects", "Containers", "Servers", "Env Variables", "Volumes", "Config", "SSH", "Plugins", "Editors"]
            .forEach(tabTitle => {
                cy.get(WorkspaceDetails.TAB_BUTTONS, { timeout: WorkspaceDetails.LOAD_PAGE_TIMEOUT })
                    .contains(tabTitle)
                    .should('be.visible')
            })
    }

    clickOnTab(tabTitle: string) {
        cy.get(WorkspaceDetails.TAB_BUTTONS)
            .contains(tabTitle)
            .should('be.visible')
            .click()
    }

    waitTabSelected(tabTitle: string) {
        cy.log("**wait tab selected**").then(() => {
            this.waitTabsPresence()

            cy.get(WorkspaceDetails.SELECTED_TAB_BUTTON)
                .should(selectedTab => {
                    expect(selectedTab).to.be.visible
                    expect(selectedTab[0].innerText.toString()).to.be.equal(tabTitle)
                })
        })
    }

}
