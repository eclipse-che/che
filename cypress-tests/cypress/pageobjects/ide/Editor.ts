/// <reference types="Cypress" />

import { EditorLine } from "./EditorLine";
import { ProposalWidget } from "../ide/ProposalWidget";
import { Promise, reject } from "bluebird";

export class Editor {

    private static readonly EDITOR_LINES: string = ".lines-content .view-line";
    private static readonly EDITOR_BODY: string = "#theia-main-content-panel .lines-content";
    private static readonly SUGGESTION_WIDGET_BODY: string = "div[widgetId='editor.widget.suggestWidget']"
    private static readonly SUGGESTION_WIDGET_ROW: string = "div[widgetId='editor.widget.suggestWidget'] .monaco-list-row";

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

    private extractPixelCoordinate(element: JQuery<HTMLElement>): number {
        let lineCoordinate: number;

        cy.wrap(element).invoke('attr', 'style').then(style => {
            let styleValue: string = "" + style;

            let valueArray: string[] = styleValue.split(';');
            let pixelsCoordinate: string = valueArray[0];
            pixelsCoordinate = pixelsCoordinate.replace(/top:/gi, "");
            pixelsCoordinate = pixelsCoordinate.replace(/px/gi, "");

            lineCoordinate = + pixelsCoordinate;
        })

        return lineCoordinate;
    }

    public addAttributeToLines(): Promise<number> {
        return new Promise<number>((resolve, reject) => {
            let elementsArray: Array<JQuery<HTMLElement>> = new Array();

            cy.get(Editor.EDITOR_LINES)
                .each((el, index, list) => {
                    elementsArray.push(el)
                }).then(() => {
                    elementsArray = elementsArray.sort((element1, element2) => {
                        return this.extractPixelCoordinate(element1) - this.extractPixelCoordinate(element2);
                    })
                }).then(() => {
                    elementsArray.forEach((element, index) => {
                        element[0].setAttribute("data-cy", `editor-line-${index + 1}`)
                    })
                }).then(() => {
                    resolve(elementsArray.length);
                })
        })
    }

    public getEditorLines(checkFunction: (lines: Array<String>) => void) {
        let linesText: Array<string> = new Array();

        cy.get('body').then(() => {
            this.addAttributeToLines().then(linesCapacity => {
                let i: number;

                for (i = 1; i <= linesCapacity; i++) {
                    cy.get(`div[data-cy='editor-line-${i}']`).invoke('text').then(text => {
                        linesText.push("" + text);
                    })
                }
            })
        }).should(() => {
            checkFunction(linesText);
        })

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

    setCursorToLineAndChar(lineNumber: number, charNumber: number) {
        this.waitEditorOpened()

        //set cursor to the first line
        cy.get('#theia-main-content-panel')
            .should('be.visible')
            .trigger("keydown", { keyCode: 40, which: 40 })
            .trigger("keydown", { keyCode: 40, which: 40 })
            .trigger("keydown", { keyCode: 36, which: 36, ctrlKey: true })
            .then(() => {
                //move cursor to specified line
                let lineIndex;

                for (lineIndex = 1; lineIndex < lineNumber; lineIndex++) {
                    cy.get('#theia-main-content-panel').trigger("keydown", { keyCode: 40, which: 40 })
                }
            }).then(() => {
                //move cursor to specified char
                let charIndex;

                for (charIndex = 1; charIndex < charNumber; charIndex++) {
                    cy.get('#theia-main-content-panel').trigger("keydown", { keyCode: 39, which: 39 })
                }
            });

    }

    performControlSpaceCombination() {
        this.waitEditorOpened()

        cy.get('#theia-main-content-panel')
            .should('be.visible')
            .trigger("keydown", { keyCode: 32, which: 32, ctrlKey: true })
    }


    private getLineLocator(lineNumber: number): string {
        return `div[data-cy='editor-line-${lineNumber}']>span`;
    }

    typeToLine(lineNumber: number, text: string) {
        //workaround for avoiding random cursor placement
        this.addAttributeToLines()
            .then(linesCapacity => {
                cy.get(this.getLineLocator(1))
                    .then(element => {
                        element[0].setAttribute('contenteditable', '');
                    })
                    .type("{leftarrow}")
            })
            .then(() => {
                this.addAttributeToLines().then(linesCapacity => {
                    cy.get(this.getLineLocator(lineNumber))
                        .then(element => {
                            element[0].setAttribute('contenteditable', '');
                        })
                        .type(text, { force: true });
                })
            })
    }































}






