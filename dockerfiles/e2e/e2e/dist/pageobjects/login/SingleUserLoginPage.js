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
require("reflect-metadata");
const inversify_1 = require("inversify");
const inversify_types_1 = require("../../inversify.types");
const TestConstants_1 = require("../../TestConstants");
const Dashboard_1 = require("../dashboard/Dashboard");
let SingleUserLoginPage = class SingleUserLoginPage {
    constructor(driver, dashboard) {
        this.driver = driver;
        this.dashboard = dashboard;
    }
    login(timeout = TestConstants_1.TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            const webDriver = this.driver.get();
            yield webDriver.navigate().to(TestConstants_1.TestConstants.TS_SELENIUM_BASE_URL);
            yield this.dashboard.waitPage(timeout);
        });
    }
};
SingleUserLoginPage = __decorate([
    inversify_1.injectable(),
    __param(0, inversify_1.inject(inversify_types_1.TYPES.Driver)),
    __param(1, inversify_1.inject(inversify_types_1.CLASSES.Dashboard)),
    __metadata("design:paramtypes", [Object, Dashboard_1.Dashboard])
], SingleUserLoginPage);
exports.SingleUserLoginPage = SingleUserLoginPage;
