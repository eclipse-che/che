"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
var __param = (this && this.__param) || function (paramIndex, decorator) {
    return function (target, key) { decorator(target, key, paramIndex); }
};
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : new P(function (resolve) { resolve(result.value); }).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
Object.defineProperty(exports, "__esModule", { value: true });
var Editor_1;
"use strict";
/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
require("reflect-metadata");
const inversify_1 = require("inversify");
const DriverHelper_1 = require("../../utils/DriverHelper");
const inversify_types_1 = require("../../inversify.types");
const TestConstants_1 = require("../../TestConstants");
const selenium_webdriver_1 = require("selenium-webdriver");
const Ide_1 = require("./Ide");
const Logger_1 = require("../../utils/Logger");
let Editor = Editor_1 = class Editor {
    constructor(driverHelper, ide) {
        this.driverHelper = driverHelper;
        this.ide = ide;
    }
    waitSuggestionContainer(timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('Editor.waitSuggestionContainer');
            yield this.driverHelper.waitVisibility(selenium_webdriver_1.By.css(Editor_1.SUGGESTION_WIDGET_BODY_CSS), timeout);
        });
    }
    waitSuggestionContainerClosed() {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('Editor.waitSuggestionContainerClosed');
            yield this.driverHelper.waitDisappearanceWithTimeout(selenium_webdriver_1.By.css(Editor_1.SUGGESTION_WIDGET_BODY_CSS));
        });
    }
    waitSuggestion(editorTabTitle, suggestionText, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Editor.waitSuggestion tabTitle: "${editorTabTitle}" suggestion: "${suggestionText}"`);
            const suggestionLocator = this.getSuggestionLineXpathLocator(suggestionText);
            yield this.driverHelper.getDriver().wait(() => __awaiter(this, void 0, void 0, function* () {
                try {
                    yield this.driverHelper.waitVisibility(suggestionLocator, 5000);
                    return true;
                }
                catch (err) {
                    if (!(err instanceof selenium_webdriver_1.error.TimeoutError)) {
                        throw err;
                    }
                    yield this.pressEscapeButton(editorTabTitle);
                    yield this.waitSuggestionContainerClosed();
                    yield this.pressControlSpaceCombination(editorTabTitle);
                }
            }), timeout);
        });
    }
    waitSuggestionWithScrolling(editorTabTitle, suggestionText, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Editor.waitSuggestion tabTitle: "${editorTabTitle}" suggestion: "${suggestionText}"`);
            const suggestionLocator = this.getSuggestionLineXpathLocator(suggestionText);
            yield this.driverHelper.getDriver().wait(() => __awaiter(this, void 0, void 0, function* () {
                try {
                    yield this.scrollAndSearchSuggestion(editorTabTitle, suggestionLocator, 40000);
                    return true;
                }
                catch (err) {
                    if (!(err instanceof selenium_webdriver_1.error.TimeoutError)) {
                        throw err;
                    }
                    yield this.pressEscapeButton(editorTabTitle);
                    yield this.waitSuggestionContainerClosed();
                    yield this.pressControlSpaceCombination(editorTabTitle);
                }
            }), timeout);
        });
    }
    pressControlSpaceCombination(editorTabTitle) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Editor.pressControlSpaceCombination "${editorTabTitle}"`);
            yield this.performKeyCombination(editorTabTitle, selenium_webdriver_1.Key.chord(selenium_webdriver_1.Key.CONTROL, selenium_webdriver_1.Key.SPACE));
        });
    }
    pressEscapeButton(editorTabTitle) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Editor.pressEscapeButton "${editorTabTitle}"`);
            yield this.performKeyCombination(editorTabTitle, selenium_webdriver_1.Key.ESCAPE);
        });
    }
    clickOnSuggestion(suggestionText, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Editor.clickOnSuggestion "${suggestionText}"`);
            yield this.driverHelper.waitAndClick(this.getSuggestionLineXpathLocator(suggestionText), timeout);
        });
    }
    waitTab(tabTitle, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Editor.waitTab "${tabTitle}"`);
            yield this.driverHelper.waitVisibility(selenium_webdriver_1.By.xpath(this.getTabXpathLocator(tabTitle)), timeout);
        });
    }
    waitTabDisappearance(tabTitle, attempt = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS, polling = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_POLLING) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Editor.waitTabDisappearance "${tabTitle}"`);
            yield this.driverHelper.waitDisappearance(selenium_webdriver_1.By.xpath(this.getTabXpathLocator(tabTitle)), attempt, polling);
        });
    }
    clickOnTab(tabTitle, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Editor.clickOnTab "${tabTitle}"`);
            yield this.ide.closeAllNotifications();
            yield this.driverHelper.waitAndClick(selenium_webdriver_1.By.xpath(this.getTabXpathLocator(tabTitle)), timeout);
        });
    }
    waitTabFocused(tabTitle, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Editor.waitTabFocused "${tabTitle}"`);
            const focusedTabLocator = selenium_webdriver_1.By.xpath(`//li[contains(@class, 'p-TabBar-tab') and contains(@class, 'theia-mod-active')]//div[text()='${tabTitle}']`);
            yield this.driverHelper.waitVisibility(focusedTabLocator, timeout);
        });
    }
    selectTab(tabTitle, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Editor.selectTab "${tabTitle}"`);
            yield this.ide.closeAllNotifications();
            yield this.waitTab(tabTitle, timeout);
            yield this.clickOnTab(tabTitle, timeout);
            yield this.waitTabFocused(tabTitle, timeout);
        });
    }
    closeTab(tabTitle, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Editor.closeTab "${tabTitle}"`);
            const tabCloseButtonLocator = selenium_webdriver_1.By.xpath(`//div[text()='${tabTitle}']/parent::li//div[contains(@class, 'p-TabBar-tabCloseIcon')]`);
            yield this.driverHelper.waitAndClick(tabCloseButtonLocator, timeout);
        });
    }
    waitTabWithUnsavedStatus(tabTitle, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Editor.waitTabWithUnsavedStatus "${tabTitle}"`);
            const unsavedTabLocator = this.getTabWithUnsavedStatus(tabTitle);
            yield this.driverHelper.waitVisibility(unsavedTabLocator, timeout);
        });
    }
    waitTabWithSavedStatus(tabTitle, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Editor.waitTabWithSavedStatus "${tabTitle}"`);
            const unsavedTabLocator = this.getTabWithUnsavedStatus(tabTitle);
            yield this.driverHelper.getDriver().wait(() => __awaiter(this, void 0, void 0, function* () {
                try {
                    yield this.driverHelper.waitDisappearanceWithTimeout(unsavedTabLocator, TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_POLLING);
                    yield this.waitTab(tabTitle, timeout);
                    return true;
                }
                catch (err) {
                    if (!(err instanceof selenium_webdriver_1.error.TimeoutError)) {
                        throw err;
                    }
                    console.log(`The editor tab with title "${tabTitle}" has unsaved status, wait once again`);
                }
            }), timeout);
        });
    }
    waitEditorOpened(editorTabTitle, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Editor.waitEditorOpened "${editorTabTitle}"`);
            const firstEditorLineLocator = selenium_webdriver_1.By.xpath(this.getEditorLineXpathLocator(1));
            yield this.driverHelper.waitPresence(this.getEditorBodyLocator(editorTabTitle), timeout);
            yield this.driverHelper.waitPresence(firstEditorLineLocator, timeout);
        });
    }
    waitEditorAvailable(tabTitle, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Editor.waitEditorAvailable "${tabTitle}"`);
            yield this.waitTab(tabTitle, timeout);
            yield this.waitEditorOpened(tabTitle, timeout);
        });
    }
    getLineText(tabTitle, lineNumber) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Editor.getLineText "${tabTitle}"`);
            const lineIndex = lineNumber - 1;
            const editorText = yield this.getEditorVisibleText(tabTitle);
            const editorLines = editorText.split('\n');
            const editorLine = editorLines[lineIndex] + '\n';
            return editorLine;
        });
    }
    getEditorVisibleText(tabTitle) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Editor.getEditorVisibleText "${tabTitle}"`);
            const editorBodyLocator = selenium_webdriver_1.By.xpath(`//div[contains(@data-uri, \'${tabTitle}')]//div[@class=\'view-lines\']`);
            // const editorBodyLocator: By = By.xpath('//div[@class=\'view-lines\']');
            const editorText = yield this.driverHelper.waitAndGetText(editorBodyLocator);
            return editorText;
        });
    }
    waitText(tabTitle, expectedText, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT, polling = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_POLLING) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Editor.waitText "${tabTitle}"`);
            yield this.driverHelper.getDriver().wait(() => __awaiter(this, void 0, void 0, function* () {
                const editorText = yield this.getEditorVisibleText(tabTitle);
                const isEditorContainText = editorText.includes(expectedText);
                if (isEditorContainText) {
                    return true;
                }
                yield this.driverHelper.wait(polling);
            }), timeout);
        });
    }
    followAndWaitForText(editorTabTitle, expectedText, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT, polling = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_POLLING) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Editor.followAndWaitForText title: "${editorTabTitle}" text: "${expectedText}"`);
            yield this.selectTab(editorTabTitle, timeout);
            yield this.driverHelper.getDriver().wait(() => __awaiter(this, void 0, void 0, function* () {
                yield this.performKeyCombination(editorTabTitle, selenium_webdriver_1.Key.chord(selenium_webdriver_1.Key.CONTROL, selenium_webdriver_1.Key.END));
                const editorText = yield this.getEditorVisibleText(editorTabTitle);
                const isEditorContainText = editorText.includes(expectedText);
                if (isEditorContainText) {
                    return true;
                }
                yield this.driverHelper.wait(polling);
            }), timeout);
        });
    }
    moveCursorToLineAndChar(editorTabTitle, line, char) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Editor.moveCursorToLineAndChar title: "${editorTabTitle}" line: "${line}" char: "${char}"`);
            // set cursor to the 1:1 position
            yield this.performKeyCombination(editorTabTitle, selenium_webdriver_1.Key.chord(selenium_webdriver_1.Key.CONTROL, selenium_webdriver_1.Key.HOME));
            // move cursor to line
            for (let i = 1; i < line; i++) {
                yield this.performKeyCombination(editorTabTitle, selenium_webdriver_1.Key.ARROW_DOWN);
            }
            // move cursor to char
            for (let i = 1; i < char; i++) {
                yield this.performKeyCombination(editorTabTitle, selenium_webdriver_1.Key.ARROW_RIGHT);
            }
        });
    }
    performKeyCombination(editorTabTitle, text) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Editor.performKeyCombination title: "${editorTabTitle}" text: "${text}"`);
            const interactionContainerLocator = this.getEditorActionArreaLocator(editorTabTitle);
            yield this.driverHelper.type(interactionContainerLocator, text);
        });
    }
    type(editorTabTitle, text, line) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Editor.type title: "${editorTabTitle}" text: "${text}"`);
            yield this.selectTab(editorTabTitle);
            yield this.moveCursorToLineAndChar(editorTabTitle, line, 1);
            yield this.performKeyCombination(editorTabTitle, text);
        });
    }
    waitErrorInLine(lineNumber, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Editor.waitErrorInLine line: "${lineNumber}"`);
            const errorInLineLocator = yield this.getErrorInLineLocator(lineNumber);
            yield this.driverHelper.waitVisibility(errorInLineLocator, timeout);
        });
    }
    waitErrorInLineDisappearance(lineNumber, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Editor.waitErrorInLineDisappearance line: "${lineNumber}"`);
            const errorInLineLocator = yield this.getErrorInLineLocator(lineNumber);
            yield this.driverHelper.waitDisappearanceWithTimeout(errorInLineLocator, timeout);
        });
    }
    waitStoppedDebugBreakpoint(tabTitle, lineNumber, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Editor.waitStoppedDebugBreakpoint title: "${tabTitle}" line: "${lineNumber}"`);
            const stoppedDebugBreakpointLocator = selenium_webdriver_1.By.xpath(yield this.getStoppedDebugBreakpointXpathLocator(tabTitle, lineNumber));
            yield this.driverHelper.waitVisibility(stoppedDebugBreakpointLocator, timeout);
        });
    }
    waitBreakpoint(tabTitle, lineNumber, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Editor.waitBreakpoint title: "${tabTitle}" line: "${lineNumber}"`);
            const debugBreakpointLocator = yield this.getDebugBreakpointLocator(tabTitle, lineNumber);
            yield this.driverHelper.waitVisibility(debugBreakpointLocator, timeout);
        });
    }
    waitBreakpointAbsence(tabTitle, lineNumber, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Editor.waitBreakpointAbsence title: "${tabTitle}" line: "${lineNumber}"`);
            const debugBreakpointLocator = yield this.getDebugBreakpointLocator(tabTitle, lineNumber);
            yield this.driverHelper.waitDisappearanceWithTimeout(debugBreakpointLocator, timeout);
        });
    }
    waitBreakpointHint(tabTitle, lineNumber, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Editor.waitBreakpointHint title: "${tabTitle}" line: "${lineNumber}"`);
            const debugBreakpointHintLocator = yield this.getDebugBreakpointHintLocator(tabTitle, lineNumber);
            yield this.driverHelper.waitVisibility(debugBreakpointHintLocator, timeout);
        });
    }
    waitBreakpointHintDisappearance(tabTitle, lineNumber, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Editor.waitBreakpointHintDisappearance title: "${tabTitle}" line: "${lineNumber}"`);
            const debugBreakpointHintLocator = yield this.getDebugBreakpointHintLocator(tabTitle, lineNumber);
            yield this.driverHelper.waitDisappearanceWithTimeout(debugBreakpointHintLocator, timeout);
        });
    }
    activateBreakpoint(tabTitle, lineNumber) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Editor.activateBreakpoint title: "${tabTitle}" line: "${lineNumber}"`);
            const attempts = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS;
            const polling = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_POLLING;
            for (let i = 0; i < attempts; i++) {
                try {
                    yield this.selectTab(tabTitle);
                    yield this.moveCursorToLineAndChar(tabTitle, lineNumber, 1);
                    yield this.performKeyCombination(tabTitle, selenium_webdriver_1.Key.F9);
                    yield this.waitBreakpoint(tabTitle, lineNumber);
                    return;
                }
                catch (err) {
                    if (i === attempts - 1) {
                        throw new selenium_webdriver_1.error.TimeoutError(`Exceeded maximum breakpoint activation attempts`);
                    }
                    // ignore errors and wait
                    yield this.driverHelper.wait(polling);
                }
            }
        });
    }
    getLineYCoordinates(lineNumber) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Editor.getLineYCoordinates line: "${lineNumber}"`);
            const lineNumberLocator = selenium_webdriver_1.By.xpath(`//div[contains(@class, 'line-numbers') and text()='${lineNumber}']` +
                `//parent::div[contains(@style, 'position')]`);
            let elementStyleValue = yield this.driverHelper.waitAndGetElementAttribute(lineNumberLocator, 'style');
            elementStyleValue = elementStyleValue.replace('position: absolute; top: ', '');
            elementStyleValue = elementStyleValue.replace('px; width: 100%; height: 19px;', '');
            const lineYCoordinate = Number.parseInt(elementStyleValue, 10);
            if (Number.isNaN(lineYCoordinate)) {
                throw new selenium_webdriver_1.error.UnsupportedOperationError(`Failed to parse the ${elementStyleValue} row to number format`);
            }
            return lineYCoordinate;
        });
    }
    clickOnLineAndChar(line, char) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Editor.clickOnLineAndChar line: "${line}" char: "${char}"`);
            const yPosition = (yield this.getLineYCoordinates(line)) + Editor_1.ADDITIONAL_SHIFTING_TO_Y;
            const xPosition = char + Editor_1.ADDITIONAL_SHIFTING_TO_X;
            new selenium_webdriver_1.ActionSequence(this.driverHelper.getDriver()).
                mouseMove({ x: xPosition, y: yPosition }).
                click().
                perform();
        });
    }
    goToDefinitionWithMouseClicking(line, char) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Editor.goToDefinitionWithMouseClicking line: "${line}" char: "${char}"`);
            const yPosition = (yield this.getLineYCoordinates(line)) + Editor_1.ADDITIONAL_SHIFTING_TO_Y;
            new selenium_webdriver_1.ActionSequence(this.driverHelper.getDriver()).
                keyDown(selenium_webdriver_1.Key.CONTROL).
                mouseMove({ x: char + Editor_1.ADDITIONAL_SHIFTING_TO_X, y: yPosition }).
                click().
                keyDown(selenium_webdriver_1.Key.CONTROL).
                perform();
        });
    }
    mouseRightButtonClick(line, char) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Editor.mouseRightButtonClick line: "${line}" char: "${char}"`);
            const yPosition = (yield this.getLineYCoordinates(line)) + Editor_1.ADDITIONAL_SHIFTING_TO_Y;
            new selenium_webdriver_1.ActionSequence(this.driverHelper.getDriver()).
                mouseMove({ x: char + Editor_1.ADDITIONAL_SHIFTING_TO_X, y: yPosition }).
                click(selenium_webdriver_1.Button.RIGHT).
                perform();
        });
    }
    scrollAndSearchSuggestion(editorTabTitle, suggestionLocator, timeout = 10000) {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.driverHelper.getDriver().wait(() => __awaiter(this, void 0, void 0, function* () {
                const loadingLocator = this.getSuggestionLineXpathLocator('Loading');
                yield this.waitSuggestionContainer();
                yield this.driverHelper.waitDisappearance(loadingLocator);
                yield this.driverHelper.wait(1000);
                if (yield this.driverHelper.isVisible(suggestionLocator)) {
                    return true;
                }
                yield this.performKeyCombination(editorTabTitle, selenium_webdriver_1.Key.ARROW_DOWN);
            }), timeout);
        });
    }
    getTabWithUnsavedStatus(tabTitle) {
        return selenium_webdriver_1.By.xpath(`//div[text()='${tabTitle}']/parent::li[contains(@class, 'theia-mod-dirty')]`);
    }
    getStoppedDebugBreakpointXpathLocator(tabTitle, lineNumber) {
        return __awaiter(this, void 0, void 0, function* () {
            const lineYPixelCoordinates = yield this.getLineYCoordinates(lineNumber);
            const stoppedDebugBreakpointXpathLocator = `//div[contains(@id, '${tabTitle}')]//div[@class='margin']` +
                `//div[contains(@style, '${lineYPixelCoordinates}px')]` +
                '//div[contains(@class, \'theia-debug-top-stack-frame\')]';
            return stoppedDebugBreakpointXpathLocator;
        });
    }
    getDebugBreakpointLocator(tabTitle, lineNumber) {
        return __awaiter(this, void 0, void 0, function* () {
            const lineYPixelCoordinates = yield this.getLineYCoordinates(lineNumber);
            return selenium_webdriver_1.By.xpath(`//div[contains(@id, '${tabTitle}')]//div[@class='margin']` +
                `//div[contains(@style, '${lineYPixelCoordinates}px')]` +
                '//div[contains(@class, \'theia-debug-breakpoint\')]');
        });
    }
    getDebugBreakpointHintLocator(tabTitle, lineNumber) {
        return __awaiter(this, void 0, void 0, function* () {
            const lineYPixelCoordinates = yield this.getLineYCoordinates(lineNumber);
            return selenium_webdriver_1.By.xpath(`//div[contains(@id, '${tabTitle}')]//div[@class='margin']` +
                `//div[contains(@style, '${lineYPixelCoordinates}px')]` +
                '//div[contains(@class, \'theia-debug-breakpoint-hint\')]');
        });
    }
    getEditorBodyLocator(editorTabTitle) {
        const editorXpathLocator = `//div[@id='theia-main-content-panel']//div[contains(@class, 'monaco-editor')` +
            ` and contains(@data-uri, '${editorTabTitle}')]//*[contains(@class, 'lines-content')]`;
        return selenium_webdriver_1.By.xpath(editorXpathLocator);
    }
    getEditorActionArreaLocator(editorTabTitle) {
        const editorActionArreaXpathLocator = `//div[@id='theia-main-content-panel']//div[contains(@class, 'monaco-editor')` +
            ` and contains(@data-uri, '${editorTabTitle}')]//textarea`;
        return selenium_webdriver_1.By.xpath(editorActionArreaXpathLocator);
    }
    getEditorLineXpathLocator(lineNumber) {
        return `(//div[contains(@class,'lines-content')]//div[@class='view-lines']/div[@class='view-line'])[${lineNumber}]`;
    }
    getSuggestionLineXpathLocator(suggestionText) {
        return selenium_webdriver_1.By.xpath(`//div[@widgetid='editor.widget.suggestWidget']//span[@class='monaco-highlighted-label' and contains(.,'${suggestionText}')]`);
    }
    getTabXpathLocator(tabTitle) {
        return `//li[contains(@class, 'p-TabBar-tab')]//div[text()='${tabTitle}']`;
    }
    getErrorInLineLocator(lineNumber) {
        return __awaiter(this, void 0, void 0, function* () {
            const lineYCoordinates = yield this.getLineYCoordinates(lineNumber);
            return selenium_webdriver_1.By.xpath(`//div[contains(@style, 'top:${lineYCoordinates}px')]//div[contains(@class, 'squiggly-error')]`);
        });
    }
};
Editor.SUGGESTION_WIDGET_BODY_CSS = 'div.visible[widgetId=\'editor.widget.suggestWidget\']';
Editor.ADDITIONAL_SHIFTING_TO_Y = 19;
Editor.ADDITIONAL_SHIFTING_TO_X = 1;
Editor = Editor_1 = __decorate([
    inversify_1.injectable(),
    __param(0, inversify_1.inject(inversify_types_1.CLASSES.DriverHelper)),
    __param(1, inversify_1.inject(inversify_types_1.CLASSES.Ide)),
    __metadata("design:paramtypes", [DriverHelper_1.DriverHelper,
        Ide_1.Ide])
], Editor);
exports.Editor = Editor;
//# sourceMappingURL=Editor.js.map