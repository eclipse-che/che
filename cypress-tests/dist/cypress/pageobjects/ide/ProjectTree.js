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
import { Ide } from "./Ide";
import { ElementStateChecker } from "../../utils/ElementStateChecker";
import { Promise } from "bluebird";
var ProjectTree = /** @class */ (function () {
    function ProjectTree() {
        this.ide = new Ide();
        this.elementStateChecker = new ElementStateChecker();
    }
    ProjectTree.prototype.getItemId = function (itemPath) {
        return "div[id='/projects:/projects/" + itemPath + "']";
    };
    ProjectTree.prototype.getCollapsedItemLocator = function (itemPath) {
        return this.getExpandIconLocator(itemPath) + ".theia-mod-collapsed";
    };
    ProjectTree.prototype.getExpandedItemLocator = function (itemPath) {
        return this.getExpandIconLocator(itemPath) + ":not(.theia-mod-collapsed)";
    };
    ProjectTree.prototype.openProjectTreeContainer = function () {
        var _this = this;
        cy.get(Ide.FILES_BUTTON)
            .should('be.visible')
            .then(function (filesButton) {
            var isProjectTreeContainerOpened = filesButton.hasClass("p-mod-current");
            //if project tree container is not opened click on "Files" button
            if (!isProjectTreeContainerOpened) {
                _this.ide.clickOnFilesButton();
            }
        }).then(function () {
            _this.waitProjectTreeContainer();
        });
    };
    ProjectTree.prototype.waitItemExpanded = function (itemPath) {
        cy.get(this.getExpandedItemLocator(itemPath)).should('be.visible');
    };
    ProjectTree.prototype.waitItemCollapsed = function (itemPath) {
        cy.get(this.getCollapsedItemLocator(itemPath)).should('be.visible');
    };
    ProjectTree.prototype.waitProjectTreeContainer = function () {
        cy.get(ProjectTree.PROJECT_TREE_CONTAINER)
            .should('be.visible')
            .should('not.have.class', 'animating')
            .wait(1000);
    };
    ProjectTree.prototype.waitProjectTreeContainerClosed = function () {
        cy.get(ProjectTree.PROJECT_TREE_CONTAINER).should('not.be.visible');
    };
    ProjectTree.prototype.waitItemDisappearance = function (itemPath) {
        cy.get(this.getItemId(itemPath)).should('not.be.visible');
    };
    ProjectTree.prototype.clickOnItem = function (itemPath) {
        cy.get(this.getItemId(itemPath))
            .should('be.visible')
            .click();
        this.waitItemSelected(itemPath);
    };
    ProjectTree.prototype.doubleClickOnItem = function (itemPath) {
        cy.get(this.getItemId(itemPath))
            .should('be.visible')
            .dblclick();
        this.waitItemSelected(itemPath);
    };
    ProjectTree.prototype.waitItemSelected = function (itemPath) {
        var selectedItemLocator = "div[title='/projects/" + itemPath + "'].theia-mod-selected.theia-mod-focus";
        cy.get(selectedItemLocator).should('be.visible');
    };
    ProjectTree.prototype.getExpandIconLocator = function (itemPath) {
        return "div[data-node-id='/projects:/projects" + itemPath + "']";
    };
    ProjectTree.prototype.getTreeItemLocator = function (itemPath) {
        return ".theia-TreeNode[title='/projects" + itemPath + "']";
    };
    ProjectTree.prototype.expandItem = function (itemPath) {
        var _this = this;
        var expandIconLocator = this.getExpandIconLocator(itemPath);
        var treeItemLocator = this.getTreeItemLocator(itemPath);
        cy.get(expandIconLocator)
            .should('be.visible')
            .then(function (expandIcon) {
            // if item collapsed click and expand it
            if (expandIcon.hasClass('theia-mod-collapsed')) {
                cy.get(treeItemLocator)
                    .should('be.visible')
                    .click();
            }
        })
            .then(function () {
            _this.waitItemExpanded(itemPath);
        });
    };
    ProjectTree.prototype.collapseItem = function (itemPath) {
        var _this = this;
        var expandIconLocator = this.getExpandIconLocator(itemPath);
        var treeItemLocator = this.getTreeItemLocator(itemPath);
        cy.get(expandIconLocator)
            .should('be.visible')
            .then(function (expandIcon) {
            // if item expanded click and collapse it
            if (!expandIcon.hasClass('theia-mod-collapsed')) {
                cy.get(treeItemLocator)
                    .should('be.visible')
                    .click();
            }
        })
            .then(function () {
            _this.waitItemCollapsed(itemPath);
        });
    };
    ProjectTree.prototype.expandPathAndOpenFile = function (pathToItem, fileName) {
        var _this = this;
        var currentPath = "";
        var paths = new Array();
        // make direct path for each project tree item
        pathToItem.split('/').forEach(function (item) {
            currentPath = currentPath + "/" + item;
            paths.push(currentPath);
        });
        //expand each project tree item
        paths.forEach(function (path) {
            _this.expandItem(path);
        });
        //open file  
        this.clickOnItem(pathToItem + "/" + fileName);
    };
    ProjectTree.prototype.waitProjectImported = function (projectName, rootSubitem) {
        var _this = this;
        cy.log("**=> ProjectTree.waitProjectImported**")
            .then(function () {
            var attempts = Cypress.env("ProjectTree.waitProjectImportedAttempts");
            var pollingEvery = Cypress.env("ProjectTree.waiProjectImportedPollingEvery");
            var currentAttempt = 1;
            _this.doWaitProjectImported(projectName, rootSubitem, attempts, currentAttempt, pollingEvery);
        });
    };
    ProjectTree.prototype.doWaitProjectImported = function (projectName, rootSubitem, attempts, currentAttempt, pollingEvery) {
        var _this = this;
        return new Promise(function (resolve, reject) {
            var rootItem = "/" + projectName;
            _this.expandItem(rootItem);
            _this.waitItemExpanded(rootItem);
            cy.get('body')
                .then(function (body) {
                var rootItemLocator = _this.getTreeItemLocator("/" + projectName);
                var rootSubitemLocator = _this.getTreeItemLocator("/" + projectName + "/" + rootSubitem);
                if (currentAttempt >= attempts) {
                    assert.isOk(false, "Exceeded the maximum number of checking attempts, project has not been imported");
                }
                cy.wait(2000);
                //If project root folder is not present, reload page, wait IDE and retry again
                if (body.find(rootItemLocator).length === 0) {
                    cy.log("**Project '" + projectName + "' has not benn found. Refreshing page and try again (attempt " + currentAttempt + " of " + attempts + ")**");
                    currentAttempt++;
                    cy.reload();
                    _this.ide.waitIde();
                    _this.openProjectTreeContainer();
                    _this.waitProjectTreeContainer();
                    cy.wait(pollingEvery);
                    _this.doWaitProjectImported(projectName, rootSubitem, attempts, currentAttempt, pollingEvery);
                }
                if (body.find(rootSubitemLocator).length > 0) {
                    return;
                }
                //If project root sub item is not present, collapse project folder, open project folder and retry again
                cy.log("**Root sub item '" + rootSubitem + "' has not benn found (attempt " + currentAttempt + " of " + attempts + ")**");
                currentAttempt++;
                _this.collapseItem(rootItem);
                _this.waitItemCollapsed(rootItem);
                cy.wait(pollingEvery);
                _this.doWaitProjectImported(projectName, rootSubitem, attempts, currentAttempt, pollingEvery);
            });
        });
    };
    ProjectTree.PROJECT_TREE_CONTAINER = "#theia-left-side-panel .theia-TreeContainer";
    return ProjectTree;
}());
export { ProjectTree };
