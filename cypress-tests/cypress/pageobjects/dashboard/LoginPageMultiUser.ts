import { ILoginPage } from "./interfaces/ILoginPage";
import { Dashboard } from "./Dashboard";
import { injectable, inject } from "inversify";
import "reflect-metadata";


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

@injectable()
export class LoginPageMultiUser implements ILoginPage {
    private static readonly LOAD_PAGE_TIMEOUT: number = Cypress.env('load_page_timeout');
    private static readonly TEST_USER_NANE: string = Cypress.env('test_user_name');
    private static readonly TEST_USER_PASSWORD: string = Cypress.env('test_user_password');

    private readonly dashboard: Dashboard = new Dashboard();


    private static readonly USERNAME_FIELD: string = "#username";
    private static readonly PASSWORD_FIELD: string = "#password";
    private static readonly LOGIN_BUTTON: string = "[name='login']";

    private typeToInputField(text: string, fieldLocator: string) {
        cy.get(fieldLocator)
            .click()
            .focus()
            .type(text);
    }

    private visitLoginPage() {
        cy.visit("/");
    }

    private typeUsername(username: string) {
        this.typeToInputField(username, LoginPageMultiUser.USERNAME_FIELD);
    }

    private typePassword(password: string) {
        this.typeToInputField(password, LoginPageMultiUser.PASSWORD_FIELD);
    }

    private clickOnLoginButton() {
        cy.get(LoginPageMultiUser.LOGIN_BUTTON)
            .click();
    }

    private waitPage() {
        [LoginPageMultiUser.LOGIN_BUTTON, LoginPageMultiUser.USERNAME_FIELD, LoginPageMultiUser.PASSWORD_FIELD]
            .forEach(elementLocator => {
                cy.get(elementLocator, { timeout: LoginPageMultiUser.LOAD_PAGE_TIMEOUT })
                    .should('be.visible');
            })
    }

    private waitPageAbcence() {
        [LoginPageMultiUser.LOGIN_BUTTON, LoginPageMultiUser.USERNAME_FIELD, LoginPageMultiUser.PASSWORD_FIELD]
            .forEach(elementLocator => {
                cy.get(elementLocator, { timeout: LoginPageMultiUser.LOAD_PAGE_TIMEOUT })
                    .should('not.be.visible');
            })
    }

    login() {
        this.visitLoginPage();
        this.waitPage();
        this.typeUsername(LoginPageMultiUser.TEST_USER_NANE);
        this.typePassword(LoginPageMultiUser.TEST_USER_PASSWORD);
        this.clickOnLoginButton();
        this.waitPageAbcence();
    }

}
