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
import { By } from 'selenium-webdriver';

@injectable()
export class Editor {
    private readonly driverHelper: DriverHelper;

    constructor(@inject(CLASSES.DriverHelper) driverHelper: DriverHelper) {
        this.driverHelper = driverHelper
    }

    private static readonly EDITOR_LINES_CSS: string = ".lines-content .view-line";
    private static readonly EDITOR_BODY_CSS: string = "#theia-main-content-panel .lines-content";
    private static readonly SUGGESTION_WIDGET_BODY_CSS: string = "div[widgetId='editor.widget.suggestWidget']"
    private static readonly SUGGESTION_WIDGET_ROW_CSS: string = "div[widgetId='editor.widget.suggestWidget'] .monaco-list-row";

    private getEditorLineXpathLocator(lineNumber: number): string {
        return `(//div[contains(@class,'lines-content')]//div[@class='view-lines']/div[@class='view-line'])[${lineNumber}]`
    }

    async waitSuggestionContainer(timeout = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.driverHelper.waitVisibility(By.css(Editor.SUGGESTION_WIDGET_BODY_CSS), timeout)
    }

    async waitSuggestionContainerClosed(attempts = TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS, polling = TestConstants.TS_SELENIUM_DEFAULT_POLLING) {
        await this.driverHelper.waitDisappearance(By.css(Editor.SUGGESTION_WIDGET_BODY_CSS), attempts, polling)
    }

    private getTabXpathLocator(tabTitle: string): string {
        return `//li[contains(@class, 'p-TabBar-tab')]//div[text()='${tabTitle}']`;
    }

    async waitTab(tabTitle: string, timeout = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.driverHelper.waitVisibility(By.xpath(this.getTabXpathLocator(tabTitle)), timeout)
    }

    async waitTabDisappearance(tabTitle: string, attempt = TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS, polling = TestConstants.TS_SELENIUM_DEFAULT_POLLING) {
        await this.driverHelper.waitDisappearance(By.xpath(this.getTabXpathLocator(tabTitle)), attempt, polling)
    }

    async clickOnTab(tabTitle: string, timeout = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.driverHelper.waitAndClick(By.xpath(this.getTabXpathLocator(tabTitle)), timeout)
    }

    async waitTabFocused(tabTitle: string, timeout = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const focusedTabLocator: By = By.xpath(`//li[contains(@class, 'p-TabBar-tab') and contains(@class, 'theia-mod-active')]//div[text()='${tabTitle}']`)

        await this.driverHelper.waitVisibility(focusedTabLocator, timeout)

        // wait for increasing stability
        await this.driverHelper.wait(2000)
    }

    async closeTab(tabTitle: string, timeout = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const tabCloseButtonLocator: By = By.xpath(`//div[text()='${tabTitle}']/parent::li//div[contains(@class, 'p-TabBar-tabCloseIcon')]`)

        await this.driverHelper.waitAndClick(tabCloseButtonLocator, timeout)
    }

    async waitEditorOpened(timeout = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const firstEditorLineLocator: By = By.xpath(this.getEditorLineXpathLocator(1))

        await this.driverHelper.waitVisibility(By.css(Editor.EDITOR_BODY_CSS), timeout)
        await this.driverHelper.waitVisibility(firstEditorLineLocator, timeout)
    }

    async waitEditorAvailable(tabTitle: string, timeout = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.waitTab(tabTitle, timeout);
        await this.waitEditorOpened(timeout);
    }

}
