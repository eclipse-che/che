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
require("chromedriver");
require("reflect-metadata");
const inversify_1 = require("inversify");
const selenium_webdriver_1 = require("selenium-webdriver");
const chrome_1 = require("selenium-webdriver/chrome");
const TestConstants_1 = require("../TestConstants");
let ChromeDriver = class ChromeDriver {
    constructor() {
        const options = this.getDriverOptions();
        this.driver = this.getDriverBuilder(options).build();
        this.driver
            .manage()
            .window()
            .setSize(TestConstants_1.TestConstants.TS_SELENIUM_RESOLUTION_WIDTH, TestConstants_1.TestConstants.TS_SELENIUM_RESOLUTION_HEIGHT);
    }
    get() {
        return this.driver;
    }
    getDriverOptions() {
        let options = new chrome_1.Options()
            .addArguments('--no-sandbox')
            .addArguments('--disable-web-security')
            .addArguments('--allow-running-insecure-content');
        // if 'true' run in 'headless' mode
        if (TestConstants_1.TestConstants.TS_SELENIUM_HEADLESS) {
            options = options.addArguments('headless');
        }
        return options;
    }
    getDriverBuilder(options) {
        let builder = new selenium_webdriver_1.Builder()
            .forBrowser('chrome')
            .setChromeOptions(options);
        // if 'true' run with remote driver
        if (TestConstants_1.TestConstants.TS_SELENIUM_REMOTE_DRIVER_URL) {
            builder = builder.usingServer(TestConstants_1.TestConstants.TS_SELENIUM_REMOTE_DRIVER_URL);
        }
        return builder;
    }
};
ChromeDriver = __decorate([
    inversify_1.injectable(),
    __metadata("design:paramtypes", [])
], ChromeDriver);
exports.ChromeDriver = ChromeDriver;
//# sourceMappingURL=ChromeDriver.js.map