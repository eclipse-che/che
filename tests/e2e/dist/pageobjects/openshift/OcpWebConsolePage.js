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
const Logger_1 = require("../../utils/Logger");
let OcpWebConsolePage = OcpWebConsolePage_1 = class OcpWebConsolePage {
    constructor(driverHelper) {
        this.driverHelper = driverHelper;
    }
    waitNavpanelOpenShift() {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('OcpWebConsolePage.waitNavpanelOpenShift');
            const navPanelOpenShiftLocator = selenium_webdriver_1.By.css('nav[class=pf-c-nav]');
            yield this.driverHelper.waitVisibility(navPanelOpenShiftLocator);
        });
    }
    clickOnCatalogListNavPanelOpenShift() {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('OcpWebConsolePage.clickOnCatalogListNavPanelOpenShift');
            const catalogListLocator = selenium_webdriver_1.By.xpath('//a[text()=\'Catalog\']');
            yield this.driverHelper.waitAndClick(catalogListLocator);
        });
    }
    clickOnOperatorHubItemNavPanel() {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('OcpWebConsolePage.clickOnOperatorHubItemNavPanel');
            const operatorHubItemLocator = selenium_webdriver_1.By.xpath('//a[text()=\'OperatorHub\']');
            yield this.driverHelper.waitAndClick(operatorHubItemLocator);
        });
    }
    waitOperatorHubMainPage() {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('OcpWebConsolePage.waitOperatorHubMainPage');
            const catalogOperatorHubPageLocator = selenium_webdriver_1.By.xpath('//span[@id=\'resource-title\' and text()=\'OperatorHub\']');
            yield this.driverHelper.waitVisibility(catalogOperatorHubPageLocator);
        });
    }
    clickOnCatalogOperatorIcon() {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('OcpWebConsolePage.clickOnCatalogOperatorIcon');
            const catalogTileOperatorNameCss = `${TestConstants_1.TestConstants.TS_SELENIUM_CATALOG_TILE_OPERATOR_NAME}`;
            const catalogTileOperatorNameLocator = selenium_webdriver_1.By.css(`a[data-test^=${catalogTileOperatorNameCss}]`);
            yield this.driverHelper.waitAndClick(catalogTileOperatorNameLocator, TestConstants_1.TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT);
        });
    }
    clickOnInstallButton() {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('OcpWebConsolePage.clickOnInstallButton');
            const installOperatorButtonLocator = selenium_webdriver_1.By.xpath('//button[text()=\'Install\']');
            yield this.driverHelper.waitAndClick(installOperatorButtonLocator);
        });
    }
    waitCreateOperatorSubscriptionPage() {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('OcpWebConsolePage.waitCreateOperatorSubscriptionPage');
            const createOperatorSubscriptionPageLocator = selenium_webdriver_1.By.xpath('//h1[text()=\'Create Operator Subscription\']');
            yield this.driverHelper.waitVisibility(createOperatorSubscriptionPageLocator);
        });
    }
    selectUpdateChannelOnSubscriptionPage() {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('OcpWebConsolePage.selectUpdateChannelOnSubscriptionPage');
            const updateChannelOperatorLocator = selenium_webdriver_1.By.css(`input[value=${TestConstants_1.TestConstants.TS_OCP_OPERATOR_UPDATE_CHANNEL}]`);
            yield this.driverHelper.waitAndClick(updateChannelOperatorLocator);
        });
    }
    clickOnDropdownNamespaceListOnSubscriptionPage() {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('OcpWebConsolePage.clickOnDropdownNamespaceListOnSubscriptionPage');
            const selectNamespaceLocator = selenium_webdriver_1.By.id('dropdown-selectbox');
            yield this.driverHelper.waitAndClick(selectNamespaceLocator);
        });
    }
    waitListBoxNamespacesOnSubscriptionPage() {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('OcpWebConsolePage.waitListBoxNamespacesOnSubscriptionPage');
            const listBoxNamespaceLocator = selenium_webdriver_1.By.css('ul[class^=dropdown-menu]');
            yield this.driverHelper.waitVisibility(listBoxNamespaceLocator);
        });
    }
    selectDefinedNamespaceOnSubscriptionPage() {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('OcpWebConsolePage.selectDefinedNamespaceOnSubscriptionPage');
            const namespaceItemInDropDownId = `${TestConstants_1.TestConstants.TS_SELENIUM_INSTALL_PROJECT_NAME}-Project-link`;
            const namespaceItemInDropDownLocator = selenium_webdriver_1.By.id(namespaceItemInDropDownId);
            yield this.driverHelper.waitAndClick(namespaceItemInDropDownLocator);
        });
    }
    clickOnSubscribeButtonOnSubscriptionPage() {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('OcpWebConsolePage.clickOnSubscribeButtonOnSubscriptionPage');
            const subscribeOperatorButtonLocator = selenium_webdriver_1.By.xpath('//button[text()=\'Subscribe\']');
            yield this.driverHelper.waitAndClick(subscribeOperatorButtonLocator);
        });
    }
    waitSubscriptionOverviewPage() {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('OcpWebConsolePage.waitSubscriptionOverviewPage');
            const subscriptionOverviewPageLocator = selenium_webdriver_1.By.xpath('//h2[text()=\'Subscription Overview\']');
            yield this.driverHelper.waitVisibility(subscriptionOverviewPageLocator);
        });
    }
    waitChannelNameOnSubscriptionOverviewPage() {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('OcpWebConsolePage.waitChannelNameOnSubscriptionOverviewPage');
            const channelNameOnSubscriptionOverviewLocator = selenium_webdriver_1.By.xpath(`//button[@type='button' and text()='${TestConstants_1.TestConstants.TS_OCP_OPERATOR_UPDATE_CHANNEL}']`);
            yield this.driverHelper.waitVisibility(channelNameOnSubscriptionOverviewLocator);
        });
    }
    waitUpgradeStatusOnSubscriptionOverviewPage() {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('OcpWebConsolePage.waitUpgradeStatusOnSubscriptionOverviewPage');
            const upgradeStatuslocator = selenium_webdriver_1.By.xpath('//span[text()=\' Up to date\']');
            yield this.driverHelper.waitVisibility(upgradeStatuslocator, TestConstants_1.TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT);
        });
    }
    waitCatalogSourceNameOnSubscriptionOverviewPage() {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('OcpWebConsolePage.waitCatalogSourceNameOnSubscriptionOverviewPage');
            const catalogSourceNameCss = `${TestConstants_1.TestConstants.TS_SELENIUM_CATALOG_SOURCE_NAME}-${TestConstants_1.TestConstants.TS_SELENIUM_INSTALL_PROJECT_NAME}`;
            const catalogSourceNameLolcator = selenium_webdriver_1.By.css(`a[title='${catalogSourceNameCss}']`);
            yield this.driverHelper.waitVisibility(catalogSourceNameLolcator);
        });
    }
    selectInstalledOperatorsOnNavPanel() {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('OcpWebConsolePage.selectInstalledOperatorsOnNavPanel');
            const installedOperatorsItemNavPanelLocator = selenium_webdriver_1.By.xpath('//a[text()=\'Installed Operators\']');
            yield this.driverHelper.waitAndClick(installedOperatorsItemNavPanelLocator);
        });
    }
    waitInstalledOperatorLogoName() {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('OcpWebConsolePage.waitInstalledOperatorLogoName');
            yield this.driverHelper.waitVisibility(selenium_webdriver_1.By.xpath(OcpWebConsolePage_1.OPERATOR_LOGO_NAME_XPATH));
        });
    }
    waitStatusInstalledOperator() {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('OcpWebConsolePage.waitStatusInstalledOperator');
            const statusInstalledOperatorLocator = selenium_webdriver_1.By.xpath('//span[text()=\'InstallSucceeded\']');
            yield this.driverHelper.waitVisibility(statusInstalledOperatorLocator, TestConstants_1.TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT);
        });
    }
    clickOnInstalledOperatorLogoName() {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('OcpWebConsolePage.clickOnInstalledOperatorLogoName');
            yield this.driverHelper.waitAndClick(selenium_webdriver_1.By.xpath(OcpWebConsolePage_1.OPERATOR_LOGO_NAME_XPATH));
        });
    }
    waitOverviewCsvEclipseCheOperator() {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('OcpWebConsolePage.waitOverviewCsvEclipseCheOperator');
            yield this.driverHelper.waitVisibility(selenium_webdriver_1.By.xpath(OcpWebConsolePage_1.OPERATOR_LOGO_NAME_XPATH));
        });
    }
    clickCreateNewCheClusterLink() {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('OcpWebConsolePage.clickCreateNewCheClusterLink');
            const createNewCheLusterLinkLocator = selenium_webdriver_1.By.xpath('//a[text()=\' Create New\']');
            yield this.driverHelper.waitAndClick(createNewCheLusterLinkLocator);
        });
    }
    waitCreateCheClusterYaml() {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('OcpWebConsolePage.waitCreateCheClusterYaml');
            const createCheClusterYamlLocator = selenium_webdriver_1.By.xpath('//h1[text()=\'Create Che Cluster\']');
            yield this.driverHelper.waitVisibility(createCheClusterYamlLocator);
        });
    }
    selectOpenShiftOAuthFieldInYaml() {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('OcpWebConsolePage.selectOpenShiftOAuthFieldInYaml');
            const openShiftOAuthLineLocator = selenium_webdriver_1.By.xpath(`//div[@class=\'ace_gutter-cell \' and text()=\'${TestConstants_1.TestConstants.TS_SELENIUM_OPENSHIFT_OAUTH_FIELD_LINE}\']`);
            yield this.driverHelper.waitAndClick(openShiftOAuthLineLocator);
        });
    }
    setValueOpenShiftOAuthField() {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('OcpWebConsolePage.setValueOpenShiftOAuthField');
            const openShiftOAuthEditorLocator = selenium_webdriver_1.By.css('textarea[class=\'ace_text-input\']');
            yield this.driverHelper.clearInvisible(openShiftOAuthEditorLocator);
            yield this.driverHelper.typeToInvisible(openShiftOAuthEditorLocator, `    openShiftoAuth: ${TestConstants_1.TestConstants.TS_SELENIUM_VALUE_OPENSHIFT_OAUTH}`);
            yield this.driverHelper.typeToInvisible(openShiftOAuthEditorLocator, selenium_webdriver_1.Key.ENTER);
        });
    }
    clickOnCreateCheClusterButton() {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('OcpWebConsolePage.clickOnCreateCheClusterButton');
            const createCheClusterButtonLocator = selenium_webdriver_1.By.id('save-changes');
            yield this.driverHelper.waitAndClick(createCheClusterButtonLocator);
        });
    }
    waitResourcesCheClusterTitle() {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('OcpWebConsolePage.waitResourcesCheClusterTitle');
            const resourcesCheClusterTitleLocator = selenium_webdriver_1.By.id('resource-title');
            yield this.driverHelper.waitVisibility(resourcesCheClusterTitleLocator);
        });
    }
    waitResourcesCheClusterTimestamp() {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('OcpWebConsolePage.waitResourcesCheClusterTimestamp');
            const resourcesCheClusterTimestampLocator = selenium_webdriver_1.By.xpath('//div[contains(@class, \'timestamp\')]/div[text()=\'a minute ago\']');
            yield this.driverHelper.waitVisibility(resourcesCheClusterTimestampLocator, TestConstants_1.TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT);
        });
    }
    clickOnCheClusterResourcesName() {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('OcpWebConsolePage.clickOnCheClusterResourcesName');
            const cheClusterResourcesNameLocator = selenium_webdriver_1.By.css('a[class=co-resource-item__resource-name]');
            yield this.driverHelper.waitAndClick(cheClusterResourcesNameLocator);
        });
    }
    waitKeycloakAdminConsoleUrl() {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('OcpWebConsolePage.waitKeycloakAdminConsoleUrl');
            const keyCloakAdminWebConsoleUrl = selenium_webdriver_1.By.partialLinkText(`keycloak-${TestConstants_1.TestConstants.TS_SELENIUM_INSTALL_PROJECT_NAME}`);
            yield this.driverHelper.waitVisibility(keyCloakAdminWebConsoleUrl, TestConstants_1.TestConstants.TS_SELENIUM_INSTALL_ECLIPSE_CHE_TIMEOUT);
        });
    }
    waitInstalledAppUrl() {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('OcpWebConsolePage.waitInstalledAppUrl');
            const installedAppUrlLocator = selenium_webdriver_1.By.partialLinkText(`${OcpWebConsolePage_1.INSTALLED_APP_URL_XPATH}`);
            yield this.driverHelper.waitVisibility(installedAppUrlLocator, TestConstants_1.TestConstants.TS_SELENIUM_INSTALL_ECLIPSE_CHE_TIMEOUT);
        });
    }
    clickOnInstalledAppUrl() {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('OcpWebConsolePage.clickOnInstalledAppUrl');
            const installedAppUrlLocator = selenium_webdriver_1.By.partialLinkText(`${OcpWebConsolePage_1.INSTALLED_APP_URL_XPATH}`);
            yield this.driverHelper.waitAndClick(installedAppUrlLocator);
        });
    }
};
OcpWebConsolePage.OPERATOR_LOGO_NAME_XPATH = `//h1[text()='${TestConstants_1.TestConstants.TS_SELENIUM_OPERATOR_LOGO_NAME}']`;
OcpWebConsolePage.INSTALLED_APP_URL_XPATH = `${TestConstants_1.TestConstants.TS_SELENIUM_INSTALL_APP_PREFIX_URL}-${TestConstants_1.TestConstants.TS_SELENIUM_INSTALL_PROJECT_NAME}`;
OcpWebConsolePage = OcpWebConsolePage_1 = __decorate([
    inversify_1.injectable(),
    __param(0, inversify_1.inject(inversify_types_1.CLASSES.DriverHelper)),
    __metadata("design:paramtypes", [DriverHelper_1.DriverHelper])
], OcpWebConsolePage);
exports.OcpWebConsolePage = OcpWebConsolePage;
//# sourceMappingURL=OcpWebConsolePage.js.map