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
import { ILoginPageOcp } from '../../pageobjects/openshift/ILoginPageOcp';
import { CLASSES, TYPES } from '../../inversify.types';
import { TestConstants } from '../../TestConstants';
import { Dashboard } from '../../pageobjects/dashboard/Dashboard';
import { OpenShiftLoginPage } from '../../pageobjects/openshift/OpenShiftLoginPage';
import { OpenShiftConsole4x } from '../../pageobjects/openshift/OpenShiftConsole4x';

const loginPage: ILoginPage = e2eContainer.get<ILoginPage>(TYPES.LoginPage);
const ocpLoginPage: ILoginPageOcp = e2eContainer.get<ILoginPageOcp>(TYPES.OcpLoginPage);
const openShiftLogin: OpenShiftLoginPage = e2eContainer.get(CLASSES.OpenShiftLoginPage);
const openShiftConsole: OpenShiftConsole4x = e2eContainer.get(CLASSES.OpenShiftConsole4x);
const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
const projectName: string = TestConstants.TS_INSTALL_CHE_PROJECT_NAME;
const channelName = TestConstants.TS_OCP_UPDATE_CHANNEL_OPERATOR;

suite('E2E', async () => {

    suite('Go to OCP 4.x and wait console OpenShift', async () => {
        test('Open login page', async () => {
            await openShiftLogin.openLoginPageOpenShift();
            await openShiftLogin.waitOpenShiftLoginPage();
        });
        test('Log into OCP 4.x', async () => {
            ocpLoginPage.login();
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
            await openShiftConsole.selectUpdateChannelOnSubscriptionPage(channelName);
            await openShiftConsole.clickOnDropdownNamespaceListOnSubscriptionPage();
            await openShiftConsole.waitListBoxNamespacesOnSubscriptionPage();
            await openShiftConsole.selectDefinedNamespaceOnSubscriptionPage(projectName);
            await openShiftConsole.clickOnSubscribeButtonOnSubscriptionPage();
        });
        test('Wait the Subscription Overview', async () => {
            await openShiftConsole.waitSubscriptionOverviewPage();
            await openShiftConsole.waitChannelNameOnSubscriptionOverviewPage(channelName);
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
            await openShiftConsole.waitOverviewCsvEclipseCheOperator();
        });
        test('Click on the Create New, wait CSV yaml', async () => {
            await openShiftConsole.clickCreateNewCheClusterLink();
            await openShiftConsole.waitCreateCheClusterYaml();
        });
        test('Change value of OpenShiftOauth field', async () => {
            await openShiftConsole.selectOpenShiftOAuthFieldInYaml();
            await openShiftConsole.changeValueOpenShiftOAuthField();
        });
        test('Create Che Cluster ', async () => {
            await openShiftConsole.clickOnCreateCheClusterButton();
            await openShiftConsole.waitResourcesCheClusterTitle();
            await openShiftConsole.waitResourcesCheClusterTimestamp();
            await openShiftConsole.clickOnCheClusterResourcesName();
        });
    });

    suite('Check the Eclipse Che is ready', async () => {
        test('Wait Keycloak Admin Console URL', async () => {
            await openShiftConsole.clickCheClusterOverviewExpandButton();
            await openShiftConsole.waitKeycloakAdminConsoleUrl(projectName);
        });
        test('Wait Eclipse Che URL', async () => {
            await openShiftConsole.waitEclipseCheUrl(projectName);
        });
    });

    suite('Log into Eclipse Che', async () => {
        test('Click on the Eclipse Che URL ', async () => {
            await openShiftConsole.clickOnEclipseCHeUrl(projectName);
        });
        test('Login to Eclipse Che', async () => {
            await loginPage.login();
        });
        test('Wait Eclipse Che dashboard', async () => {
            await dashboard.waitPage();
        });
    });
});
