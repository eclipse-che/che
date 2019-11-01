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

    constructor(
        @inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) { }

    async waitNavpanelOpenShift() {
        Logger.debug('OcpWebConsolePage.waitNavpanelOpenShift');

        const navPanelOpenShiftLocator: By = By.css('nav[class=pf-c-nav]');
        await this.driverHelper.waitVisibility(navPanelOpenShiftLocator);
    }

    async clickOnCatalogListNavPanelOpenShift() {
        Logger.debug('OcpWebConsolePage.clickOnCatalogListNavPanelOpenShift');

        const catalogListLocator: By = By.xpath('//a[text()=\'Catalog\']');
        await this.driverHelper.waitAndClick(catalogListLocator);
    }

    async clickOnOperatorHubItemNavPanel() {
        Logger.debug('OcpWebConsolePage.clickOnOperatorHubItemNavPanel');

        const operatorHubItemLocator: By = By.xpath('//a[text()=\'OperatorHub\']');
        await this.driverHelper.waitAndClick(operatorHubItemLocator);
    }

    async waitOperatorHubMainPage() {
        Logger.debug('OcpWebConsolePage.waitOperatorHubMainPage');

        const catalogOperatorHubPageLocator: By = By.xpath('//span[@id=\'resource-title\' and text()=\'OperatorHub\']');
        await this.driverHelper.waitVisibility(catalogOperatorHubPageLocator);
    }

    async clickOnCatalogOperatorIcon() {
        Logger.debug('OcpWebConsolePage.clickOnCatalogOperatorIcon');

        const catalogTileOperatorNameCss: string = `${TestConstants.TS_SELENIUM_CATALOG_TILE_OPERATOR_NAME}`;
        const catalogTileOperatorNameLocator: By = By.css(`a[data-test^=${catalogTileOperatorNameCss}]`);
        await this.driverHelper.waitAndClick(catalogTileOperatorNameLocator, TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT);
    }
    async clickOnInstallButton() {
        Logger.debug('OcpWebConsolePage.clickOnInstallButton');

        const installOperatorButtonLocator: By = By.xpath('//button[text()=\'Install\']');
        await this.driverHelper.waitAndClick(installOperatorButtonLocator);
    }

    async waitCreateOperatorSubscriptionPage() {
        Logger.debug('OcpWebConsolePage.waitCreateOperatorSubscriptionPage');

        const createOperatorSubscriptionPageLocator: By = By.xpath('//h1[text()=\'Create Operator Subscription\']');
        await this.driverHelper.waitVisibility(createOperatorSubscriptionPageLocator);
    }

    async selectUpdateChannelOnSubscriptionPage() {
        Logger.debug('OcpWebConsolePage.selectUpdateChannelOnSubscriptionPage');

        const updateChannelOperatorLocator: By = By.css(`input[value=${TestConstants.TS_OCP_OPERATOR_UPDATE_CHANNEL}]`);
        await this.driverHelper.waitAndClick(updateChannelOperatorLocator);
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

    async waitSubscriptionOverviewPage() {
        Logger.debug('OcpWebConsolePage.waitSubscriptionOverviewPage');

        const subscriptionOverviewPageLocator: By = By.xpath('//h2[text()=\'Subscription Overview\']');
        await this.driverHelper.waitVisibility(subscriptionOverviewPageLocator);
    }

    async waitChannelNameOnSubscriptionOverviewPage() {
        Logger.debug('OcpWebConsolePage.waitChannelNameOnSubscriptionOverviewPage');

        const channelNameOnSubscriptionOverviewLocator: By = By.xpath(`//button[@type='button' and text()='${TestConstants.TS_OCP_OPERATOR_UPDATE_CHANNEL}']`);
        await this.driverHelper.waitVisibility(channelNameOnSubscriptionOverviewLocator);
    }

    async waitUpgradeStatusOnSubscriptionOverviewPage() {
        Logger.debug('OcpWebConsolePage.waitUpgradeStatusOnSubscriptionOverviewPage');

        const upgradeStatuslocator: By = By.xpath('//span[text()=\' Up to date\']');
        await this.driverHelper.waitVisibility(upgradeStatuslocator, TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT);
    }

    async waitCatalogSourceNameOnSubscriptionOverviewPage() {
        Logger.debug('OcpWebConsolePage.waitCatalogSourceNameOnSubscriptionOverviewPage');

        const catalogSourceNameCss: string = `${TestConstants.TS_SELENIUM_CATALOG_SOURCE_NAME}-${TestConstants.TS_SELENIUM_INSTALL_PROJECT_NAME}`;
        const catalogSourceNameLolcator: By = By.css(`a[title='${catalogSourceNameCss}']`);
        await this.driverHelper.waitVisibility(catalogSourceNameLolcator);
    }

    async selectInstalledOperatorsOnNavPanel() {
        Logger.debug('OcpWebConsolePage.selectInstalledOperatorsOnNavPanel');

        const installedOperatorsItemNavPanelLocator: By = By.xpath('//a[text()=\'Installed Operators\']');
        await this.driverHelper.waitAndClick(installedOperatorsItemNavPanelLocator);
    }

    async waitInstalledOperatorLogoName() {
        Logger.debug('OcpWebConsolePage.waitInstalledOperatorLogoName');

        await this.driverHelper.waitVisibility(By.xpath(OcpWebConsolePage.OPERATOR_LOGO_NAME_XPATH));
    }

    async waitStatusInstalledOperator() {
        Logger.debug('OcpWebConsolePage.waitStatusInstalledOperator');

        const statusInstalledOperatorLocator: By = By.xpath('//span[text()=\'InstallSucceeded\']');
        await this.driverHelper.waitVisibility(statusInstalledOperatorLocator, TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT);
    }

    async clickOnInstalledOperatorLogoName() {
        Logger.debug('OcpWebConsolePage.clickOnInstalledOperatorLogoName');

        await this.driverHelper.waitAndClick(By.xpath(OcpWebConsolePage.OPERATOR_LOGO_NAME_XPATH));
    }

    async waitOverviewCsvEclipseCheOperator() {
        Logger.debug('OcpWebConsolePage.waitOverviewCsvEclipseCheOperator');

        await this.driverHelper.waitVisibility(By.xpath(OcpWebConsolePage.OPERATOR_LOGO_NAME_XPATH));
    }

    async clickCreateNewCheClusterLink() {
        Logger.debug('OcpWebConsolePage.clickCreateNewCheClusterLink');

        const createNewCheLusterLinkLocator: By = By.xpath('//a[text()=\' Create New\']');
        await this.driverHelper.waitAndClick(createNewCheLusterLinkLocator);
    }

    async waitCreateCheClusterYaml() {
        Logger.debug('OcpWebConsolePage.waitCreateCheClusterYaml');

        const createCheClusterYamlLocator: By = By.xpath('//h1[text()=\'Create Che Cluster\']');
        await this.driverHelper.waitVisibility(createCheClusterYamlLocator);
    }

    async selectOpenShiftOAuthFieldInYaml() {
        Logger.debug('OcpWebConsolePage.selectOpenShiftOAuthFieldInYaml');

        const openShiftOAuthLineLocator: By = By.xpath(`//div[@class=\'ace_gutter-cell \' and text()=\'${TestConstants.TS_SELENIUM_OPENSHIFT_OAUTH_FIELD_LINE}\']`);
        await this.driverHelper.waitAndClick(openShiftOAuthLineLocator);
    }

    async setValueOpenShiftOAuthField() {
        Logger.debug('OcpWebConsolePage.setValueOpenShiftOAuthField');

        const openShiftOAuthEditorLocator: By = By.css('textarea[class=\'ace_text-input\']');
        await this.driverHelper.clearInvisible(openShiftOAuthEditorLocator);
        await this.driverHelper.typeToInvisible(openShiftOAuthEditorLocator, `    openShiftoAuth: ${TestConstants.TS_SELENIUM_VALUE_OPENSHIFT_OAUTH}`);
        await this.driverHelper.typeToInvisible(openShiftOAuthEditorLocator, Key.ENTER);
    }

    async clickOnCreateCheClusterButton() {
        Logger.debug('OcpWebConsolePage.clickOnCreateCheClusterButton');

        const createCheClusterButtonLocator: By = By.id('save-changes');
        await this.driverHelper.waitAndClick(createCheClusterButtonLocator);
    }

    async waitResourcesCheClusterTitle() {
        Logger.debug('OcpWebConsolePage.waitResourcesCheClusterTitle');

        const resourcesCheClusterTitleLocator: By = By.id('resource-title');
        await this.driverHelper.waitVisibility(resourcesCheClusterTitleLocator);
    }

    async waitResourcesCheClusterTimestamp() {
        Logger.debug('OcpWebConsolePage.waitResourcesCheClusterTimestamp');

        const resourcesCheClusterTimestampLocator: By = By.xpath('//div[contains(@class, \'timestamp\')]/div[text()=\'a minute ago\']');
        await this.driverHelper.waitVisibility(resourcesCheClusterTimestampLocator, TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT);
    }

    async clickOnCheClusterResourcesName() {
        Logger.debug('OcpWebConsolePage.clickOnCheClusterResourcesName');

        const cheClusterResourcesNameLocator: By = By.css('a[class=co-resource-item__resource-name]');
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

    async clickOnInstalledAppUrl() {
        Logger.debug('OcpWebConsolePage.clickOnInstalledAppUrl');

        const installedAppUrlLocator: By = By.partialLinkText(`${OcpWebConsolePage.INSTALLED_APP_URL_XPATH}`);
        await this.driverHelper.waitAndClick(installedAppUrlLocator);
    }
}
