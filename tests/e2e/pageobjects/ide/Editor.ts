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
import { Ide } from './Ide';
import { Logger } from '../../utils/Logger';


@injectable()
export class Editor {
    private static readonly SUGGESTION_WIDGET_BODY_CSS: string = 'div.visible[widgetId=\'editor.widget.suggestWidget\']';

    private static readonly ADDITIONAL_SHIFTING_TO_Y: number = 19;
    private static readonly ADDITIONAL_SHIFTING_TO_X: number = 1;


    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper,
        @inject(CLASSES.Ide) private readonly ide: Ide) { }

    public async waitSuggestionContainer(timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug('Editor.waitSuggestionContainer');

        await this.driverHelper.waitVisibility(By.css(Editor.SUGGESTION_WIDGET_BODY_CSS), timeout);
    }

    public async waitSuggestionContainerClosed() {
        Logger.debug('Editor.waitSuggestionContainerClosed');

        await this.driverHelper.waitDisappearanceWithTimeout(By.css(Editor.SUGGESTION_WIDGET_BODY_CSS));
    }

    public async waitSuggestion(editorTabTitle: string,
        suggestionText: string,
        timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {

        Logger.debug(`Editor.waitSuggestion tabTitle: "${editorTabTitle}" suggestion: "${suggestionText}"`);

        const suggestionLocator: By = this.getSuggestionLineXpathLocator(suggestionText);

        await this.driverHelper.getDriver().wait(async () => {
            try {
                await this.driverHelper.waitVisibility(suggestionLocator, 5000);
                return true;
            } catch (err) {
                if (!(err instanceof error.TimeoutError)) {
                    throw err;
                }

                await this.pressEscapeButton(editorTabTitle);
                await this.waitSuggestionContainerClosed();
                await this.pressControlSpaceCombination(editorTabTitle);
            }
        }, timeout);
    }

    public async waitSuggestionWithScrolling(editorTabTitle: string,
        suggestionText: string,
        timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {

        Logger.debug(`Editor.waitSuggestion tabTitle: "${editorTabTitle}" suggestion: "${suggestionText}"`);

        const suggestionLocator: By = this.getSuggestionLineXpathLocator(suggestionText);

        await this.driverHelper.getDriver().wait(async () => {
            try {
                await this.scrollAndSearchSuggestion(editorTabTitle, suggestionLocator, 40000);
                return true;
            } catch (err) {
                if (!(err instanceof error.TimeoutError)) {
                    throw err;
                }

                await this.pressEscapeButton(editorTabTitle);
                await this.waitSuggestionContainerClosed();
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

    public async clickOnSuggestion(suggestionText: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug(`Editor.clickOnSuggestion "${suggestionText}"`);

        await this.driverHelper.waitAndClick(this.getSuggestionLineXpathLocator(suggestionText), timeout);
    }

    public async waitTab(tabTitle: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug(`Editor.waitTab "${tabTitle}"`);

        await this.driverHelper.waitVisibility(By.xpath(this.getTabXpathLocator(tabTitle)), timeout);
    }

    public async waitTabDisappearance(tabTitle: string,
        attempt: number = TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS,
        polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING) {

        Logger.debug(`Editor.waitTabDisappearance "${tabTitle}"`);

        await this.driverHelper.waitDisappearance(By.xpath(this.getTabXpathLocator(tabTitle)), attempt, polling);
    }

    public async clickOnTab(tabTitle: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug(`Editor.clickOnTab "${tabTitle}"`);

        await this.ide.closeAllNotifications();
        await this.driverHelper.waitAndClick(By.xpath(this.getTabXpathLocator(tabTitle)), timeout);
    }

    public async waitTabFocused(tabTitle: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug(`Editor.waitTabFocused "${tabTitle}"`);

        const focusedTabLocator: By = By.xpath(`//li[contains(@class, 'p-TabBar-tab') and contains(@class, 'theia-mod-active')]//div[text()='${tabTitle}']`);

        await this.driverHelper.waitVisibility(focusedTabLocator, timeout);
    }

    public async selectTab(tabTitle: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug(`Editor.selectTab "${tabTitle}"`);

        await this.ide.closeAllNotifications();
        await this.waitTab(tabTitle, timeout);
        await this.clickOnTab(tabTitle, timeout);
        await this.waitTabFocused(tabTitle, timeout);
    }

    async closeTab(tabTitle: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug(`Editor.closeTab "${tabTitle}"`);

        const tabCloseButtonLocator: By = By.xpath(`//div[text()='${tabTitle}']/parent::li//div[contains(@class, 'p-TabBar-tabCloseIcon')]`);

        await this.driverHelper.waitAndClick(tabCloseButtonLocator, timeout);
    }

    async waitTabWithUnsavedStatus(tabTitle: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug(`Editor.waitTabWithUnsavedStatus "${tabTitle}"`);

        const unsavedTabLocator: By = this.getTabWithUnsavedStatus(tabTitle);

        await this.driverHelper.waitVisibility(unsavedTabLocator, timeout);
    }

    async waitTabWithSavedStatus(tabTitle: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
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

    async waitEditorOpened(editorTabTitle: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug(`Editor.waitEditorOpened "${editorTabTitle}"`);

        const firstEditorLineLocator: By = By.xpath(this.getEditorLineXpathLocator(1));

        await this.driverHelper.waitPresence(this.getEditorBodyLocator(editorTabTitle), timeout);
        await this.driverHelper.waitPresence(firstEditorLineLocator, timeout);
    }

    async waitEditorAvailable(tabTitle: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
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
        timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT,
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
        timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT,
        polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING) {

        Logger.debug(`Editor.followAndWaitForText title: "${editorTabTitle}" text: "${expectedText}"`);

        await this.selectTab(editorTabTitle, timeout);
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

    async waitErrorInLine(lineNumber: number, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug(`Editor.waitErrorInLine line: "${lineNumber}"`);

        const errorInLineLocator: By = await this.getErrorInLineLocator(lineNumber);
        await this.driverHelper.waitVisibility(errorInLineLocator, timeout);
    }

    async waitErrorInLineDisappearance(lineNumber: number, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug(`Editor.waitErrorInLineDisappearance line: "${lineNumber}"`);

        const errorInLineLocator: By = await this.getErrorInLineLocator(lineNumber);
        await this.driverHelper.waitDisappearanceWithTimeout(errorInLineLocator, timeout);
    }

    async waitStoppedDebugBreakpoint(tabTitle: string, lineNumber: number, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug(`Editor.waitStoppedDebugBreakpoint title: "${tabTitle}" line: "${lineNumber}"`);

        const stoppedDebugBreakpointLocator: By = By.xpath(await this.getStoppedDebugBreakpointXpathLocator(tabTitle, lineNumber));
        await this.driverHelper.waitVisibility(stoppedDebugBreakpointLocator, timeout);
    }

    async waitBreakpoint(tabTitle: string, lineNumber: number, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug(`Editor.waitBreakpoint title: "${tabTitle}" line: "${lineNumber}"`);

        const debugBreakpointLocator: By = await this.getDebugBreakpointLocator(tabTitle, lineNumber);
        await this.driverHelper.waitVisibility(debugBreakpointLocator, timeout);
    }

    async waitBreakpointAbsence(tabTitle: string, lineNumber: number, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug(`Editor.waitBreakpointAbsence title: "${tabTitle}" line: "${lineNumber}"`);

        const debugBreakpointLocator: By = await this.getDebugBreakpointLocator(tabTitle, lineNumber);
        await this.driverHelper.waitDisappearanceWithTimeout(debugBreakpointLocator, timeout);
    }

    async waitBreakpointHint(tabTitle: string, lineNumber: number, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug(`Editor.waitBreakpointHint title: "${tabTitle}" line: "${lineNumber}"`);

        const debugBreakpointHintLocator: By = await this.getDebugBreakpointHintLocator(tabTitle, lineNumber);
        await this.driverHelper.waitVisibility(debugBreakpointHintLocator, timeout);
    }

    async waitBreakpointHintDisappearance(tabTitle: string, lineNumber: number, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug(`Editor.waitBreakpointHintDisappearance title: "${tabTitle}" line: "${lineNumber}"`);

        const debugBreakpointHintLocator: By = await this.getDebugBreakpointHintLocator(tabTitle, lineNumber);
        await this.driverHelper.waitDisappearanceWithTimeout(debugBreakpointHintLocator, timeout);
    }

    async activateBreakpoint(tabTitle: string, lineNumber: number) {
        Logger.debug(`Editor.activateBreakpoint title: "${tabTitle}" line: "${lineNumber}"`);

        const attempts: number = TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS;
        const polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING;

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
                await this.driverHelper.wait(polling);
            }
        }
    }


    async getLineYCoordinates(lineNumber: number): Promise<number> {
        Logger.debug(`Editor.getLineYCoordinates line: "${lineNumber}"`);

        const lineNumberLocator: By = By.xpath(`//div[contains(@class, 'line-numbers') and text()='${lineNumber}']` +
            `//parent::div[contains(@style, 'position')]`);

        let elementStyleValue: string = await this.driverHelper.waitAndGetElementAttribute(lineNumberLocator, 'style');

        elementStyleValue = elementStyleValue.replace('position: absolute; top: ', '');
        elementStyleValue = elementStyleValue.replace('px; width: 100%; height: 19px;', '');

        const lineYCoordinate: number = Number.parseInt(elementStyleValue, 10);

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
            const loadingLocator: By = this.getSuggestionLineXpathLocator('Loading');

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

    private async getStoppedDebugBreakpointXpathLocator(tabTitle: string, lineNumber: number): Promise<string> {
        const lineYPixelCoordinates: number = await this.getLineYCoordinates(lineNumber);
        const stoppedDebugBreakpointXpathLocator: string = `//div[contains(@id, '${tabTitle}')]//div[@class='margin']` +
            `//div[contains(@style, '${lineYPixelCoordinates}px')]` +
            '//div[contains(@class, \'theia-debug-top-stack-frame\')]';

        return stoppedDebugBreakpointXpathLocator;
    }

    private async getDebugBreakpointLocator(tabTitle: string, lineNumber: number): Promise<By> {
        const lineYPixelCoordinates: number = await this.getLineYCoordinates(lineNumber);

        return By.xpath(`//div[contains(@id, '${tabTitle}')]//div[@class='margin']` +
            `//div[contains(@style, '${lineYPixelCoordinates}px')]` +
            '//div[contains(@class, \'theia-debug-breakpoint\')]');
    }

    private async getDebugBreakpointHintLocator(tabTitle: string, lineNumber: number): Promise<By> {
        const lineYPixelCoordinates: number = await this.getLineYCoordinates(lineNumber);

        return By.xpath(`//div[contains(@id, '${tabTitle}')]//div[@class='margin']` +
            `//div[contains(@style, '${lineYPixelCoordinates}px')]` +
            '//div[contains(@class, \'theia-debug-breakpoint-hint\')]');
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
}
