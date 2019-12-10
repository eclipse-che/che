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
var __importStar = (this && this.__importStar) || function (mod) {
    if (mod && mod.__esModule) return mod;
    var result = {};
    if (mod != null) for (var k in mod) if (Object.hasOwnProperty.call(mod, k)) result[k] = mod[k];
    result["default"] = mod;
    return result;
};
Object.defineProperty(exports, "__esModule", { value: true });
const TestConstants_1 = require("../../TestConstants");
const inversify_1 = require("inversify");
const DriverHelper_1 = require("../DriverHelper");
const inversify_types_1 = require("../../inversify.types");
require("reflect-metadata");
const rm = __importStar(require("typed-rest-client/RestClient"));
const selenium_webdriver_1 = require("selenium-webdriver");
let TestWorkspaceUtil = class TestWorkspaceUtil {
    constructor(driverHelper, rest = new rm.RestClient('rest-samples')) {
        this.driverHelper = driverHelper;
        this.rest = rest;
    }
    waitWorkspaceStatus(namespace, workspaceName, expectedWorkspaceStatus) {
        return __awaiter(this, void 0, void 0, function* () {
            const workspaceStatusApiUrl = `${TestConstants_1.TestConstants.TS_SELENIUM_BASE_URL}/api/workspace/${namespace}:${workspaceName}`;
            const attempts = TestConstants_1.TestConstants.TS_SELENIUM_WORKSPACE_STATUS_ATTEMPTS;
            const polling = TestConstants_1.TestConstants.TS_SELENIUM_WORKSPACE_STATUS_POLLING;
            for (let i = 0; i < attempts; i++) {
                const response = yield this.rest.get(workspaceStatusApiUrl);
                if (response.statusCode !== 200) {
                    yield this.driverHelper.wait(polling);
                    continue;
                }
                const workspaceStatus = yield response.result.status;
                if (workspaceStatus === expectedWorkspaceStatus) {
                    return;
                }
                yield this.driverHelper.wait(polling);
            }
            throw new selenium_webdriver_1.error.TimeoutError(`Exceeded the maximum number of checking attempts, workspace status is different to '${expectedWorkspaceStatus}'`);
        });
    }
    waitPluginAdding(namespace, workspaceName, pluginId) {
        return __awaiter(this, void 0, void 0, function* () {
            const workspaceStatusApiUrl = `${TestConstants_1.TestConstants.TS_SELENIUM_BASE_URL}/api/workspace/${namespace}:${workspaceName}`;
            const attempts = TestConstants_1.TestConstants.TS_SELENIUM_PLUGIN_PRECENCE_ATTEMPTS;
            const polling = TestConstants_1.TestConstants.TS_SELENIUM_PLUGIN_PRECENCE_POLLING;
            for (let i = 0; i < attempts; i++) {
                const response = yield this.rest.get(workspaceStatusApiUrl);
                if (response.statusCode !== 200) {
                    yield this.driverHelper.wait(polling);
                    continue;
                }
                const machines = JSON.stringify(response.result.runtime.machines);
                const isPluginPresent = machines.search(pluginId) > 0;
                if (isPluginPresent) {
                    break;
                }
                if (i === attempts - 1) {
                    throw new selenium_webdriver_1.error.TimeoutError(`Exceeded maximum tries attempts, the '${pluginId}' plugin is not present in the workspace runtime.`);
                }
                yield this.driverHelper.wait(polling);
            }
        });
    }
    getIdOfRunningWorkspace(namespace) {
        return __awaiter(this, void 0, void 0, function* () {
            throw new Error('Method not implemented.');
        });
    }
    removeWorkspaceById(id) {
        throw new Error('Method not implemented.');
    }
    stopWorkspaceById(id) {
        throw new Error('Method not implemented.');
    }
};
TestWorkspaceUtil = __decorate([
    inversify_1.injectable(),
    __param(0, inversify_1.inject(inversify_types_1.CLASSES.DriverHelper)),
    __metadata("design:paramtypes", [DriverHelper_1.DriverHelper, rm.RestClient])
], TestWorkspaceUtil);
exports.TestWorkspaceUtil = TestWorkspaceUtil;
//# sourceMappingURL=TestWorkspaceUtil.js.map