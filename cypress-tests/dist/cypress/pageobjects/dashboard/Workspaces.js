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
var Workspaces = /** @class */ (function () {
    function Workspaces() {
    }
    Workspaces.prototype.getWorkspaceListItemLocator = function (workspaceName) {
        return "#ws-name-" + workspaceName;
    };
    Workspaces.prototype.waitPage = function () {
        cy.get(Workspaces.ADD_WORKSPACE_BUTTON).should('be.visible');
    };
    Workspaces.prototype.clickAddWorkspaceButton = function () {
        cy.get(Workspaces.ADD_WORKSPACE_BUTTON).should('be.visible').click();
    };
    Workspaces.prototype.waitWorkspaceListItem = function (workspaceName) {
        cy.get(this.getWorkspaceListItemLocator(workspaceName));
    };
    Workspaces.prototype.clickOnStopWorkspaceButton = function (workspaceName) {
        cy.get("#ws-name-" + workspaceName + " .workspace-status[uib-tooltip=\"Stop workspace\"]")
            .click({ force: true });
    };
    Workspaces.prototype.waitWorkspaceWithRunningStatus = function (workspaceName) {
        cy.get(this.getWorkspaceListItemLocator(workspaceName), { timeout: Workspaces.START_STOP_WORKSPACE_TIMEOUT })
            .should('have.attr', 'data-ws-status', 'RUNNING');
    };
    Workspaces.prototype.waitWorkspaceWithStoppedStatus = function (workspaceName) {
        cy.get(this.getWorkspaceListItemLocator(workspaceName), { timeout: Workspaces.START_STOP_WORKSPACE_TIMEOUT })
            .should('have.attr', 'data-ws-status', 'STOPPED');
    };
    Workspaces.prototype.clickWorkspaceListItem = function (workspaceName) {
        cy.get("div[id='ws-full-name-che/" + workspaceName + "']").click({ force: true });
    };
    Workspaces.prototype.clickDeleteButtonOnWorkspaceDetails = function () {
        cy.get("che-button-danger[che-button-title='Delete']").should('be.visible').click();
    };
    Workspaces.prototype.waitWorkspaceListItemAbcence = function (workspaceName) {
        cy.get("div[id='ws-full-name-che/" + workspaceName + "']").should('not.be.visible');
    };
    Workspaces.prototype.clickConfirmDeletionButton = function () {
        cy.get('#ok-dialog-button').should('be.visible').click();
    };
    Workspaces.TITLE = ".che-toolbar-title-label";
    Workspaces.ADD_WORKSPACE_BUTTON = "#add-item-button";
    Workspaces.START_STOP_WORKSPACE_TIMEOUT = Cypress.env("load_page_timeout");
    return Workspaces;
}());
export { Workspaces };
