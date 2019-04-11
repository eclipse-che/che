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
Object.defineProperty(exports, "__esModule", { value: true });
var inversify_1 = require("inversify");
require("reflect-metadata");
var types_1 = require("../../types");
/*********************************************************************
 * Copyright (c) 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
var Dashboard = /** @class */ (function () {
    function Dashboard(driver) {
        this.driver = driver;
    }
    Dashboard.DASHBOARD_BUTTON = "#dashboard-item";
    Dashboard.WORKSPACES_BUTTON = "#workspaces-item";
    Dashboard.STACKS_BUTTON = "#stacks-item";
    Dashboard.FACTORIES_BUTTON = "#factories-item";
    Dashboard.LOADER_PAGE = ".main-page-loader";
    Dashboard = __decorate([
        __param(0, inversify_1.inject(types_1.TYPES.Driver)),
        __metadata("design:paramtypes", [Object])
    ], Dashboard);
    return Dashboard;
}());
exports.Dashboard = Dashboard;
