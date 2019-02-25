/// <reference types="Cypress" />

export class NewWorkspace{

    private static readonly CHE_7_STACK: string = "div[data-stack-id='che7-preview']";
    private static readonly SELECTED_CHE_7_STACK: string = ".stack-selector-item-selected[data-stack-id='che7-preview']"
    private static readonly CREATE_AND_OPEN_BUTTON: string = "che-button-save-flat[che-button-title='Create & Open']>button"
    private static readonly ADD_OR_IMPORT_PROJECT_BUTTON: string = ".add-import-project-toggle-button";
    private static readonly WEB_JAVA_SPRING_CHECKBOX: string = "#sample-web-java-spring>md-checkbox>div";
    private static readonly WEB_JAVA_SPRING_CHECKBOX_ENABLED: string = "#sample-web-java-spring>md-checkbox[aria-checked='true']";
    private static readonly ADD_BUTTON: string = "button[aria-disabled='false'][name='addButton']";
    private static readonly NAME_FIELD: string = "#workspace-name-input";


    typeWorkspaceName(workspaceName: string){
        it("Type workspace name", ()=>{
            cy.get(NewWorkspace.NAME_FIELD)
            .clear()
            .type(workspaceName);
        })
    }


    clickOnChe7Stack(){
        it("Click on \"Che 7\" stack on the \"New Workspace\" page", ()=>{
            cy.get(NewWorkspace.CHE_7_STACK).click();
        })
    }
 
    waitChe7StackSelected(){
        it("Wait until \"Che 7\" stack on the \"New Workspace\" page is selected", ()=>{
            cy.get(NewWorkspace.SELECTED_CHE_7_STACK);    
        })
    }

    clickOnCreateAndOpenButton(){
        it("Click on \"CREATE & OPEN\" button on the \"New Workspace\" page", ()=>{
            cy.get(NewWorkspace.CREATE_AND_OPEN_BUTTON).first().click();
        })
    }

    clickOnAddOrImportProjectButton(){
        it("Click on \"Add or Import Project\" button on the \"New Workspace\" page", ()=>{
            cy.get(NewWorkspace.ADD_OR_IMPORT_PROJECT_BUTTON).click();
        })
    }

    enableWebJavaSpringCheckbox(){
        it("Click on \"web-java-spring\" checkbox on the \"New Workspace\" page", ()=>{
            cy.get(NewWorkspace.WEB_JAVA_SPRING_CHECKBOX).first().click( { force:true } ); 
        })

        it("Wait enabling of \"web-java-spring\" checkbox on the \"New Workspace\" page", ()=>{
            cy.get(NewWorkspace.WEB_JAVA_SPRING_CHECKBOX_ENABLED);
        })
    }

    clickOnAddButton(){
        it("Click on \"Add\" button from \"Add or Import Project\" form on the \"New Workspace\" page", ()=>{
            cy.get(NewWorkspace.ADD_BUTTON).click(); 
        })
    }










}