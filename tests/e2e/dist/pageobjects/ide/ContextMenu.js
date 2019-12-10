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
var ContextMenu_1;
"use strict";
require("reflect-metadata");
const inversify_1 = require("inversify");
const DriverHelper_1 = require("../../utils/DriverHelper");
const inversify_types_1 = require("../../inversify.types");
const selenium_webdriver_1 = require("selenium-webdriver");
const TestConstants_1 = require("../../TestConstants");
const Logger_1 = require("../../utils/Logger");
let ContextMenu = ContextMenu_1 = class ContextMenu {
    constructor(driverHelper) {
        this.driverHelper = driverHelper;
    }
    invokeContextMenuOnTheElementWithMouse(elementLocator, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`ContextMenu.invokeContextMenuOnTheElementWithMouse ${elementLocator}`);
            const webElement = yield this.driverHelper.waitVisibility(elementLocator, timeout);
            yield this.driverHelper.getAction().click(webElement, selenium_webdriver_1.Button.RIGHT).perform();
            this.waitContextMenu(timeout);
        });
    }
    invokeContextMenuOnActiveElementWithKeys(timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('ContextMenu.invokeContextMenuOnActiveElementWithKeys');
            this.driverHelper.getDriver().switchTo().activeElement().sendKeys(selenium_webdriver_1.Key.SHIFT + selenium_webdriver_1.Key.F10);
            this.waitContextMenu(timeout);
        });
    }
    waitContextMenuAndClickOnItem(nameOfItem, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`ContextMenu.waitContextMenuAndClickOnItem "${nameOfItem}"`);
            const itemLocator = `//div[@class='p-Menu-itemLabel' and text()='${nameOfItem}']`;
            yield this.waitContextMenu();
            yield this.driverHelper.waitAndClick(selenium_webdriver_1.By.xpath(itemLocator), timeout);
        });
    }
    waitContextMenu(timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`ContextMenu.waitContextMenu`);
            yield this.driverHelper.waitVisibility(selenium_webdriver_1.By.css(ContextMenu_1.SUGGESTION_WIDGET_BODY_CSS), timeout);
        });
    }
};
ContextMenu.SUGGESTION_WIDGET_BODY_CSS = 'ul.p-Menu-content';
ContextMenu = ContextMenu_1 = __decorate([
    inversify_1.injectable(),
    __param(0, inversify_1.inject(inversify_types_1.CLASSES.DriverHelper)),
    __metadata("design:paramtypes", [DriverHelper_1.DriverHelper])
], ContextMenu);
exports.ContextMenu = ContextMenu;
//# sourceMappingURL=ContextMenu.js.map