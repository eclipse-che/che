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

@injectable()
export class OcpWebConsolePage {

    private static readonly CHE_OPERATOR_LOGO_NAME: string = '//h1[contains(@class, \'logo__name__clusterserviceversion\') and text()=\'Eclipse Che\']';
    private static readonly ECLIPSE_CHE_PREFIX_URL: string = 'che-';

    constructor(
        @inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) { }

    async waitNavpanelOpenShift () {
        await this.driverHelper.waitVisibility(By.xpath('//nav[@class=\'pf-c-nav\']'));
    }

    async clickOnCatalogListNavPanelOpenShift () {
        await this.driverHelper.waitAndClick(By.xpath('//a[text()=\'Catalog\']'));
    }

    async clickOnOperatorHubItemNavPanel () {
        await this.driverHelper.waitAndClick(By.xpath('//a[text()=\'OperatorHub\']'));
    }

    async waitOperatorHubMainPage () {
        await this.driverHelper.waitVisibility(By.xpath('//span[@id=\'resource-title\' and text()=\'OperatorHub\']'));
    }

    async clickOnEclipseCheOperatorIcon () {
        await this.driverHelper.waitAndClick(By.xpath('//a[contains(@data-test, \'eclipse-che-preview\')]'));
    }
    async clickOnInstallEclipseCheButton () {
        await this.driverHelper.waitAndClick(By.xpath('//button[text()=\'Install\']'));
    }

    async selectStableUpdateChannel () {
        await this.driverHelper.waitAndClick(By.xpath('//input[@type=\'radio\' and @value=\'stable\']'));
    }

    async selectNightltyUpdateChannel () {
        await this.driverHelper.waitAndClick(By.xpath('//input[@type=\'radio\' and @value=\'nightly\']'));
    }

    async selectUpdateChannelOnSubscriptionPage (channelName: string) {
        await this.driverHelper.waitAndClick(By.xpath(`//input[@type=\'radio\' and @value=\'${channelName}\']`));
    }

    async waitCreateOperatorSubscriptionPage () {
        await this.driverHelper.waitVisibility(By.xpath('//h1[text()=\'Create Operator Subscription\']'));
    }

    async clickOnDropdownNamespaceListOnSubscriptionPage () {
        await this.driverHelper.waitAndClick(By.xpath('//button[@id=\'dropdown-selectbox\']'));
    }

    async waitListBoxNamespacesOnSubscriptionPage () {
        await this.driverHelper.waitVisibility(By.xpath('//ul[@role=\'listbox\' and contains(@class, \'dropdown-menu\')]'));
    }

    async selectDefinedNamespaceOnSubscriptionPage (projectName: string) {
        await this.driverHelper.waitAndClick(By.xpath(`//a[@id=\'${projectName}-Project-link\']`));
    }

    async clickOnSubscribeButtonOnSubscriptionPage () {
        await this.driverHelper.waitAndClick(By.xpath('//button[text()=\'Subscribe\']'));
    }

    async waitSubscriptionOverviewPage () {
        await this.driverHelper.waitVisibility(By.xpath('//h2[text()=\'Subscription Overview\']'));
    }

    async waitChannelNameOnSubscriptionOverviewPage (channelName: string) {
        await this.driverHelper.waitVisibility(By.xpath(`//button[contains(@class, 'btn-link') and text()='${channelName}']`));
    }

    async waitUpgradeStatusOnSubscriptionOverviewPage () {
        await this.driverHelper.waitVisibility(By.xpath('//span[text()=\' Up to date\']'), TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT);
    }

    async waitCatalogSourceNameOnSubscriptionOverviewPage (projectName: string) {
        await this.driverHelper.waitVisibility(By.xpath(`//a[contains(@title, \'${projectName}\')]`));
    }

    async selectInstalledOperatorsOnNavPanel () {
        await this.driverHelper.waitAndClick(By.xpath('//a[contains(@class, \'pf-c-nav\') and text()=\'Installed Operators\']'));
    }

    async waitEclipseCheOperatorLogoName () {
        await this.driverHelper.waitVisibility(By.xpath(OcpWebConsolePage.CHE_OPERATOR_LOGO_NAME));
    }

    async waitStatusInstalledEclipseCheOperator () {
        await this.driverHelper.waitVisibility(By.xpath('//div[@class=\'row co-resource-list__item\']//span[text()=\'InstallSucceeded\']'), TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT);
    }

    async clickOnEclipseCheOperatorLogoName () {
        await this.driverHelper.waitAndClick(By.xpath(OcpWebConsolePage.CHE_OPERATOR_LOGO_NAME));
    }

    async waitOverviewCsvEclipseCheOperator () {
        await this.driverHelper.waitVisibility(By.xpath('//h2[@class=\'co-section-heading\' and text()=\'ClusterServiceVersion Overview\']'));
    }

    async clickCreateNewCheClusterLink () {
        await this.driverHelper.waitAndClick(By.xpath('//div[contains(@class, \'ClusterServiceVersion\')]//a[text()=\' Create New\']'));
    }

    async waitCreateCheClusterYaml () {
        await this.driverHelper.waitVisibility(By.xpath('//h1[text()=\'Create Che Cluster\']'));
    }

    async selectOpenShiftOAuthFieldInYaml () {
        await this.driverHelper.waitAndClick(By.xpath(`//div[@class=\'ace_gutter-cell \' and text()=\'21\']`));
    }

    async changeValueOpenShiftOAuthField () {
        await this.driverHelper.getAction().sendKeys(Key.DELETE.toString()).sendKeys(Key.ENTER.toString()).sendKeys(Key.UP.toString()).perform();
        await this.driverHelper.getAction().sendKeys('    openShiftoAuth: false');
    }

    async clickOnCreateCheClusterButton () {
        await this.driverHelper.waitAndClick(By.xpath('//button[@id=\'save-changes\' and text()=\'Create\']'));
    }

    async waitResourcesCheClusterTitle () {
        await this.driverHelper.waitVisibility(By.xpath('//span[@id=\'resource-title\' and text()=\'Che Clusters\']'));
    }

    async waitResourcesCheClusterTimestamp () {
        await this.driverHelper.waitVisibility(By.xpath('//div[contains(@class, \'timestamp\')]/div[text()=\'a minute ago\']'), TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT);
    }
    async clickOnCheClusterResourcesName () {
        await this.driverHelper.waitAndClick(By.xpath('//a[contains(@class, \'resource-name\') and text()=\'eclipse-che\']'));
    }

    async clickCheClusterOverviewExpandButton () {
        await this.driverHelper.waitAndClick(By.xpath('//label[@class=\'btn compaction-btn btn-default\']'));
    }

    async waitKeycloakAdminConsoleUrl (projectName: string) {
        await this.driverHelper.waitVisibility(By.partialLinkText(`keycloak-${projectName}`), TestConstants.TS_SELENIUM_START_WORKSPACE_TIMEOUT);
    }

    async waitEclipseCheUrl (projectName: string) {
        await this.driverHelper.waitVisibility(By.partialLinkText(`${OcpWebConsolePage.ECLIPSE_CHE_PREFIX_URL}${projectName}`), TestConstants.TS_SELENIUM_START_WORKSPACE_TIMEOUT);
    }

    async clickOnEclipseCHeUrl (projectName: string) {
        await this.driverHelper.waitAndClick(By.partialLinkText(`${OcpWebConsolePage.ECLIPSE_CHE_PREFIX_URL}${projectName}`));
    }
}
