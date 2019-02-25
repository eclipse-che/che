/// <reference types="Cypress" />


export class ProjectTree{

    private static readonly PROJECT_TREE_CONTAINER: string = "#theia-left-side-panel .theia-TreeContainer";

    private getItemId(itemPath: string): string {
        return `div[id='/projects:/projects/${itemPath}']`;
    }

    private getItemExpandIconLocator(itemPath: string): string{
        return `div[data-node-id='/projects:/projects/${itemPath}']`;
    }

    private getColapsedItemLocator(itemPath: string): string{
        return `${this.getItemExpandIconLocator(itemPath)}.theia-mod-collapsed`;
    }

    private getExpandedItemLocator(itemPath: string): string{
        return `${this.getItemExpandIconLocator(itemPath)}:not(.theia-mod-collapsed)`;
    }
    
    waitItemExpanded(itemPath: string){
        it(`Wait until item \"${itemPath}\" is expanded`, ()=>{
            cy.get(this.getExpandedItemLocator(itemPath)).should('be.visible');
        })
    }

    waitItemColapsed(itemPath: string){
        it(`Wait until item \"${itemPath}\" is colapsed`, ()=>{
            cy.get(this.getColapsedItemLocator(itemPath)).should('be.visible');
        })
    }

    waitProjectTreeContainer(){
        it("Wait until project tree container is opened", ()=>{
            cy.get(ProjectTree.PROJECT_TREE_CONTAINER)
            .should('be.visible')
            .should('not.have.class', 'animating');
        })
    }

    waitProjectTreeContainerClosed(){
        it("Wait until project tree container is closed", ()=>{
            cy.get(ProjectTree.PROJECT_TREE_CONTAINER).should('not.be.visible');
        })
    }

    waitItemVisibility(itemPath: string){
        it(`Wait until item \"${itemPath}\" is visible`, ()=>{
            cy.get(this.getItemId(itemPath)).should('be.visible');
        })
    }

    waitItemDisappearance(itemPath: string){
        it(`Wait until item \"${itemPath}\" is unvisible`, ()=>{
            cy.get(this.getItemId(itemPath)).should('not.be.visible');
        })
    }

    clickOnItem(itemPath: string){

        this.waitItemVisibility(itemPath);

        it(`Perform click on \"${itemPath}\" item`, ()=>{
            cy.get(this.getItemId(itemPath)).click();
        })

        this.waitItemSelected(itemPath);
    }

    doubleClickOnItem(itemPath: string){
        this.waitItemVisibility(itemPath);

        it(`Perform double click on \"${itemPath}\" item`, ()=>{
            cy.get(this.getItemId(itemPath)).dblclick();
        })

        this.waitItemSelected(itemPath);
    }

    waitItemSelected(itemPath: string){
        let selectedItemLocator: string = `div[title='/projects/${itemPath}'].theia-mod-selected.theia-mod-focus`;

        it(`Wait until \"${itemPath}\" project is selected and focused`, ()=>{
            cy.get(selectedItemLocator).should('be.visible');
        })
    }








































} 