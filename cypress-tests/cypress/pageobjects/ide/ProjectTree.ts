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
        cy.get(this.getExpandedItemLocator(itemPath)).should('be.visible');
    }

    waitItemColapsed(itemPath: string){
        cy.get(this.getColapsedItemLocator(itemPath)).should('be.visible');
    }

    waitProjectTreeContainer(){
        cy.get(ProjectTree.PROJECT_TREE_CONTAINER)
            .should('be.visible')
            .should('not.have.class', 'animating');
    }

    waitProjectTreeContainerClosed(){
        cy.get(ProjectTree.PROJECT_TREE_CONTAINER).should('not.be.visible');
    }

    waitItemVisibility(itemPath: string){
        cy.get(this.getItemId(itemPath)).should('be.visible');
    }

    waitItemDisappearance(itemPath: string){
        cy.get(this.getItemId(itemPath)).should('not.be.visible');
    }

    clickOnItem(itemPath: string){

        this.waitItemVisibility(itemPath);

        cy.get(this.getItemId(itemPath)).click();
        this.waitItemSelected(itemPath);
    }

    doubleClickOnItem(itemPath: string){
        this.waitItemVisibility(itemPath);

        cy.get(this.getItemId(itemPath)).dblclick();
        this.waitItemSelected(itemPath);
    }

    waitItemSelected(itemPath: string){
        let selectedItemLocator: string = `div[title='/projects/${itemPath}'].theia-mod-selected.theia-mod-focus`;

        cy.get(selectedItemLocator).should('be.visible');
    }
    
}
