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
const Logger_1 = require("../../utils/Logger");
let Terminal = class Terminal {
    constructor(driverHelper) {
        this.driverHelper = driverHelper;
    }
    waitTab(tabTitle, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Terminal.waitTab "${tabTitle}"`);
            const terminalTabLocator = selenium_webdriver_1.By.css(this.getTerminalTabCssLocator(tabTitle));
            yield this.driverHelper.waitVisibility(terminalTabLocator, timeout);
        });
    }
    waitTabAbsence(tabTitle, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Terminal.waitTabAbsence "${tabTitle}"`);
            const terminalTabLocator = selenium_webdriver_1.By.css(this.getTerminalTabCssLocator(tabTitle));
            yield this.driverHelper.waitDisappearanceWithTimeout(terminalTabLocator, timeout);
        });
    }
    clickOnTab(tabTitle, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Terminal.clickOnTab "${tabTitle}"`);
            const terminalTabLocator = selenium_webdriver_1.By.css(this.getTerminalTabCssLocator(tabTitle));
            yield this, this.driverHelper.waitAndClick(terminalTabLocator, timeout);
        });
    }
    waitTabFocused(tabTitle, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Terminal.waitTabFocused "${tabTitle}"`);
            const focusedTerminalTabLocator = this.getFocusedTerminalTabLocator(tabTitle);
            yield this.driverHelper.waitVisibility(focusedTerminalTabLocator, timeout);
        });
    }
    selectTerminalTab(tabTitle, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Terminal.selectTerminalTab "${tabTitle}"`);
            yield this.clickOnTab(tabTitle, timeout);
            yield this.waitTabFocused(tabTitle, timeout);
        });
    }
    clickOnTabCloseIcon(tabTitle, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Terminal.clickOnTabCloseIcon "${tabTitle}"`);
            const terminalTabCloseIconLocator = selenium_webdriver_1.By.css(`${this.getTerminalTabCssLocator(tabTitle)} div.p-TabBar-tabCloseIcon`);
            yield this.driverHelper.waitAndClick(terminalTabCloseIconLocator, timeout);
        });
    }
    closeTerminalTab(tabTitle, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Terminal.closeTerminalTab "${tabTitle}"`);
            yield this.clickOnTabCloseIcon(tabTitle, timeout);
            yield this.waitTabAbsence(tabTitle, timeout);
        });
    }
    type(terminalTabTitle, text) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Terminal.type "${terminalTabTitle}"`);
            const terminalIndex = yield this.getTerminalIndex(terminalTabTitle);
            const terminalInteractionContainer = this.getTerminalEditorInteractionEditorLocator(terminalIndex);
            yield this.driverHelper.typeToInvisible(terminalInteractionContainer, text);
        });
    }
    rejectTerminalProcess(tabTitle, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Terminal.rejectTerminalProcess "${tabTitle}"`);
            yield this.selectTerminalTab(tabTitle, timeout);
            yield this.type(tabTitle, selenium_webdriver_1.Key.chord(selenium_webdriver_1.Key.CONTROL, 'c'));
        });
    }
    getTerminalTabCssLocator(tabTitle) {
        return `li[title='${tabTitle}']`;
    }
    getFocusedTerminalTabLocator(tabTitle) {
        return selenium_webdriver_1.By.css(`li[title='${tabTitle}'].p-mod-current.theia-mod-active`);
    }
    getTerminalIndex(terminalTitle) {
        return __awaiter(this, void 0, void 0, function* () {
            const terminalTabTitleXpathLocator = `//div[@id='theia-bottom-content-panel']` +
                `//li[contains(@id, 'shell-tab-terminal') or contains(@id, 'shell-tab-plugin')]` +
                `//div[@class='p-TabBar-tabLabel']`;
            const terminalTabs = yield this.driverHelper.waitAllPresence(selenium_webdriver_1.By.xpath(terminalTabTitleXpathLocator));
            let terminalTitles = [];
            for (let i = 1; i <= terminalTabs.length; i++) {
                const terminalTabLocator = selenium_webdriver_1.By.xpath(`(${terminalTabTitleXpathLocator})[${i}]`);
                const currentTerminalTitle = yield this.driverHelper.waitAndGetText(terminalTabLocator);
                if (currentTerminalTitle.search(terminalTitle) > -1) {
                    return i;
                }
                terminalTitles.push(currentTerminalTitle);
            }
            throw new selenium_webdriver_1.error.WebDriverError(`The terminal with title '${terminalTitle}' has not been found.\n` +
                `List of the tabs:\n${terminalTitles}`);
        });
    }
    getTerminalEditorInteractionEditorLocator(terminalIndex) {
        return selenium_webdriver_1.By.xpath(`(//textarea[@aria-label='Terminal input'])[${terminalIndex}]`);
    }
};
Terminal = __decorate([
    inversify_1.injectable(),
    __param(0, inversify_1.inject(inversify_types_1.CLASSES.DriverHelper)),
    __metadata("design:paramtypes", [DriverHelper_1.DriverHelper])
], Terminal);
exports.Terminal = Terminal;
//# sourceMappingURL=Terminal.js.map