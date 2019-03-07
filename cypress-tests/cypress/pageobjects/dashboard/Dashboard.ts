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

export class Dashboard {

    private static readonly ROOT_URL: string = Cypress.env("root_url");
    private static readonly PAGE_LOAD_TIMEOUT: number = Cypress.env("load_page_timeout");

    private static readonly DASHBOARD_BUTTON: string = "#dashboard-item";
    private static readonly WORKSPACES_BUTTON: string = "#workspaces-item";
    private static readonly STACKS_BUTTON: string = "#stacks-item";
    private static readonly FACTORIES_BUTTON: string = "#factories-item";
    private static readonly LOADER_PAGE: string = ".main-page-loader"



    openDashboard() {
        cy.visit(Dashboard.ROOT_URL);
    }

    waitDashboard(){
        this.waitButton("Dashboard", Dashboard.DASHBOARD_BUTTON);
        this.waitButton("Workspoaces", Dashboard.WORKSPACES_BUTTON);
        this.waitButton("Stacks", Dashboard.STACKS_BUTTON);
        this.waitButton("Factories", Dashboard.FACTORIES_BUTTON);
    }

    clickDashboardButton() {
        this.clickButton("Dashboard", Dashboard.DASHBOARD_BUTTON);
    }

    clickWorkspacesButton() {
        this.clickButton("Workspaces", Dashboard.WORKSPACES_BUTTON);
    }

    clickStacksButton() {
        this.clickButton("Stacks", Dashboard.DASHBOARD_BUTTON);
    }

    clickFactoriesButton() {
        this.clickButton("Factories", Dashboard.DASHBOARD_BUTTON);
    }

    waitLoaderPage(){
        cy.get(Dashboard.LOADER_PAGE, { timeout: Dashboard.PAGE_LOAD_TIMEOUT }).should('be.visible')
    }

    waitLoaderPageAbcence(){
        cy.get(Dashboard.LOADER_PAGE, { timeout: Dashboard.PAGE_LOAD_TIMEOUT }).should('not.be.visible')
    }

    private waitButton(buttonName: string, locator: string) {
        cy.get(locator, { timeout: Dashboard.PAGE_LOAD_TIMEOUT }).should('be.visible', { timeout: Dashboard.PAGE_LOAD_TIMEOUT });
    }

    private clickButton(buttonName: string, locator: string) {
        cy.get(locator).click();
    }























}