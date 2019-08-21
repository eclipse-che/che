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
var OcpWebConsolePage_1;
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
require("reflect-metadata");
const inversify_1 = require("inversify");
const DriverHelper_1 = require("../../utils/DriverHelper");
const inversify_types_1 = require("../../inversify.types");
const TestConstants_1 = require("../../TestConstants");
const selenium_webdriver_1 = require("selenium-webdriver");
let OcpWebConsolePage = OcpWebConsolePage_1 = class OcpWebConsolePage {
    constructor(driverHelper) {
        this.driverHelper = driverHelper;
    }
    waitNavpanelOpenShift() {
        return __awaiter(this, void 0, void 0, function* () {
            const navPanelOpenShiftLocator = selenium_webdriver_1.By.css('nav[class=pf-c-nav]');
            yield this.driverHelper.waitVisibility(navPanelOpenShiftLocator);
        });
    }
    clickOnCatalogListNavPanelOpenShift() {
        return __awaiter(this, void 0, void 0, function* () {
            const catalogListLocator = selenium_webdriver_1.By.xpath('//a[text()=\'Catalog\']');
            yield this.driverHelper.waitAndClick(catalogListLocator);
        });
    }
    clickOnOperatorHubItemNavPanel() {
        return __awaiter(this, void 0, void 0, function* () {
            const operatorHubItemLocator = selenium_webdriver_1.By.xpath('//a[text()=\'OperatorHub\']');
            yield this.driverHelper.waitAndClick(operatorHubItemLocator);
        });
    }
    waitOperatorHubMainPage() {
        return __awaiter(this, void 0, void 0, function* () {
            const catalogOperatorHubPageLocator = selenium_webdriver_1.By.xpath('//span[@id=\'resource-title\' and text()=\'OperatorHub\']');
            yield this.driverHelper.waitVisibility(catalogOperatorHubPageLocator);
        });
    }
    clickOnEclipseCheOperatorIcon() {
        return __awaiter(this, void 0, void 0, function* () {
            const catalogEclipseCheOperatorTitleLocator = selenium_webdriver_1.By.css('a[data-test^=eclipse-che-preview-openshift]');
            yield this.driverHelper.waitAndClick(catalogEclipseCheOperatorTitleLocator, TestConstants_1.TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT);
        });
    }
    clickOnInstallEclipseCheButton() {
        return __awaiter(this, void 0, void 0, function* () {
            const installEclipsCheOperatorButtonLocator = selenium_webdriver_1.By.xpath('//button[text()=\'Install\']');
            yield this.driverHelper.waitAndClick(installEclipsCheOperatorButtonLocator);
        });
    }
    waitCreateOperatorSubscriptionPage() {
        return __awaiter(this, void 0, void 0, function* () {
            const createOperatorSubscriptionPageLocator = selenium_webdriver_1.By.xpath('//h1[text()=\'Create Operator Subscription\']');
            yield this.driverHelper.waitVisibility(createOperatorSubscriptionPageLocator);
        });
    }
    selectUpdateChannelOnSubscriptionPage(channelName) {
        return __awaiter(this, void 0, void 0, function* () {
            const updateChannelOperatorLocator = selenium_webdriver_1.By.css(`input[value=${channelName}]`);
            yield this.driverHelper.waitAndClick(updateChannelOperatorLocator);
        });
    }
    clickOnDropdownNamespaceListOnSubscriptionPage() {
        return __awaiter(this, void 0, void 0, function* () {
            const selectNamespaceLocator = selenium_webdriver_1.By.id('dropdown-selectbox');
            yield this.driverHelper.waitAndClick(selectNamespaceLocator);
        });
    }
    waitListBoxNamespacesOnSubscriptionPage() {
        return __awaiter(this, void 0, void 0, function* () {
            const listBoxNamespaceLocator = selenium_webdriver_1.By.css('ul[class^=dropdown-menu]');
            yield this.driverHelper.waitVisibility(listBoxNamespaceLocator);
        });
    }
    selectDefinedNamespaceOnSubscriptionPage(projectName) {
        return __awaiter(this, void 0, void 0, function* () {
            const namespaceItemInDropDownLocator = selenium_webdriver_1.By.id(`${projectName}-Project-link`);
            yield this.driverHelper.waitAndClick(namespaceItemInDropDownLocator);
        });
    }
    clickOnSubscribeButtonOnSubscriptionPage() {
        return __awaiter(this, void 0, void 0, function* () {
            const subscribeOperatorButtonLocator = selenium_webdriver_1.By.xpath('//button[text()=\'Subscribe\']');
            yield this.driverHelper.waitAndClick(subscribeOperatorButtonLocator);
        });
    }
    waitSubscriptionOverviewPage() {
        return __awaiter(this, void 0, void 0, function* () {
            const subscriptionOverviewPageLocator = selenium_webdriver_1.By.xpath('//h2[text()=\'Subscription Overview\']');
            yield this.driverHelper.waitVisibility(subscriptionOverviewPageLocator);
        });
    }
    waitChannelNameOnSubscriptionOverviewPage(channelName) {
        return __awaiter(this, void 0, void 0, function* () {
            const channelNameOnSubscriptionOverviewLocator = selenium_webdriver_1.By.xpath(`//button[@type='button' and text()='${channelName}']`);
            yield this.driverHelper.waitVisibility(channelNameOnSubscriptionOverviewLocator);
        });
    }
    waitUpgradeStatusOnSubscriptionOverviewPage() {
        return __awaiter(this, void 0, void 0, function* () {
            const upgradeStatuslocator = selenium_webdriver_1.By.xpath('//span[text()=\' Up to date\']');
            yield this.driverHelper.waitVisibility(upgradeStatuslocator, TestConstants_1.TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT);
        });
    }
    waitCatalogSourceNameOnSubscriptionOverviewPage(projectName) {
        return __awaiter(this, void 0, void 0, function* () {
            const catalogSourceNameLolcator = selenium_webdriver_1.By.css(`a[title=\'installed-custom-${projectName}\']`);
            yield this.driverHelper.waitVisibility(catalogSourceNameLolcator);
        });
    }
    selectInstalledOperatorsOnNavPanel() {
        return __awaiter(this, void 0, void 0, function* () {
            const installedOperatorsItemNavPanelLocator = selenium_webdriver_1.By.xpath('//a[text()=\'Installed Operators\']');
            yield this.driverHelper.waitAndClick(installedOperatorsItemNavPanelLocator);
        });
    }
    waitEclipseCheOperatorLogoName() {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.driverHelper.waitVisibility(selenium_webdriver_1.By.xpath(OcpWebConsolePage_1.CHE_OPERATOR_LOGO_NAME));
        });
    }
    waitStatusInstalledEclipseCheOperator() {
        return __awaiter(this, void 0, void 0, function* () {
            const statusInstalledCheOperatorLocator = selenium_webdriver_1.By.xpath('//span[text()=\'InstallSucceeded\']');
            yield this.driverHelper.waitVisibility(statusInstalledCheOperatorLocator, TestConstants_1.TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT);
        });
    }
    clickOnEclipseCheOperatorLogoName() {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.driverHelper.waitAndClick(selenium_webdriver_1.By.xpath(OcpWebConsolePage_1.CHE_OPERATOR_LOGO_NAME));
        });
    }
    waitOverviewCsvEclipseCheOperator() {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.driverHelper.waitVisibility(selenium_webdriver_1.By.xpath(OcpWebConsolePage_1.CHE_OPERATOR_LOGO_NAME));
        });
    }
    clickCreateNewCheClusterLink() {
        return __awaiter(this, void 0, void 0, function* () {
            const createNewCheLusterLinkLocator = selenium_webdriver_1.By.xpath('//a[text()=\' Create New\']');
            yield this.driverHelper.waitAndClick(createNewCheLusterLinkLocator);
        });
    }
    waitCreateCheClusterYaml() {
        return __awaiter(this, void 0, void 0, function* () {
            const createCheClusterYamlLocator = selenium_webdriver_1.By.xpath('//h1[text()=\'Create Che Cluster\']');
            yield this.driverHelper.waitVisibility(createCheClusterYamlLocator);
        });
    }
    selectOpenShiftOAuthFieldInYaml(line) {
        return __awaiter(this, void 0, void 0, function* () {
            const openShiftOAuthFieldLocator = selenium_webdriver_1.By.xpath(`//div[@class=\'ace_gutter-cell \' and text()=\'${line}\']`);
            yield this.driverHelper.waitAndClick(openShiftOAuthFieldLocator);
        });
    }
    changeValueOpenShiftOAuthField() {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.driverHelper.getAction().sendKeys(selenium_webdriver_1.Key.DELETE.toString()).sendKeys(selenium_webdriver_1.Key.ENTER.toString()).sendKeys(selenium_webdriver_1.Key.UP.toString()).perform();
            yield this.driverHelper.getAction().sendKeys('    openShiftoAuth: false');
        });
    }
    clickOnCreateCheClusterButton() {
        return __awaiter(this, void 0, void 0, function* () {
            const createCheClusterButtonLocator = selenium_webdriver_1.By.id('save-changes');
            yield this.driverHelper.waitAndClick(createCheClusterButtonLocator);
        });
    }
    waitResourcesCheClusterTitle() {
        return __awaiter(this, void 0, void 0, function* () {
            const resourcesCheClusterTitleLocator = selenium_webdriver_1.By.id('resource-title');
            yield this.driverHelper.waitVisibility(resourcesCheClusterTitleLocator);
        });
    }
    waitResourcesCheClusterTimestamp() {
        return __awaiter(this, void 0, void 0, function* () {
            const resourcesCheClusterTimestampLocator = selenium_webdriver_1.By.xpath('//div[contains(@class, \'timestamp\')]/div[text()=\'a minute ago\']');
            yield this.driverHelper.waitVisibility(resourcesCheClusterTimestampLocator, TestConstants_1.TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT);
        });
    }
    clickOnCheClusterResourcesName() {
        return __awaiter(this, void 0, void 0, function* () {
            const cheClusterResourcesNameLocator = selenium_webdriver_1.By.css('a[class=co-resource-item__resource-name]');
            yield this.driverHelper.waitAndClick(cheClusterResourcesNameLocator);
        });
    }
    clickCheClusterOverviewExpandButton() {
        return __awaiter(this, void 0, void 0, function* () {
            const cheClusterOverviewExpandButton = selenium_webdriver_1.By.css('label[class=\'btn compaction-btn btn-default\']');
            yield this.driverHelper.waitAndClick(cheClusterOverviewExpandButton);
        });
    }
    waitKeycloakAdminConsoleUrl(projectName) {
        return __awaiter(this, void 0, void 0, function* () {
            const keyCloakAdminWebConsoleUrl = selenium_webdriver_1.By.partialLinkText(`keycloak-${projectName}`);
            yield this.driverHelper.waitVisibility(keyCloakAdminWebConsoleUrl, TestConstants_1.TestConstants.TS_SELENIUM_INSTALL_ECLIPSE_CHE_TIMEOUT);
        });
    }
    waitEclipseCheUrl(projectName) {
        return __awaiter(this, void 0, void 0, function* () {
            const eclipseCheUrlLocator = selenium_webdriver_1.By.partialLinkText(`${OcpWebConsolePage_1.ECLIPSE_CHE_PREFIX_URL}${projectName}`);
            yield this.driverHelper.waitVisibility(eclipseCheUrlLocator, TestConstants_1.TestConstants.TS_SELENIUM_INSTALL_ECLIPSE_CHE_TIMEOUT);
        });
    }
    clickOnEclipseCHeUrl(projectName) {
        return __awaiter(this, void 0, void 0, function* () {
            const eclipseCheUrlLocator = selenium_webdriver_1.By.partialLinkText(`${OcpWebConsolePage_1.ECLIPSE_CHE_PREFIX_URL}${projectName}`);
            yield this.driverHelper.waitAndClick(eclipseCheUrlLocator);
        });
    }
};
OcpWebConsolePage.CHE_OPERATOR_LOGO_NAME = '//h1[text()=\'Eclipse Che\']';
OcpWebConsolePage.ECLIPSE_CHE_PREFIX_URL = 'che-';
OcpWebConsolePage = OcpWebConsolePage_1 = __decorate([
    inversify_1.injectable(),
    __param(0, inversify_1.inject(inversify_types_1.CLASSES.DriverHelper)),
    __metadata("design:paramtypes", [DriverHelper_1.DriverHelper])
], OcpWebConsolePage);
exports.OcpWebConsolePage = OcpWebConsolePage;
