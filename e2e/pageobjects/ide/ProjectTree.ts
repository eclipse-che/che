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
import { Ide, RightToolbarButton } from './Ide';
import { TestConstants } from '../../TestConstants';
import { By } from 'selenium-webdriver';
import { Editor } from './Editor';

@injectable()
export class ProjectTree {
    private static readonly PROJECT_TREE_CONTAINER_CSS: string = '#theia-left-side-panel .theia-TreeContainer';

    constructor(
        @inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper,
        @inject(CLASSES.Ide) private readonly ide: Ide,
        @inject(CLASSES.Editor) private readonly editor: Editor) { }

    async openProjectTreeContainer(timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const selectedExplorerButtonLocator: By = By.xpath(Ide.SELECTED_EXPLORER_BUTTON_XPATH);

        await this.ide.waitRightToolbarButton(RightToolbarButton.Explorer, timeout);

        const isButtonEnabled: boolean = await this.driverHelper.waitVisibilityBoolean(selectedExplorerButtonLocator);

        if (!isButtonEnabled) {
            await this.ide.waitAndClickRightToolbarButton(RightToolbarButton.Explorer, timeout);
        }

        await this.waitProjectTreeContainer();
    }

    async waitItemExpanded(itemPath: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const expandedItemLocator: By = By.css(this.getExpandedItemCssLocator(itemPath));

        await this.driverHelper.waitVisibility(expandedItemLocator, timeout);
    }

    async waitItemCollapsed(itemPath: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const collapsedItemLocator: By = By.css(this.getCollapsedItemCssLocator(itemPath));

        await this.driverHelper.waitVisibility(collapsedItemLocator, timeout);
    }

    async waitProjectTreeContainer(timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.driverHelper.waitPresence(By.css(ProjectTree.PROJECT_TREE_CONTAINER_CSS), timeout);
    }

    async waitProjectTreeContainerClosed(attempts: number = TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS,
        polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING) {

        await this.driverHelper.waitDisappearance(By.css(ProjectTree.PROJECT_TREE_CONTAINER_CSS), attempts, polling);
    }

    async waitItem(itemPath: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.driverHelper.waitVisibility(By.css(this.getItemCss(itemPath)), timeout);
    }

    async waitItemDisappearance(itemPath: string,
        attempts: number = TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS,
        polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING) {

        await this.driverHelper.waitDisappearance(By.css(this.getItemCss(itemPath)), attempts, polling);
    }

    async clickOnItem(itemPath: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.driverHelper.waitAndClick(By.css(this.getItemCss(itemPath)), timeout);
        await this.waitItemSelected(itemPath, timeout);
    }

    async waitItemSelected(itemPath: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const selectedItemLocator: By = By.css(`div[title='/projects/${itemPath}'].theia-mod-selected.theia-mod-focus`);

        await this.driverHelper.waitVisibility(selectedItemLocator, timeout);
    }

    async expandItem(itemPath: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const expandIconLocator: By = By.css(this.getExpandIconCssLocator(itemPath));
        const treeItemLocator: By = By.css(this.getTreeItemCssLocator(itemPath));


        const classAttributeValue: string = await this.driverHelper.waitAndGetElementAttribute(expandIconLocator, 'class', timeout);
        const isItemCollapsed: boolean = classAttributeValue.search('theia-mod-collapsed') > 0;

        if (isItemCollapsed) {
            await this.driverHelper.waitAndClick(treeItemLocator, timeout);
        }

        await this.waitItemExpanded(itemPath, timeout);
    }

    async collapseItem(itemPath: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const expandIconLocator: By = By.css(this.getExpandIconCssLocator(itemPath));
        const treeItemLocator: By = By.css(this.getTreeItemCssLocator(itemPath));

        const classAttributeValue: string = await this.driverHelper.waitAndGetElementAttribute(expandIconLocator, 'class', timeout);
        const isItemCollapsed: boolean = classAttributeValue.search('theia-mod-collapsed') > 0;

        if (!isItemCollapsed) {
            await this.driverHelper.waitAndClick(treeItemLocator, timeout);
        }

        await this.waitItemCollapsed(itemPath, timeout);
    }

    async expandPathAndOpenFile(pathToItem: string, fileName: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        let currentPath: string = '';
        let paths: Array<string> = new Array();

        // make direct path for each project tree item
        pathToItem.split('/')
            .forEach(item => {
                currentPath = `${currentPath}/${item}`;
                paths.push(currentPath);
            });

        // expand each project tree item
        for (const path of paths) {
            await this.expandItem(path, timeout);
        }

        // open file
        await this.clickOnItem(`${pathToItem}/${fileName}`, timeout);

        // check file appearance in the editor
        await this.editor.waitEditorOpened(fileName, timeout);
        await this.editor.waitTab(fileName);
    }

    async waitProjectImported(projectName: string,
        rootSubItem: string,
        attempts: number = TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS,
        visibilityItemPolling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING * 5,
        triesPolling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING * 30) {

        const rootItem: string = `/${projectName}`;
        const rootItemLocator: By = By.css(this.getTreeItemCssLocator(`/${projectName}`));
        const rootSubitemLocator: By = By.css(this.getTreeItemCssLocator(`/${projectName}/${rootSubItem}`));

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

        throw new Error('Exceeded the maximum number of checking attempts, project has not been imported');
    }

    private getItemCss(itemPath: string): string {
        return `div[id='/projects:/projects/${itemPath}']`;
    }

    private getCollapsedItemCssLocator(itemPath: string): string {
        return `${this.getExpandIconCssLocator(itemPath)}.theia-mod-collapsed`;
    }

    private getExpandedItemCssLocator(itemPath: string): string {
        return `${this.getExpandIconCssLocator(itemPath)}:not(.theia-mod-collapsed)`;
    }

    private getExpandIconCssLocator(itemPath: string): string {
        return `div[data-node-id='/projects:/projects${itemPath}']`;
    }

    private getTreeItemCssLocator(itemPath: string): string {
        return `.theia-TreeNode[title='/projects${itemPath}']`;
    }

}
