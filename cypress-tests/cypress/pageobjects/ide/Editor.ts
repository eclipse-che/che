/// <reference types="Cypress" />

import { EditorLine } from "./EditorLine";
import { Promise } from "bluebird";

export class Editor {

    private getTabLocator(itemPath: string) {
        return `li[title='${itemPath}']`;
    }

    waitTab(itemPath: string, editorTabTitle: string) {
        cy.get(this.getTabLocator(itemPath))
            .contains(editorTabTitle);
    }

    waitTabDisappearance(itemPath: string) {
        cy.get(this.getTabLocator(itemPath))
            .should('not.exist');
    }

    clickOnTab(itemPath: string) {
        cy.get(this.getTabLocator(itemPath)).should('be.visible').click();
    }

    waitTabFocused(itemPath: string) {
        cy.get(this.getTabLocator(itemPath)).should('have.class', 'theia-mod-active');
    }

    closeTab(itemPath: string) {
        cy.get(this.getTabLocator(itemPath)).should('be.visible')
            .children('.p-TabBar-tabCloseIcon').should('be.visible')
            .click();
    }

    //#################################################################

    getEditorLines(): Promise<Array<string>> {
        let linesLocator: string = ".lines-content .view-line";
        let linesArray: Array<EditorLine> = new Array();
        let linesText: Array<string> = new Array();

        return new Promise((resolve) => {

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
                    }).then(() => {
                        cy.wrap(el).invoke('text').then(text => {
                            lineText = "" + text;
                        });
                    }).then(() => {
                        linesArray.push(new EditorLine(lineCoordinate, lineText));
                    });

                }).then(() => {
                    linesArray = linesArray.sort((editorLine1, editorLine2) => {
                        return editorLine1.getLinePixelsCoordinate() - editorLine2.getLinePixelsCoordinate()
                    })
                }).then(() => {
                    linesArray.forEach(editorLine => {
                        linesText.push(editorLine.getLineText());
                    });
                    resolve(linesText);
                })
        });
    }


    checkText(expectedText: string) {
        this.getEditorLines().then(lines => {
            cy.log("Check that text is present in the editor");

            let editorText: string = lines.join('\n'); 
            let isTextPresent: boolean = editorText.search(new RegExp(expectedText, "gi")) > 0;


            console.log("========>>>>>>   ", isTextPresent);
        });
    }




    //#################################################################






}
