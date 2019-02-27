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

    private getEditorLines(checkFunction: (lines: Array<String>) => void) {
        let linesLocator: string = ".lines-content .view-line";
        let linesArray: Array<EditorLine> = new Array();
        let linesText: Array<string> = new Array();

        cy.log("Get text from editor");

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
            }).should(() => {
                checkFunction(linesText);
            });

    }


    checkTextPresence(regexp: string) {
        let isTextPresent = (editorLines: Array<string>) => {
            assert
                .isTrue(
                    editorLines.join('\n')
                        .search(new RegExp(regexp)) > 0, "Have no string matches with provided regexp in the editor");
        }

        this.getEditorLines(isTextPresent);
    }

    checkTextAbsence(regexp: string) {
        let isTextAbsent = (editorLines: Array<string>) => {
            assert
                .isTrue(
                    editorLines.join('\n').search(new RegExp(regexp)) < 1, "At least one match with provided regexp has been found in the editor"
                )
        }

        this.getEditorLines(isTextAbsent);
    }




}


    //#################################################################






