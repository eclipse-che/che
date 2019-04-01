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
var Dashboard = /** @class */ (function () {
    function Dashboard() {
    }
    Dashboard.prototype.openDashboard = function () {
        cy.visit("/");
    };
    Dashboard.prototype.waitDashboard = function () {
        [Dashboard.DASHBOARD_BUTTON, Dashboard.WORKSPACES_BUTTON, Dashboard.STACKS_BUTTON, Dashboard.FACTORIES_BUTTON]
            .forEach(function (buttonLocator) {
            cy.get(buttonLocator, { timeout: Dashboard.PAGE_LOAD_TIMEOUT })
                .should('be.visible');
        });
    };
    Dashboard.prototype.clickDashboardButton = function () {
        cy.get(Dashboard.DASHBOARD_BUTTON).should('be.visible').click();
    };
    Dashboard.prototype.clickWorkspacesButton = function () {
        cy.get(Dashboard.WORKSPACES_BUTTON).should('be.visible').click();
    };
    Dashboard.prototype.clickStacksButton = function () {
        cy.get(Dashboard.STACKS_BUTTON).should('be.visible').click();
    };
    Dashboard.prototype.clickFactoriesButton = function () {
        cy.get(Dashboard.FACTORIES_BUTTON).should('be.visible').click();
    };
    Dashboard.prototype.waitLoaderPage = function () {
        cy.get(Dashboard.LOADER_PAGE, { timeout: Dashboard.PAGE_LOAD_TIMEOUT }).should('be.visible');
    };
    Dashboard.prototype.waitLoaderPageAbcence = function () {
        cy.get(Dashboard.LOADER_PAGE, { timeout: Dashboard.PAGE_LOAD_TIMEOUT }).should('not.be.visible');
    };
    Dashboard.PAGE_LOAD_TIMEOUT = Cypress.env("load_page_timeout");
    Dashboard.DASHBOARD_BUTTON = "#dashboard-item";
    Dashboard.WORKSPACES_BUTTON = "#workspaces-item";
    Dashboard.STACKS_BUTTON = "#stacks-item";
    Dashboard.FACTORIES_BUTTON = "#factories-item";
    Dashboard.LOADER_PAGE = ".main-page-loader";
    return Dashboard;
}());
export { Dashboard };
