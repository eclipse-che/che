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

@injectable()
export class ProjectTree {
    private static readonly PROJECT_TREE_CONTAINER_CSS: string = '#theia-left-side-panel .theia-TreeContainer';

    constructor(
        @inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper,
        @inject(CLASSES.Ide) private readonly ide: Ide,
        @inject(CLASSES.Editor) private readonly editor: Editor) { }

    async clickCollapseAllButton() {
        Logger.debug('ProjectTree.clickCollapseAllButton');

        const collapseAllButtonLocator: By = By.css('div.theia-sidepanel-toolbar div.theia-collapse-all-icon');
        await this.driverHelper.waitAndClick(collapseAllButtonLocator);
    }

    async waitTreeCollapsed(projectName: string, rootSubItem: string) {
        Logger.debug(`ProjectTree.waitTreeCollapsed project: "${projectName}", subitem: "${rootSubItem}"`);

        const rootSubitemLocator: By = By.css(this.getTreeItemCssLocator(`${projectName}/${rootSubItem}`));
        await this.driverHelper.waitDisappearanceWithTimeout(rootSubitemLocator);
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

    async collapseAssociatedWorkspaceProjectTree(projectName: string, rootSubItem: string) {
        Logger.debug(`ProjectTree.collapseProjectTree project: "${projectName}", subitem: "${rootSubItem}"`);

        await this.clickCollapseAllButton();
        await this.waitTreeCollapsed(projectName, rootSubItem);
    }

    async openProjectTreeContainer(timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug('ProjectTree.openProjectTreeContainer');

        const selectedExplorerButtonLocator: By = By.css(Ide.SELECTED_EXPLORER_BUTTON_CSS);

        await this.ide.waitLeftToolbarButton(LeftToolbarButton.Explorer, timeout);

        const isButtonEnabled: boolean = await this.driverHelper.waitVisibilityBoolean(selectedExplorerButtonLocator);

        if (!isButtonEnabled) {
            await this.ide.waitAndClickLeftToolbarButton(LeftToolbarButton.Explorer, timeout);
        }

        await this.waitProjectTreeContainer();
    }

    async waitItemExpanded(itemPath: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug(`ProjectTree.waitItemExpanded "${itemPath}"`);

        const locator: string = await this.getExpandedItemCssLocator(itemPath);
        const expandedItemLocator: By = By.css(locator);
        await this.driverHelper.waitVisibility(expandedItemLocator, timeout);
    }

    async waitItemCollapsed(itemPath: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug(`ProjectTree.waitItemCollapsed "${itemPath}"`);

        const locator: string = await this.getCollapsedItemCssLocator(itemPath);
        const collapsedItemLocator: By = By.css(locator);

        await this.driverHelper.waitVisibility(collapsedItemLocator, timeout);
    }

    async waitProjectTreeContainer(timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug('ProjectTree.waitProjectTreeContainer');

        await this.driverHelper.waitPresence(By.css(ProjectTree.PROJECT_TREE_CONTAINER_CSS), timeout);
    }

    async waitProjectTreeContainerClosed(attempts: number = TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS,
        polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING) {

        Logger.debug('ProjectTree.waitProjectTreeContainerClosed');

        await this.driverHelper.waitDisappearance(By.css(ProjectTree.PROJECT_TREE_CONTAINER_CSS), attempts, polling);
    }

    async waitItem(itemPath: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
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

    async clickOnItem(itemPath: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug(`ProjectTree.clickOnItem "${itemPath}"`);

        const locator: string = await this.getItemCss(itemPath);
        await this.driverHelper.waitAndClick(By.css(locator), timeout);
        await this.waitItemSelected(itemPath, timeout);
    }

    async waitItemSelected(itemPath: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug(`ProjectTree.waitItemSelected "${itemPath}"`);

        const selectedItemLocator: By = By.css(`div[title='/projects/${itemPath}'].theia-mod-selected.theia-mod-focus`);
        await this.driverHelper.waitVisibility(selectedItemLocator, timeout);
    }

    async expandItem(itemPath: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
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
                    throw err('Unexpected error during project tree expanding');
                }

                console.log(`The '${itemPath}' item has not been expanded, try again`);
                await this.driverHelper.wait(TestConstants.TS_SELENIUM_DEFAULT_POLLING);
            }
        }, timeout);
    }

    async collapseItem(itemPath: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
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

    async expandPath(path: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
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

    async expandPathAndOpenFile(pathToItem: string, fileName: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug(`ProjectTree.expandPathAndOpenFile "${pathToItem}" filename: ${fileName}`);

        await this.expandPath(pathToItem, timeout);
        await this.clickOnItem(`${pathToItem}/${fileName}`, timeout);

        await this.editor.waitEditorOpened(fileName, timeout);
        await this.editor.waitTab(fileName);
    }

    async expandPathAndOpenFileInAssociatedWorkspace(pathToItem: string, fileName: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug(`ProjectTree.expandPathAndOpenFileInAssociatedWorkspace "${pathToItem}"`);

        let projectName: string = pathToItem.split('/')[0];
        let pathEntry = `${projectName}`;
        let pathToItemInAssociatedWorkspace = pathToItem.replace(`${projectName}/`, '');
        let paths: Array<string> = new Array();

        // if we in the root of project
        if (pathToItem.split('/').length < 2) {
            await this.clickOnItem(`${projectName}/${fileName}`, timeout);
            return;
        }
        // make direct path for each project tree item
        pathToItemInAssociatedWorkspace.split('/')
            .forEach(item => {
                pathEntry = pathEntry + `/${item}`;
                paths.push(pathEntry);
            });


        // expand each project tree item
        for (const path of paths) {
            await this.expandItem(path, timeout);
        }
        // open file
        await this.clickOnItem(`${projectName}/${pathToItemInAssociatedWorkspace}/${fileName}`, timeout);

        // check file appearance in the editor
        await this.editor.waitEditorOpened(fileName, timeout);
        await this.editor.waitTab(fileName);
    }

    async waitProjectImported(projectName: string,
        rootSubItem: string,
        attempts: number = TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS,
        visibilityItemPolling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING * 5,
        triesPolling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING * 30) {

        Logger.debug(`ProjectTree.waitProjectImported "${projectName}" rootSubItem: "${rootSubItem}"`);

        const rootItem: string = `${projectName}`;
        const rootItemLocator: By = By.css(this.getTreeItemCssLocator(`${projectName}`));
        const rootSubitemLocator: By = By.css(this.getTreeItemCssLocator(`${projectName}/${rootSubItem}`));

        for (let i = 0; i < attempts; i++) {
            const isProjectFolderVisible = await this.driverHelper.waitVisibilityBoolean(rootItemLocator, attempts, visibilityItemPolling);

            if (!isProjectFolderVisible) {
                await this.driverHelper.reloadPage();
                await this.driverHelper.wait(triesPolling);
                await this.ide.waitAndSwitchToIdeFrame();
                await this.ide.waitIde();
                await this.openProjectTreeContainer();
                continue;
            }

            await this.expandItem(rootItem);
            await this.waitItemExpanded(rootItem);

            const isRootSubItemVisible = await this.driverHelper.waitVisibilityBoolean(rootSubitemLocator, attempts, visibilityItemPolling);

            if (!isRootSubItemVisible) {
                await this.driverHelper.reloadPage();
                await this.driverHelper.wait(triesPolling);
                await this.ide.waitAndSwitchToIdeFrame();
                await this.ide.waitIde();
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
        const attribute: string = await this.driverHelper.waitAndGetElementAttribute(By.css(`div[${nodeAttribute}]`), nodeAttribute);
        return attribute.split(splitDelimeter)[0] + splitDelimeter;
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
        const entry: string = await this.getWorkspacePathEntry();
        return `div[data-node-id='${entry}/projects/${itemPath}']`;
    }

    private getTreeItemCssLocator(itemPath: string): string {
        return `.theia-TreeNode[title='/projects/${itemPath}']`;
    }

}
