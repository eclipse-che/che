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
import { By } from 'selenium-webdriver';

@injectable()
export class OpenShiftConsole4x {

    private static readonly CHE_OPERATOR_LOGO_NAME: string = '//h1[contains(@class, \'logo__name__clusterserviceversion\') and text()=\'Eclipse Che\']';

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
        await this.driverHelper.waitAndClick(By.xpath('//a[contains(@data-test, \'eclipse-che\')]'));
    }
    async clickOnInstallEclipseCheButton () {
        await this.driverHelper.waitAndClick(By.xpath('//button[text()=\'Install\']'));
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
        await this.driverHelper.waitVisibility(By.xpath(OpenShiftConsole4x.CHE_OPERATOR_LOGO_NAME));
    }

    async waitStatusInstalledEclipseCheOperator () {
        await this.driverHelper.waitVisibility(By.xpath('//div[@class=\'row co-resource-list__item\']//span[text()=\'InstallSucceeded\']'));
    }

    async clickOnEclipseCheOperatorLogoName () {
        await this.driverHelper.waitAndClick(By.xpath(OpenShiftConsole4x.CHE_OPERATOR_LOGO_NAME));
    }
}
