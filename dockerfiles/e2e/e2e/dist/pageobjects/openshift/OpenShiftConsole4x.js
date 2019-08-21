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
var OpenShiftConsole4x_1;
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
let OpenShiftConsole4x = OpenShiftConsole4x_1 = class OpenShiftConsole4x {
    constructor(driverHelper) {
        this.driverHelper = driverHelper;
    }
    waitNavpanelOpenShift() {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.driverHelper.waitVisibility(selenium_webdriver_1.By.xpath('//nav[@class=\'pf-c-nav\']'));
        });
    }
    clickOnCatalogListNavPanelOpenShift() {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.driverHelper.waitAndClick(selenium_webdriver_1.By.xpath('//a[text()=\'Catalog\']'));
        });
    }
    clickOnOperatorHubItemNavPanel() {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.driverHelper.waitAndClick(selenium_webdriver_1.By.xpath('//a[text()=\'OperatorHub\']'));
        });
    }
    waitOperatorHubMainPage() {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.driverHelper.waitVisibility(selenium_webdriver_1.By.xpath('//span[@id=\'resource-title\' and text()=\'OperatorHub\']'));
        });
    }
    clickOnEclipseCheOperatorIcon() {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.driverHelper.waitAndClick(selenium_webdriver_1.By.xpath('//a[contains(@data-test, \'eclipse-che-preview\')]'));
        });
    }
    clickOnInstallEclipseCheButton() {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.driverHelper.waitAndClick(selenium_webdriver_1.By.xpath('//button[text()=\'Install\']'));
        });
    }
    selectStableUpdateChannel() {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.driverHelper.waitAndClick(selenium_webdriver_1.By.xpath('//input[@type=\'radio\' and @value=\'stable\']'));
        });
    }
    selectNightltyUpdateChannel() {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.driverHelper.waitAndClick(selenium_webdriver_1.By.xpath('//input[@type=\'radio\' and @value=\'nightly\']'));
        });
    }
    selectUpdateChannelOnSubscriptionPage(channelName) {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.driverHelper.waitAndClick(selenium_webdriver_1.By.xpath(`//input[@type=\'radio\' and @value=\'${channelName}\']`));
        });
    }
    waitCreateOperatorSubscriptionPage() {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.driverHelper.waitVisibility(selenium_webdriver_1.By.xpath('//h1[text()=\'Create Operator Subscription\']'));
        });
    }
    clickOnDropdownNamespaceListOnSubscriptionPage() {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.driverHelper.waitAndClick(selenium_webdriver_1.By.xpath('//button[@id=\'dropdown-selectbox\']'));
        });
    }
    waitListBoxNamespacesOnSubscriptionPage() {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.driverHelper.waitVisibility(selenium_webdriver_1.By.xpath('//ul[@role=\'listbox\' and contains(@class, \'dropdown-menu\')]'));
        });
    }
    selectDefinedNamespaceOnSubscriptionPage(projectName) {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.driverHelper.waitAndClick(selenium_webdriver_1.By.xpath(`//a[@id=\'${projectName}-Project-link\']`));
        });
    }
    clickOnSubscribeButtonOnSubscriptionPage() {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.driverHelper.waitAndClick(selenium_webdriver_1.By.xpath('//button[text()=\'Subscribe\']'));
        });
    }
    waitSubscriptionOverviewPage() {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.driverHelper.waitVisibility(selenium_webdriver_1.By.xpath('//h2[text()=\'Subscription Overview\']'));
        });
    }
    waitChannelNameOnSubscriptionOverviewPage(channelName) {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.driverHelper.waitVisibility(selenium_webdriver_1.By.xpath(`//button[contains(@class, 'btn-link') and text()='${channelName}']`));
        });
    }
    waitUpgradeStatusOnSubscriptionOverviewPage() {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.driverHelper.waitVisibility(selenium_webdriver_1.By.xpath('//span[text()=\' Up to date\']'), TestConstants_1.TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT);
        });
    }
    waitCatalogSourceNameOnSubscriptionOverviewPage(projectName) {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.driverHelper.waitVisibility(selenium_webdriver_1.By.xpath(`//a[contains(@title, \'${projectName}\')]`));
        });
    }
    selectInstalledOperatorsOnNavPanel() {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.driverHelper.waitAndClick(selenium_webdriver_1.By.xpath('//a[contains(@class, \'pf-c-nav\') and text()=\'Installed Operators\']'));
        });
    }
    waitEclipseCheOperatorLogoName() {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.driverHelper.waitVisibility(selenium_webdriver_1.By.xpath(OpenShiftConsole4x_1.CHE_OPERATOR_LOGO_NAME));
        });
    }
    waitStatusInstalledEclipseCheOperator() {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.driverHelper.waitVisibility(selenium_webdriver_1.By.xpath('//div[@class=\'row co-resource-list__item\']//span[text()=\'InstallSucceeded\']'));
        });
    }
    clickOnEclipseCheOperatorLogoName() {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.driverHelper.waitAndClick(selenium_webdriver_1.By.xpath(OpenShiftConsole4x_1.CHE_OPERATOR_LOGO_NAME));
        });
    }
    waitOverviewCsvEclipseCheOperator() {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.driverHelper.waitVisibility(selenium_webdriver_1.By.xpath('//h2[@class=\'co-section-heading\' and text()=\'ClusterServiceVersion Overview\']'));
        });
    }
    clickCreateNewCheClusterLink() {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.driverHelper.waitAndClick(selenium_webdriver_1.By.xpath('//div[contains(@class, \'ClusterServiceVersion\')]//a[text()=\' Create New\']'));
        });
    }
    waitCreateCheClusterYaml() {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.driverHelper.waitVisibility(selenium_webdriver_1.By.xpath('//h1[text()=\'Create Che Cluster\']'));
        });
    }
    selectOpenShiftOAuthFieldInYaml() {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.driverHelper.waitAndClick(selenium_webdriver_1.By.xpath(`//div[@class=\'ace_gutter-cell \' and text()=\'21\']`));
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
            yield this.driverHelper.waitAndClick(selenium_webdriver_1.By.xpath('//button[@id=\'save-changes\' and text()=\'Create\']'));
        });
    }
    waitResourcesCheClusterTitle() {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.driverHelper.waitVisibility(selenium_webdriver_1.By.xpath('//span[@id=\'resource-title\' and text()=\'Che Clusters\']'));
        });
    }
    waitResourcesCheClusterTimestamp() {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.driverHelper.waitVisibility(selenium_webdriver_1.By.xpath('//div[contains(@class, \'timestamp\')]/div[text()=\'a minute ago\']'), TestConstants_1.TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT);
        });
    }
    clickOnCheClusterResourcesName() {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.driverHelper.waitAndClick(selenium_webdriver_1.By.xpath('//a[contains(@class, \'resource-name\') and text()=\'eclipse-che\']'));
        });
    }
    clickCheClusterOverviewExpandButton() {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.driverHelper.waitAndClick(selenium_webdriver_1.By.xpath('//label[@class=\'btn compaction-btn btn-default\']'));
        });
    }
    waitKeycloakAdminConsoleUrl(projectName) {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.driverHelper.waitVisibility(selenium_webdriver_1.By.partialLinkText(`keycloak-${projectName}`), TestConstants_1.TestConstants.TS_SELENIUM_START_WORKSPACE_TIMEOUT);
        });
    }
    waitEclipseCheUrl(projectName) {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.driverHelper.waitVisibility(selenium_webdriver_1.By.partialLinkText(`${OpenShiftConsole4x_1.ECLIPSE_CHE_PREFIX_URL}--${projectName}`), TestConstants_1.TestConstants.TS_SELENIUM_START_WORKSPACE_TIMEOUT);
        });
    }
    clickOnEclipseCHeUrl(projectName) {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.driverHelper.waitAndClick(selenium_webdriver_1.By.partialLinkText(`${OpenShiftConsole4x_1.ECLIPSE_CHE_PREFIX_URL}-${projectName}`));
        });
    }
};
OpenShiftConsole4x.CHE_OPERATOR_LOGO_NAME = '//h1[contains(@class, \'logo__name__clusterserviceversion\') and text()=\'Eclipse Che\']';
OpenShiftConsole4x.ECLIPSE_CHE_PREFIX_URL = 'che-';
OpenShiftConsole4x = OpenShiftConsole4x_1 = __decorate([
    inversify_1.injectable(),
    __param(0, inversify_1.inject(inversify_types_1.CLASSES.DriverHelper)),
    __metadata("design:paramtypes", [DriverHelper_1.DriverHelper])
], OpenShiftConsole4x);
exports.OpenShiftConsole4x = OpenShiftConsole4x;
