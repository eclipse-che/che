/*********************************************************************
 * Copyright (c) 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
/// <reference types="Cypress" />
import { Promise } from "bluebird";
var Editor = /** @class */ (function () {
    function Editor() {
    }
    Editor.prototype.waitSuggestionContainer = function () {
        cy.get(Editor.SUGGESTION_WIDGET_BODY).should('be.visible');
    };
    Editor.prototype.waitSuggestionContainerClosed = function () {
        cy.get(Editor.SUGGESTION_WIDGET_BODY).should('be.not.visible');
    };
    Editor.prototype.clickOnSuggestion = function (suggestion) {
        cy.get(Editor.SUGGESTION_WIDGET_ROW)
            .contains(suggestion)
            .first()
            .click();
    };
    Editor.prototype.waitSuggestion = function (suggestion) {
        cy.get(Editor.SUGGESTION_WIDGET_ROW)
            .contains(suggestion);
    };
    Editor.prototype.getTabLocator = function (itemPath) {
        return "li[title='" + itemPath + "'] .p-TabBar-tabLabel";
    };
    Editor.prototype.waitTab = function (itemPath, editorTabTitle) {
        cy.get(this.getTabLocator(itemPath))
            .contains(editorTabTitle);
    };
    Editor.prototype.waitTabDisappearance = function (itemPath) {
        cy.get(this.getTabLocator(itemPath))
            .should('not.exist');
    };
    Editor.prototype.clickOnTab = function (itemPath) {
        cy.get(this.getTabLocator(itemPath)).should('be.visible').click();
    };
    Editor.prototype.waitTabFocused = function (itemPath) {
        cy.get("li[title='" + itemPath + "'].theia-mod-active").should('be.visible').wait(2000);
    };
    Editor.prototype.closeTab = function (itemPath) {
        cy.get(this.getTabLocator(itemPath)).should('be.visible')
            .children('.p-TabBar-tabCloseIcon').should('be.visible')
            .click();
    };
    Editor.prototype.waitEditorOpened = function () {
        cy.get(Editor.EDITOR_BODY).should('be.visible');
        cy.get(Editor.EDITOR_LINES).first().should('be.visible');
    };
    Editor.prototype.waitEditorAvailable = function (itemPath, tabTitle) {
        this.waitTab(itemPath, tabTitle);
        this.waitEditorOpened();
    };
    Editor.prototype.extractPixelCoordinate = function (element) {
        var lineCoordinate;
        cy.wrap(element).invoke('attr', 'style').then(function (style) {
            var styleValue = "" + style;
            var valueArray = styleValue.split(';');
            var pixelsCoordinate = valueArray[0];
            pixelsCoordinate = pixelsCoordinate.replace(/top:/gi, "");
            pixelsCoordinate = pixelsCoordinate.replace(/px/gi, "");
            lineCoordinate = +pixelsCoordinate;
        });
        return lineCoordinate;
    };
    Editor.prototype.addAttributeToLines = function () {
        var _this = this;
        return new Promise(function (resolve, reject) {
            var elementsArray = new Array();
            cy.get(Editor.EDITOR_LINES)
                .each(function (el, index, list) {
                elementsArray.push(el);
            }).then(function () {
                elementsArray = elementsArray.sort(function (element1, element2) {
                    return _this.extractPixelCoordinate(element1) - _this.extractPixelCoordinate(element2);
                });
            }).then(function () {
                elementsArray.forEach(function (element, index) {
                    element[0].setAttribute("data-cy", "editor-line-" + (index + 1));
                });
            }).then(function () {
                resolve(elementsArray.length);
            });
        });
    };
    Editor.prototype.getEditorLines = function (checkFunction) {
        var _this = this;
        var linesText = new Array();
        cy.get('body').then(function () {
            _this.addAttributeToLines().then(function (linesCapacity) {
                var i;
                for (i = 1; i <= linesCapacity; i++) {
                    cy.get("div[data-cy='editor-line-" + i + "']").invoke('text').then(function (text) {
                        linesText.push("" + text);
                    });
                }
            });
        }).should(function () {
            checkFunction(linesText);
        });
    };
    Editor.prototype.checkTextPresence = function (regexp) {
        var isTextPresent = function (editorLines) {
            assert
                .isTrue(editorLines.join('\n')
                .search(new RegExp(regexp)) > 0, "Have no string matches with provided regexp in the editor");
        };
        this.getEditorLines(isTextPresent);
    };
    Editor.prototype.checkTextAbsence = function (regexp) {
        var isTextAbsent = function (editorLines) {
            assert
                .isTrue(editorLines.join('\n').search(new RegExp(regexp)) < 1, "At least one match with provided regexp has been found in the editor");
        };
        this.getEditorLines(isTextAbsent);
    };
    Editor.prototype.checkLineTextContains = function (lineNumber, regexp) {
        var isTextPresentInLine = function (editorLines) {
            var lineText = editorLines[lineNumber - 1];
            var re = new RegExp(regexp);
            assert
                .isTrue(lineText.search(re) > 0, "Have no string matches with provided regexp in the \"" + lineNumber + " \" \"" + lineText + "\" editor line");
        };
        this.getEditorLines(isTextPresentInLine);
    };
    Editor.prototype.checkLineTextAbsence = function (lineNumber, regexp) {
        var isTextAbsentInLine = function (editorLines) {
            var lineText = editorLines[lineNumber - 1];
            var re = new RegExp(regexp);
            assert
                .isTrue(lineText.search(re) < 1, "At least one match with provided regexp has been found in the \"" + lineNumber + " \" \"" + lineText + "\" editor line");
        };
        this.getEditorLines(isTextAbsentInLine);
    };
    Editor.prototype.setCursorToLineAndChar = function (lineNumber, charNumber) {
        this.waitEditorOpened();
        //set cursor to the first line
        cy.get('#theia-main-content-panel')
            .should('be.visible')
            .trigger("keydown", { keyCode: 40, which: 40 })
            .trigger("keydown", { keyCode: 40, which: 40 })
            .trigger("keydown", { keyCode: 36, which: 36, ctrlKey: true })
            .then(function () {
            //move cursor to specified line
            var lineIndex;
            for (lineIndex = 1; lineIndex < lineNumber; lineIndex++) {
                cy.get('#theia-main-content-panel').trigger("keydown", { keyCode: 40, which: 40 });
            }
        }).then(function () {
            //move cursor to specified char
            var charIndex;
            for (charIndex = 1; charIndex < charNumber; charIndex++) {
                cy.get('#theia-main-content-panel').trigger("keydown", { keyCode: 39, which: 39 });
            }
        });
    };
    Editor.prototype.performControlSpaceCombination = function () {
        this.waitEditorOpened();
        cy.get('#theia-main-content-panel')
            .should('be.visible')
            .trigger("keydown", { keyCode: 32, which: 32, ctrlKey: true });
    };
    Editor.prototype.getLineLocator = function (lineNumber) {
        return "div[data-cy='editor-line-" + lineNumber + "']>span";
    };
    Editor.prototype.typeToLine = function (lineNumber, text) {
        var _this = this;
        //workaround for avoiding random cursor placement
        this.addAttributeToLines()
            .then(function (linesCapacity) {
            cy.get(_this.getLineLocator(1))
                .then(function (element) {
                element[0].setAttribute('contenteditable', '');
            })
                .type("{leftarrow}");
        })
            .then(function () {
            _this.addAttributeToLines().then(function (linesCapacity) {
                cy.get(_this.getLineLocator(lineNumber))
                    .then(function (element) {
                    element[0].setAttribute('contenteditable', '');
                })
                    .type(text, { force: true });
            });
        });
    };
    Editor.EDITOR_LINES = ".lines-content .view-line";
    Editor.EDITOR_BODY = "#theia-main-content-panel .lines-content";
    Editor.SUGGESTION_WIDGET_BODY = "div[widgetId='editor.widget.suggestWidget']";
    Editor.SUGGESTION_WIDGET_ROW = "div[widgetId='editor.widget.suggestWidget'] .monaco-list-row";
    return Editor;
}());
export { Editor };
