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
var OpenWorkspaceWidget_1;
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
const inversify_1 = require("inversify");
const inversify_types_1 = require("../../inversify.types");
const DriverHelper_1 = require("../../utils/DriverHelper");
const selenium_webdriver_1 = require("selenium-webdriver");
const TestConstants_1 = require("../../TestConstants");
const Logger_1 = require("../../utils/Logger");
let OpenWorkspaceWidget = OpenWorkspaceWidget_1 = class OpenWorkspaceWidget {
    constructor(driverHelper) {
        this.driverHelper = driverHelper;
    }
    waitOpenWorkspaceWidget(timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('OpenWorkspaceWidget.waitOpenWorkspaceWidget');
            yield this.driverHelper.waitVisibility(selenium_webdriver_1.By.xpath(OpenWorkspaceWidget_1.OPEN_WORKSPACE_MAIN_VIEW_XPATH), timeout);
        });
    }
    waitWidgetIsClosed(timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('OpenWorkspaceWidget.waitWidgetIsClosed');
            yield this.driverHelper.waitDisappearance(selenium_webdriver_1.By.xpath(OpenWorkspaceWidget_1.OPEN_WORKSPACE_MAIN_VIEW_XPATH), timeout);
        });
    }
    selectItemInTree(pathToItem, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`OpenWorkspaceWidget.selectItemInTree "${pathToItem}"`);
            yield this.driverHelper.waitAndClick(selenium_webdriver_1.By.id(pathToItem), timeout);
        });
    }
    clickOnOpenButton(timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('OpenWorkspaceWidget.clickOnOpenButton');
            yield this.driverHelper.waitAndClick(selenium_webdriver_1.By.css(OpenWorkspaceWidget_1.OPEN_WORKSPACE_OPEN_BTN_CSS), timeout);
        });
    }
    selectItemInTreeAndOpenWorkspace(item, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`OpenWorkspaceWidget.selectItemInTreeAndOpenWorkspace "${item}"`);
            yield this.selectItemInTree(item, timeout);
            yield this.clickOnOpenButton();
            yield this.waitWidgetIsClosed();
        });
    }
    expandTreeToPath(path) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`OpenWorkspaceWidget.expandTreeToPath "${path}"`);
            const pathNodes = path.split('/');
            for (let currentPath of pathNodes) {
                yield this.driverHelper.waitAndClick(selenium_webdriver_1.By.id(`/${currentPath}`));
            }
        });
    }
    selectRootWorkspaceItemInDropDawn(rootProject) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`OpenWorkspaceWidget.selectRootWorkspaceItemInDropDawn "${rootProject}"`);
            yield this.driverHelper.waitAndClick(selenium_webdriver_1.By.css(OpenWorkspaceWidget_1.THEIA_LOCATION_LIST_CSS));
            yield this.driverHelper.waitAndClick(selenium_webdriver_1.By.css(`option[value=\'file:///${rootProject}']`));
        });
    }
};
OpenWorkspaceWidget.OPEN_WORKSPACE_MAIN_VIEW_XPATH = '//div[@class=\'dialogTitle\']/div[text()=\'Open Workspace\']';
OpenWorkspaceWidget.OPEN_WORKSPACE_OPEN_BTN_CSS = 'div.dialogControl>button.main';
OpenWorkspaceWidget.THEIA_LOCATION_LIST_CSS = 'select.theia-LocationList';
OpenWorkspaceWidget = OpenWorkspaceWidget_1 = __decorate([
    inversify_1.injectable(),
    __param(0, inversify_1.inject(inversify_types_1.CLASSES.DriverHelper)),
    __metadata("design:paramtypes", [DriverHelper_1.DriverHelper])
], OpenWorkspaceWidget);
exports.OpenWorkspaceWidget = OpenWorkspaceWidget;
//# sourceMappingURL=OpenWorkspaceWidget.js.map