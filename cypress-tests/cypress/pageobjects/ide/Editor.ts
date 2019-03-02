/// <reference types="Cypress" />

import { EditorLine } from "./EditorLine";
import { ProposalWidget } from "../ide/ProposalWidget";

export class Editor {

    private static readonly EDITOR_LINES: string = ".lines-content .view-line";
    private static readonly EDITOR_BODY: string = "#theia-main-content-panel .lines-content";

    private readonly proposalWidget: ProposalWidget = new ProposalWidget();


    private getTabLocator(itemPath: string) {
        return `li[title='${itemPath}'] .p-TabBar-tabLabel`;
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
        cy.get(`li[title='${itemPath}'].theia-mod-active`).should('be.visible').wait(2000);
    }

    closeTab(itemPath: string) {
        cy.get(this.getTabLocator(itemPath)).should('be.visible')
            .children('.p-TabBar-tabCloseIcon').should('be.visible')
            .click();
    }

    waitEditorOpened() {
        cy.get(Editor.EDITOR_BODY).should('be.visible');
        cy.get(Editor.EDITOR_LINES).first().should('be.visible');
    }

    waitEditorAvailable(itemPath: string, tabTitle: string) {
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

    performCtrlKeyCombination(buttonCode: number) {
        this.waitEditorOpened();
        
        cy.get('#theia-main-content-panel')
        .trigger("keydown", { keyCode: buttonCode, which: buttonCode, ctrlKey: true })
    }

    setCursorToLine(lineNumber: number) {

        this.performCtrlKeyCombination(71);

        this.proposalWidget.waitWidget();
        this.proposalWidget.typeToInputFieldAndPressEnter(lineNumber.toString())
        this.proposalWidget.waitWidgetClosed();
    }

    performFindFileKeyShortcut() {
        // this.performKeyCombination('{ctrl}P')
    
    }

    waitSuggestionContainer(){
        
    }




























}






