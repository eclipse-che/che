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
const inversify_1 = require("inversify");
const inversify_types_1 = require("../inversify.types");
const selenium_webdriver_1 = require("selenium-webdriver");
require("reflect-metadata");
const selenium_webdriver_2 = require("selenium-webdriver");
const TestConstants_1 = require("../TestConstants");
const Logger_1 = require("./Logger");
let DriverHelper = class DriverHelper {
    constructor(driver) {
        this.driver = driver.get();
    }
    getAction() {
        Logger_1.Logger.trace('DriverHelper.getAction');
        return this.driver.actions();
    }
    isVisible(locator) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.trace(`DriverHelper.isVisible ${locator}`);
            try {
                const element = yield this.driver.findElement(locator);
                const isVisible = yield element.isDisplayed();
                return isVisible;
            }
            catch (_a) {
                return false;
            }
        });
    }
    wait(milliseconds) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.trace(`DriverHelper.wait (${milliseconds} milliseconds)`);
            yield this.driver.sleep(milliseconds);
        });
    }
    waitVisibilityBoolean(locator, attempts = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS, polling = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_POLLING) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.trace(`DriverHelper.waitVisibilityBoolean ${locator}`);
            for (let i = 0; i < attempts; i++) {
                const isVisible = yield this.isVisible(locator);
                if (isVisible) {
                    return true;
                }
                yield this.wait(polling);
            }
            return false;
        });
    }
    waitDisappearanceBoolean(locator, attempts = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS, polling = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_POLLING) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.trace(`DriverHelper.waitDisappearanceBoolean ${locator}`);
            for (let i = 0; i < attempts; i++) {
                const isVisible = yield this.isVisible(locator);
                if (!isVisible) {
                    return true;
                }
                yield this.wait(polling);
            }
            return false;
        });
    }
    waitVisibility(elementLocator, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            const attempts = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS;
            const polling = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_POLLING;
            Logger_1.Logger.trace(`DriverHelper.waitVisibility ${elementLocator}`);
            for (let i = 0; i < attempts; i++) {
                const webElement = yield this.driver.wait(selenium_webdriver_2.until.elementLocated(elementLocator), timeout);
                try {
                    const visibleWebElement = yield this.driver.wait(selenium_webdriver_2.until.elementIsVisible(webElement), timeout);
                    return visibleWebElement;
                }
                catch (err) {
                    if (err instanceof selenium_webdriver_1.error.StaleElementReferenceError) {
                        yield this.wait(polling);
                        continue;
                    }
                    throw err;
                }
            }
            throw new selenium_webdriver_1.error.TimeoutError(`Exceeded maximum visibility checkings attempts, problems with 'StaleElementReferenceError' of '${elementLocator}' element`);
        });
    }
    waitPresence(elementLocator, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            const attempts = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS;
            const polling = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_POLLING;
            Logger_1.Logger.trace(`DriverHelper.waitPresence ${elementLocator}`);
            for (let i = 0; i < attempts; i++) {
                try {
                    const webElement = yield this.driver.wait(selenium_webdriver_2.until.elementLocated(elementLocator), timeout);
                    return webElement;
                }
                catch (err) {
                    if (err instanceof selenium_webdriver_1.error.StaleElementReferenceError) {
                        yield this.wait(polling);
                        continue;
                    }
                    throw err;
                }
            }
            throw new selenium_webdriver_1.error.TimeoutError(`Exceeded maximum presence checkings attempts, problems with 'StaleElementReferenceError' of '${elementLocator}' element`);
        });
    }
    waitAllPresence(elementLocator, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            const attempts = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS;
            const polling = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_POLLING;
            Logger_1.Logger.trace(`DriverHelper.waitAllPresence ${elementLocator}`);
            for (let i = 0; i < attempts; i++) {
                try {
                    const webElements = yield this.driver.wait(selenium_webdriver_2.until.elementsLocated(elementLocator), timeout);
                    return webElements;
                }
                catch (err) {
                    if (err instanceof selenium_webdriver_1.error.StaleElementReferenceError) {
                        yield this.wait(polling);
                        continue;
                    }
                    throw err;
                }
            }
            throw new selenium_webdriver_1.error.TimeoutError(`Exceeded maximum presence checkings attempts, problems with 'StaleElementReferenceError' of '${elementLocator}' element`);
        });
    }
    waitAllVisibility(locators, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.trace(`DriverHelper.waitAllVisibility ${locators}`);
            for (const elementLocator of locators) {
                yield this.waitVisibility(elementLocator, timeout);
            }
        });
    }
    waitDisappearance(elementLocator, attempts = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS, polling = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_POLLING) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.trace(`DriverHelper.waitDisappearance ${elementLocator}`);
            const isDisappeared = yield this.waitDisappearanceBoolean(elementLocator, attempts, polling);
            if (!isDisappeared) {
                throw new selenium_webdriver_1.error.TimeoutError(`Waiting attempts exceeded, element '${elementLocator}' is still visible`);
            }
        });
    }
    waitDisappearanceWithTimeout(elementLocator, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.trace(`DriverHelper.waitDisappearanceWithTimeout ${elementLocator}`);
            yield this.getDriver().wait(() => __awaiter(this, void 0, void 0, function* () {
                const isVisible = yield this.isVisible(elementLocator);
                if (!isVisible) {
                    return true;
                }
            }), timeout);
        });
    }
    waitAllDisappearance(locators, attempts = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS, polling = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_POLLING) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.trace(`DriverHelper.waitAllDisappearance ${locators}`);
            for (const elementLocator of locators) {
                yield this.waitDisappearance(elementLocator, attempts, polling);
            }
        });
    }
    waitAndClick(elementLocator, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            const attempts = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS;
            const polling = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_POLLING;
            Logger_1.Logger.trace(`DriverHelper.waitAndClick ${elementLocator}`);
            for (let i = 0; i < attempts; i++) {
                const element = yield this.waitVisibility(elementLocator, timeout);
                try {
                    yield element.click();
                    return;
                }
                catch (err) {
                    if (err instanceof selenium_webdriver_1.error.StaleElementReferenceError) {
                        yield this.wait(polling);
                        continue;
                    }
                    if (err instanceof selenium_webdriver_1.error.WebDriverError) {
                        yield this.wait(polling);
                        continue;
                    }
                    throw err;
                }
            }
            throw new selenium_webdriver_1.error.TimeoutError(`Exceeded maximum clicking attempts, the '${elementLocator}' element is not clickable`);
        });
    }
    waitAndGetElementAttribute(elementLocator, attribute, visibilityTimeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            const attempts = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS;
            const polling = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_POLLING;
            Logger_1.Logger.trace(`DriverHelper.waitAndGetElementAttribute ${elementLocator} attribute: '${attribute}'`);
            for (let i = 0; i < attempts; i++) {
                const element = yield this.waitVisibility(elementLocator, visibilityTimeout);
                try {
                    const attributeValue = yield element.getAttribute(attribute);
                    return attributeValue;
                }
                catch (err) {
                    if (err instanceof selenium_webdriver_1.error.StaleElementReferenceError) {
                        yield this.wait(polling);
                        continue;
                    }
                    throw err;
                }
            }
            throw new selenium_webdriver_1.error.TimeoutError(`Exceeded maximum gettin of the '${attribute}' attribute attempts, from the '${elementLocator}' element`);
        });
    }
    waitAndGetCssValue(elementLocator, cssAttribute, visibilityTimeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            const attempts = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS;
            const polling = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_POLLING;
            Logger_1.Logger.trace(`DriverHelper.waitAndGetCssValue ${elementLocator} cssAttribute: ${cssAttribute}`);
            for (let i = 0; i < attempts; i++) {
                const element = yield this.waitVisibility(elementLocator, visibilityTimeout);
                try {
                    const cssAttributeValue = yield element.getCssValue(cssAttribute);
                    return cssAttributeValue;
                }
                catch (err) {
                    if (err instanceof selenium_webdriver_1.error.StaleElementReferenceError) {
                        yield this.wait(polling);
                        continue;
                    }
                    throw err;
                }
            }
            throw new selenium_webdriver_1.error.TimeoutError(`Exceeded maximum gettin of the '${cssAttribute}' css attribute attempts, from the '${elementLocator}' element`);
        });
    }
    waitAttributeValue(elementLocator, attribute, expectedValue, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.trace(`DriverHelper.waitAttributeValue ${elementLocator}`);
            yield this.driver.wait(() => __awaiter(this, void 0, void 0, function* () {
                const attributeValue = yield this.waitAndGetElementAttribute(elementLocator, attribute, timeout);
                return expectedValue === attributeValue;
            }), timeout, `The '${attribute}' attribute value doesn't match with expected value '${expectedValue}'`);
        });
    }
    type(elementLocator, text, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            const attempts = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS;
            const polling = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_POLLING;
            Logger_1.Logger.trace(`DriverHelper.type ${elementLocator} text: ${text}`);
            for (let i = 0; i < attempts; i++) {
                const element = yield this.waitVisibility(elementLocator, timeout);
                try {
                    yield element.sendKeys(text);
                    return;
                }
                catch (err) {
                    if (err instanceof selenium_webdriver_1.error.StaleElementReferenceError) {
                        yield this.wait(polling);
                        continue;
                    }
                    throw err;
                }
            }
            throw new selenium_webdriver_1.error.TimeoutError(`Exceeded maximum typing attempts, to the '${elementLocator}' element`);
        });
    }
    typeToInvisible(elementLocator, text, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            const attempts = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS;
            const polling = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_POLLING;
            Logger_1.Logger.trace(`DriverHelper.typeToInvisible ${elementLocator} text: ${text}`);
            for (let i = 0; i < attempts; i++) {
                const element = yield this.waitPresence(elementLocator, timeout);
                try {
                    yield element.sendKeys(text);
                    return;
                }
                catch (err) {
                    if (err instanceof selenium_webdriver_1.error.StaleElementReferenceError) {
                        yield this.wait(polling);
                        continue;
                    }
                    throw err;
                }
            }
            throw new selenium_webdriver_1.error.TimeoutError(`Exceeded maximum typing attempts, to the '${elementLocator}' element`);
        });
    }
    clear(elementLocator, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            const attempts = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS;
            const polling = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_POLLING;
            Logger_1.Logger.trace(`DriverHelper.clear ${elementLocator}`);
            for (let i = 0; i < attempts; i++) {
                const element = yield this.waitVisibility(elementLocator, timeout);
                try {
                    yield element.clear();
                    return;
                }
                catch (err) {
                    if (err instanceof selenium_webdriver_1.error.StaleElementReferenceError) {
                        yield this.wait(polling);
                        continue;
                    }
                    throw err;
                }
            }
            throw new selenium_webdriver_1.error.TimeoutError(`Exceeded maximum clearing attempts, to the '${elementLocator}' element`);
        });
    }
    clearInvisible(elementLocator, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            const attempts = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS;
            const polling = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_POLLING;
            Logger_1.Logger.trace(`DriverHelper.clearInvisible ${elementLocator}`);
            for (let i = 0; i < attempts; i++) {
                const element = yield this.waitPresence(elementLocator, timeout);
                try {
                    yield element.clear();
                    return;
                }
                catch (err) {
                    if (err instanceof selenium_webdriver_1.error.StaleElementReferenceError) {
                        yield this.wait(polling);
                        continue;
                    }
                    throw err;
                }
            }
            throw new selenium_webdriver_1.error.TimeoutError(`Exceeded maximum clearing attempts, to the '${elementLocator}' element`);
        });
    }
    enterValue(elementLocator, text, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.trace(`DriverHelper.enterValue ${elementLocator} text: ${text}`);
            yield this.waitVisibility(elementLocator, timeout);
            yield this.clear(elementLocator, timeout);
            yield this.waitAttributeValue(elementLocator, 'value', '', timeout);
            yield this.type(elementLocator, text, timeout);
            yield this.waitAttributeValue(elementLocator, 'value', text, timeout);
        });
    }
    waitAndSwitchToFrame(iframeLocator, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.trace(`DriverHelper.waitAndSwitchToFrame ${iframeLocator}`);
            yield this.driver.wait(selenium_webdriver_2.until.ableToSwitchToFrame(iframeLocator), timeout);
        });
    }
    waitAndGetText(elementLocator, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            const attempts = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS;
            const polling = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_POLLING;
            Logger_1.Logger.trace(`DriverHelper.waitAndGetText ${elementLocator}`);
            for (let i = 0; i < attempts; i++) {
                const element = yield this.waitVisibility(elementLocator, timeout);
                try {
                    const innerText = yield element.getText();
                    return innerText;
                }
                catch (err) {
                    if (err instanceof selenium_webdriver_1.error.StaleElementReferenceError) {
                        yield this.wait(polling);
                        continue;
                    }
                    throw err;
                }
            }
            throw new selenium_webdriver_1.error.TimeoutError(`Exceeded maximum text obtaining attempts, from the '${elementLocator}' element`);
        });
    }
    waitAndGetValue(elementLocator, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.trace(`DriverHelper.waitAndGetValue ${elementLocator}`);
            const elementValue = yield this.waitAndGetElementAttribute(elementLocator, 'value', timeout);
            return elementValue;
        });
    }
    waitUntilTrue(callback, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.trace('DriverHelper.waitUntilTrue');
            yield this.driver.wait(callback(), timeout);
        });
    }
    reloadPage() {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('DriverHelper.reloadPage');
            yield this.driver.navigate().refresh();
        });
    }
    navigateAndWaitToUrl(url) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.trace(`DriverHelper.navigateAndWaitToUrl ${url}`);
            yield this.navigateToUrl(url);
            yield this.waitURL(url);
        });
    }
    navigateToUrl(url) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`DriverHelper.navigateToUrl ${url}`);
            yield this.driver.navigate().to(url);
        });
    }
    waitURL(expectedUrl, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.trace(`DriverHelper.waitURL ${expectedUrl}`);
            yield this.getDriver().wait(() => __awaiter(this, void 0, void 0, function* () {
                const currentUrl = yield this.getDriver().getCurrentUrl();
                const urlEquals = currentUrl === expectedUrl;
                if (urlEquals) {
                    return true;
                }
            }));
        });
    }
    scrollTo(elementLocator, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            const attempts = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS;
            const polling = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_POLLING;
            Logger_1.Logger.trace(`DriverHelper.scrollTo ${elementLocator}`);
            for (let i = 0; i < attempts; i++) {
                const element = yield this.waitPresence(elementLocator, timeout);
                try {
                    yield this.getAction().mouseMove(element).perform();
                    return;
                }
                catch (err) {
                    if (err instanceof selenium_webdriver_1.error.StaleElementReferenceError) {
                        yield this.wait(polling);
                        continue;
                    }
                    throw err;
                }
            }
            throw new selenium_webdriver_1.error.TimeoutError(`Exceeded maximum mouse move attempts, for the '${elementLocator}' element`);
        });
    }
    getDriver() {
        Logger_1.Logger.trace('DriverHelper.getDriver');
        return this.driver;
    }
    waitOpenningSecondWindow(timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.trace('DriverHelper.waitOpenningSecondWindow');
            yield this.driver.wait(() => __awaiter(this, void 0, void 0, function* () {
                const handles = yield this.driver.getAllWindowHandles();
                if (handles.length > 1) {
                    return true;
                }
            }), timeout);
        });
    }
    switchToSecondWindow(mainWindowHandle) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('DriverHelper.switchToSecondWindow');
            yield this.waitOpenningSecondWindow();
            const handles = yield this.driver.getAllWindowHandles();
            handles.splice(handles.indexOf(mainWindowHandle), 1);
            yield this.driver.switchTo().window(handles[0]);
        });
    }
};
DriverHelper = __decorate([
    inversify_1.injectable(),
    __param(0, inversify_1.inject(inversify_types_1.TYPES.Driver)),
    __metadata("design:paramtypes", [Object])
], DriverHelper);
exports.DriverHelper = DriverHelper;
//# sourceMappingURL=DriverHelper.js.map