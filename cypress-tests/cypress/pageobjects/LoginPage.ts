/// <reference types="Cypress" />

export class LoginPage {

    private static readonly USERNAME_FIELD: string = "#username";
    private static readonly PASSWORD_FIELD: string = "#password";
    private static readonly LOGIN_BUTTON: string = "[name='login']";
    private static readonly PAGE_URL: string = "http://che-eclipse-che.192.168.0.104.nip.io";

private typeToInputField(text:string, fieldLocator:string){
    it("Click on the field and wait it focusing", ()=>{
        cy.get(fieldLocator).click();



    })

    // it("Clear textfield", ()=>{
    //     cy.get(fieldLocator).focus().clear();
    // });

    it("Type text", ()=>{
        cy.get(fieldLocator).focus().type(text);
    });
}

visitLoginPage(){
    it("Go to login page", ()=>{
        cy.visit(LoginPage.PAGE_URL);
    });
}

typeUsername(username:string){
    this.typeToInputField(username, LoginPage.USERNAME_FIELD);
}    

typePassword(password:string){
    this.typeToInputField(password, LoginPage.PASSWORD_FIELD);
}

clickOnLoginButton(){
    it("Click on login button", ()=>{
        cy.get(LoginPage.LOGIN_BUTTON).click();
    })
}




















}