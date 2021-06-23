/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import 'reflect-metadata';
import { injectable, inject } from 'inversify';
import { DriverHelper } from '../../utils/DriverHelper';
import { CLASSES } from '../../inversify.types';
import { TestConstants } from '../../TestConstants';
import { By, Key, error, ActionSequence, Button } from 'selenium-webdriver';
import { Logger } from '../../utils/Logger';
import { TimeoutConstants } from '../../TimeoutConstants';


@injectable()
export class Editor {
    private static readonly SUGGESTION_WIDGET_BODY_CSS: string = 'div.visible[widgetId=\'editor.widget.suggestWidget\']';
    private static readonly SUGGESTION_LOADING_XPATH: string = '//div[@widgetid=\'editor.widget.suggestWidget\']//div[@class=\'message\' and contains(.,\'Loading...\')]';
    private static readonly ADDITIONAL_SHIFTING_TO_Y: number = 19;
    private static readonly ADDITIONAL_SHIFTING_TO_X: number = 1;


    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) { }

    public async waitSuggestionContainer(timeout: number = TimeoutConstants.TS_SUGGESTION_TIMEOUT) {
        Logger.debug('Editor.waitSuggestionContainer');

        await this.driverHelper.waitVisibility(By.css(Editor.SUGGESTION_WIDGET_BODY_CSS), timeout);
    }

    public async waitSuggestionContainerClosed(timeout: number = TimeoutConstants.TS_SUGGESTION_TIMEOUT) {
        Logger.debug('Editor.waitSuggestionContainerClosed');

        await this.driverHelper.waitDisappearanceWithTimeout(By.css(Editor.SUGGESTION_WIDGET_BODY_CSS), timeout);
    }

    public async waitSuggestion(editorTabTitle: string,
        suggestionText: string,
        timeout: number = TimeoutConstants.TS_SUGGESTION_TIMEOUT,
        lineNumber?: number,
        charNumber?: number) {

        const charInLineNumber: number = (charNumber ? charNumber : 1);

        // if line defined the method sets cursor to line and char
        // before invoking suggestion container and repeat this
        // cycle if suggestion didn't display
        if (lineNumber) {
            await this.waitSuggestionWithResettingCursor(editorTabTitle, suggestionText, timeout, lineNumber, charInLineNumber);
            return;
        }

        // if line not defined the method just invoke suggestion container
        // without setting cursor to line and char and repeat this
        // cycle if suggestion didn't display
        await this.waitSuggestionWithoutResettingCursor(editorTabTitle, suggestionText, timeout);

    }

    public async closeSuggestionContainer(editorTabTitle: string, timeout: number = TimeoutConstants.TS_SUGGESTION_TIMEOUT) {
        Logger.debug(`Editor.closeSuggestionContainer tabTitle: "${editorTabTitle}"`);

        await this.driverHelper.getDriver().wait(async () => {
            // if container already closed stop the method execution
            try {
                // for avoiding problem when the inner timeout
                // bigger than timeout of the method
                const suggestionContainerTimeout: number = timeout / 2;

                await this.waitSuggestionContainer(suggestionContainerTimeout);
            } catch (err) {
                if (err instanceof error.TimeoutError) {
                    return true;
                }

                throw err;
            }

            // try to close container
            try {
                await this.pressEscapeButton(editorTabTitle);
                await this.waitSuggestionContainerClosed(2000);
                return true;
            } catch (err) {
                if (!(err instanceof error.TimeoutError)) {
                    throw err;
                }
            }

        }, timeout);
    }

    public async waitSuggestionWithScrolling(editorTabTitle: string,
        suggestionText: string,
        timeout: number = TimeoutConstants.TS_SUGGESTION_TIMEOUT) {

        Logger.debug(`Editor.waitSuggestion tabTitle: "${editorTabTitle}" suggestion: "${suggestionText}"`);

        const suggestionLocator: By = this.getSuggestionLineXpathLocator(suggestionText);

        await this.driverHelper.getDriver().wait(async () => {
            try {
                await this.scrollAndSearchSuggestion(editorTabTitle, suggestionLocator, timeout / 3);
                return true;
            } catch (err) {
                if (!(err instanceof error.TimeoutError)) {
                    throw err;
                }

                await this.closeSuggestionContainer(editorTabTitle, TimeoutConstants.TS_CLOSE_SUGGESTION_CONTAINER_TIMEOUT);
                await this.pressControlSpaceCombination(editorTabTitle);
            }
        }, timeout);
    }

    public async pressControlSpaceCombination(editorTabTitle: string) {
        Logger.debug(`Editor.pressControlSpaceCombination "${editorTabTitle}"`);

        await this.performKeyCombination(editorTabTitle, Key.chord(Key.CONTROL, Key.SPACE));
    }

    public async pressEscapeButton(editorTabTitle: string) {
        Logger.debug(`Editor.pressEscapeButton "${editorTabTitle}"`);

        await this.performKeyCombination(editorTabTitle, Key.ESCAPE);
    }

    public async clickOnSuggestion(suggestionText: string, timeout: number = TimeoutConstants.TS_SUGGESTION_TIMEOUT) {
        Logger.debug(`Editor.clickOnSuggestion "${suggestionText}"`);

        await this.driverHelper.waitAndClick(this.getSuggestionLineXpathLocator(suggestionText), timeout);
    }

    public async waitTab(tabTitle: string, timeout: number = TimeoutConstants.TS_EDITOR_TAB_INTERACTION_TIMEOUT) {
        Logger.debug(`Editor.waitTab "${tabTitle}"`);

        await this.driverHelper.waitVisibility(By.xpath(this.getTabXpathLocator(tabTitle)), timeout);
    }

    public async waitTabDisappearance(tabTitle: string,
        attempt: number = TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS,
        polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING) {

        Logger.debug(`Editor.waitTabDisappearance "${tabTitle}"`);

        await this.driverHelper.waitDisappearance(By.xpath(this.getTabXpathLocator(tabTitle)), attempt, polling);
    }

    public async clickOnTab(tabTitle: string, timeout: number = TimeoutConstants.TS_EDITOR_TAB_INTERACTION_TIMEOUT) {
        Logger.debug(`Editor.clickOnTab "${tabTitle}"`);

        await this.driverHelper.waitAndClick(By.xpath(this.getTabXpathLocator(tabTitle)), timeout);
    }

    public async waitTabFocused(tabTitle: string, timeout: number = TimeoutConstants.TS_EDITOR_TAB_INTERACTION_TIMEOUT) {
        Logger.debug(`Editor.waitTabFocused "${tabTitle}"`);

        const focusedTabLocator: By = By.xpath(`//li[contains(@class, 'p-TabBar-tab') and contains(@class, 'theia-mod-active')]//div[text()='${tabTitle}']`);

        await this.driverHelper.waitVisibility(focusedTabLocator, timeout);
    }

    public async selectTab(tabTitle: string) {
        Logger.debug(`Editor.selectTab "${tabTitle}"`);

        await this.waitTab(tabTitle);
        await this.clickOnTab(tabTitle);
        await this.waitTabFocused(tabTitle);
    }

    async closeTab(tabTitle: string, timeout: number = TimeoutConstants.TS_EDITOR_TAB_INTERACTION_TIMEOUT) {
        Logger.debug(`Editor.closeTab "${tabTitle}"`);

        const tabCloseButtonLocator: By = By.xpath(`//div[text()='${tabTitle}']/parent::li//div[contains(@class, 'p-TabBar-tabCloseIcon')]`);

        await this.driverHelper.waitAndClick(tabCloseButtonLocator, timeout);
    }

    async waitTabWithUnsavedStatus(tabTitle: string, timeout: number = TimeoutConstants.TS_EDITOR_TAB_INTERACTION_TIMEOUT) {
        Logger.debug(`Editor.waitTabWithUnsavedStatus "${tabTitle}"`);

        const unsavedTabLocator: By = this.getTabWithUnsavedStatus(tabTitle);

        await this.driverHelper.waitVisibility(unsavedTabLocator, timeout);
    }

    async waitTabWithSavedStatus(tabTitle: string, timeout: number = TimeoutConstants.TS_EDITOR_TAB_INTERACTION_TIMEOUT) {
        Logger.debug(`Editor.waitTabWithSavedStatus "${tabTitle}"`);

        const unsavedTabLocator: By = this.getTabWithUnsavedStatus(tabTitle);

        await this.driverHelper.getDriver().wait(async () => {
            try {
                await this.driverHelper.waitDisappearanceWithTimeout(unsavedTabLocator, TestConstants.TS_SELENIUM_DEFAULT_POLLING);
                await this.waitTab(tabTitle, timeout);
                return true;
            } catch (err) {
                if (!(err instanceof error.TimeoutError)) {
                    throw err;
                }

                console.log(`The editor tab with title "${tabTitle}" has unsaved status, wait once again`);
            }
        }, timeout);

    }

    async waitEditorOpened(editorTabTitle: string, timeout: number = TimeoutConstants.TS_EDITOR_TAB_INTERACTION_TIMEOUT) {
        Logger.debug(`Editor.waitEditorOpened "${editorTabTitle}"`);

        const firstEditorLineLocator: By = By.xpath(this.getEditorLineXpathLocator(1));

        await this.driverHelper.waitPresence(this.getEditorBodyLocator(editorTabTitle), timeout);
        await this.driverHelper.waitPresence(firstEditorLineLocator, timeout);
    }

    async waitEditorAvailable(tabTitle: string, timeout: number = TimeoutConstants.TS_EDITOR_TAB_INTERACTION_TIMEOUT) {
        Logger.debug(`Editor.waitEditorAvailable "${tabTitle}"`);

        await this.waitTab(tabTitle, timeout);
        await this.waitEditorOpened(tabTitle, timeout);
    }

    async getLineText(tabTitle: string, lineNumber: number): Promise<string> {
        Logger.debug(`Editor.getLineText "${tabTitle}"`);

        const lineIndex: number = lineNumber - 1;
        const editorText: string = await this.getEditorVisibleText(tabTitle);
        const editorLines: string[] = editorText.split('\n');
        const editorLine = editorLines[lineIndex] + '\n';

        return editorLine;
    }

    async getEditorVisibleText(tabTitle: string): Promise<string> {
        Logger.debug(`Editor.getEditorVisibleText "${tabTitle}"`);

        const editorBodyLocator: By = By.xpath(`//div[contains(@data-uri, \'${tabTitle}')]//div[@class=\'view-lines\']`);
        // const editorBodyLocator: By = By.xpath('//div[@class=\'view-lines\']');
        const editorText: string = await this.driverHelper.waitAndGetText(editorBodyLocator);
        return editorText;
    }

    async waitText(tabTitle: string, expectedText: string,
        timeout: number = TimeoutConstants.TS_EDITOR_TAB_INTERACTION_TIMEOUT,
        polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING) {

        Logger.debug(`Editor.waitText "${tabTitle}"`);

        await this.driverHelper.getDriver().wait(async () => {
            const editorText: string = await this.getEditorVisibleText(tabTitle);
            const isEditorContainText: boolean = editorText.includes(expectedText);

            if (isEditorContainText) {
                return true;
            }

            await this.driverHelper.wait(polling);
        }, timeout);
    }

    async followAndWaitForText(editorTabTitle: string,
        expectedText: string,
        timeout: number = TimeoutConstants.TS_EDITOR_TAB_INTERACTION_TIMEOUT,
        polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING) {

        Logger.debug(`Editor.followAndWaitForText title: "${editorTabTitle}" text: "${expectedText}"`);

        await this.selectTab(editorTabTitle);
        await this.driverHelper.getDriver().wait(async () => {
            await this.performKeyCombination(editorTabTitle, Key.chord(Key.CONTROL, Key.END));
            const editorText: string = await this.getEditorVisibleText(editorTabTitle);

            const isEditorContainText: boolean = editorText.includes(expectedText);

            if (isEditorContainText) {
                return true;
            }

            await this.driverHelper.wait(polling);
        }, timeout);
    }

    async moveCursorToLineAndChar(editorTabTitle: string, line: number, char: number) {
        Logger.debug(`Editor.moveCursorToLineAndChar title: "${editorTabTitle}" line: "${line}" char: "${char}"`);

        // set cursor to the 1:1 position
        await this.performKeyCombination(editorTabTitle, Key.chord(Key.CONTROL, Key.HOME));

        // move cursor to line
        for (let i = 1; i < line; i++) {
            await this.performKeyCombination(editorTabTitle, Key.ARROW_DOWN);
        }

        // move cursor to char
        for (let i = 1; i < char; i++) {
            await this.performKeyCombination(editorTabTitle, Key.ARROW_RIGHT);
        }
    }

    async performKeyCombination(editorTabTitle: string, text: string) {
        Logger.debug(`Editor.performKeyCombination title: "${editorTabTitle}" text: "${text}"`);

        const interactionContainerLocator: By = this.getEditorActionArreaLocator(editorTabTitle);
        await this.driverHelper.type(interactionContainerLocator, text);
    }

    async type(editorTabTitle: string, text: string, line: number) {
        Logger.debug(`Editor.type title: "${editorTabTitle}" text: "${text}"`);

        await this.selectTab(editorTabTitle);
        await this.moveCursorToLineAndChar(editorTabTitle, line, 1);
        await this.performKeyCombination(editorTabTitle, text);
    }

    async waitErrorInLine(lineNumber: number, timeout: number = TimeoutConstants.TS_ERROR_HIGHLIGHTING_TIMEOUT) {
        Logger.debug(`Editor.waitErrorInLine line: "${lineNumber}"`);

        const errorInLineLocator: By = await this.getErrorInLineLocator(lineNumber);
        await this.driverHelper.waitVisibility(errorInLineLocator, timeout);
    }

    async waitErrorInLineDisappearance(lineNumber: number, timeout: number = TimeoutConstants.TS_ERROR_HIGHLIGHTING_TIMEOUT) {
        Logger.debug(`Editor.waitErrorInLineDisappearance line: "${lineNumber}"`);

        const errorInLineLocator: By = await this.getErrorInLineLocator(lineNumber);
        await this.driverHelper.waitDisappearanceWithTimeout(errorInLineLocator, timeout);
    }

    async waitWarningInLine(lineNumber: number, timeout: number = TimeoutConstants.TS_ERROR_HIGHLIGHTING_TIMEOUT) {
        Logger.debug(`Editor.waitWarningInLine line: "${lineNumber}"`);

        const warningInLineLocator: By = await this.getWarningInLineLocator(lineNumber);
        await this.driverHelper.waitVisibility(warningInLineLocator, timeout);
    }

    async waitWarningInLineDisappearance(lineNumber: number, timeout: number = TimeoutConstants.TS_ERROR_HIGHLIGHTING_TIMEOUT) {
        Logger.debug(`Editor.waitWarningInLineDisappearance line: "${lineNumber}"`);

        const warningInLineLocator: By = await this.getWarningInLineLocator(lineNumber);
        await this.driverHelper.waitDisappearanceWithTimeout(warningInLineLocator, timeout);
    }

    async waitStoppedDebugBreakpoint(tabTitle: string, lineNumber: number, timeout: number = TimeoutConstants.TS_BREAKPOINT_DEFAULT_TIMEOUT) {
        Logger.debug(`Editor.waitStoppedDebugBreakpoint title: "${tabTitle}" line: "${lineNumber}"`);

        await this.driverHelper.waitUntilTrue(() => this.isBreakpointPresent(tabTitle, lineNumber, true), timeout);
    }

    async waitBreakpoint(tabTitle: string, lineNumber: number, timeout: number = TimeoutConstants.TS_BREAKPOINT_DEFAULT_TIMEOUT) {
        Logger.debug(`Editor.waitBreakpoint title: "${tabTitle}" line: "${lineNumber}"`);

        await this.driverHelper.waitUntilTrue(() => this.isBreakpointPresent(tabTitle, lineNumber), timeout);
    }

    async waitBreakpointAbsence(tabTitle: string, lineNumber: number, timeout: number = TimeoutConstants.TS_BREAKPOINT_DEFAULT_TIMEOUT) {
        Logger.debug(`Editor.waitBreakpointAbsence title: "${tabTitle}" line: "${lineNumber}"`);
        await this.driverHelper.waitUntilTrue(() => !this.isBreakpointPresent(tabTitle, lineNumber), timeout);
    }

    async activateBreakpoint(tabTitle: string, lineNumber: number) {
        Logger.debug(`Editor.activateBreakpoint title: "${tabTitle}" line: "${lineNumber}"`);

        const attempts: number = TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS;
        const polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING;
        const breakpointStatus: boolean = await this.isBreakpointPresent(tabTitle, lineNumber);

        if (breakpointStatus) { return; }

        for (let i = 0; i < attempts; i++) {
            try {
                await this.selectTab(tabTitle);
                await this.moveCursorToLineAndChar(tabTitle, lineNumber, 1);
                await this.performKeyCombination(tabTitle, Key.F9);
                await this.waitBreakpoint(tabTitle, lineNumber);
                return;
            } catch (err) {
                if (i === attempts - 1) {
                    throw new error.TimeoutError(`Exceeded maximum breakpoint activation attempts`);
                }

                // ignore errors and wait
                Logger.debug(`Editor.activateBreakpoint - Error: ${err}`);
                await this.driverHelper.wait(polling);
            }
        }
    }

    async getLineYCoordinates(lineNumber: number): Promise<number> {
        Logger.debug(`Editor.getLineYCoordinates line: "${lineNumber}"`);

        const lineNumberLocator: By = By.xpath(`//div[contains(@class, 'line-numbers') and text()='${lineNumber}']` +
            `//parent::div[contains(@style, 'position')]`);
        let elementStyleValue: string = await this.driverHelper.waitAndGetElementAttribute(lineNumberLocator, 'style', TimeoutConstants.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM);

        Logger.trace(`Editor.getLineYCoordinates style string:${elementStyleValue}`);
        const lineYCoordinate: number = Number.parseInt(elementStyleValue.split('top:')[1].split('px')[0], 10);

        if (Number.isNaN(lineYCoordinate)) {
            throw new error.UnsupportedOperationError(`Failed to parse the ${elementStyleValue} row to number format`);
        }

        return lineYCoordinate;
    }

    async clickOnLineAndChar(line: number, char: number) {
        Logger.debug(`Editor.clickOnLineAndChar line: "${line}" char: "${char}"`);

        const yPosition: number = await this.getLineYCoordinates(line) + Editor.ADDITIONAL_SHIFTING_TO_Y;
        const xPosition: number = char + Editor.ADDITIONAL_SHIFTING_TO_X;

        new ActionSequence(this.driverHelper.getDriver()).
            mouseMove({ x: xPosition, y: yPosition }).
            click().
            perform();
    }

    async goToDefinitionWithMouseClicking(line: number, char: number) {
        Logger.debug(`Editor.goToDefinitionWithMouseClicking line: "${line}" char: "${char}"`);

        const yPosition: number = await this.getLineYCoordinates(line) + Editor.ADDITIONAL_SHIFTING_TO_Y;

        new ActionSequence(this.driverHelper.getDriver()).
            keyDown(Key.CONTROL).
            mouseMove({ x: char + Editor.ADDITIONAL_SHIFTING_TO_X, y: yPosition }).
            click().
            keyDown(Key.CONTROL).
            perform();
    }

    async mouseRightButtonClick(line: number, char: number) {
        Logger.debug(`Editor.mouseRightButtonClick line: "${line}" char: "${char}"`);

        const yPosition: number = await this.getLineYCoordinates(line) + Editor.ADDITIONAL_SHIFTING_TO_Y;

        new ActionSequence(this.driverHelper.getDriver()).
            mouseMove({ x: char + Editor.ADDITIONAL_SHIFTING_TO_X, y: yPosition }).
            click(Button.RIGHT).
            perform();
    }

    private async scrollAndSearchSuggestion(editorTabTitle: string, suggestionLocator: By, timeout: number = 10000) {
        await this.driverHelper.getDriver().wait(async () => {
            const loadingLocator: By = By.xpath(Editor.SUGGESTION_LOADING_XPATH);
            await this.waitSuggestionContainer();
            await this.driverHelper.waitDisappearance(loadingLocator);
            await this.driverHelper.wait(1000);

            if (await this.driverHelper.isVisible(suggestionLocator)) {
                return true;
            }

            await this.performKeyCombination(editorTabTitle, Key.ARROW_DOWN);
        }, timeout);
    }

    private getTabWithUnsavedStatus(tabTitle: string): By {
        return By.xpath(`//div[text()='${tabTitle}']/parent::li[contains(@class, 'theia-mod-dirty')]`);
    }

    private getEditorBodyLocator(editorTabTitle: string): By {
        const editorXpathLocator: string = `//div[@id='theia-main-content-panel']//div[contains(@class, 'monaco-editor')` +
            ` and contains(@data-uri, '${editorTabTitle}')]//*[contains(@class, 'lines-content')]`;

        return By.xpath(editorXpathLocator);
    }

    private getEditorActionArreaLocator(editorTabTitle: string): By {
        const editorActionArreaXpathLocator: string = `//div[@id='theia-main-content-panel']//div[contains(@class, 'monaco-editor')` +
            ` and contains(@data-uri, '${editorTabTitle}')]//textarea`;

        return By.xpath(editorActionArreaXpathLocator);
    }

    private getEditorLineXpathLocator(lineNumber: number): string {
        return `(//div[contains(@class,'lines-content')]//div[@class='view-lines']/div[@class='view-line'])[${lineNumber}]`;
    }

    private getSuggestionLineXpathLocator(suggestionText: string): By {
        return By.xpath(`//div[@widgetid='editor.widget.suggestWidget']//span[@class='monaco-highlighted-label' and contains(.,'${suggestionText}')]`);
    }

    private getTabXpathLocator(tabTitle: string): string {
        return `//li[contains(@class, 'p-TabBar-tab')]//div[text()='${tabTitle}']`;
    }

    private async getErrorInLineLocator(lineNumber: number): Promise<By> {
        const lineYCoordinates: number = await this.getLineYCoordinates(lineNumber);

        return By.xpath(`//div[contains(@style, 'top:${lineYCoordinates}px')]//div[contains(@class, 'squiggly-error')]`);
    }

    private async getWarningInLineLocator(lineNumber: number): Promise<By> {
        const lineYCoordinates: number = await this.getLineYCoordinates(lineNumber);

        return By.xpath(`//div[contains(@style, 'top:${lineYCoordinates}px')]//div[contains(@class, 'squiggly-warning')]`);
    }

    private async waitSuggestionWithResettingCursor(editorTabTitle: string,
        suggestionText: string,
        timeout: number = TimeoutConstants.TS_SUGGESTION_TIMEOUT,
        lineNumber: number,
        charNumber: number) {

        const suggestionLocator: By = this.getSuggestionLineXpathLocator(suggestionText);

        const methodLogText: string = `Editor.waitSuggestion tabTitle: "${editorTabTitle}" ` +
            `suggestion: "${suggestionText}" ` +
            `line: "${lineNumber}" ` +
            `char: "${charNumber}"`;

        Logger.debug(methodLogText);

        await this.driverHelper.getDriver().wait(async () => {
            await this.selectTab(editorTabTitle);
            await this.moveCursorToLineAndChar(editorTabTitle, lineNumber, charNumber);
            await this.pressControlSpaceCombination(editorTabTitle);

            try {
                await this.driverHelper.waitVisibility(suggestionLocator, 5000);
                await this.closeSuggestionContainer(editorTabTitle);
                return true;
            } catch (err) {
                if (!(err instanceof error.TimeoutError)) {
                    throw err;
                }
            }
        }, timeout);
    }

    private async waitSuggestionWithoutResettingCursor(editorTabTitle: string,
        suggestionText: string,
        timeout: number = TimeoutConstants.TS_SUGGESTION_TIMEOUT) {

        Logger.debug(`Editor.waitSuggestion tabTitle: "${editorTabTitle}" suggestion: "${suggestionText}"`);

        const suggestionLocator: By = this.getSuggestionLineXpathLocator(suggestionText);

        await this.driverHelper.getDriver().wait(async () => {
            try {
                await this.driverHelper.waitVisibility(suggestionLocator, 5000);
                await this.closeSuggestionContainer(editorTabTitle);
                return true;
            } catch (err) {
                if (!(err instanceof error.TimeoutError)) {
                    throw err;
                }

                await this.closeSuggestionContainer(editorTabTitle, timeout);
                await this.pressControlSpaceCombination(editorTabTitle);
            }
        }, timeout);
    }

    /**
     * Checks for breakpoint presence in currently opened editor on given line.
     *
     * @param lineNumber Line number to check the breakpoint presence on.
     * @param triggered Whether this breakpoint is triggered or not. Default false.
     */

    private async isBreakpointPresent(tabTitle: string, lineNumber: number, triggered: boolean = false): Promise<boolean> {
        Logger.debug('Editor.isBreakpointPresent');

        const linesBarXpathLocator: string = `//div[contains(@id, '${tabTitle}')]//div[@class='margin']`;
        const triggeredBreakpointXpathLocator: string = `//div[contains(@class, 'theia-debug-breakpoint') and contains(@class, 'theia-debug-top-stack-frame')]`;
        const regularBreakpointXpathLocator: string = `//div[contains(@class, 'theia-debug-breakpoint')]`;
        const lineNumberRelativeXpathLocator: string = `/parent::div/div[contains(@class, 'line-numbers') and text()=${lineNumber}]`;

        const breakpointLocator: string = (triggered ? triggeredBreakpointXpathLocator : regularBreakpointXpathLocator);
        const breakpointLineNumberXpathLocator = linesBarXpathLocator + breakpointLocator + lineNumberRelativeXpathLocator;

        return await this.driverHelper.isVisible(By.xpath(breakpointLineNumberXpathLocator));
    }

}
