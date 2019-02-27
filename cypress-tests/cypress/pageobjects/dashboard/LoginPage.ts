/// <reference types="Cypress" />

export class LoginPage {

    private static readonly USERNAME_FIELD: string = "#username";
    private static readonly PASSWORD_FIELD: string = "#password";
    private static readonly LOGIN_BUTTON: string = "[name='login']";
    private static readonly PAGE_URL: string = "http://che-eclipse-che.192.168.0.104.nip.io";

    private typeToInputField(text: string, fieldLocator: string) {
        cy.get(fieldLocator)
            .click()
            .focus()
            .type(text);
    }

    visitLoginPage() {
        cy.visit(LoginPage.PAGE_URL);
    }

    typeUsername(username: string) {
        this.typeToInputField(username, LoginPage.USERNAME_FIELD);
    }

    typePassword(password: string) {
        this.typeToInputField(password, LoginPage.PASSWORD_FIELD);
    }

    clickOnLoginButton() {
        cy.get(LoginPage.LOGIN_BUTTON).click();
    }




















}