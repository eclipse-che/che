/// <reference types="Cypress" />

import { EditorLine } from "./EditorLine";
import { Promise } from "bluebird";

export class Editor {

private static readonly EDITOR_LINES: string = ".lines-content .view-line"

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

    waitEditorOpened(){
        cy.get(Editor.EDITOR_LINES).first().should('be.visible');
    }

    waitEditorAvailable(itemPath: string, tabTitle: string){
        this.waitTab(itemPath, tabTitle);
        this.waitEditorOpened();
    }

    private getEditorLines(checkFunction: (lines: Array<String>) => void) {
        let linesArray: Array<EditorLine> = new Array();
        let linesText: Array<string> = new Array();

        cy.get(Editor.EDITOR_LINES)
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

    checkLineTextContains(lineNumber: number, regexp: string) {
        let isTextPresentInLine = (editorLines: Array<string>) => {
            let lineText: string = editorLines[lineNumber];
            let re = new RegExp(regexp);

            // console.log("==>>  0 ", editorLines[0])
            // console.log("==>>  1 ", editorLines[1])
            // console.log("==>>  2 ", editorLines[2])
            // console.log("==>>  3 ", editorLines[3])
            // console.log("==>>  4 ", editorLines[4])
            // console.log("==>>  5 ", editorLines[5])
            // console.log("==>>  6 ", editorLines[6])
            // console.log("==>>  7 ", editorLines[7])
            // console.log("==>>  8 ", editorLines[8])
            // console.log("==>>  9 ", editorLines[9])
            // console.log("==>> 10 ", editorLines[10])
            // console.log("==>> 11 ", editorLines[11])
            // console.log("==>> 12 ", editorLines[12])
            // console.log("==>> 13 ", editorLines[13])



            assert
                .isTrue(
                    lineText.search(re) > 0, `Have no string matches with provided regexp in the \"${lineNumber}\ " \"${lineText}\" editor line`
                );
        }

        this.getEditorLines(isTextPresentInLine);
    }

    checkLineTextAbsence(lineNumber: number, regexp: string) {
        let isTextAbsentInLine = (editorLines: Array<string>) => {
            let lineText: string = editorLines[lineNumber];
            let re = new RegExp(regexp);

            assert
                .isTrue(
                    lineText.search(re) < 1, `At least one match with provided regexp has been found in the \"${lineNumber}\ " \"${lineText}\" editor line`
                );
        }

        this.getEditorLines(isTextAbsentInLine);
    }






}






