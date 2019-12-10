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
var Workspaces_1;
"use strict";
const TestConstants_1 = require("../../TestConstants");
const inversify_1 = require("inversify");
const DriverHelper_1 = require("../../utils/DriverHelper");
const inversify_types_1 = require("../../inversify.types");
const selenium_webdriver_1 = require("selenium-webdriver");
const Logger_1 = require("../../utils/Logger");
let Workspaces = Workspaces_1 = class Workspaces {
    constructor(driverHelper) {
        this.driverHelper = driverHelper;
    }
    waitPage(timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('Workspaces.waitPage');
            yield this.driverHelper.waitVisibility(selenium_webdriver_1.By.css(Workspaces_1.ADD_WORKSPACE_BUTTON_CSS), timeout);
        });
    }
    clickAddWorkspaceButton(timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('Workspaces.clickAddWorkspaceButton');
            yield this.driverHelper.waitAndClick(selenium_webdriver_1.By.css(Workspaces_1.ADD_WORKSPACE_BUTTON_CSS), timeout);
        });
    }
    waitWorkspaceListItem(workspaceName, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Workspaces.waitWorkspaceListItem "${workspaceName}"`);
            const workspaceListItemLocator = selenium_webdriver_1.By.css(this.getWorkspaceListItemLocator(workspaceName));
            yield this.driverHelper.waitVisibility(workspaceListItemLocator, timeout);
        });
    }
    clickOnStopWorkspaceButton(workspaceName, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Workspaces.clickOnStopWorkspaceButton "${workspaceName}"`);
            const stopWorkspaceButtonLocator = selenium_webdriver_1.By.css(`#ws-name-${workspaceName} .workspace-status[uib-tooltip="Stop workspace"]`);
            yield this.driverHelper.waitAndClick(stopWorkspaceButtonLocator, timeout);
        });
    }
    waitWorkspaceWithRunningStatus(workspaceName, timeout = TestConstants_1.TestConstants.TS_SELENIUM_START_WORKSPACE_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Workspaces.waitWorkspaceWithRunningStatus "${workspaceName}"`);
            const runningStatusLocator = selenium_webdriver_1.By.css(this.getWorkspaceStatusCssLocator(workspaceName, 'RUNNING'));
            yield this.driverHelper.waitVisibility(runningStatusLocator, timeout);
        });
    }
    waitWorkspaceWithStoppedStatus(workspaceName, timeout = TestConstants_1.TestConstants.TS_SELENIUM_START_WORKSPACE_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Workspaces.waitWorkspaceWithStoppedStatus "${workspaceName}"`);
            const stoppedStatusLocator = selenium_webdriver_1.By.css(this.getWorkspaceStatusCssLocator(workspaceName, 'STOPPED'));
            yield this.driverHelper.waitVisibility(stoppedStatusLocator, timeout);
        });
    }
    clickWorkspaceListItem(workspaceName, timeout = TestConstants_1.TestConstants.TS_SELENIUM_START_WORKSPACE_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Workspaces.clickWorkspaceListItem "${workspaceName}"`);
            var namespace = TestConstants_1.TestConstants.TS_SELENIUM_USERNAME;
            const workspaceListItemLocator = selenium_webdriver_1.By.css(`div[id='ws-full-name-${namespace}/${workspaceName}']`);
            yield this.driverHelper.waitAndClick(workspaceListItemLocator, timeout);
        });
    }
    clickDeleteButtonOnWorkspaceDetails(timeout = TestConstants_1.TestConstants.TS_SELENIUM_START_WORKSPACE_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('Workspaces.clickDeleteButtonOnWorkspaceDetails');
            const deleteButtonOnWorkspaceDetailsLocator = selenium_webdriver_1.By.css('che-button-danger[che-button-title=\'Delete\']');
            yield this.driverHelper.waitAndClick(deleteButtonOnWorkspaceDetailsLocator, timeout);
        });
    }
    waitWorkspaceListItemAbcence(workspaceName, timeout = TestConstants_1.TestConstants.TS_SELENIUM_START_WORKSPACE_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Workspaces.waitWorkspaceListItemAbcence "${workspaceName}"`);
            var namespace = TestConstants_1.TestConstants.TS_SELENIUM_USERNAME;
            const workspaceListItemLocator = selenium_webdriver_1.By.css(`div[id='ws-full-name-${namespace}/${workspaceName}']`);
            yield this.driverHelper.waitDisappearance(workspaceListItemLocator, timeout);
        });
    }
    clickConfirmDeletionButton(timeout = TestConstants_1.TestConstants.TS_SELENIUM_START_WORKSPACE_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('Workspaces.clickConfirmDeletionButton');
            yield this.driverHelper.waitAndClick(selenium_webdriver_1.By.css('#ok-dialog-button'), timeout);
        });
    }
    getWorkspaceListItemLocator(workspaceName) {
        return `#ws-name-${workspaceName}`;
    }
    getWorkspaceStatusCssLocator(workspaceName, workspaceStatus) {
        return `#ws-name-${workspaceName}[data-ws-status='${workspaceStatus}']`;
    }
};
Workspaces.ADD_WORKSPACE_BUTTON_CSS = '#add-item-button';
Workspaces = Workspaces_1 = __decorate([
    inversify_1.injectable(),
    __param(0, inversify_1.inject(inversify_types_1.CLASSES.DriverHelper)),
    __metadata("design:paramtypes", [DriverHelper_1.DriverHelper])
], Workspaces);
exports.Workspaces = Workspaces;
//# sourceMappingURL=Workspaces.js.map