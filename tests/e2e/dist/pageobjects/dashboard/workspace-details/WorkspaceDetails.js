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
var WorkspaceDetails_1;
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
const DriverHelper_1 = require("../../../utils/DriverHelper");
const inversify_1 = require("inversify");
const inversify_types_1 = require("../../../inversify.types");
require("reflect-metadata");
const TestConstants_1 = require("../../../TestConstants");
const selenium_webdriver_1 = require("selenium-webdriver");
const Ide_1 = require("../../ide/Ide");
const WorkspaceStatus_1 = require("../../../utils/workspace/WorkspaceStatus");
const Logger_1 = require("../../../utils/Logger");
let WorkspaceDetails = WorkspaceDetails_1 = class WorkspaceDetails {
    constructor(driverHelper, testWorkspaceUtil) {
        this.driverHelper = driverHelper;
        this.testWorkspaceUtil = testWorkspaceUtil;
    }
    waitLoaderDisappearance(attempts = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS, polling = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_POLLING) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('WorkspaceDetails.waitLoaderDisappearance');
            yield this.driverHelper.waitDisappearance(selenium_webdriver_1.By.css(WorkspaceDetails_1.WORKSPACE_DETAILS_LOADER_CSS), attempts, polling);
        });
    }
    saveChanges() {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('WorkspaceDetails.saveChanges');
            yield this.waitSaveButton();
            yield this.clickOnSaveButton();
            yield this.waitSaveButtonDisappearance();
        });
    }
    waitPage(workspaceName, timeout = TestConstants_1.TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`WorkspaceDetails.saveChanges workspace: "${workspaceName}"`);
            yield this.waitWorkspaceTitle(workspaceName, timeout);
            yield this.waitOpenButton(timeout);
            yield this.waitRunButton(timeout);
            yield this.waitTabsPresence(timeout);
            yield this.waitLoaderDisappearance(timeout);
        });
    }
    waitWorkspaceTitle(workspaceName, timeout = TestConstants_1.TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`WorkspaceDetails.waitWorkspaceTitle title: "${workspaceName}"`);
            const workspaceTitleLocator = selenium_webdriver_1.By.css(this.getWorkspaceTitleCssLocator(workspaceName));
            yield this.driverHelper.waitVisibility(workspaceTitleLocator, timeout);
        });
    }
    waitRunButton(timeout = TestConstants_1.TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('WorkspaceDetails.waitRunButton');
            yield this.driverHelper.waitVisibility(selenium_webdriver_1.By.css(WorkspaceDetails_1.RUN_BUTTON_CSS), timeout);
        });
    }
    clickOnRunButton(timeout = TestConstants_1.TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('WorkspaceDetails.clickOnRunButton');
            yield this.driverHelper.waitAndClick(selenium_webdriver_1.By.css(WorkspaceDetails_1.RUN_BUTTON_CSS), timeout);
        });
    }
    waitOpenButton(timeout = TestConstants_1.TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('WorkspaceDetails.waitOpenButton');
            yield this.driverHelper.waitVisibility(selenium_webdriver_1.By.css(WorkspaceDetails_1.OPEN_BUTTON_CSS), timeout);
        });
    }
    openWorkspace(namespace, workspaceName, timeout = TestConstants_1.TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`WorkspaceDetails.openWorkspace "${namespace}/${workspaceName}"`);
            yield this.clickOnOpenButton(timeout);
            yield this.driverHelper.waitVisibility(selenium_webdriver_1.By.css(Ide_1.Ide.ACTIVATED_IDE_IFRAME_CSS));
            yield this.testWorkspaceUtil.waitWorkspaceStatus(namespace, workspaceName, WorkspaceStatus_1.WorkspaceStatus.STARTING);
        });
    }
    waitTabsPresence(timeout = TestConstants_1.TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('WorkspaceDetails.waitTabsPresence');
            const workspaceDetailsTabs = ['Overview', 'Projects', 'Containers', 'Servers',
                'Env Variables', 'Volumes', 'Config', 'SSH', 'Plugins', 'Editors'];
            for (const tabTitle of workspaceDetailsTabs) {
                const workspaceDetailsTabLocator = selenium_webdriver_1.By.xpath(this.getTabXpathLocator(tabTitle));
                yield this.driverHelper.waitVisibility(workspaceDetailsTabLocator, timeout);
            }
        });
    }
    selectTab(tabTitle, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`WorkspaceDetails.selectTab ${tabTitle}`);
            yield this.clickOnTab(tabTitle, timeout);
            yield this.waitTabSelected(tabTitle, timeout);
        });
    }
    getWorkspaceTitleCssLocator(workspaceName) {
        return `che-row-toolbar[che-title='${workspaceName}']`;
    }
    getTabXpathLocator(tabTitle) {
        return `//md-tabs-canvas//md-tab-item//span[text()='${tabTitle}']`;
    }
    getSelectedTabXpathLocator(tabTitle) {
        return `//md-tabs-canvas[@role='tablist']//md-tab-item[@aria-selected='true']//span[text()='${tabTitle}']`;
    }
    waitSaveButton(timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.driverHelper.waitVisibility(selenium_webdriver_1.By.css(WorkspaceDetails_1.ENABLED_SAVE_BUTTON_CSS), timeout);
        });
    }
    waitSaveButtonDisappearance(attempts = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS, polling = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_POLLING) {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.driverHelper.waitDisappearance(selenium_webdriver_1.By.css(WorkspaceDetails_1.SAVE_BUTTON_CSS), attempts, polling);
        });
    }
    clickOnSaveButton(timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.driverHelper.waitAndClick(selenium_webdriver_1.By.css(WorkspaceDetails_1.ENABLED_SAVE_BUTTON_CSS), timeout);
        });
    }
    clickOnOpenButton(timeout = TestConstants_1.TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.driverHelper.waitAndClick(selenium_webdriver_1.By.css(WorkspaceDetails_1.OPEN_BUTTON_CSS), timeout);
        });
    }
    clickOnTab(tabTitle, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            const workspaceDetailsTabLocator = selenium_webdriver_1.By.xpath(this.getTabXpathLocator(tabTitle));
            yield this.driverHelper.waitAndClick(workspaceDetailsTabLocator, timeout);
        });
    }
    waitTabSelected(tabTitle, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            const selectedTabLocator = selenium_webdriver_1.By.xpath(this.getSelectedTabXpathLocator(tabTitle));
            yield this.driverHelper.waitVisibility(selectedTabLocator, timeout);
        });
    }
};
WorkspaceDetails.RUN_BUTTON_CSS = '#run-workspace-button[che-button-title=\'Run\']';
WorkspaceDetails.OPEN_BUTTON_CSS = '#open-in-ide-button[che-button-title=\'Open\']';
WorkspaceDetails.SAVE_BUTTON_CSS = 'button[name=\'save-button\']';
WorkspaceDetails.ENABLED_SAVE_BUTTON_CSS = 'button[name=\'save-button\'][aria-disabled=\'false\']';
WorkspaceDetails.WORKSPACE_DETAILS_LOADER_CSS = 'workspace-details-overview md-progress-linear';
WorkspaceDetails = WorkspaceDetails_1 = __decorate([
    inversify_1.injectable(),
    __param(0, inversify_1.inject(inversify_types_1.CLASSES.DriverHelper)),
    __param(1, inversify_1.inject(inversify_types_1.TYPES.WorkspaceUtil)),
    __metadata("design:paramtypes", [DriverHelper_1.DriverHelper, Object])
], WorkspaceDetails);
exports.WorkspaceDetails = WorkspaceDetails;
//# sourceMappingURL=WorkspaceDetails.js.map