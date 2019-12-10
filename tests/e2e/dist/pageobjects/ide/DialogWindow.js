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
var DialogWindow_1;
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
const Logger_1 = require("../../utils/Logger");
const TestConstants_1 = require("../../TestConstants");
let DialogWindow = DialogWindow_1 = class DialogWindow {
    constructor(driverHelper) {
        this.driverHelper = driverHelper;
    }
    dialogDisplayes() {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('WarningDialog.dialogDisplayes');
            return yield this.driverHelper.isVisible(selenium_webdriver_1.By.xpath(DialogWindow_1.DIALOG_BODY_XPATH_LOCATOR));
        });
    }
    waitAndCloseIfAppear() {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('WarningDialog.waitAndCloseIfAppear');
            const dialogDisplayes = yield this.driverHelper.waitVisibilityBoolean(selenium_webdriver_1.By.xpath(DialogWindow_1.DIALOG_BODY_XPATH_LOCATOR));
            if (dialogDisplayes) {
                yield this.closeDialog();
                yield this.waitDialogDissappearance();
            }
        });
    }
    clickToButton(buttonText) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('WarningDialog.clickToButton');
            const buttonLocator = selenium_webdriver_1.By.xpath(`${DialogWindow_1.DIALOG_BODY_XPATH_LOCATOR}//button[text()='${buttonText}']`);
            yield this.driverHelper.waitAndClick(buttonLocator);
        });
    }
    closeDialog() {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('WarningDialog.closeDialog');
            yield this.clickToButton('close');
        });
    }
    clickToOpenLinkButton() {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('WarningDialog.clickToOpenLinkButton');
            yield this.clickToButton('Open Link');
        });
    }
    waitDialog(timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('WarningDialog.waitDialog');
            yield this.driverHelper.waitVisibility(selenium_webdriver_1.By.xpath(DialogWindow_1.DIALOG_BODY_XPATH_LOCATOR), timeout);
        });
    }
    waitDialogAndOpenLink(timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('WarningDialog.waitDialogAndOpenLink');
            yield this.waitDialog(timeout);
            yield this.clickToOpenLinkButton();
            yield this.waitDialogDissappearance();
        });
    }
    waitDialogDissappearance() {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('WarningDialog.waitDialogDissappearance');
            yield this.driverHelper.waitDisappearanceWithTimeout(selenium_webdriver_1.By.xpath(DialogWindow_1.CLOSE_BUTTON_XPATH_LOCATOR));
        });
    }
};
DialogWindow.DIALOG_BODY_XPATH_LOCATOR = '//div[@id=\'theia-dialog-shell\']//div[@class=\'dialogBlock\']';
DialogWindow.CLOSE_BUTTON_XPATH_LOCATOR = `${DialogWindow_1.DIALOG_BODY_XPATH_LOCATOR}//button[text()='close']`;
DialogWindow = DialogWindow_1 = __decorate([
    inversify_1.injectable(),
    __param(0, inversify_1.inject(inversify_types_1.CLASSES.DriverHelper)),
    __metadata("design:paramtypes", [DriverHelper_1.DriverHelper])
], DialogWindow);
exports.DialogWindow = DialogWindow;
//# sourceMappingURL=DialogWindow.js.map