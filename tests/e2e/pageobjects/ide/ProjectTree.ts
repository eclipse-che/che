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
import { Ide, LeftToolbarButton } from './Ide';
import { TestConstants } from '../../TestConstants';
import { By, error } from 'selenium-webdriver';
import { Editor } from './Editor';
import { Logger } from '../../utils/Logger';
import { TimeoutConstants } from '../../TimeoutConstants';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';

@injectable()
export class ProjectTree {
    private static readonly PROJECT_TREE_CONTAINER_LOCATOR: By = By.css('#theia-left-side-panel #explorer-view-container--files .theia-TreeContainer');

    constructor(
        @inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper,
        @inject(CLASSES.Ide) private readonly ide: Ide,
        @inject(CLASSES.Editor) private readonly editor: Editor,
        @inject(CLASSES.BrowserTabsUtil) private readonly browserTabsUtil: BrowserTabsUtil) { }

    async clickCollapseAllButton() {
        Logger.debug('ProjectTree.clickCollapseAllButton');

        const collapseAllButtonLocator: By = By.id('navigator.collapse.all');
        await this.driverHelper.waitAndClick(collapseAllButtonLocator);
    }

    async waitTreeCollapsed(projectName: string, rootSubItem: string) {
        Logger.debug(`ProjectTree.waitTreeCollapsed project: "${projectName}", subitem: "${rootSubItem}"`);

        const rootSubitemLocator: By = By.css(this.getTreeItemCssLocator(`${projectName}/${rootSubItem}`));
        await this.driverHelper.waitDisappearanceWithTimeout(rootSubitemLocator, TimeoutConstants.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM);
    }

    async collapseProjectTree(projectName: string, rootSubItem: string) {
        Logger.debug(`ProjectTree.collapseProjectTree project: "${projectName}", subitem: "${rootSubItem}"`);

        await this.clickCollapseAllButton();
        await this.waitTreeCollapsed(projectName, rootSubItem);
    }

    async waitAssociatedWorkspaceProjectTreeCollapsed(projectName: string, expandedRootItem: string) {
        Logger.debug(`ProjectTree.waitTreeCollapsed project name: "${projectName}", expanded root item: "${expandedRootItem}"`);

        // const rootSubitemLocator: By = By.css(this.getTreeItemCssLocator(`${projectName}/${expandedRootItem}`));
        await this.waitItemCollapsed(`${projectName}/${expandedRootItem}`);
    }

    async openProjectTreeContainer(timeout: number = TimeoutConstants.TS_PROJECT_TREE_TIMEOUT) {
        Logger.debug('ProjectTree.openProjectTreeContainer');

        Logger.trace(`ProjectTree.openProjectTreeContainer waitLeftToolbarButtonPresence`);
        await this.ide.waitLeftToolbarButton(LeftToolbarButton.Explorer, timeout);

        Logger.trace(`ProjectTree.openProjectTreeContainer waitForExplorerToolbarButton`);
        const explorerButtonActiveLocator: By = this.getLeftToolbarButtonActiveLocator(LeftToolbarButton.Explorer);
        const isButtonActive: boolean = await this.driverHelper.waitVisibilityBoolean(explorerButtonActiveLocator);

        Logger.debug(`ProjectTree.openProjectTreeContainer leftToolbarButtonActive:${isButtonActive}`);
        if (!isButtonActive) {
            await this.ide.waitAndClickLeftToolbarButton(LeftToolbarButton.Explorer, timeout);
        }

        await this.waitProjectTreeContainer();
    }

    async waitItemExpanded(itemPath: string, timeout: number = TimeoutConstants.TS_PROJECT_TREE_TIMEOUT) {
        Logger.debug(`ProjectTree.waitItemExpanded "${itemPath}"`);

        const locator: string = await this.getExpandedItemCssLocator(itemPath);
        const expandedItemLocator: By = By.css(locator);
        await this.driverHelper.waitVisibility(expandedItemLocator, timeout);
    }

    async waitItemCollapsed(itemPath: string, timeout: number = TimeoutConstants.TS_PROJECT_TREE_TIMEOUT) {
        Logger.debug(`ProjectTree.waitItemCollapsed "${itemPath}"`);

        const locator: string = await this.getCollapsedItemCssLocator(itemPath);
        const collapsedItemLocator: By = By.css(locator);

        await this.driverHelper.waitVisibility(collapsedItemLocator, timeout);
    }

    async waitProjectTreeContainer(timeout: number = TimeoutConstants.TS_PROJECT_TREE_TIMEOUT) {
        Logger.debug('ProjectTree.waitProjectTreeContainer');

        await this.driverHelper.waitPresence(ProjectTree.PROJECT_TREE_CONTAINER_LOCATOR, timeout);
    }

    async waitProjectTreeContainerClosed(attempts: number = TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS,
        polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING) {

        Logger.debug('ProjectTree.waitProjectTreeContainerClosed');

        await this.driverHelper.waitDisappearance(ProjectTree.PROJECT_TREE_CONTAINER_LOCATOR, attempts, polling);
    }

    async waitItem(itemPath: string, timeout: number = TimeoutConstants.TS_PROJECT_TREE_TIMEOUT) {
        Logger.debug(`ProjectTree.waitItem "${itemPath}"`);

        const locator: string = await this.getItemCss(itemPath);
        await this.driverHelper.waitVisibility(By.css(locator), timeout);
    }

    async waitItemDisappearance(itemPath: string,
        attempts: number = TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS,
        polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING) {

        Logger.debug(`ProjectTree.waitItemDisappearance "${itemPath}"`);

        const locator: string = await this.getItemCss(itemPath);
        await this.driverHelper.waitDisappearance(By.css(locator), attempts, polling);
    }

    async clickOnItem(itemPath: string, timeout: number = TimeoutConstants.TS_PROJECT_TREE_CLICK_ON_ITEM_TIMEOUT) {
        Logger.debug(`ProjectTree.clickOnItem "${itemPath}"`);

        const locator: string = await this.getItemCss(itemPath);
        await this.driverHelper.waitAndClick(By.css(locator), timeout);
        await this.waitItemSelected(itemPath, timeout);
    }

    async waitItemSelected(itemPath: string, timeout: number = TimeoutConstants.TS_PROJECT_TREE_TIMEOUT) {
        Logger.debug(`ProjectTree.waitItemSelected "${itemPath}"`);

        const selectedItemLocator: By = By.css(`div[title='/projects/${itemPath}'].theia-mod-selected.theia-mod-focus`);
        await this.driverHelper.waitVisibility(selectedItemLocator, timeout);
    }

    async expandItem(itemPath: string, timeout: number = TimeoutConstants.TS_PROJECT_TREE_CLICK_ON_ITEM_TIMEOUT) {
        Logger.debug(`ProjectTree.expandItem "${itemPath}"`);

        const locator: string = await this.getExpandIconCssLocator(itemPath);
        const expandIconLocator: By = By.css(locator);
        const treeItemLocator: By = By.css(this.getTreeItemCssLocator(itemPath));

        await this.driverHelper.getDriver().wait(async () => {
            const classAttributeValue: string = await this.driverHelper.waitAndGetElementAttribute(expandIconLocator, 'class', timeout);
            const isItemCollapsed: boolean = classAttributeValue.search('theia-mod-collapsed') > 0;
            if (isItemCollapsed) {
                await this.driverHelper.waitAndClick(treeItemLocator, timeout);
            }

            try {
                await this.waitItemExpanded(itemPath, TestConstants.TS_SELENIUM_DEFAULT_POLLING);
                return true;
            } catch (err) {
                if (!(err instanceof error.TimeoutError)) {
                    throw err;
                }

                console.log(`The '${itemPath}' item has not been expanded, try again`);
                await this.driverHelper.wait(TestConstants.TS_SELENIUM_DEFAULT_POLLING);
            }
        }, timeout);
    }

    async collapseItem(itemPath: string, timeout: number = TimeoutConstants.TS_PROJECT_TREE_CLICK_ON_ITEM_TIMEOUT) {
        Logger.debug(`ProjectTree.collapseItem "${itemPath}"`);

        const locator: string = await this.getExpandIconCssLocator(itemPath);
        const expandIconLocator: By = By.css(locator);
        const treeItemLocator: By = By.css(this.getTreeItemCssLocator(itemPath));

        const classAttributeValue: string = await this.driverHelper.waitAndGetElementAttribute(expandIconLocator, 'class', timeout);
        const isItemCollapsed: boolean = classAttributeValue.search('theia-mod-collapsed') > 0;

        if (!isItemCollapsed) {
            await this.driverHelper.waitAndClick(treeItemLocator, timeout);
        }

        await this.waitItemCollapsed(itemPath, timeout);
    }

    async expandPath(path: string, timeout: number = TimeoutConstants.TS_EXPAND_PROJECT_TREE_ITEM_TIMEOUT) {
        Logger.debug(`ProjectTree.expandPath "${path}"`);

        let items: Array<string> = path.split('/');
        let projectName: string = items[0];
        let paths: Array<string> = new Array();
        paths.push(projectName);

        // make direct path for each project tree item
        for (let i = 1; i < items.length; i++) {
            let item = items[i];
            projectName = `${projectName}/${item}`;
            paths.push(projectName);
        }

        // expand each project tree item
        for (const path of paths) {
            await this.expandItem(path, timeout);
        }
    }

    /**
     *
     * @param pathToItem path to the file that should be opened
     * @param fileName file that should be opened
     * @param timeoutForSigleItem timeout applied for every item in path to be opened
     */
    async expandPathAndOpenFile(pathToItem: string, fileName: string, timeoutForSigleItem: number = TimeoutConstants.TS_OPEN_EDITOR_TIMEOUT) {
        Logger.debug(`ProjectTree.expandPathAndOpenFile "${pathToItem}" filename: ${fileName}`);

        await this.expandPath(pathToItem, timeoutForSigleItem);
        await this.clickOnItem(`${pathToItem}/${fileName}`, timeoutForSigleItem);

        await this.editor.waitEditorOpened(fileName);
        await this.editor.waitTab(fileName);
    }

    async waitProjectImported(projectName: string,
        rootSubItem: string,
        attempts: number = TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS,
        visibilityItemPolling: number = TimeoutConstants.TS_IMPORT_PROJECT_DEFAULT_POLLING,
        triesPolling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING * 30) {

        Logger.debug(`ProjectTree.waitProjectImported "${projectName}" rootSubItem: "${rootSubItem}"`);

        const rootItem: string = `${projectName}`;
        const rootItemLocator: By = By.css(this.getTreeItemCssLocator(`${projectName}`));
        const rootSubitemLocator: By = By.css(this.getTreeItemCssLocator(`${projectName}/${rootSubItem}`));

        for (let i = 0; i < attempts; i++) {
            // do five checks of the item in one fifth of the time given for root folder item (was causing frequent reloads of the workspace)
            const isRootFolderVisible = await this.driverHelper.waitVisibilityBoolean(rootItemLocator, 5, visibilityItemPolling / 5);

            if (!isRootFolderVisible) {
                Logger.trace(`ProjectTree.waitProjectImported project not located, reloading page.`);
                await this.browserTabsUtil.refreshPage();
                await this.ide.waitWorkspaceAndIde();
                await this.openProjectTreeContainer();
                if (i === attempts - 1) {
                    throw new error.TimeoutError('Exceeded the maximum number of checking attempts, project has not been imported [unable to locate project root folder]');
                }
                continue;
            }

            Logger.trace(`ProjectTree.waitProjectImported project found, waiting for sub-items`);
            await this.expandItem(rootItem);
            await this.waitItemExpanded(rootItem);

            // do five checks of the item in one fifth of the time given for root folder item (was causing frequent reloads of the workspace)
            const isSubfolderVisible = await this.driverHelper.waitVisibilityBoolean(rootSubitemLocator, 5, visibilityItemPolling / 5);

            if (!isSubfolderVisible) {
                Logger.trace(`ProjectTree.waitProjectImported sub-items not found, reloading page.`);
                await this.browserTabsUtil.refreshPage();
                await this.ide.waitWorkspaceAndIde();
                await this.openProjectTreeContainer();
                if (i === attempts - 1) {
                    throw new error.TimeoutError('Exceeded the maximum number of checking attempts, project has not been imported [unable to locate project subfolder]');
                }
                continue;
            }

            Logger.trace(`ProjectTree.waitProjectImported project successfully imported`);
            break;
        }
    }

    async waitProjectImportedNoSubfolder(projectName: string,
        attempts: number = TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS,
        visibilityItemPolling: number = TimeoutConstants.TS_IMPORT_PROJECT_DEFAULT_POLLING,
        triesPolling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING * 60) {

        Logger.debug(`ProjectTree.waitProjectImportedNoSubfolder "${projectName}"`);

        const rootItemLocator: By = By.css(this.getTreeItemCssLocator(`${projectName}`));

        for (let i = 0; i < attempts; i++) {
            const isProjectFolderVisible = await this.driverHelper.waitVisibilityBoolean(rootItemLocator, 5, visibilityItemPolling / 5);

            if (!isProjectFolderVisible) {
                Logger.trace(`ProjectTree.waitProjectImportedNoSubfolder project not located, reloading page.`);
                await this.browserTabsUtil.refreshPage();
                await this.driverHelper.wait(triesPolling);
                await this.ide.waitWorkspaceAndIde();
                await this.openProjectTreeContainer();
                continue;
            }

            return;
        }

        throw new error.TimeoutError('Exceeded the maximum number of checking attempts, project has not been imported');
    }

    private async  getWorkspacePathEntry(): Promise<string> {
        const nodeAttribute: string = 'data-node-id';
        const splitDelimeter = ':';
        const attribute: string = await this.driverHelper.waitAndGetElementAttribute(By.css(`div[${nodeAttribute}]`), nodeAttribute, TimeoutConstants.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM);
        return attribute.split(splitDelimeter)[0] + splitDelimeter;
    }

    private getLeftToolbarButtonActiveLocator(buttonTitle: String): By {
        return By.xpath(`//div[@id='theia-left-content-panel']//ul[@class='p-TabBar-content']` +
            `//li[@title[contains(.,'${buttonTitle}')] and contains(@id, 'shell-tab') and contains(@class, 'p-mod-current')]`);
    }

    private async getItemCss(itemPath: string): Promise<string> {
        const entry: string = await this.getWorkspacePathEntry();
        return `div[id='${entry}/projects/${itemPath}']`;
    }

    private async getCollapsedItemCssLocator(itemPath: string): Promise<string> {
        const item: string = await this.getExpandIconCssLocator(itemPath);
        return item + '.theia-mod-collapsed';
    }

    private async getExpandedItemCssLocator(itemPath: string): Promise<string> {
        const item: string = await this.getExpandIconCssLocator(itemPath);
        return item + ':not(.theia-mod-collapsed)';
    }

    private async getExpandIconCssLocator(itemPath: string): Promise<string> {
        const items: Array<string> = itemPath.split('/');
        const entry: string = items.length > 1 ? await this.getWorkspacePathEntry() : '';
        return `div[data-node-id='${entry}/projects/${itemPath}']`;
    }

    private getTreeItemCssLocator(itemPath: string): string {
        return `.theia-TreeNode[title='/projects/${itemPath}']`;
    }

}
