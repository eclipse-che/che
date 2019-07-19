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
import { DriverHelper } from '../../utils/DriverHelper';
import { CLASSES } from '../../inversify.types';
import { TestConstants } from '../../TestConstants';
import { By, Key } from 'selenium-webdriver';
import { Dashboard } from '../../pageobjects/dashboard/Dashboard';

const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);

const openShiftUrl: string = TestConstants.TS_SELENIUM_OPENSHIFT4_URL;
const ocpUserName: string = TestConstants.TS_SELENIUM_OPENSHIFT4_USERNAME;
const ocpPassword: string = TestConstants.TS_SELENIUM_OPENSHIFT4_PASSWORD;
const chUserName: string = TestConstants.TS_SELENIUM_USERNAME;
const chePassw: string = TestConstants.TS_SELENIUM_PASSWORD;
const projectName: string = TestConstants.TS_INSTALL_CHE_PROJECT_NAME;

const loginPageLocator: By = By.xpath('//div[contains(@class, \'login\')]');
const loginWithKubeAdminLocator: By = By.xpath('//a[@title=\'Log in with kube:admin\']');
const loginInputUserNameLocator: By = By.id('inputUsername');
const loginInputPasswordLocator: By = By.id('inputPassword');
const logInButtonLocator: By = By.xpath('//button[text()=\'Log In\']');

const navPanelOpenShiftLocator: By = By.xpath('//nav[@class=\'pf-c-nav\']');
const catalogListLocator: By = By.xpath('//a[text()=\'Catalog\']');
const operatorHubItemLocator: By = By.xpath('//a[text()=\'OperatorHub\']');
const catalogEclipseCheOperatorLocator: By = By.xpath('//a[contains(@data-test, \'eclipse-che\')]');
const installEclipsCheOperatorLocator: By = By.xpath('//button[text()=\'Install\']');

const createOperatorSubscriptionPageLocator: By = By.xpath('//h1[text()=\'Create Operator Subscription\']');
const subscribeOperatorLocator: By = By.xpath('//button[text()=\'Subscribe\']');
const selectNamespaceLocator: By = By.xpath('//button[@id=\'dropdown-selectbox\']');
const listBoxNamespaceLocator: By = By.xpath('//ul[@role=\'listbox\' and contains(@class, \'dropdown-menu\')]');
const namespaceItemInDropDownLocator: By = By.xpath(`//a[@id=\'${projectName}-Project-link\']`);
const subscriptionOverviewPageLocator: By = By.xpath('//h2[text()=\'Subscription Overview\']');
const upgradeStatusLocator: By = By.xpath('//span[text()=\' Up to date\']');
const catalogSourceNameLocator: By = By.xpath(`//a[contains(@title, \'${projectName}\')]`);

const installedOperatorsNavPanelLocator: By = By.xpath('//a[contains(@class, \'pf-c-nav\') and text()=\'Installed Operators\']');
const logoNameLocator: By = By.xpath('//h1[contains(@class, \'logo__name__clusterserviceversion\') and text()=\'Eclipse Che\']');
const statusInstalledCheOperator: By = By.xpath('//div[@class=\'row co-resource-list__item\']//span[text()=\'InstallSucceeded\']');
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

const eclipseCheFormLoginLocator: By = By.id('kc-form-login');
const eclipseCheUsernameInputLocator: By = By.id('username');
const eclipseChePasswordInputLocator: By = By.id('password');
const eclipseCheLoginButtonLocator: By = By.id('kc-login');

suite('E2E', async () => {

    suite('Go to OCP 4.x and wait console OpenShift', async () => {
        test('Open login page', async () => {
            await driverHelper.navigateToUrl(openShiftUrl);
            await driverHelper.waitVisibility(loginPageLocator);
            await driverHelper.waitAndClick(loginWithKubeAdminLocator);
        });
        test('Log into OCP 4.x', async () => {
            await driverHelper.enterValue(loginInputUserNameLocator, ocpUserName);
            await driverHelper.enterValue(loginInputPasswordLocator, ocpPassword);
            await driverHelper.waitAndClick(logInButtonLocator);
            await driverHelper.waitDisappearance(logInButtonLocator);
        });
    });

    suite('Subscribe Eclipse Che Operator to defined namespace', async () => {
        test('Open Catalog, select OperatorHub', async () => {
            await driverHelper.waitVisibility(navPanelOpenShiftLocator);
            await driverHelper.waitAndClick(catalogListLocator);
            await driverHelper.waitAndClick(operatorHubItemLocator);
        });
        test('Select Operator and install it', async () => {
            await driverHelper.waitAndClick(catalogEclipseCheOperatorLocator);
            await driverHelper.waitAndClick(installEclipsCheOperatorLocator);
        });
        test('Select a namespace and subscribe Eclipse Che Operator', async () => {
            await driverHelper.waitVisibility(createOperatorSubscriptionPageLocator);
            await driverHelper.waitAndClick(selectNamespaceLocator);
            await driverHelper.waitVisibility(listBoxNamespaceLocator);
            await driverHelper.waitAndClick(namespaceItemInDropDownLocator);
            await driverHelper.waitAndClick(subscribeOperatorLocator);
        });
        test('Wait the Subscription Overview', async () => {
            await driverHelper.waitVisibility(subscriptionOverviewPageLocator);
            await driverHelper.waitVisibility(upgradeStatusLocator, TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT);
            await driverHelper.waitVisibility(catalogSourceNameLocator);
        });
    });

    suite('Wait the Eclipse Che operator is represented by CSV', async () => {
        test('Select the Installed Operators in the nav panel', async () => {
            await driverHelper.waitAndClick(installedOperatorsNavPanelLocator);
        });
        test('Wait installed Eclipse Che operator', async () => {
            await driverHelper.waitVisibility(logoNameLocator);
            await driverHelper.waitVisibility(statusInstalledCheOperator);
        });
    });

    suite('Create new Eclipse Che cluster', async () => {
        test('Click on the logo-name Eclipse Che operator', async () => {
            await driverHelper.waitAndClick(logoNameLocator);
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
            await driverHelper.waitVisibility(eclipseCheFormLoginLocator);
        });
        test('Login to Eclipse Che', async () => {
            await driverHelper.enterValue(eclipseCheUsernameInputLocator, chUserName);
            await driverHelper.enterValue(eclipseChePasswordInputLocator, chePassw);
            await driverHelper.waitAndClick(eclipseCheLoginButtonLocator);
        });
        test('Wait Eclipse Che dashboard', async () => {
            await dashboard.waitPage();
        });
    });
});
