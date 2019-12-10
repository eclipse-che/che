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
var Dashboard_1;
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
require("reflect-metadata");
const inversify_types_1 = require("../../inversify.types");
const selenium_webdriver_1 = require("selenium-webdriver");
const DriverHelper_1 = require("../../utils/DriverHelper");
const TestConstants_1 = require("../../TestConstants");
const Workspaces_1 = require("./Workspaces");
const Logger_1 = require("../../utils/Logger");
let Dashboard = Dashboard_1 = class Dashboard {
    constructor(driverHelper, workspaces) {
        this.driverHelper = driverHelper;
        this.workspaces = workspaces;
    }
    stopWorkspaceByUI(workspaceName) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Dashboard.stopWorkspaceByUI "${workspaceName}"`);
            yield this.openDashboard();
            yield this.clickWorkspacesButton();
            yield this.workspaces.waitPage();
            yield this.workspaces.waitWorkspaceListItem(workspaceName);
            yield this.workspaces.waitWorkspaceWithRunningStatus(workspaceName);
            yield this.workspaces.clickOnStopWorkspaceButton(workspaceName);
            yield this.workspaces.waitWorkspaceWithStoppedStatus(workspaceName);
        });
    }
    deleteWorkspaceByUI(workspaceName) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Dashboard.deleteWorkspaceByUI "${workspaceName}"`);
            yield this.openDashboard();
            yield this.clickWorkspacesButton();
            yield this.workspaces.waitPage();
            yield this.workspaces.waitWorkspaceListItem(workspaceName);
            yield this.workspaces.clickWorkspaceListItem(workspaceName);
            yield this.workspaces.clickDeleteButtonOnWorkspaceDetails();
            yield this.workspaces.clickConfirmDeletionButton();
            yield this.workspaces.waitPage();
            yield this.workspaces.waitWorkspaceListItemAbcence(workspaceName);
        });
    }
    openDashboard() {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('Dashboard.openDashboard');
            yield this.driverHelper.getDriver().navigate().to(TestConstants_1.TestConstants.TS_SELENIUM_BASE_URL);
            yield this.waitPage();
        });
    }
    waitPage(timeout = TestConstants_1.TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('Dashboard.waitPage');
            yield this.driverHelper.waitVisibility(selenium_webdriver_1.By.css(Dashboard_1.DASHBOARD_BUTTON_CSS), timeout);
            yield this.driverHelper.waitVisibility(selenium_webdriver_1.By.css(Dashboard_1.WORKSPACES_BUTTON_CSS), timeout);
            yield this.driverHelper.waitVisibility(selenium_webdriver_1.By.css(Dashboard_1.STACKS_BUTTON_CSS), timeout);
            yield this.driverHelper.waitVisibility(selenium_webdriver_1.By.css(Dashboard_1.FACTORIES_BUTTON_CSS), timeout);
        });
    }
    clickDashboardButton(timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('Dashboard.clickDashboardButton');
            yield this.driverHelper.waitAndClick(selenium_webdriver_1.By.css(Dashboard_1.DASHBOARD_BUTTON_CSS), timeout);
        });
    }
    clickWorkspacesButton(timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('Dashboard.clickWorkspacesButton');
            yield this.driverHelper.waitAndClick(selenium_webdriver_1.By.css(Dashboard_1.WORKSPACES_BUTTON_CSS), timeout);
        });
    }
    clickStacksdButton(timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('Dashboard.clickStacksdButton');
            yield this.driverHelper.waitAndClick(selenium_webdriver_1.By.css(Dashboard_1.STACKS_BUTTON_CSS), timeout);
        });
    }
    clickFactoriesButton(timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('Dashboard.clickFactoriesButton');
            yield this.driverHelper.waitAndClick(selenium_webdriver_1.By.css(Dashboard_1.FACTORIES_BUTTON_CSS), timeout);
        });
    }
    waitLoader(timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('Dashboard.waitLoader');
            yield this.driverHelper.waitVisibility(selenium_webdriver_1.By.css(Dashboard_1.LOADER_PAGE_CSS), timeout);
        });
    }
    waitLoaderDisappearance(timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('Dashboard.waitLoaderDisappearance');
            yield this.driverHelper.waitDisappearance(selenium_webdriver_1.By.css(Dashboard_1.LOADER_PAGE_CSS), timeout);
        });
    }
};
Dashboard.DASHBOARD_BUTTON_CSS = '#dashboard-item';
Dashboard.WORKSPACES_BUTTON_CSS = '#workspaces-item';
Dashboard.STACKS_BUTTON_CSS = '#stacks-item';
Dashboard.FACTORIES_BUTTON_CSS = '#factories-item';
Dashboard.LOADER_PAGE_CSS = '.main-page-loader';
Dashboard = Dashboard_1 = __decorate([
    inversify_1.injectable(),
    __param(0, inversify_1.inject(inversify_types_1.CLASSES.DriverHelper)),
    __param(1, inversify_1.inject(inversify_types_1.CLASSES.Workspaces)),
    __metadata("design:paramtypes", [DriverHelper_1.DriverHelper,
        Workspaces_1.Workspaces])
], Dashboard);
exports.Dashboard = Dashboard;
//# sourceMappingURL=Dashboard.js.map