/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import 'reflect-metadata';
import { injectable, inject } from 'inversify';
import { DriverHelper } from '../../utils/DriverHelper';
import { CLASSES } from '../../inversify.types';
import { TestConstants } from '../../TestConstants';
import { By, Key } from 'selenium-webdriver';
import { Logger } from '../../utils/Logger';

@injectable()
export class OcpWebConsolePage {

    private static readonly OPERATOR_LOGO_NAME_XPATH: string = `//h1[text()='${TestConstants.TS_SELENIUM_OPERATOR_LOGO_NAME}']`;
    private static readonly INSTALLED_APP_URL_XPATH: string = `${TestConstants.TS_SELENIUM_INSTALL_APP_PREFIX_URL}-${TestConstants.TS_SELENIUM_INSTALL_PROJECT_NAME}`;
    private static readonly NAV_PANEL_OPENSHIFT_CSS: string = '.pf-c-nav';

    constructor(
        @inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) { }

    async waitNavpanelOpenShift() {
        Logger.debug('OcpWebConsolePage.waitNavpanelOpenShift');

        await this.driverHelper.waitVisibility(By.css(OcpWebConsolePage.NAV_PANEL_OPENSHIFT_CSS));
    }

    async waitDisappearanceNavpanelOpenShift() {
        Logger.debug('OcpWebConsolePage.waitDisappearanceNavpanelOpenShift');

        await this.driverHelper.waitDisappearance(By.css(OcpWebConsolePage.NAV_PANEL_OPENSHIFT_CSS));
    }

    async openOperatorHubMainPageByUrl(url: string) {
        Logger.debug('OcpWebConsolePage.openOperatorHubMainPageByUrl');

        await this.driverHelper.navigateToUrl(url);
    }

    async waitOperatorHubMainPage() {
        Logger.debug('OcpWebConsolePage.waitOperatorHubMainPage');

        const catalogOperatorHubPageLocator: By = By.xpath('//span[text()=\'OperatorHub\']');
        await this.driverHelper.waitVisibility(catalogOperatorHubPageLocator);
    }

    async clickOnCatalogOperatorIcon() {
        Logger.debug('OcpWebConsolePage.clickOnCatalogOperatorIcon');

        const catalogTileOperatorNameLocator: By = By.css(`a[data-test^=${TestConstants.TS_SELENIUM_CATALOG_TILE_OPERATOR_NAME}]`);
        await this.driverHelper.waitAndClick(catalogTileOperatorNameLocator, TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT);
    }
    async clickOnInstallButton() {
        Logger.debug('OcpWebConsolePage.clickOnInstallButton');

        const installOperatorButtonLocator: By = By.xpath('//*[text()=\'Install\']');
        await this.driverHelper.waitAndClick(installOperatorButtonLocator);
    }

    async waitCreateOperatorSubscriptionPage() {
        Logger.debug('OcpWebConsolePage.waitCreateOperatorSubscriptionPage');

        const createOperatorSubscriptionPageLocator: By = By.xpath('//h1[text()=\'Create Operator Subscription\']');
        await this.driverHelper.waitVisibility(createOperatorSubscriptionPageLocator, TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT);
    }

    async selectUpdateChannelOnSubscriptionPage() {
        Logger.debug('OcpWebConsolePage.selectUpdateChannelOnSubscriptionPage');

        const updateChannelOperatorLocator: By = By.css(`input[value=${TestConstants.TS_OCP_OPERATOR_UPDATE_CHANNEL}]`);
        await this.driverHelper.waitAndClick(updateChannelOperatorLocator, TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT);
    }

    async clickOnDropdownNamespaceListOnSubscriptionPage() {
        Logger.debug('OcpWebConsolePage.clickOnDropdownNamespaceListOnSubscriptionPage');

        const selectNamespaceLocator: By = By.id('dropdown-selectbox');
        await this.driverHelper.waitAndClick(selectNamespaceLocator);
    }

    async waitListBoxNamespacesOnSubscriptionPage() {
        Logger.debug('OcpWebConsolePage.waitListBoxNamespacesOnSubscriptionPage');

        const listBoxNamespaceLocator: By = By.css('ul[class^=dropdown-menu]');
        await this.driverHelper.waitVisibility(listBoxNamespaceLocator);
    }

    async selectDefinedNamespaceOnSubscriptionPage() {
        Logger.debug('OcpWebConsolePage.selectDefinedNamespaceOnSubscriptionPage');

        const namespaceItemInDropDownId: string = `${TestConstants.TS_SELENIUM_INSTALL_PROJECT_NAME}-Project-link`;
        const namespaceItemInDropDownLocator: By = By.id(namespaceItemInDropDownId);
        await this.driverHelper.waitAndClick(namespaceItemInDropDownLocator);
    }

    async clickOnSubscribeButtonOnSubscriptionPage() {
        Logger.debug('OcpWebConsolePage.clickOnSubscribeButtonOnSubscriptionPage');

        const subscribeOperatorButtonLocator: By = By.xpath('//button[text()=\'Subscribe\']');
        await this.driverHelper.waitAndClick(subscribeOperatorButtonLocator);
    }

    async selectInstalledOperatorsOnNavPanel() {
        Logger.debug('OcpWebConsolePage.selectInstalledOperatorsOnNavPanel');

        const installedOperatorsItemNavPanelLocator: By = By.xpath('//a[text()=\'Installed Operators\']');
        await this.driverHelper.waitAndClick(installedOperatorsItemNavPanelLocator);
    }

    async waitInstalledOperatorLogoName() {
        Logger.debug('OcpWebConsolePage.waitInstalledOperatorLogoName');

        await this.driverHelper.waitVisibility(By.xpath(OcpWebConsolePage.OPERATOR_LOGO_NAME_XPATH), TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT);
    }

    async clickOnInstalledOperatorLogoName() {
        Logger.debug('OcpWebConsolePage.clickOnInstalledOperatorLogoName');

        await this.driverHelper.waitAndClick(By.xpath(OcpWebConsolePage.OPERATOR_LOGO_NAME_XPATH));
    }

    async waitOverviewCsvOperator() {
        Logger.debug('OcpWebConsolePage.waitOverviewCsvEclipseCheOperator');

        await this.driverHelper.waitVisibility(By.xpath(OcpWebConsolePage.OPERATOR_LOGO_NAME_XPATH));
    }

    async clickCreateNewCheClusterLink() {
        Logger.debug('OcpWebConsolePage.clickCreateNewCheClusterLink');

        const createNewCheLusterLinkLocator: By = By.partialLinkText('Create');
        await this.driverHelper.waitAndClick(createNewCheLusterLinkLocator);
    }

    async waitCreateCheClusterYaml() {
        Logger.debug('OcpWebConsolePage.waitCreateCheClusterYaml');

        const headerTextCheClusterLocator: By = By.xpath('//h1[contains(text(), \'Cluster\')]');
        const createCheClusterYamlLocator: By = By.css('div[class=yaml-editor]');
        await this.driverHelper.waitVisibility(headerTextCheClusterLocator);
        await this.driverHelper.waitVisibility(createCheClusterYamlLocator);
    }

    async openEditorReplaceWidget() {
        Logger.debug('OcpWebConsolePage.openEditorReplaceWidget');

        await this.driverHelper.getAction().sendKeys(Key.chord(Key.CONTROL, 'h')).perform();
    }

    async setValuePropertyInCheClusterYaml(propertyName: string, propertyDefaultValue: string, propertyNewValue: string) {
        Logger.debug('OcpWebConsolePage.editPropertyInCheClusterYaml');

        await this.driverHelper.getAction().sendKeys(`${propertyName}: ${propertyDefaultValue}`).sendKeys(Key.TAB).perform();
        await this.driverHelper.getAction().sendKeys(`${propertyName}: ${propertyNewValue}`).sendKeys(Key.ENTER).perform();
        await this.driverHelper.getAction().sendKeys(Key.chord(Key.SHIFT, Key.HOME)).sendKeys(Key.DELETE).sendKeys(Key.chord(Key.SHIFT, Key.TAB)).perform();
        await this.driverHelper.getAction().sendKeys(Key.END).sendKeys(Key.chord(Key.SHIFT, Key.HOME)).sendKeys(Key.DELETE).perform();
    }

    async clickOnCreateCheClusterButton() {
        Logger.debug('OcpWebConsolePage.clickOnCreateCheClusterButton');

        const createCheClusterButtonLocator: By = By.id('save-changes');
        await this.driverHelper.waitAndClick(createCheClusterButtonLocator);
    }

    async waitResourcesCheClusterTitle() {
        Logger.debug('OcpWebConsolePage.waitResourcesCheClusterTitle');

        const resourcesCheClusterTitleLocator: By = By.xpath('//*[contains(text(), \'Clusters\')]');
        await this.driverHelper.waitVisibility(resourcesCheClusterTitleLocator);
    }

    async clickOnCheClusterResourcesName() {
        Logger.debug('OcpWebConsolePage.clickOnCheClusterResourcesName');

        const cheClusterResourcesNameLocator: By = By.css('a[class^=co-resource-item]');
        await this.driverHelper.waitAndClick(cheClusterResourcesNameLocator);
    }

    async waitKeycloakAdminConsoleUrl() {
        Logger.debug('OcpWebConsolePage.waitKeycloakAdminConsoleUrl');

        const keyCloakAdminWebConsoleUrl: By = By.partialLinkText(`keycloak-${TestConstants.TS_SELENIUM_INSTALL_PROJECT_NAME}`);
        await this.driverHelper.waitVisibility(keyCloakAdminWebConsoleUrl, TestConstants.TS_SELENIUM_INSTALL_ECLIPSE_CHE_TIMEOUT);
    }

    async waitInstalledAppUrl() {
        Logger.debug('OcpWebConsolePage.waitInstalledAppUrl');

        const installedAppUrlLocator: By = By.partialLinkText(`${OcpWebConsolePage.INSTALLED_APP_URL_XPATH}`);
        await this.driverHelper.waitVisibility(installedAppUrlLocator, TestConstants.TS_SELENIUM_INSTALL_ECLIPSE_CHE_TIMEOUT);
    }

    async logoutFromWebConsole() {
        Logger.debug('OcpWebConsolePage.logoutFromWebConsole');

        const userDropdownLocator: By = By.css('*[data-test=user-dropdown]');
        const dropdownMenuLocator: By = By.css('*[role=menu]');
        const dropdownMenuItemLocator: By = By.xpath('//a[text()=\'Log out\']');
        await this.driverHelper.waitAndClick(userDropdownLocator);
        await this.driverHelper.waitVisibility(dropdownMenuLocator);
        await this.driverHelper.waitAndClick(dropdownMenuItemLocator);
    }

}
