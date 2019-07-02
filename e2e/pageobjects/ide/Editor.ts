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
import { By, Key, error, WebElement } from 'selenium-webdriver';
import { Ide } from './Ide';

@injectable()
export class Editor {
    private static readonly SUGGESTION_WIDGET_BODY_CSS: string = 'div.visible[widgetId=\'editor.widget.suggestWidget\']';

    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper,
        @inject(CLASSES.Ide) private readonly ide: Ide) { }

    public async waitSuggestionContainer(timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.driverHelper.waitVisibility(By.css(Editor.SUGGESTION_WIDGET_BODY_CSS), timeout);
    }

    public async waitSuggestionContainerClosed() {
        await this.driverHelper.waitDisappearanceWithTimeout(By.css(Editor.SUGGESTION_WIDGET_BODY_CSS));
    }

    public async waitSuggestion(editorTabTitle: string,
        suggestionText: string,
        timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {

        const suggestionLocator: By = this.getSuggestionLineXpathLocator(suggestionText);

        await this.driverHelper.getDriver().wait(async () => {
            await this.waitSuggestionContainer();
            try {
                await this.driverHelper.waitVisibility(suggestionLocator, 5000);
                return true;
            } catch (err) {
                const isTimeoutError: boolean = err instanceof error.TimeoutError;
                if (!isTimeoutError) {
                    throw err;
                }

                await this.pressEscapeButton(editorTabTitle);
                await this.waitSuggestionContainerClosed();
                await this.pressControlSpaceCombination(editorTabTitle);
            }
        }, timeout);
    }

    public async waitHighlightedSuggestion(editorTabTitle: string,
        suggestionText: string,
        timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {

        const suggestionLocator: By = this.getSuggestionLineXpathLocator(suggestionText);

        await this.driverHelper.getDriver().wait(async () => {
            await this.waitSuggestionContainer();
            try {
                await this.driverHelper.waitVisibility(suggestionLocator, 5000);
                return true;
            } catch (err) {
                const isTimeoutError: boolean = err instanceof error.TimeoutError;
                if (!isTimeoutError) {
                    throw err;
                }

                await this.pressEscapeButton(editorTabTitle);
                await this.waitSuggestionContainerClosed();
                await this.pressControlSpaceCombination(editorTabTitle);
            }
        }, timeout);
    }

    public async pressControlSpaceCombination(editorTabTitle: string) {
        await this.performKeyCombination(editorTabTitle, Key.chord(Key.CONTROL, Key.SPACE));
    }

    public async pressEscapeButton(editorTabTitle: string) {
        await this.performKeyCombination(editorTabTitle, Key.ESCAPE);
    }

    public async clickOnSuggestion(suggestionText: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.driverHelper.waitAndClick(this.getSuggestionLineXpathLocator(suggestionText), timeout);
    }

    public async waitTab(tabTitle: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.driverHelper.waitVisibility(By.xpath(this.getTabXpathLocator(tabTitle)), timeout);
    }

    public async waitTabDisappearance(tabTitle: string,
        attempt: number = TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS,
        polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING) {
        await this.driverHelper.waitDisappearance(By.xpath(this.getTabXpathLocator(tabTitle)), attempt, polling);
    }

    public async clickOnTab(tabTitle: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.ide.closeAllNotifications();
        await this.driverHelper.waitAndClick(By.xpath(this.getTabXpathLocator(tabTitle)), timeout);
    }

    public async waitTabFocused(tabTitle: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const focusedTabLocator: By = By.xpath(`//li[contains(@class, 'p-TabBar-tab') and contains(@class, 'theia-mod-active')]//div[text()='${tabTitle}']`);

        await this.driverHelper.waitVisibility(focusedTabLocator, timeout);

        // wait for increasing stability
        await this.driverHelper.wait(2000);
    }

    public async selectTab(tabTitle: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.waitTab(tabTitle, timeout);
        await this.clickOnTab(tabTitle, timeout);
        await this.waitTabFocused(tabTitle, timeout);
    }

    async closeTab(tabTitle: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const tabCloseButtonLocator: By = this.getTabCloseIconLocator(tabTitle);

        await this.driverHelper.waitAndClick(tabCloseButtonLocator, timeout);
    }

    async waitTabWithUnsavedStatus(tabTitle: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const unsavedTabLocator: By = this.getTabWithUnsavedStatus(tabTitle);

        await this.driverHelper.waitVisibility(unsavedTabLocator, timeout);
    }

    async waitTabWithSavedStatus(tabTitle: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const unsavedTabLocator: By = this.getTabWithUnsavedStatus(tabTitle);

        await this.driverHelper.waitDisappearanceWithTimeout(unsavedTabLocator, timeout);
        await this.waitTab(tabTitle, timeout);
    }

    async waitEditorOpened(editorTabTitle: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const firstEditorLineLocator: By = By.xpath(this.getEditorLineXpathLocator(1));

        await this.driverHelper.waitPresence(this.getEditorBodyLocator(editorTabTitle), timeout);
        await this.driverHelper.waitPresence(firstEditorLineLocator, timeout);
    }

    async waitEditorAvailable(tabTitle: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.waitTab(tabTitle, timeout);
        await this.waitEditorOpened(tabTitle, timeout);
    }

    async getLineText(tabTitle: string, lineNumber: number): Promise<string> {
        const lineIndex: number = lineNumber - 1;
        const editorText: string = await this.getEditorVisibleText(tabTitle);
        const editorLines: string[] = editorText.split('\n');
        const editorLine = editorLines[lineIndex] + '\n';

        return editorLine;
    }

    async getEditorVisibleText(tabTitle: string): Promise<string> {
        const editorBodyLocator: By = By.xpath(`//div[contains(@data-uri, \'${tabTitle}')]//div[@class=\'view-lines\']`);
        // const editorBodyLocator: By = By.xpath('//div[@class=\'view-lines\']');
        const editorText: string = await this.driverHelper.waitAndGetText(editorBodyLocator);
        return editorText;
    }

    async waitText(tabTitle: string, expectedText: string,
        timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT,
        polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING) {
        await this.driverHelper.getDriver().wait(async () => {
            const editorText: string = await this.getEditorVisibleText(tabTitle);
            const isEditorContainText: boolean = editorText.includes(expectedText);

            if (isEditorContainText) {
                return true;
            }

            this.driverHelper.wait(polling);
        }, timeout);
    }

    async followAndWaitForText(editorTabTitle: string,
        expectedText: string,
        timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT,
        polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING) {

        await this.ide.closeAllNotifications();
        await this.clickOnTab(editorTabTitle);
        await this.waitTabFocused(editorTabTitle);

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
        // set cursor to the 1:1 position
        await this.performKeyCombination(editorTabTitle, Key.chord(Key.CONTROL, Key.HOME));

        // for ensuring that cursor has been set to the 1:1 position
        await this.driverHelper.wait(1000);

        // move cursor to line
        for (let i = 1; i < line; i++) {
            await this.performKeyCombination(editorTabTitle, Key.ARROW_DOWN);
        }

        // move cursor to char
        for (let i = 1; i < char; i++) {
            await this.performKeyCombination(editorTabTitle, Key.ARROW_RIGHT);
        }
    }

    public async performKeyCombination(editorTabTitle: string, text: string) {
        const interactionContainerLocator: By = this.getEditorActionArreaLocator(editorTabTitle);

        await this.driverHelper.type(interactionContainerLocator, text);
    }

    async type(editorTabTitle: string, text: string, line: number) {
        await this.selectTab(editorTabTitle);
        await this.moveCursorToLineAndChar(editorTabTitle, line, 1);
        await this.performKeyCombination(editorTabTitle, text);
    }

    async waitErrorInLine(lineNumber: number, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const errorInLineLocator: By = await this.getErrorInLineLocator(lineNumber);

        await this.driverHelper.waitVisibility(errorInLineLocator, timeout);
    }

    async waitErrorInLineDisappearance(lineNumber: number, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const errorInLineLocator: By = await this.getErrorInLineLocator(lineNumber);

        await this.driverHelper.waitDisappearanceWithTimeout(errorInLineLocator, timeout);
    }

    async waitStoppedDebugBreakpoint(tabTitle: string, lineNumber: number, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const stoppedDebugBreakpointLocator: By = By.xpath(await this.getStoppedDebugBreakpointXpathLocator(tabTitle, lineNumber));

        await this.driverHelper.waitVisibility(stoppedDebugBreakpointLocator, timeout);
    }

    async waitBreakpoint(tabTitle: string, lineNumber: number, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const debugBreakpointLocator: By = await this.getDebugBreakpointLocator(tabTitle, lineNumber);

        await this.driverHelper.waitVisibility(debugBreakpointLocator, timeout);
    }

    async waitBreakpointAbsence(tabTitle: string, lineNumber: number, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const debugBreakpointLocator: By = await this.getDebugBreakpointLocator(tabTitle, lineNumber);

        await this.driverHelper.waitDisappearanceWithTimeout(debugBreakpointLocator, timeout);
    }

    async waitBreakpointHint(tabTitle: string, lineNumber: number, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const debugBreakpointHintLocator: By = await this.getDebugBreakpointHintLocator(tabTitle, lineNumber);

        await this.driverHelper.waitVisibility(debugBreakpointHintLocator, timeout);
    }

    async waitBreakpointHintDisappearance(tabTitle: string, lineNumber: number, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const debugBreakpointHintLocator: By = await this.getDebugBreakpointHintLocator(tabTitle, lineNumber);

        await this.driverHelper.waitDisappearanceWithTimeout(debugBreakpointHintLocator, timeout);
    }

    async activateBreakpoint(tabTitle: string,
        lineNumber: number,
        timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {

        const attempts: number = TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS;
        const polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING;

        for (let i = 0; i < attempts; i++) {
            const elementLocator: By = await this.getLineNumberBlockLocator(tabTitle, lineNumber);
            const element: WebElement = await this.driverHelper.waitVisibility(elementLocator, timeout);

            try {
                await this.driverHelper.getAction().mouseMove(element, { x: 5, y: 5 }).perform();
                await this.waitBreakpointHint(tabTitle, lineNumber);
                await this.driverHelper.getAction().click().perform();
                await this.waitBreakpoint(tabTitle, lineNumber);
                return;
            } catch (err) {
                if (err instanceof error.StaleElementReferenceError) {
                    await this.driverHelper.wait(polling);
                    continue;
                }

                if (err instanceof error.TimeoutError) {
                    throw (err);
                }

                if (err instanceof error.WebDriverError) {
                    await this.driverHelper.wait(polling);
                    continue;
                }

                throw err;
            }
        }

        throw new Error(`Exceeded maximum breakpoint activation attempts`);
    }

    private async getLineYCoordinates(lineNumber: number): Promise<number> {
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

    private async getLineNumberBlockLocator(tabTitle: string, lineNumber: number): Promise<By> {
        const lineYPixelCoordinates: number = await this.getLineYCoordinates(lineNumber);

        return By.xpath(`//div[contains(@id, '${tabTitle}')]//div[@class='margin']` +
            `//div[contains(@style, '${lineYPixelCoordinates}px')]`);
    }

    private getSuggestionLineXpathLocator(suggestionText: string): By {
        return By.xpath(`//div[@widgetid='editor.widget.suggestWidget']//div[@aria-label='${suggestionText}, suggestion, has details']`);
    }

    private getTabXpathLocator(tabTitle: string): string {
        return `//li[contains(@class, 'p-TabBar-tab')]//div[text()='${tabTitle}']`;
    }

    private getTabCloseIconLocator(tabTitle: string): By {
        return By.xpath(`//div[text()='${tabTitle}']/parent::li//div[contains(@class, 'p-TabBar-tabCloseIcon')]`);
    }

    private async getErrorInLineLocator(lineNumber: number): Promise<By> {
        const lineYCoordinates: number = await this.getLineYCoordinates(lineNumber);

        return By.xpath(`//div[contains(@style, 'top:${lineYCoordinates}px')]//div[contains(@class, 'squiggly-error')]`);
    }
}
