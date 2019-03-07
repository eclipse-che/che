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


    waitPage(){
        cy.get(Workspaces.ADD_WORKSPACE_BUTTON).should('be.visible');
    }

    clickAddWorkspaceButton() {
        cy.get(Workspaces.ADD_WORKSPACE_BUTTON).should('be.visible').click();
    }

    clickWorkspaceListItem(workspaceName: string){
        cy.get(`div[id='ws-full-name-che/${workspaceName}']`).click({force: true});
    }

    clickDeleteButtonOnWorkspaceDetails(){
        cy.get("che-button-danger[che-button-title='Delete']").should('be.visible').click();
    }

    waitWorkspaceListItemAbcence(workspaceName: string){
        cy.get(`div[id='ws-full-name-che/${workspaceName}']`).should('not.be.visible')
    }

    clickConfirmDeletionButton(){
        cy.get('#ok-dialog-button').should('be.visible').click();
    }

}
