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
var LoginPage = /** @class */ (function () {
    function LoginPage() {
    }
    LoginPage.prototype.typeToInputField = function (text, fieldLocator) {
        cy.get(fieldLocator)
            .click()
            .focus()
            .type(text);
    };
    LoginPage.prototype.visitLoginPage = function () {
        cy.visit("/");
    };
    LoginPage.prototype.typeUsername = function (username) {
        this.typeToInputField(username, LoginPage.USERNAME_FIELD);
    };
    LoginPage.prototype.typePassword = function (password) {
        this.typeToInputField(password, LoginPage.PASSWORD_FIELD);
    };
    LoginPage.prototype.clickOnLoginButton = function () {
        cy.get(LoginPage.LOGIN_BUTTON).click();
    };
    LoginPage.prototype.waitPage = function () {
        [LoginPage.LOGIN_BUTTON, LoginPage.USERNAME_FIELD, LoginPage.PASSWORD_FIELD]
            .forEach(function (elementLocator) {
            cy.get(elementLocator, { timeout: LoginPage.LOAD_PAGE_TIMEOUT }).should('be.visible');
        });
    };
    LoginPage.prototype.waitPageAbcence = function () {
        [LoginPage.LOGIN_BUTTON, LoginPage.USERNAME_FIELD, LoginPage.PASSWORD_FIELD]
            .forEach(function (elementLocator) {
            cy.get(elementLocator, { timeout: LoginPage.LOAD_PAGE_TIMEOUT }).should('not.be.visible');
        });
    };
    LoginPage.prototype.login = function (username, password) {
        this.waitPage();
        this.typeUsername(username);
        this.typePassword(password);
        this.clickOnLoginButton();
        this.waitPageAbcence();
    };
    LoginPage.prototype.defaultLogin = function () {
        this.login(LoginPage.TEST_USER_NANE, LoginPage.TEST_USER_PASSWORD);
    };
    LoginPage.LOAD_PAGE_TIMEOUT = Cypress.env('load_page_timeout');
    LoginPage.TEST_USER_NANE = Cypress.env('test_user_name');
    LoginPage.TEST_USER_PASSWORD = Cypress.env('test_user_password');
    LoginPage.USERNAME_FIELD = "#username";
    LoginPage.PASSWORD_FIELD = "#password";
    LoginPage.LOGIN_BUTTON = "[name='login']";
    return LoginPage;
}());
export { LoginPage };
