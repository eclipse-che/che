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
require("reflect-metadata");
const inversify_types_1 = require("../../../inversify.types");
const TestConstants_1 = require("../../../TestConstants");
const selenium_webdriver_1 = require("selenium-webdriver");
const WorkspaceDetails_1 = require("./WorkspaceDetails");
const WorkspaceStatus_1 = require("../../../utils/workspace/WorkspaceStatus");
const Logger_1 = require("../../../utils/Logger");
let WorkspaceDetailsPlugins = class WorkspaceDetailsPlugins {
    constructor(driverHelper, workspaceDetails, testWorkspaceUtil) {
        this.driverHelper = driverHelper;
        this.workspaceDetails = workspaceDetails;
        this.testWorkspaceUtil = testWorkspaceUtil;
    }
    waitPluginListItem(pluginName, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`WorkspaceDetailsPlugins.waitPluginListItem ${pluginName}`);
            const pluginListItemLocator = selenium_webdriver_1.By.css(this.getPluginListItemCssLocator(pluginName));
            yield this.driverHelper.waitVisibility(pluginListItemLocator, timeout);
        });
    }
    enablePlugin(pluginName, pluginVersion, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`WorkspaceDetailsPlugins.enablePlugin ${pluginName}:${pluginVersion}`);
            yield this.waitPluginDisabling(pluginName, pluginVersion, timeout);
            yield this.clickOnPluginListItemSwitcher(pluginName, pluginVersion, timeout);
            yield this.waitPluginEnabling(pluginName, pluginVersion, timeout);
        });
    }
    disablePlugin(pluginName, pluginVersion, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`WorkspaceDetailsPlugins.disablePlugin ${pluginName}:${pluginVersion}`);
            yield this.waitPluginEnabling(pluginName, pluginVersion, timeout);
            yield this.clickOnPluginListItemSwitcher(pluginName, pluginVersion, timeout);
            yield this.waitPluginDisabling(pluginName, pluginVersion, timeout);
        });
    }
    addPluginAndOpenWorkspace(namespace, workspaceName, pluginName, pluginId, pluginVersion) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`WorkspaceDetailsPlugins.addPluginAndOpenWorkspace ${namespace}/${workspaceName} plugin: ${pluginName}:${pluginVersion}`);
            yield this.workspaceDetails.selectTab('Plugins');
            yield this.enablePlugin(pluginName, pluginVersion);
            yield this.workspaceDetails.saveChanges();
            yield this.workspaceDetails.openWorkspace(namespace, workspaceName);
            yield this.testWorkspaceUtil.waitWorkspaceStatus(namespace, workspaceName, WorkspaceStatus_1.WorkspaceStatus.RUNNING);
            yield this.testWorkspaceUtil.waitPluginAdding(namespace, workspaceName, pluginId);
        });
    }
    getPluginListItemCssLocator(pluginName, pluginVersion) {
        if (pluginVersion) {
            return `.plugin-item div[plugin-item-name*='${pluginName}'][plugin-item-version='${pluginVersion}']`;
        }
        return `.plugin-item div[plugin-item-name*='${pluginName}']`;
    }
    getPluginListItemSwitcherCssLocator(pluginName, pluginVersion) {
        return `${this.getPluginListItemCssLocator(pluginName, pluginVersion)} md-switch`;
    }
    clickOnPluginListItemSwitcher(pluginName, pluginVersion, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            const pluginListItemSwitcherLocator = selenium_webdriver_1.By.css(this.getPluginListItemSwitcherCssLocator(pluginName, pluginVersion));
            yield this.driverHelper.waitAndClick(pluginListItemSwitcherLocator, timeout);
        });
    }
    waitPluginEnabling(pluginName, pluginVersion, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            const enabledPluginSwitcherLocator = selenium_webdriver_1.By.css(`${this.getPluginListItemCssLocator(pluginName, pluginVersion)} md-switch[aria-checked='true']`);
            yield this.driverHelper.waitVisibility(enabledPluginSwitcherLocator, timeout);
        });
    }
    waitPluginDisabling(pluginName, pluginVersion, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            const disabledPluginSwitcherLocator = selenium_webdriver_1.By.css(`${this.getPluginListItemCssLocator(pluginName, pluginVersion)} md-switch[aria-checked='false']`);
            yield this.driverHelper.waitVisibility(disabledPluginSwitcherLocator, timeout);
        });
    }
};
WorkspaceDetailsPlugins = __decorate([
    inversify_1.injectable(),
    __param(0, inversify_1.inject(inversify_types_1.CLASSES.DriverHelper)),
    __param(1, inversify_1.inject(inversify_types_1.CLASSES.WorkspaceDetails)),
    __param(2, inversify_1.inject(inversify_types_1.TYPES.WorkspaceUtil)),
    __metadata("design:paramtypes", [DriverHelper_1.DriverHelper,
        WorkspaceDetails_1.WorkspaceDetails, Object])
], WorkspaceDetailsPlugins);
exports.WorkspaceDetailsPlugins = WorkspaceDetailsPlugins;
//# sourceMappingURL=WorkspaceDetailsPlugins.js.map