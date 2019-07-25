/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { e2eContainer } from '../../inversify.config';
import { ILoginPage } from '../../pageobjects/login/ILoginPage';
import { DriverHelper } from '../../utils/DriverHelper';
import { CLASSES, TYPES } from '../../inversify.types';
import { TestConstants } from '../../TestConstants';
import { By, Key } from 'selenium-webdriver';
import { Dashboard } from '../../pageobjects/dashboard/Dashboard';
import { OpenShiftLoginPage } from '../../pageobjects/openshift/OpenShiftLoginPage';
import { OpenShiftConsole4x } from '../../pageobjects/openshift/OpenShiftConsole4x';

const loginPage: ILoginPage = e2eContainer.get<ILoginPage>(TYPES.LoginPage);
const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
const openShiftLogin: OpenShiftLoginPage = e2eContainer.get(CLASSES.OpenShiftLoginPage);
const openShiftConsole: OpenShiftConsole4x = e2eContainer.get(CLASSES.OpenShiftConsole4x);
const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
const projectName: string = TestConstants.TS_INSTALL_CHE_PROJECT_NAME;

const overviewCsvCheOperatorLocator: By = By.xpath('//h2[@class=\'co-section-heading\' and text()=\'ClusterServiceVersion Overview\']');
const createNewCheClusterLinkLocator: By = By.xpath('//div[contains(@class, \'ClusterServiceVersion\')]//a[text()=\' Create New\']');
const createCheClusterYamlLocator: By = By.xpath('//h1[text()=\'Create Che Cluster\']');
const fieldOpenShiftOAuthLocator: By = By.xpath('//div[@class=\'ace_gutter-cell \' and text()=\'19\']');
const createCheClusterButtonLocator: By = By.xpath('//button[@id=\'save-changes\' and text()=\'Create\']');
const resourceCheClustersTitleLocator: By = By.xpath('//span[@id=\'resource-title\' and text()=\'Che Clusters\']');
const resourceCheClustersTimestampLocator: By = By.xpath('//div[contains(@class, \'timestamp\')]/div[text()=\'a minute ago\']');
const cheClusterResourceNameLocator: By = By.xpath('//a[contains(@class, \'resource-name\') and text()=\'eclipse-che\']');
const cheClusterOverviewExpandButtonLocator: By = By.xpath('//label[@class=\'btn compaction-btn btn-default\']');
const keycloakAdminUrlLocator: By = By.partialLinkText(`keycloak-${projectName}`);
const eclipseCheUrlLocator: By = By.partialLinkText(`che-${projectName}`);

suite('E2E', async () => {

    suite('Go to OCP 4.x and wait console OpenShift', async () => {
        test('Open login page', async () => {
            await openShiftLogin.openLoginPageOpenShift();
            await openShiftLogin.waitOpenShiftLoginPage();
            await openShiftLogin.clickOnLoginWitnKubeAdmin();
        });
        test('Log into OCP 4.x', async () => {
            await openShiftLogin.enterUserNameOpenShift(TestConstants.TS_SELENIUM_OPENSHIFT4_USERNAME);
            await openShiftLogin.enterPasswordOpenShift(TestConstants.TS_SELENIUM_OPENSHIFT4_PASSWORD);
            await openShiftLogin.clickOnLoginButton();
            await openShiftLogin.waitDisappearanceLoginPageOpenShift();
        });
    });

    suite('Subscribe Eclipse Che Operator to defined namespace', async () => {
        test('Open Catalog, select OperatorHub', async () => {
            await openShiftConsole.waitNavpanelOpenShift();
            await openShiftConsole.clickOnCatalogListNavPanelOpenShift();
            await openShiftConsole.clickOnOperatorHubItemNavPanel();
            await openShiftConsole.waitOperatorHubMainPage();
        });
        test('Select eclipse Che Operator and install it', async () => {
            await openShiftConsole.clickOnEclipseCheOperatorIcon();
            await openShiftConsole.clickOnInstallEclipseCheButton();
        });
        test('Select a namespace and subscribe Eclipse Che Operator', async () => {
            await openShiftConsole.waitCreateOperatorSubscriptionPage();
            await openShiftConsole.clickOnDropdownNamespaceListOnSubscriptionPage();
            await openShiftConsole.waitListBoxNamespacesOnSubscriptionPage();
            await openShiftConsole.selectDefinedNamespaceOnSubscriptionPage(projectName);
            await openShiftConsole.clickOnSubscribeButtonOnSubscriptionPage();
        });
        test('Wait the Subscription Overview', async () => {
            await openShiftConsole.waitSubscriptionOverviewPage();
            await openShiftConsole.waitUpgradeStatusOnSubscriptionOverviewPage();
            await openShiftConsole.waitCatalogSourceNameOnSubscriptionOverviewPage(projectName);
        });
    });

    suite('Wait the Eclipse Che operator is represented by CSV', async () => {
        test('Select the Installed Operators in the nav panel', async () => {
            await openShiftConsole.selectInstalledOperatorsOnNavPanel();
        });
        test('Wait installed Eclipse Che operator', async () => {
            await openShiftConsole.waitEclipseCheOperatorLogoName();
            await openShiftConsole.waitStatusInstalledEclipseCheOperator();
        });
    });

    suite('Create new Eclipse Che cluster', async () => {
        test('Click on the logo-name Eclipse Che operator', async () => {
            await openShiftConsole.clickOnEclipseCheOperatorLogoName();
            await driverHelper.waitVisibility(overviewCsvCheOperatorLocator);
        });
        test('Click on the Create New, wait CSV yaml', async () => {
            await driverHelper.waitAndClick(createNewCheClusterLinkLocator);
            await driverHelper.waitVisibility(createCheClusterYamlLocator);
        });
        test('Change value of OpenShiftOauth field', async () => {
            await driverHelper.waitAndClick(fieldOpenShiftOAuthLocator);
            await driverHelper.getAction().sendKeys(Key.DELETE.toString()).sendKeys(Key.ENTER.toString()).sendKeys(Key.UP.toString()).perform();
            await driverHelper.getAction().sendKeys('    openShiftoAuth: false');
        });
        test('Create Che Cluster ', async () => {
            await driverHelper.waitAndClick(createCheClusterButtonLocator);
            await driverHelper.waitVisibility(resourceCheClustersTitleLocator);
            await driverHelper.waitVisibility(resourceCheClustersTimestampLocator, TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT);
            await driverHelper.waitAndClick(cheClusterResourceNameLocator);
        });
    });

    suite('Check the Eclipse Che is ready', async () => {
        test('Wait Keycloak Admin Console URL', async () => {
            await driverHelper.waitAndClick(cheClusterOverviewExpandButtonLocator);
            await driverHelper.waitVisibility(keycloakAdminUrlLocator, TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT);
        });
        test('Wait Eclipse Che URL', async () => {
            await driverHelper.waitVisibility(eclipseCheUrlLocator, TestConstants.TS_SELENIUM_START_WORKSPACE_TIMEOUT);
        });
    });

    suite('Log into Eclipse Che', async () => {
        test('Click on the Eclipse Che URL ', async () => {
            await driverHelper.waitAndClick(eclipseCheUrlLocator);
        });
        test('Login to Eclipse Che', async () => {
            await loginPage.login();
        });
        test('Wait Eclipse Che dashboard', async () => {
            await dashboard.waitPage();
        });
    });
});
