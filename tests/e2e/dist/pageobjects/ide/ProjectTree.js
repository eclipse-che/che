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
var ProjectTree_1;
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
const Ide_1 = require("./Ide");
const TestConstants_1 = require("../../TestConstants");
const selenium_webdriver_1 = require("selenium-webdriver");
const Editor_1 = require("./Editor");
const Logger_1 = require("../../utils/Logger");
let ProjectTree = ProjectTree_1 = class ProjectTree {
    constructor(driverHelper, ide, editor) {
        this.driverHelper = driverHelper;
        this.ide = ide;
        this.editor = editor;
    }
    clickCollapseAllButton() {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('ProjectTree.clickCollapseAllButton');
            const collapseAllButtonLocator = selenium_webdriver_1.By.css('div.theia-sidepanel-toolbar div.collapse-all');
            yield this.driverHelper.waitAndClick(collapseAllButtonLocator);
        });
    }
    waitTreeCollapsed(projectName, rootSubItem) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`ProjectTree.waitTreeCollapsed project: "${projectName}", subitem: "${rootSubItem}"`);
            const rootSubitemLocator = selenium_webdriver_1.By.css(this.getTreeItemCssLocator(`${projectName}/${rootSubItem}`));
            yield this.driverHelper.waitDisappearanceWithTimeout(rootSubitemLocator);
        });
    }
    collapseProjectTree(projectName, rootSubItem) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`ProjectTree.collapseProjectTree project: "${projectName}", subitem: "${rootSubItem}"`);
            yield this.clickCollapseAllButton();
            yield this.waitTreeCollapsed(projectName, rootSubItem);
        });
    }
    waitAssociatedWorkspaceProjectTreeCollapsed(projectName, expandedRootItem) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`ProjectTree.waitTreeCollapsed project name: "${projectName}", expanded root item: "${expandedRootItem}"`);
            // const rootSubitemLocator: By = By.css(this.getTreeItemCssLocator(`${projectName}/${expandedRootItem}`));
            yield this.waitItemCollapsed(`${projectName}/${expandedRootItem}`);
        });
    }
    collapseAssociatedWorkspaceProjectTree(projectName, rootSubItem) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`ProjectTree.collapseProjectTree project: "${projectName}", subitem: "${rootSubItem}"`);
            yield this.clickCollapseAllButton();
            yield this.waitTreeCollapsed(projectName, rootSubItem);
        });
    }
    openProjectTreeContainer(timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('ProjectTree.openProjectTreeContainer');
            const selectedExplorerButtonLocator = selenium_webdriver_1.By.css(Ide_1.Ide.SELECTED_EXPLORER_BUTTON_CSS);
            yield this.ide.waitRightToolbarButton(Ide_1.RightToolbarButton.Explorer, timeout);
            const isButtonEnabled = yield this.driverHelper.waitVisibilityBoolean(selectedExplorerButtonLocator);
            if (!isButtonEnabled) {
                yield this.ide.waitAndClickRightToolbarButton(Ide_1.RightToolbarButton.Explorer, timeout);
            }
            yield this.waitProjectTreeContainer();
        });
    }
    waitItemExpanded(itemPath, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`ProjectTree.waitItemExpanded "${itemPath}"`);
            const locator = yield this.getExpandedItemCssLocator(itemPath);
            const expandedItemLocator = selenium_webdriver_1.By.css(locator);
            yield this.driverHelper.waitVisibility(expandedItemLocator, timeout);
        });
    }
    waitItemCollapsed(itemPath, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`ProjectTree.waitItemCollapsed "${itemPath}"`);
            const locator = yield this.getCollapsedItemCssLocator(itemPath);
            const collapsedItemLocator = selenium_webdriver_1.By.css(locator);
            yield this.driverHelper.waitVisibility(collapsedItemLocator, timeout);
        });
    }
    waitProjectTreeContainer(timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('ProjectTree.waitProjectTreeContainer');
            yield this.driverHelper.waitPresence(selenium_webdriver_1.By.css(ProjectTree_1.PROJECT_TREE_CONTAINER_CSS), timeout);
        });
    }
    waitProjectTreeContainerClosed(attempts = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS, polling = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_POLLING) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('ProjectTree.waitProjectTreeContainerClosed');
            yield this.driverHelper.waitDisappearance(selenium_webdriver_1.By.css(ProjectTree_1.PROJECT_TREE_CONTAINER_CSS), attempts, polling);
        });
    }
    waitItem(itemPath, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`ProjectTree.waitItem "${itemPath}"`);
            const locator = yield this.getItemCss(itemPath);
            yield this.driverHelper.waitVisibility(selenium_webdriver_1.By.css(locator), timeout);
        });
    }
    waitItemDisappearance(itemPath, attempts = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS, polling = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_POLLING) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`ProjectTree.waitItemDisappearance "${itemPath}"`);
            const locator = yield this.getItemCss(itemPath);
            yield this.driverHelper.waitDisappearance(selenium_webdriver_1.By.css(locator), attempts, polling);
        });
    }
    clickOnItem(itemPath, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`ProjectTree.clickOnItem "${itemPath}"`);
            const locator = yield this.getItemCss(itemPath);
            yield this.driverHelper.waitAndClick(selenium_webdriver_1.By.css(locator), timeout);
            yield this.waitItemSelected(itemPath, timeout);
        });
    }
    waitItemSelected(itemPath, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`ProjectTree.waitItemSelected "${itemPath}"`);
            const selectedItemLocator = selenium_webdriver_1.By.css(`div[title='/projects/${itemPath}'].theia-mod-selected.theia-mod-focus`);
            yield this.driverHelper.waitVisibility(selectedItemLocator, timeout);
        });
    }
    expandItem(itemPath, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`ProjectTree.expandItem "${itemPath}"`);
            const locator = yield this.getExpandIconCssLocator(itemPath);
            const expandIconLocator = selenium_webdriver_1.By.css(locator);
            const treeItemLocator = selenium_webdriver_1.By.css(this.getTreeItemCssLocator(itemPath));
            yield this.driverHelper.getDriver().wait(() => __awaiter(this, void 0, void 0, function* () {
                const classAttributeValue = yield this.driverHelper.waitAndGetElementAttribute(expandIconLocator, 'class', timeout);
                const isItemCollapsed = classAttributeValue.search('theia-mod-collapsed') > 0;
                if (isItemCollapsed) {
                    yield this.driverHelper.waitAndClick(treeItemLocator, timeout);
                }
                try {
                    yield this.waitItemExpanded(itemPath, TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_POLLING);
                    return true;
                }
                catch (err) {
                    if (!(err instanceof selenium_webdriver_1.error.TimeoutError)) {
                        throw err('Unexpected error during project tree expanding');
                    }
                    console.log(`The '${itemPath}' item has not been expanded, try again`);
                    yield this.driverHelper.wait(TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_POLLING);
                }
            }), timeout);
        });
    }
    collapseItem(itemPath, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`ProjectTree.collapseItem "${itemPath}"`);
            const locator = yield this.getExpandIconCssLocator(itemPath);
            const expandIconLocator = selenium_webdriver_1.By.css(locator);
            const treeItemLocator = selenium_webdriver_1.By.css(this.getTreeItemCssLocator(itemPath));
            const classAttributeValue = yield this.driverHelper.waitAndGetElementAttribute(expandIconLocator, 'class', timeout);
            const isItemCollapsed = classAttributeValue.search('theia-mod-collapsed') > 0;
            if (!isItemCollapsed) {
                yield this.driverHelper.waitAndClick(treeItemLocator, timeout);
            }
            yield this.waitItemCollapsed(itemPath, timeout);
        });
    }
    expandPathAndOpenFile(pathToItem, fileName, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`ProjectTree.expandPathAndOpenFile "${pathToItem}"`);
            let items = pathToItem.split('/');
            let projectName = items[0];
            let paths = new Array();
            paths.push(projectName);
            // make direct path for each project tree item
            for (let i = 1; i < items.length; i++) {
                let item = items[i];
                projectName = `${projectName}/${item}`;
                paths.push(projectName);
            }
            // expand each project tree item
            for (const path of paths) {
                yield this.expandItem(path, timeout);
            }
            // open file
            yield this.clickOnItem(`${pathToItem}/${fileName}`, timeout);
            // check file appearance in the editor
            yield this.editor.waitEditorOpened(fileName, timeout);
            yield this.editor.waitTab(fileName);
        });
    }
    expandPathAndOpenFileInAssociatedWorkspace(pathToItem, fileName, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`ProjectTree.expandPathAndOpenFileInAssociatedWorkspace "${pathToItem}"`);
            let projectName = pathToItem.split('/')[0];
            let pathEntry = `${projectName}`;
            let pathToItemInAssociatedWorkspace = pathToItem.replace(`${projectName}/`, '');
            let paths = new Array();
            // if we in the root of project
            if (pathToItem.split('/').length < 2) {
                yield this.clickOnItem(`${projectName}/${fileName}`, timeout);
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
                yield this.expandItem(path, timeout);
            }
            // open file
            yield this.clickOnItem(`${projectName}/${pathToItemInAssociatedWorkspace}/${fileName}`, timeout);
            // check file appearance in the editor
            yield this.editor.waitEditorOpened(fileName, timeout);
            yield this.editor.waitTab(fileName);
        });
    }
    waitProjectImported(projectName, rootSubItem, attempts = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS, visibilityItemPolling = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_POLLING * 5, triesPolling = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_POLLING * 30) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`ProjectTree.waitProjectImported "${projectName}" rootSubItem: "${rootSubItem}"`);
            const rootItem = `${projectName}`;
            const rootItemLocator = selenium_webdriver_1.By.css(this.getTreeItemCssLocator(`${projectName}`));
            const rootSubitemLocator = selenium_webdriver_1.By.css(this.getTreeItemCssLocator(`${projectName}/${rootSubItem}`));
            for (let i = 0; i < attempts; i++) {
                const isProjectFolderVisible = yield this.driverHelper.waitVisibilityBoolean(rootItemLocator, attempts, visibilityItemPolling);
                if (!isProjectFolderVisible) {
                    yield this.driverHelper.reloadPage();
                    yield this.driverHelper.wait(triesPolling);
                    yield this.ide.waitAndSwitchToIdeFrame();
                    yield this.ide.waitIde();
                    yield this.openProjectTreeContainer();
                    continue;
                }
                yield this.expandItem(rootItem);
                yield this.waitItemExpanded(rootItem);
                const isRootSubItemVisible = yield this.driverHelper.waitVisibilityBoolean(rootSubitemLocator, attempts, visibilityItemPolling);
                if (!isRootSubItemVisible) {
                    yield this.driverHelper.reloadPage();
                    yield this.driverHelper.wait(triesPolling);
                    yield this.ide.waitAndSwitchToIdeFrame();
                    yield this.ide.waitIde();
                    yield this.openProjectTreeContainer();
                    continue;
                }
                return;
            }
            throw new selenium_webdriver_1.error.TimeoutError('Exceeded the maximum number of checking attempts, project has not been imported');
        });
    }
    getWorkspacePathEntry() {
        return __awaiter(this, void 0, void 0, function* () {
            const nodeAttribute = 'data-node-id';
            const splitDelimeter = ':';
            const attribute = yield this.driverHelper.waitAndGetElementAttribute(selenium_webdriver_1.By.css(`div[${nodeAttribute}]`), nodeAttribute);
            return attribute.split(splitDelimeter)[0] + splitDelimeter;
        });
    }
    getItemCss(itemPath) {
        return __awaiter(this, void 0, void 0, function* () {
            const entry = yield this.getWorkspacePathEntry();
            return `div[id='${entry}/projects/${itemPath}']`;
        });
    }
    getCollapsedItemCssLocator(itemPath) {
        return __awaiter(this, void 0, void 0, function* () {
            const item = yield this.getExpandIconCssLocator(itemPath);
            return item + '.theia-mod-collapsed';
        });
    }
    getExpandedItemCssLocator(itemPath) {
        return __awaiter(this, void 0, void 0, function* () {
            const item = yield this.getExpandIconCssLocator(itemPath);
            return item + ':not(.theia-mod-collapsed)';
        });
    }
    getExpandIconCssLocator(itemPath) {
        return __awaiter(this, void 0, void 0, function* () {
            const entry = yield this.getWorkspacePathEntry();
            return `div[data-node-id='${entry}/projects/${itemPath}']`;
        });
    }
    getTreeItemCssLocator(itemPath) {
        return `.theia-TreeNode[title='/projects/${itemPath}']`;
    }
};
ProjectTree.PROJECT_TREE_CONTAINER_CSS = '#theia-left-side-panel .theia-TreeContainer';
ProjectTree = ProjectTree_1 = __decorate([
    inversify_1.injectable(),
    __param(0, inversify_1.inject(inversify_types_1.CLASSES.DriverHelper)),
    __param(1, inversify_1.inject(inversify_types_1.CLASSES.Ide)),
    __param(2, inversify_1.inject(inversify_types_1.CLASSES.Editor)),
    __metadata("design:paramtypes", [DriverHelper_1.DriverHelper,
        Ide_1.Ide,
        Editor_1.Editor])
], ProjectTree);
exports.ProjectTree = ProjectTree;
//# sourceMappingURL=ProjectTree.js.map