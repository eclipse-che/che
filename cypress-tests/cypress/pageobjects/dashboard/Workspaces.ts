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

export class Workspaces {
    private static readonly TITLE: string = ".che-toolbar-title-label";
    private static readonly ADD_WORKSPACE_BUTTON: string = "#add-item-button";
    private static readonly START_STOP_WORKSPACE_TIMEOUT: number = Cypress.env("load_page_timeout")


    private getWorkspaceListItemLocator(workspaceName: string): string {
        return `#ws-name-${workspaceName}`
    }

    waitPage() {
        cy.get(Workspaces.ADD_WORKSPACE_BUTTON).should('be.visible');
    }

    clickAddWorkspaceButton() {
        cy.get(Workspaces.ADD_WORKSPACE_BUTTON).should('be.visible').click();
    }

    waitWorkspaceListItem(workspaceName: string) {
        cy.get(this.getWorkspaceListItemLocator(workspaceName))
    }

    clickOnStopWorkspaceButton(workspaceName: string) {
        cy.get(`#ws-name-${workspaceName} .workspace-status[uib-tooltip="Stop workspace"]`)
            .click({ force: true })
    }

    waitWorkspaceWithRunningStatus(workspaceName: string) {
        cy.get(this.getWorkspaceListItemLocator(workspaceName), { timeout: Workspaces.START_STOP_WORKSPACE_TIMEOUT })
            .should('have.attr', 'data-ws-status', 'RUNNING')
    }

    waitWorkspaceWithStoppedStatus(workspaceName: string) {
        cy.get(this.getWorkspaceListItemLocator(workspaceName), { timeout: Workspaces.START_STOP_WORKSPACE_TIMEOUT })
            .should('have.attr', 'data-ws-status', 'STOPPED')
    }

    clickWorkspaceListItem(workspaceName: string) {
        cy.get(`div[id='ws-full-name-che/${workspaceName}']`).click({ force: true });
    }

    clickDeleteButtonOnWorkspaceDetails() {
        cy.get("che-button-danger[che-button-title='Delete']").should('be.visible').click();
    }

    waitWorkspaceListItemAbcence(workspaceName: string) {
        cy.get(`div[id='ws-full-name-che/${workspaceName}']`).should('not.be.visible')
    }

    clickConfirmDeletionButton() {
        cy.get('#ok-dialog-button').should('be.visible').click();
    }

}
