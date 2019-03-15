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

export class NewWorkspace {

    private static readonly CHE_7_STACK: string = "div[data-stack-id='che7-preview']";
    private static readonly SELECTED_CHE_7_STACK: string = ".stack-selector-item-selected[data-stack-id='che7-preview']"
    private static readonly CREATE_AND_OPEN_BUTTON: string = "che-button-save-flat[che-button-title='Create & Open']>button"
    private static readonly ADD_OR_IMPORT_PROJECT_BUTTON: string = ".add-import-project-toggle-button";
    private static readonly WEB_JAVA_SPRING_CHECKBOX: string = "#sample-web-java-spring>md-checkbox>div";
    private static readonly WEB_JAVA_SPRING_CHECKBOX_ENABLED: string = "#sample-web-java-spring>md-checkbox[aria-checked='true']";
    private static readonly ADD_BUTTON: string = "button[aria-disabled='false'][name='addButton']";
    private static readonly NAME_FIELD: string = "#workspace-name-input";


    typeWorkspaceName(workspaceName: string) {
        cy.get(NewWorkspace.NAME_FIELD)
            .clear()
            .should('have.value', "")
            .type(workspaceName)
            .should('have.value', workspaceName);
    }

    clickOnChe7Stack() {
        cy.get(NewWorkspace.CHE_7_STACK)
            .click();
    }

    waitChe7StackSelected() {
        cy.get(NewWorkspace.SELECTED_CHE_7_STACK)
            .should('be.visible');
    }

    clickOnCreateAndOpenButton() {
        let ideFrameLocator: string = "ide-iframe#ide-iframe-window";

        cy.get(NewWorkspace.CREATE_AND_OPEN_BUTTON)
            .first()
            .should('be.visible')
            .click();

        //check that the workspace has started to boot
        cy.get(ideFrameLocator)
            .should('have.attr', 'aria-hidden', 'false')
    }

    clickOnAddOrImportProjectButton() {
        cy.get(NewWorkspace.ADD_OR_IMPORT_PROJECT_BUTTON)
            .should('be.visible')
            .click();
    }

    enableWebJavaSpringCheckbox() {
        cy.get(NewWorkspace.WEB_JAVA_SPRING_CHECKBOX)
            .first()
            .click({ force: true });

        //check that checkbox is succesfully enabled
        cy.get(NewWorkspace.WEB_JAVA_SPRING_CHECKBOX_ENABLED);
    }

    clickOnAddButton() {
        cy.get(NewWorkspace.ADD_BUTTON)
            .should('be.visible')
            .click();
    }

}
