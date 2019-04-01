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
var Ide = /** @class */ (function () {
    function Ide() {
        this.testWorkspaceUtil = new TestWorkspaceUtil();
    }
    Ide.prototype.waitNotification = function (notificationMessage) {
        var notificationLocator = "div[id='notification-container-3-" + notificationMessage + "-|']";
        cy.get(notificationLocator, { timeout: Ide.LOAD_PAGE_TIMEOUT })
            .should('be.visible');
    };
    Ide.prototype.waitNotificationDisappearance = function (notificationMessage) {
        var notificationLocator = "div[id='notification-container-3-" + notificationMessage + "-|']";
        cy.get(notificationLocator, { timeout: Ide.LOAD_PAGE_TIMEOUT })
            .should('not.be.visible');
    };
    Ide.prototype.waitWorkspaceAndIdeInIframe = function (workspaceNamespace, workspaceName) {
        var _this = this;
        cy.log("**=> Ide.waitWorkspaceAndIdeInIframe**")
            .then(function () {
            cy.log("**Wait until workspace is started**");
        })
            .then(function () {
            _this.testWorkspaceUtil.waitWorkspaceRunning(workspaceNamespace, workspaceName);
        })
            .then(function () {
            cy.log("**Wait until defined parts of IDE are visible**");
        })
            .then(function () {
            [Ide.TOP_MENU_PANEL, Ide.LEFT_CONTENT_PANEL, Ide.FILES_BUTTON]
                .forEach(function (idePartLocator) {
                cy.get(Ide.IDE_IFRAME, { timeout: Ide.LOAD_PAGE_TIMEOUT })
                    .should(function (iframe) {
                    expect(iframe.contents().find(idePartLocator)).to.have.length(1);
                    expect(iframe.contents().find(idePartLocator)).to.be.visible;
                });
            });
        });
    };
    Ide.prototype.waitWorkspaceAndIde = function (workspaceNamespace, workspaceName) {
        var _this = this;
        cy.log("**=> Ide.waitWorkspaceAndIde**")
            .then(function () {
            _this.testWorkspaceUtil.waitWorkspaceRunning(workspaceNamespace, workspaceName);
        })
            .then(function () {
            [Ide.TOP_MENU_PANEL, Ide.LEFT_CONTENT_PANEL, Ide.FILES_BUTTON]
                .forEach(function (idePart) {
                cy.get(idePart, { timeout: Ide.LOAD_PAGE_TIMEOUT })
                    .should('be.visible');
            });
        });
    };
    Ide.prototype.waitIde = function () {
        [Ide.TOP_MENU_PANEL, Ide.LEFT_CONTENT_PANEL, Ide.FILES_BUTTON]
            .forEach(function (idePart) {
            cy.get(idePart, { timeout: Ide.LOAD_PAGE_TIMEOUT })
                .should('be.visible');
        });
    };
    Ide.prototype.openIdeWithoutFrames = function (workspaceName) {
        cy.log("**=> Ide.openIdeWithoutFrames**")
            .then(function () {
            var workspaceUrl = "/che/" + workspaceName;
            cy.visit(workspaceUrl);
        });
    };
    Ide.prototype.waitFilesButton = function () {
        cy.get(Ide.FILES_BUTTON)
            .should('be.visible');
    };
    Ide.prototype.clickOnFilesButton = function () {
        cy.get(Ide.FILES_BUTTON)
            .first()
            .click();
    };
    Ide.prototype.waitTopMenuPanel = function () {
        cy.get(Ide.TOP_MENU_PANEL)
            .should('be.visible');
    };
    Ide.prototype.waitLeftContentPanel = function () {
        cy.get(Ide.LEFT_CONTENT_PANEL)
            .should('be.visible');
    };
    Ide.prototype.waitPreloaderAbsent = function () {
        cy.get(Ide.PRELOADER)
            .should('not.be.visible');
    };
    Ide.prototype.waitStatusBarContains = function (expectedText) {
        cy.get("div[id='theia-statusBar']", { timeout: Ide.LANGUAGE_SERVER_INITIALIZATION_TIMEOUT })
            .should(function (elem) {
            var elementText = elem[0].innerText.toString();
            expect(elementText).contain(expectedText);
        });
    };
    Ide.prototype.waitStatusBarTextAbcence = function (expectedText) {
        cy.get("div[id='theia-statusBar']", { timeout: Ide.LANGUAGE_SERVER_INITIALIZATION_TIMEOUT })
            .should(function (elem) {
            var elementText = elem[0].innerText.toString();
            expect(elementText).not.contain(expectedText);
        });
    };
    Ide.START_WORKSPACE_TIMEOUT = Cypress.env("start_workspace_timeout");
    Ide.LANGUAGE_SERVER_INITIALIZATION_TIMEOUT = Cypress.env("language_server_initialization_timeout");
    Ide.LOAD_PAGE_TIMEOUT = Cypress.env("load_page_timeout");
    Ide.TOP_MENU_PANEL = "#theia-app-shell #theia-top-panel .p-MenuBar-content";
    Ide.LEFT_CONTENT_PANEL = "#theia-left-content-panel";
    Ide.FILES_BUTTON = ".theia-app-left .p-TabBar-content li[title='Files']";
    Ide.PRELOADER = ".theia-preload";
    Ide.IDE_IFRAME = "iframe#ide-application-iframe";
    return Ide;
}());
export { Ide };
