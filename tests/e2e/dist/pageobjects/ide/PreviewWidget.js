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
const inversify_1 = require("inversify");
const inversify_types_1 = require("../../inversify.types");
const DriverHelper_1 = require("../../utils/DriverHelper");
const selenium_webdriver_1 = require("selenium-webdriver");
const TestConstants_1 = require("../../TestConstants");
const Ide_1 = require("./Ide");
const Logger_1 = require("../../utils/Logger");
let PreviewWidget = class PreviewWidget {
    constructor(driverHelper, ide) {
        this.driverHelper = driverHelper;
        this.ide = ide;
    }
    waitAndSwitchToWidgetFrame() {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('PreviewWidget.waitAndSwitchToWidgetFrame');
            const iframeLocator = selenium_webdriver_1.By.css('div.theia-mini-browser iframe');
            yield this.driverHelper.waitAndSwitchToFrame(iframeLocator);
        });
    }
    waitPreviewWidget(timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('PreviewWidget.waitPreviewWidget');
            yield this.driverHelper.waitVisibility(selenium_webdriver_1.By.css('div.theia-mini-browser'), timeout);
        });
    }
    waitPreviewWidgetAbsence() {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('PreviewWidget.waitPreviewWidgetAbsence');
            yield this.driverHelper.waitDisappearance(selenium_webdriver_1.By.css('div.theia-mini-browser'));
        });
    }
    waitContentAvailable(contentLocator, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT, polling = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_POLLING * 5) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`PreviewWidget.waitContentAvailable ${contentLocator}`);
            yield this.waitAndSwitchToWidgetFrame();
            yield this.driverHelper.getDriver().wait(() => __awaiter(this, void 0, void 0, function* () {
                const isApplicationTitleVisible = yield this.driverHelper.isVisible(contentLocator);
                if (isApplicationTitleVisible) {
                    yield this.driverHelper.getDriver().switchTo().defaultContent();
                    yield this.ide.waitAndSwitchToIdeFrame();
                    return true;
                }
                yield this.switchBackToIdeFrame();
                yield this.refreshPage();
                yield this.waitAndSwitchToWidgetFrame();
                yield this.driverHelper.wait(polling);
            }), timeout);
        });
    }
    waitContentAvailableInAssociatedWorkspace(contentLocator, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT, polling = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_POLLING * 5) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`PreviewWidget.waitContentAvailableInAssociatedWorkspace ${contentLocator}`);
            yield this.waitAndSwitchToWidgetFrame();
            yield this.driverHelper.getDriver().wait(() => __awaiter(this, void 0, void 0, function* () {
                const isApplicationTitleVisible = yield this.driverHelper.isVisible(contentLocator);
                if (isApplicationTitleVisible) {
                    yield this.driverHelper.getDriver().switchTo().defaultContent();
                    return true;
                }
                yield this.driverHelper.getDriver().switchTo().defaultContent();
                yield this.refreshPage();
                yield this.waitAndSwitchToWidgetFrame();
                yield this.driverHelper.wait(polling);
            }), timeout);
        });
    }
    waitVisibility(element, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`PreviewWidget.waitVisibility ${element}`);
            yield this.driverHelper.waitVisibility(element, timeout);
        });
    }
    waitAndClick(element, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`PreviewWidget.waitAndClick ${element}`);
            yield this.driverHelper.waitAndClick(element, timeout);
        });
    }
    refreshPage() {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('PreviewWidget.refreshPage');
            const refreshButtonLocator = selenium_webdriver_1.By.css('.theia-mini-browser .theia-mini-browser-refresh');
            yield this.driverHelper.waitAndClick(refreshButtonLocator);
        });
    }
    switchBackToIdeFrame() {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('PreviewWidget.switchBackToIdeFrame');
            yield this.driverHelper.getDriver().switchTo().defaultContent();
            yield this.ide.waitAndSwitchToIdeFrame();
        });
    }
};
PreviewWidget = __decorate([
    inversify_1.injectable(),
    __param(0, inversify_1.inject(inversify_types_1.CLASSES.DriverHelper)),
    __param(1, inversify_1.inject(inversify_types_1.CLASSES.Ide)),
    __metadata("design:paramtypes", [DriverHelper_1.DriverHelper,
        Ide_1.Ide])
], PreviewWidget);
exports.PreviewWidget = PreviewWidget;
//# sourceMappingURL=PreviewWidget.js.map