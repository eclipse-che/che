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
import { Promise, resolve } from "bluebird";

export class ProjectTree {

    private readonly ide: Ide = new Ide();
    private readonly elementStateChecker: ElementStateChecker = new ElementStateChecker();
    private static readonly PROJECT_TREE_CONTAINER: string = "#theia-left-side-panel .theia-TreeContainer";

    private getItemId(itemPath: string): string {
        return `div[id='/projects:/projects/${itemPath}']`;
    }

    private getCollapsedItemLocator(itemPath: string): string {
        return `${this.getExpandIconLocator(itemPath)}.theia-mod-collapsed`;
    }

    private getExpandedItemLocator(itemPath: string): string {
        return `${this.getExpandIconLocator(itemPath)}:not(.theia-mod-collapsed)`;
    }

    openProjectTreeContainer() {
        cy.get(Ide.FILES_BUTTON)
            .should('be.visible')
            .then(filesButton => {
                let isProjectTreeContainerOpened: boolean = filesButton.hasClass("p-mod-current");

                //if project tree container is not opened click on "Files" button
                if (!isProjectTreeContainerOpened) {
                    this.ide.clickOnFilesButton();
                }
            }).then(() => {
                this.waitProjectTreeContainer();
            })
    }

    waitItemExpanded(itemPath: string) {
        cy.get(this.getExpandedItemLocator(itemPath)).should('be.visible');
    }

    waitItemCollapsed(itemPath: string) {
        cy.get(this.getCollapsedItemLocator(itemPath)).should('be.visible');
    }

    waitProjectTreeContainer() {
        cy.get(ProjectTree.PROJECT_TREE_CONTAINER)
            .should('be.visible')
            .should('not.have.class', 'animating')
            .wait(1000);
    }

    waitProjectTreeContainerClosed() {
        cy.get(ProjectTree.PROJECT_TREE_CONTAINER).should('not.be.visible');
    }

    waitItemDisappearance(itemPath: string) {
        cy.get(this.getItemId(itemPath)).should('not.be.visible');
    }

    clickOnItem(itemPath: string) {
        cy.get(this.getItemId(itemPath))
            .should('be.visible')
            .click();

        this.waitItemSelected(itemPath);
    }

    doubleClickOnItem(itemPath: string) {
        cy.get(this.getItemId(itemPath))
            .should('be.visible')
            .dblclick();

        this.waitItemSelected(itemPath);
    }

    waitItemSelected(itemPath: string) {
        let selectedItemLocator: string = `div[title='/projects/${itemPath}'].theia-mod-selected.theia-mod-focus`;

        cy.get(selectedItemLocator).should('be.visible');
    }

    private getExpandIconLocator(itemPath: string) {
        return `div[data-node-id='/projects:/projects${itemPath}']`;
    }

    private getTreeItemLocator(itemPath: string) {
        return `.theia-TreeNode[title='/projects${itemPath}']`
    }

    expandItem(itemPath: string) {
        let expandIconLocator: string = this.getExpandIconLocator(itemPath);
        let treeItemLocator: string = this.getTreeItemLocator(itemPath);

        cy.get(expandIconLocator)
            .should('be.visible')
            .then(expandIcon => {
                // if item collapsed click and expand it
                if (expandIcon.hasClass('theia-mod-collapsed')) {
                    cy.get(treeItemLocator)
                        .should('be.visible')
                        .click();
                }
            })
            .then(() => {
                this.waitItemExpanded(itemPath);
            })

    }

    collapseItem(itemPath: string) {
        let expandIconLocator: string = this.getExpandIconLocator(itemPath);
        let treeItemLocator: string = this.getTreeItemLocator(itemPath);

        cy.get(expandIconLocator)
            .should('be.visible')
            .then(expandIcon => {
                // if item expanded click and collapse it
                if (!expandIcon.hasClass('theia-mod-collapsed')) {
                    cy.get(treeItemLocator)
                        .should('be.visible')
                        .click();
                }
            })
            .then(() => {
                this.waitItemCollapsed(itemPath);
            })

    }

    expandPathAndOpenFile(pathToItem: string, fileName: string) {
        let currentPath: string = "";
        let paths: Array<string> = new Array();

        // make direct path for each project tree item
        pathToItem.split('/').forEach(item => {
            currentPath = `${currentPath}/${item}`;
            paths.push(currentPath);
        })

        //expand each project tree item
        paths.forEach(path => {
            this.expandItem(path)
        })

        //open file  
        this.clickOnItem(`${pathToItem}/${fileName}`)
    }

    waitProjectImported(projectName: string, rootSubitem: string) {
        cy.log("**=> ProjectTree.waitProjectImported**")
            .then(() => {
                let attempts: number = Cypress.env("ProjectTree.waitProjectImportedAttempts");
                let pollingEvery: number = Cypress.env("ProjectTree.waiProjectImportedPollingEvery");
                let currentAttempt: number = 1;

                this.doWaitProjectImported(projectName, rootSubitem, attempts, currentAttempt, pollingEvery)
            })
    }

    private doWaitProjectImported(projectName: string, rootSubitem: string, attempts: number, currentAttempt: number, pollingEvery: number): Promise<void> {
        return new Promise((resolve, reject) => {
            let rootItem: string = `/${projectName}`;

            this.expandItem(rootItem)
            this.waitItemExpanded(rootItem)

            cy.get('body')
                .then(body => {
                    let rootItemLocator: string = this.getTreeItemLocator(`/${projectName}`);
                    let rootSubitemLocator: string = this.getTreeItemLocator(`/${projectName}/${rootSubitem}`)

                    if (currentAttempt >= attempts) {
                        assert.isOk(false, "Exceeded the maximum number of checking attempts, project has not been imported")
                    }

                    cy.wait(2000)

                    //If project root folder is not present, reload page, wait IDE and retry again
                    if (body.find(rootItemLocator).length === 0) {
                        cy.log(`**Project '${projectName}' has not benn found. Refreshing page and try again (attempt ${currentAttempt} of ${attempts})**`)

                        currentAttempt++

                        cy.reload();
                        this.ide.waitIde()
                        this.openProjectTreeContainer();
                        this.waitProjectTreeContainer();

                        cy.wait(pollingEvery)
                        this.doWaitProjectImported(projectName, rootSubitem, attempts, currentAttempt, pollingEvery)
                    }

                    if (body.find(rootSubitemLocator).length > 0) {
                        return;
                    }

                    //If project root sub item is not present, collapse project folder, open project folder and retry again
                    cy.log(`**Root sub item '${rootSubitem}' has not benn found (attempt ${currentAttempt} of ${attempts})**`)
                    currentAttempt++
                    this.collapseItem(rootItem)
                    this.waitItemCollapsed(rootItem)
                    cy.wait(pollingEvery)
                    this.doWaitProjectImported(projectName, rootSubitem, attempts, currentAttempt, pollingEvery)
                })
        })
    }



}
