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
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
var Ide_1;
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
const axios_1 = __importDefault(require("axios"));
const DriverHelper_1 = require("../../utils/DriverHelper");
const inversify_1 = require("inversify");
const inversify_types_1 = require("../../inversify.types");
const TestConstants_1 = require("../../TestConstants");
const selenium_webdriver_1 = require("selenium-webdriver");
const Logger_1 = require("../../utils/Logger");
var RightToolbarButton;
(function (RightToolbarButton) {
    RightToolbarButton["Explorer"] = "Explorer";
    RightToolbarButton["Git"] = "Git";
    RightToolbarButton["Debug"] = "Debug";
})(RightToolbarButton = exports.RightToolbarButton || (exports.RightToolbarButton = {}));
let Ide = Ide_1 = class Ide {
    constructor(driverHelper) {
        this.driverHelper = driverHelper;
    }
    waitAndSwitchToIdeFrame(timeout = TestConstants_1.TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('Ide.waitAndSwitchToIdeFrame');
            yield this.driverHelper.waitAndSwitchToFrame(selenium_webdriver_1.By.css(Ide_1.IDE_IFRAME_CSS), timeout);
        });
    }
    waitNotification(notificationText, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Ide.waitNotification "${notificationText}"`);
            const notificationLocator = selenium_webdriver_1.By.xpath(this.getNotificationXpathLocator(notificationText));
            yield this.driverHelper.waitVisibility(notificationLocator, timeout);
        });
    }
    waitNotificationAndClickOnButton(notificationText, buttonText, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Ide.waitNotificationAndClickOnButton "${notificationText}" buttonText: "${buttonText}"`);
            yield this.driverHelper.getDriver().wait(() => __awaiter(this, void 0, void 0, function* () {
                yield this.waitNotification(notificationText, timeout);
                yield this.clickOnNotificationButton(notificationText, buttonText);
                try {
                    yield this.waitNotificationDisappearance(notificationText);
                    return true;
                }
                catch (err) {
                    if (!(err instanceof selenium_webdriver_1.error.TimeoutError)) {
                        throw err;
                    }
                    console.log(`After clicking on "${buttonText}" button of the notification with text "${notificationText}" \n` +
                        'it is still visible (issue #14121), try again.');
                    yield this.driverHelper.wait(TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_POLLING);
                }
            }), timeout);
        });
    }
    waitNotificationAndConfirm(notificationText, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Ide.waitNotificationAndConfirm "${notificationText}"`);
            yield this.waitNotificationAndClickOnButton(notificationText, 'yes', timeout);
        });
    }
    waitNotificationAndOpenLink(notificationText, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Ide.waitNotificationAndOpenLink "${notificationText}"`);
            yield this.waitApllicationIsReady(yield this.getApplicationUrlFromNotification(notificationText));
            yield this.waitNotificationAndClickOnButton(notificationText, 'Open Link', timeout);
        });
    }
    isNotificationPresent(notificationText) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Ide.isNotificationPresent "${notificationText}"`);
            const notificationLocator = selenium_webdriver_1.By.xpath(this.getNotificationXpathLocator(notificationText));
            return yield this.driverHelper.waitVisibilityBoolean(notificationLocator);
        });
    }
    waitNotificationDisappearance(notificationText, attempts = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS, polling = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_POLLING) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Ide.waitNotificationDisappearance "${notificationText}"`);
            const notificationLocator = selenium_webdriver_1.By.xpath(this.getNotificationXpathLocator(notificationText));
            yield this.driverHelper.waitDisappearance(notificationLocator, attempts, polling);
        });
    }
    clickOnNotificationButton(notificationText, buttonText) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Ide.clickOnNotificationButton "${notificationText}" buttonText: "${buttonText}"`);
            const yesButtonLocator = `//div[@class='theia-notification-list']//span[contains(.,'${notificationText}')]/parent::div/parent::div/parent::div/div[@class='theia-notification-list-item-content-bottom']//div[@class='theia-notification-buttons']//button[text()='${buttonText}'] `;
            yield this.driverHelper.waitAndClick(selenium_webdriver_1.By.xpath(yesButtonLocator));
        });
    }
    waitWorkspaceAndIde(workspaceNamespace, workspaceName, timeout = TestConstants_1.TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('Ide.waitWorkspaceAndIde');
            yield this.waitAndSwitchToIdeFrame(timeout);
            yield this.waitIde(timeout);
        });
    }
    waitIde(timeout = TestConstants_1.TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('Ide.waitIde');
            const mainIdeParts = [selenium_webdriver_1.By.css(Ide_1.TOP_MENU_PANEL_CSS), selenium_webdriver_1.By.css(Ide_1.LEFT_CONTENT_PANEL_CSS), selenium_webdriver_1.By.id(Ide_1.EXPLORER_BUTTON_ID)];
            for (const idePartLocator of mainIdeParts) {
                yield this.driverHelper.waitVisibility(idePartLocator, timeout);
            }
        });
    }
    waitRightToolbarButton(buttonTitle, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('Ide.waitRightToolbarButton');
            const buttonLocator = this.getRightToolbarButtonLocator(buttonTitle);
            yield this.driverHelper.waitVisibility(buttonLocator, timeout);
        });
    }
    waitAndClickRightToolbarButton(buttonTitle, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('Ide.waitAndClickRightToolbarButton');
            const buttonLocator = this.getRightToolbarButtonLocator(buttonTitle);
            yield this.driverHelper.waitAndClick(buttonLocator, timeout);
        });
    }
    waitTopMenuPanel(timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('Ide.waitTopMenuPanel');
            yield this.driverHelper.waitVisibility(selenium_webdriver_1.By.css(Ide_1.TOP_MENU_PANEL_CSS), timeout);
        });
    }
    waitLeftContentPanel(timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('Ide.waitLeftContentPanel');
            yield this.driverHelper.waitVisibility(selenium_webdriver_1.By.css(Ide_1.LEFT_CONTENT_PANEL_CSS));
        });
    }
    waitPreloaderAbsent(attempts = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS, polling = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_POLLING) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('Ide.waitPreloaderAbsent');
            yield this.driverHelper.waitDisappearance(selenium_webdriver_1.By.css(Ide_1.PRELOADER_CSS), attempts, polling);
        });
    }
    waitStatusBarContains(expectedText, timeout = TestConstants_1.TestConstants.TS_SELENIUM_LANGUAGE_SERVER_START_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            const statusBarLocator = selenium_webdriver_1.By.css('div[id=\'theia-statusBar\']');
            Logger_1.Logger.debug(`Ide.waitStatusBarContains "${expectedText}"`);
            yield this.driverHelper.getDriver().wait(() => __awaiter(this, void 0, void 0, function* () {
                const elementText = yield this.driverHelper.waitAndGetText(statusBarLocator, timeout);
                const isTextPresent = elementText.search(expectedText) > 0;
                if (isTextPresent) {
                    return true;
                }
                yield this.driverHelper.wait(TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_POLLING * 2);
            }), timeout);
        });
    }
    waitStatusBarTextAbsence(expectedText, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            const statusBarLocator = selenium_webdriver_1.By.css('div[id=\'theia-statusBar\']');
            Logger_1.Logger.debug(`Ide.waitStatusBarTextAbsence "${expectedText}"`);
            // for ensuring that check is not invoked in the gap of status displaying
            for (let i = 0; i < 3; i++) {
                yield this.driverHelper.getDriver().wait(() => __awaiter(this, void 0, void 0, function* () {
                    const elementText = yield this.driverHelper.waitAndGetText(statusBarLocator, timeout);
                    const isTextAbsent = elementText.search(expectedText) === -1;
                    if (isTextAbsent) {
                        return true;
                    }
                    yield this.driverHelper.wait(TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_POLLING * 2);
                }), timeout);
            }
        });
    }
    waitIdeFrameAndSwitchOnIt(timeout = TestConstants_1.TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('Ide.waitIdeFrameAndSwitchOnIt');
            yield this.driverHelper.waitAndSwitchToFrame(selenium_webdriver_1.By.css(Ide_1.IDE_IFRAME_CSS), timeout);
        });
    }
    checkLsInitializationStart(expectedTextInStatusBar) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('Ide.checkLsInitializationStart');
            yield this.waitStatusBarContains(expectedTextInStatusBar, 20000);
        });
    }
    closeAllNotifications() {
        return __awaiter(this, void 0, void 0, function* () {
            const notificationLocator = selenium_webdriver_1.By.css('.theia-Notification');
            Logger_1.Logger.debug('Ide.closeAllNotifications');
            if (!(yield this.driverHelper.isVisible(notificationLocator))) {
                return;
            }
            const notifications = yield this.driverHelper.waitAllPresence(notificationLocator);
            const notificationsCapacity = notifications.length;
            for (let i = 1; i <= notificationsCapacity; i++) {
                const notificationLocator = selenium_webdriver_1.By.xpath('//div[@class=\'theia-Notification\']//button[text()=\'Close\']');
                try {
                    yield this.driverHelper.waitAndClick(notificationLocator);
                }
                catch (err) {
                    if (err instanceof selenium_webdriver_1.error.TimeoutError) {
                        console.log(`The '${notificationLocator}' element is not visible and can't be clicked`);
                        continue;
                    }
                    throw err;
                }
            }
        });
    }
    performKeyCombination(keyCombination) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Ide.performKeyCombination "${keyCombination}"`);
            const bodyLocator = selenium_webdriver_1.By.tagName('body');
            yield this.driverHelper.type(bodyLocator, keyCombination);
        });
    }
    waitRightToolbarButtonSelection(buttonTitle, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('Ide.waitRightToolbarButtonSelection');
            const selectedRightToolbarButtonLocator = this.getSelectedRightToolbarButtonLocator(buttonTitle);
            yield this.driverHelper.waitVisibility(selectedRightToolbarButtonLocator, timeout);
        });
    }
    getApplicationUrlFromNotification(notificationText) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Ide.getApplicationUrlFromNotification ${notificationText}`);
            const notificationTextLocator = selenium_webdriver_1.By.xpath(`//div[@class='theia-notification-message']/span[contains(.,'${notificationText}')]`);
            let notification = yield this.driverHelper.waitAndGetText(notificationTextLocator);
            let regexp = new RegExp('^.*(https?://.*)$');
            if (!regexp.test(notification)) {
                throw new Error('Cannot obtaine url from notification message');
            }
            return notification.split(regexp)[1];
        });
    }
    waitApllicationIsReady(url, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`Ide.waitApllicationIsReady ${url}`);
            yield this.driverHelper.getDriver().wait(() => __awaiter(this, void 0, void 0, function* () {
                try {
                    const res = yield axios_1.default.get(url);
                    if (res.status === 200) {
                        return true;
                    }
                }
                catch (error) {
                    yield this.driverHelper.wait(TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_POLLING);
                }
            }), timeout);
        });
    }
    getSelectedRightToolbarButtonLocator(buttonTitle) {
        return selenium_webdriver_1.By.xpath(`//div[@id='theia-left-content-panel']//ul[@class='p-TabBar-content']` +
            `//li[@title[contains(.,'${buttonTitle}')] and contains(@id, 'shell-tab')] and contains(@class, 'p-mod-current')`);
    }
    getRightToolbarButtonLocator(buttonTitle) {
        return selenium_webdriver_1.By.xpath(`//div[@id='theia-left-content-panel']//ul[@class='p-TabBar-content']` +
            `//li[@title[contains(.,'${buttonTitle}')] and contains(@id, 'shell-tab')]`);
    }
    getNotificationXpathLocator(notificationText) {
        return `//div[@class='theia-notification-message']/span[contains(.,'${notificationText}')]`;
    }
};
Ide.EXPLORER_BUTTON_ID = 'shell-tab-explorer-view-container';
Ide.SELECTED_EXPLORER_BUTTON_CSS = 'li#shell-tab-explorer-view-container.theia-mod-active';
Ide.ACTIVATED_IDE_IFRAME_CSS = '#ide-iframe-window[aria-hidden=\'false\']';
Ide.SELECTED_GIT_BUTTON_XPATH = '(//ul[@class=\'p-TabBar-content\']//li[@title=\'Git\' and contains(@class, \'p-mod-current\')])[1]';
Ide.TOP_MENU_PANEL_CSS = '#theia-app-shell #theia-top-panel .p-MenuBar-content';
Ide.LEFT_CONTENT_PANEL_CSS = '#theia-left-content-panel';
Ide.PRELOADER_CSS = '.theia-preload';
Ide.IDE_IFRAME_CSS = 'iframe#ide-application-iframe';
Ide = Ide_1 = __decorate([
    inversify_1.injectable(),
    __param(0, inversify_1.inject(inversify_types_1.CLASSES.DriverHelper)),
    __metadata("design:paramtypes", [DriverHelper_1.DriverHelper])
], Ide);
exports.Ide = Ide;
//# sourceMappingURL=Ide.js.map