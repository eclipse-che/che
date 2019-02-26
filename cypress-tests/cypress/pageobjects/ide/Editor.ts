/// <reference types="Cypress" />

import { EditorLine } from "./EditorLine";

export class Editor{
    
    private getTabLocator(itemPath: string){
        return `li[title='${itemPath}']`;
    }

    waitTab(itemPath: string, editorTabTitle: string){
        it(`Wait editor tab of the \"${itemPath}\" element with a \"${editorTabTitle}\" title`, ()=>{
            cy.get(this.getTabLocator(itemPath))
                .contains(editorTabTitle);
        })
    }

    waitTabDisappearance(itemPath: string){
        it(`Wait disappearance of the editor tab of the \"${itemPath}\" element`, ()=>{
            cy.get(this.getTabLocator(itemPath))
                .should('not.exist');
        })
    }

    clickOnTab(itemPath:string){
        it(`Click on editor tab of the \"${itemPath}\" element`, ()=>{
            cy.get(this.getTabLocator(itemPath)).should('be.visible').click();
        })
    }

    waitTabFocused(itemPath: string){
        it(`Wait until editor tab of the \"${itemPath}\" element is focused`, ()=>{
            cy.get(this.getTabLocator(itemPath)).should('have.class', 'theia-mod-active');
        })
    }

    closeTab(itemPath: string){
        it(`Close editor tab of the \"${itemPath}\" by clicking on close icon`, ()=>{
            cy.get(this.getTabLocator(itemPath)).should('be.visible')
                .children('.p-TabBar-tabCloseIcon').should('be.visible')
                    .click();
        })
    }
    
    //#################################################################

    getEditorLines(): Array<string> {
        let linesLocator: string = ".lines-content .view-line";
        let linesArray: Array<EditorLine> = new Array();
        let linesText: Array<string> = new Array(); 

        it("Get editor text", ()=>{
            cy.get(linesLocator)
                .each((el, index, list) => {
                    let lineCoordinate: number;
                    let lineText: string;

                    cy.wrap(el).invoke('attr', 'style').then(style => {
                        let styleValue: string = "" + style;

                        let valueArray: string[] = styleValue.split(';');
                        let pixelsCoordinate: string = valueArray[0];
                        pixelsCoordinate = pixelsCoordinate.replace(/top:/gi, "");
                        pixelsCoordinate = pixelsCoordinate.replace(/px/gi, "");

                        lineCoordinate = + pixelsCoordinate;
                    }).then(()=>{
                        cy.wrap(el).invoke('text').then(text => {
                            lineText = "" + text;
                        });
                    }).then(()=>{
                        linesArray.push(new EditorLine(lineCoordinate, lineText));
                    });

            }).then(()=>{
                linesArray = linesArray.sort((editorLine1, editorLine2) => {
                    return editorLine1.getLinePixelsCoordinate() - editorLine2.getLinePixelsCoordinate()})
            }).then(()=>{
                linesArray.forEach( editorLine =>{
                    linesText.push(editorLine.getLineText());
                })
            })

        });

        return linesText;
    }


    checkText(expectedText: string){
        let editorLines: Array<string> = this.getEditorLines();
        let editorText: string = editorLines.join('\n');


        it("Check that expected text is present in the editor", ()=>{
            console.log(editorText);
            
            let re = new RegExp(expectedText, "gi");
            
            assert(editorText.search(re) > 0)
        })

    }




    //#################################################################






}
