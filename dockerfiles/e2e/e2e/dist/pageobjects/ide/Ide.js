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
const DriverHelper_1 = require("../../utils/DriverHelper");
const inversify_1 = require("inversify");
const inversify_types_1 = require("../../inversify.types");
const TestConstants_1 = require("../../TestConstants");
const selenium_webdriver_1 = require("selenium-webdriver");
const TestWorkspaceUtil_1 = require("../../utils/workspace/TestWorkspaceUtil");
var RightToolbarButton;
(function (RightToolbarButton) {
    RightToolbarButton["Explorer"] = "Explorer";
    RightToolbarButton["Git"] = "Git";
    RightToolbarButton["Debug"] = "Debug";
})(RightToolbarButton = exports.RightToolbarButton || (exports.RightToolbarButton = {}));
let Ide = Ide_1 = class Ide {
    constructor(driverHelper, testWorkspaceUtil) {
        this.driverHelper = driverHelper;
        this.testWorkspaceUtil = testWorkspaceUtil;
    }
    waitAndSwitchToIdeFrame(timeout = TestConstants_1.TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.driverHelper.waitAndSwitchToFrame(selenium_webdriver_1.By.css(Ide_1.IDE_IFRAME_CSS), timeout);
        });
    }
    waitNotification(notificationText, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            const notificationLocator = selenium_webdriver_1.By.xpath(this.getNotificationXpathLocator(notificationText));
            yield this.driverHelper.waitVisibility(notificationLocator, timeout);
        });
    }
    waitNotificationAndClickOnButton(notificationText, buttonText, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
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
            yield this.waitNotificationAndClickOnButton(notificationText, 'yes', timeout);
        });
    }
    waitNotificationAndOpenLink(notificationText, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.waitNotificationAndClickOnButton(notificationText, 'Open Link', timeout);
        });
    }
    isNotificationPresent(notificationText) {
        return __awaiter(this, void 0, void 0, function* () {
            const notificationLocator = selenium_webdriver_1.By.xpath(this.getNotificationXpathLocator(notificationText));
            return yield this.driverHelper.waitVisibilityBoolean(notificationLocator);
        });
    }
    waitNotificationDisappearance(notificationText, attempts = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS, polling = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_POLLING) {
        return __awaiter(this, void 0, void 0, function* () {
            const notificationLocator = selenium_webdriver_1.By.xpath(this.getNotificationXpathLocator(notificationText));
            yield this.driverHelper.waitDisappearance(notificationLocator, attempts, polling);
        });
    }
    clickOnNotificationButton(notificationText, buttonText) {
        return __awaiter(this, void 0, void 0, function* () {
            const notificationLocator = this.getNotificationXpathLocator(notificationText);
            const yesButtonLocator = notificationLocator + `//button[text()=\'${buttonText}\']`;
            yield this.driverHelper.waitAndClick(selenium_webdriver_1.By.xpath(yesButtonLocator));
        });
    }
    waitWorkspaceAndIde(workspaceNamespace, workspaceName, timeout = TestConstants_1.TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.waitAndSwitchToIdeFrame(timeout);
            yield this.testWorkspaceUtil.waitWorkspaceStatus(workspaceNamespace, workspaceName, TestWorkspaceUtil_1.WorkspaceStatus.RUNNING);
            yield this.waitIde(timeout);
        });
    }
    waitIde(timeout = TestConstants_1.TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            const mainIdeParts = [selenium_webdriver_1.By.css(Ide_1.TOP_MENU_PANEL_CSS), selenium_webdriver_1.By.css(Ide_1.LEFT_CONTENT_PANEL_CSS), selenium_webdriver_1.By.xpath(Ide_1.EXPLORER_BUTTON_XPATH)];
            for (const idePartLocator of mainIdeParts) {
                yield this.driverHelper.waitVisibility(idePartLocator, timeout);
            }
        });
    }
    waitRightToolbarButton(buttonTitle, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            const buttonLocator = this.getRightToolbarButtonLocator(buttonTitle);
            yield this.driverHelper.waitVisibility(buttonLocator, timeout);
        });
    }
    waitAndClickRightToolbarButton(buttonTitle, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            const buttonLocator = this.getRightToolbarButtonLocator(buttonTitle);
            yield this.driverHelper.waitAndClick(buttonLocator, timeout);
        });
    }
    waitTopMenuPanel(timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.driverHelper.waitVisibility(selenium_webdriver_1.By.css(Ide_1.TOP_MENU_PANEL_CSS), timeout);
        });
    }
    waitLeftContentPanel(timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.driverHelper.waitVisibility(selenium_webdriver_1.By.css(Ide_1.LEFT_CONTENT_PANEL_CSS));
        });
    }
    waitPreloaderAbsent(attempts = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS, polling = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_POLLING) {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.driverHelper.waitDisappearance(selenium_webdriver_1.By.css(Ide_1.PRELOADER_CSS), attempts, polling);
        });
    }
    waitStatusBarContains(expectedText, timeout = TestConstants_1.TestConstants.TS_SELENIUM_LANGUAGE_SERVER_START_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            const statusBarLocator = selenium_webdriver_1.By.css('div[id=\'theia-statusBar\']');
            yield this.driverHelper.getDriver().wait(() => __awaiter(this, void 0, void 0, function* () {
                const elementText = yield this.driverHelper.waitAndGetText(statusBarLocator, timeout);
                const isTextPresent = elementText.search(expectedText) > 0;
                if (isTextPresent) {
                    return true;
                }
            }), timeout);
        });
    }
    waitStatusBarTextAbsence(expectedText, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            const statusBarLocator = selenium_webdriver_1.By.css('div[id=\'theia-statusBar\']');
            // for ensuring that check is not invoked in the gap of status displaying
            for (let i = 0; i < 3; i++) {
                yield this.driverHelper.getDriver().wait(() => __awaiter(this, void 0, void 0, function* () {
                    const elementText = yield this.driverHelper.waitAndGetText(statusBarLocator, timeout);
                    const isTextAbsent = elementText.search(expectedText) === -1;
                    if (isTextAbsent) {
                        return true;
                    }
                }), timeout);
            }
        });
    }
    waitIdeFrameAndSwitchOnIt(timeout = TestConstants_1.TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.driverHelper.waitAndSwitchToFrame(selenium_webdriver_1.By.css(Ide_1.IDE_IFRAME_CSS), timeout);
        });
    }
    checkLsInitializationStart(expectedTextInStatusBar) {
        return __awaiter(this, void 0, void 0, function* () {
            try {
                yield this.waitStatusBarContains(expectedTextInStatusBar, 20000);
            }
            catch (err) {
                if (!(err instanceof selenium_webdriver_1.error.TimeoutError)) {
                    throw err;
                }
                yield this.driverHelper.getDriver().navigate().refresh();
                yield this.waitAndSwitchToIdeFrame();
                yield this.waitStatusBarContains(expectedTextInStatusBar);
            }
        });
    }
    closeAllNotifications() {
        return __awaiter(this, void 0, void 0, function* () {
            const notificationLocator = selenium_webdriver_1.By.css('.theia-Notification');
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
            const bodyLocator = selenium_webdriver_1.By.tagName('body');
            yield this.driverHelper.type(bodyLocator, keyCombination);
        });
    }
    waitRightToolbarButtonSelection(buttonTitle, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            const selectedRightToolbarButtonLocator = this.getSelectedRightToolbarButtonLocator(buttonTitle);
            yield this.driverHelper.waitVisibility(selectedRightToolbarButtonLocator, timeout);
        });
    }
    getSelectedRightToolbarButtonLocator(buttonTitle) {
        return selenium_webdriver_1.By.xpath(`//div[@id='theia-left-content-panel']//ul[@class='p-TabBar-content']` +
            `//li[@title='${buttonTitle}' and contains(@id, 'shell-tab')] and contains(@class, 'p-mod-current')`);
    }
    getRightToolbarButtonLocator(buttonTitle) {
        return selenium_webdriver_1.By.xpath(`//div[@id='theia-left-content-panel']//ul[@class='p-TabBar-content']` +
            `//li[@title='${buttonTitle}' and contains(@id, 'shell-tab')]`);
    }
    getNotificationXpathLocator(notificationText) {
        return `//div[@class='theia-Notification' and contains(@id,'${notificationText}')]`;
    }
};
Ide.EXPLORER_BUTTON_XPATH = '(//ul[@class=\'p-TabBar-content\']//li[@title=\'Explorer\'])[1]';
Ide.SELECTED_EXPLORER_BUTTON_XPATH = '(//ul[@class=\'p-TabBar-content\']//li[@title=\'Explorer\' and contains(@class, \'p-mod-current\')])[1]';
Ide.ACTIVATED_IDE_IFRAME_CSS = '#ide-iframe-window[aria-hidden=\'false\']';
Ide.SELECTED_GIT_BUTTON_XPATH = '(//ul[@class=\'p-TabBar-content\']//li[@title=\'Git\' and contains(@class, \'p-mod-current\')])[1]';
Ide.TOP_MENU_PANEL_CSS = '#theia-app-shell #theia-top-panel .p-MenuBar-content';
Ide.LEFT_CONTENT_PANEL_CSS = '#theia-left-content-panel';
Ide.PRELOADER_CSS = '.theia-preload';
Ide.IDE_IFRAME_CSS = 'iframe#ide-application-iframe';
Ide = Ide_1 = __decorate([
    inversify_1.injectable(),
    __param(0, inversify_1.inject(inversify_types_1.CLASSES.DriverHelper)),
    __param(1, inversify_1.inject(inversify_types_1.CLASSES.TestWorkspaceUtil)),
    __metadata("design:paramtypes", [DriverHelper_1.DriverHelper,
        TestWorkspaceUtil_1.TestWorkspaceUtil])
], Ide);
exports.Ide = Ide;
