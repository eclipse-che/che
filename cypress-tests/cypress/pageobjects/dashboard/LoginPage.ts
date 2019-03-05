/// <reference types="Cypress" />

export class LoginPage {
    private static readonly LOAD_PAGE_TIMEOUT: number = Cypress.env('load_page_timeout');
    private static readonly TEST_USER_NANE: string = Cypress.env('test_user_name');
    private static readonly TEST_USER_PASSWORD: string = Cypress.env('test_user_password');


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

    waitPage() {
        [LoginPage.LOGIN_BUTTON, LoginPage.USERNAME_FIELD, LoginPage.PASSWORD_FIELD]
            .forEach(elementLocator => {
                cy.get(elementLocator, { timeout: LoginPage.LOAD_PAGE_TIMEOUT }).should('be.visible');
            })
    }

    waitPageAbcence(){
        [LoginPage.LOGIN_BUTTON, LoginPage.USERNAME_FIELD, LoginPage.PASSWORD_FIELD]
            .forEach(elementLocator => {
                cy.get(elementLocator, { timeout: LoginPage.LOAD_PAGE_TIMEOUT }).should('not.be.visible');
            })
    }

    login(username: string, password: string){
        this.waitPage();
        this.typeUsername(username);
        this.typePassword(password);
        this.clickOnLoginButton();
        this.waitPageAbcence();
    }

    defaultLogin(){
        this.login(LoginPage.TEST_USER_NANE, LoginPage.TEST_USER_PASSWORD);
    }






















}