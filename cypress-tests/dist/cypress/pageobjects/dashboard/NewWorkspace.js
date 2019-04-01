import { ElementStateChecker } from "../../utils/ElementStateChecker";
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
var NewWorkspace = /** @class */ (function () {
    function NewWorkspace() {
        this.elementStateChecker = new ElementStateChecker();
    }
    NewWorkspace.prototype.getPluginListItemLocator = function (pluginName) {
        return ".plugin-item div[plugin-item-name='" + pluginName + "']";
    };
    NewWorkspace.prototype.getPluginListItemSwitcherLocator = function (pluginName) {
        return this.getPluginListItemLocator(pluginName) + " md-switch";
    };
    NewWorkspace.prototype.waitPluginListItem = function (pluginName) {
        var _this = this;
        cy.get(this.getPluginListItemLocator(pluginName))
            .should(function (element) {
            expect(_this.elementStateChecker.isVisible(element)).to.be.true;
        });
    };
    NewWorkspace.prototype.clickOnPluginListItemSwitcher = function (pluginName) {
        var _this = this;
        cy.get(this.getPluginListItemSwitcherLocator(pluginName))
            .should(function (element) {
            expect(_this.elementStateChecker.isVisible(element)).to.be.true;
        })
            .click({ force: true });
    };
    NewWorkspace.prototype.waitPluginEnabling = function (pluginName) {
        var _this = this;
        cy.get(this.getPluginListItemSwitcherLocator(pluginName))
            .should(function (element) {
            expect(_this.elementStateChecker.isVisible(element)).to.be.true;
        })
            .should('have.attr', 'aria-checked', 'true');
    };
    NewWorkspace.prototype.waitPluginDisabling = function (pluginName) {
        var _this = this;
        cy.get(this.getPluginListItemSwitcherLocator(pluginName))
            .should(function (element) {
            expect(_this.elementStateChecker.isVisible(element)).to.be.true;
        })
            .should('have.attr', 'aria-checked', 'false');
    };
    NewWorkspace.prototype.typeWorkspaceName = function (workspaceName) {
        cy.get(NewWorkspace.NAME_FIELD)
            .clear()
            .should('have.value', "")
            .type(workspaceName)
            .should('have.value', workspaceName);
    };
    NewWorkspace.prototype.clickOnChe7Stack = function () {
        cy.get(NewWorkspace.CHE_7_STACK)
            .click();
    };
    NewWorkspace.prototype.waitChe7StackSelected = function () {
        cy.get(NewWorkspace.SELECTED_CHE_7_STACK)
            .should('be.visible');
    };
    NewWorkspace.prototype.clickOnCreateAndOpenButton = function () {
        var ideFrameLocator = "ide-iframe#ide-iframe-window";
        cy.get(NewWorkspace.CREATE_AND_OPEN_BUTTON)
            .first()
            .should('be.visible')
            .click();
        //check that the workspace has started to boot
        cy.get(ideFrameLocator)
            .should('have.attr', 'aria-hidden', 'false');
    };
    NewWorkspace.prototype.clickOnAddOrImportProjectButton = function () {
        cy.get(NewWorkspace.ADD_OR_IMPORT_PROJECT_BUTTON)
            .should('be.visible')
            .click();
    };
    NewWorkspace.prototype.enableSampleCheckbox = function (sampleName) {
        var _this = this;
        cy.get("#sample-" + sampleName + ">md-checkbox>div")
            .first()
            .should(function (element) {
            expect(_this.elementStateChecker.isVisible(element)).to.be.true;
        })
            .click({ force: true });
        this.waitSampleCheckboxEnabling(sampleName);
    };
    NewWorkspace.prototype.waitSampleCheckboxEnabling = function (sampleName) {
        var _this = this;
        cy.get("#sample-" + sampleName + ">md-checkbox[aria-checked='true']")
            .should(function (element) {
            expect(_this.elementStateChecker.isVisible(element)).to.be.true;
        });
    };
    NewWorkspace.prototype.waitProjectAdding = function (projectName) {
        var _this = this;
        cy.get("#project-source-selector toggle-single-button#" + projectName)
            .should(function (element) {
            expect(_this.elementStateChecker.isVisible(element)).to.be.true;
        });
    };
    NewWorkspace.prototype.waitProjectAbsence = function (projectName) {
        var _this = this;
        cy.get("#project-source-selector toggle-single-button#" + projectName)
            .should(function (element) {
            expect(_this.elementStateChecker.isVisible(element)).to.be.false;
        });
    };
    NewWorkspace.prototype.clickOnAddButton = function () {
        cy.get(NewWorkspace.ADD_BUTTON)
            .should('be.visible')
            .click();
    };
    NewWorkspace.CHE_7_STACK = "div[data-stack-id='che7-preview']";
    NewWorkspace.SELECTED_CHE_7_STACK = ".stack-selector-item-selected[data-stack-id='che7-preview']";
    NewWorkspace.CREATE_AND_OPEN_BUTTON = "che-button-save-flat[che-button-title='Create & Open']>button";
    NewWorkspace.ADD_OR_IMPORT_PROJECT_BUTTON = ".add-import-project-toggle-button";
    NewWorkspace.ADD_BUTTON = "button[aria-disabled='false'][name='addButton']";
    NewWorkspace.NAME_FIELD = "#workspace-name-input";
    return NewWorkspace;
}());
export { NewWorkspace };
