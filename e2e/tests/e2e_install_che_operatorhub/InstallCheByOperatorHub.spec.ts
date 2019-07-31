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
import { ICheLoginPage } from '../../pageobjects/login/ICheLoginPage';
import { IOcpLoginPage } from '../../pageobjects/login/IOcpLoginPage';
import { CLASSES, TYPES } from '../../inversify.types';
import { TestConstants } from '../../TestConstants';
import { Dashboard } from '../../pageobjects/dashboard/Dashboard';
import { OcpLoginPage } from '../../pageobjects/openshift/OcpLoginPage';
import { OcpWebConsolePage } from '../../pageobjects/openshift/OcpWebConsolePage';

const cheLogin: ICheLoginPage = e2eContainer.get<ICheLoginPage>(TYPES.CheLogin);
const ocpLogin: IOcpLoginPage = e2eContainer.get<IOcpLoginPage>(TYPES.OcpLogin);
const ocpLoginPage: OcpLoginPage = e2eContainer.get(CLASSES.OcpLoginPage);
const ocpWebConsole: OcpWebConsolePage = e2eContainer.get(CLASSES.OcpWebConsolePage);
const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
const projectName: string = TestConstants.TS_INSTALL_CHE_PROJECT_NAME;
const channelName = TestConstants.TS_OCP_UPDATE_CHANNEL_OPERATOR;
const openShiftOAuthLine = '21';

suite('E2E', async () => {

    suite('Go to OCP and wait console OpenShift', async () => {
        test('Open login page', async () => {
            await ocpLoginPage.openLoginPageOpenShift();
            await ocpLoginPage.waitOpenShiftLoginPage();
        });
        test('Log into OCP', async () => {
            ocpLogin.login();
        });
    });

    suite('Subscribe Eclipse Che Operator to defined namespace', async () => {
        test('Open Catalog, select OperatorHub', async () => {
            await ocpWebConsole.waitNavpanelOpenShift();
            await ocpWebConsole.clickOnCatalogListNavPanelOpenShift();
            await ocpWebConsole.clickOnOperatorHubItemNavPanel();
            await ocpWebConsole.waitOperatorHubMainPage();
        });


        test('Select eclipse Che Operator and install it', async () => {
            await ocpWebConsole.clickOnEclipseCheOperatorIcon();
            await ocpWebConsole.clickOnInstallEclipseCheButton();
        });

        test('Select a namespace and subscribe Eclipse Che Operator', async () => {
            await ocpWebConsole.waitCreateOperatorSubscriptionPage();
            await ocpWebConsole.selectUpdateChannelOnSubscriptionPage(channelName);
            await ocpWebConsole.clickOnDropdownNamespaceListOnSubscriptionPage();
            await ocpWebConsole.waitListBoxNamespacesOnSubscriptionPage();
            await ocpWebConsole.selectDefinedNamespaceOnSubscriptionPage(projectName);
            await ocpWebConsole.clickOnSubscribeButtonOnSubscriptionPage();
        });

        test('Wait the Subscription Overview', async () => {
            await ocpWebConsole.waitSubscriptionOverviewPage();
            await ocpWebConsole.waitChannelNameOnSubscriptionOverviewPage(channelName);
            await ocpWebConsole.waitUpgradeStatusOnSubscriptionOverviewPage();
            await ocpWebConsole.waitCatalogSourceNameOnSubscriptionOverviewPage(projectName);
        });
    });

    suite('Wait the Eclipse Che operator is represented by CSV', async () => {
        test('Select the Installed Operators in the nav panel', async () => {
            await ocpWebConsole.selectInstalledOperatorsOnNavPanel();
        });

        test('Wait installed Eclipse Che operator', async () => {
            await ocpWebConsole.waitEclipseCheOperatorLogoName();
            await ocpWebConsole.waitStatusInstalledEclipseCheOperator();
        });
    });

    suite('Create new Eclipse Che cluster', async () => {
        test('Click on the logo-name Eclipse Che operator', async () => {
            await ocpWebConsole.clickOnEclipseCheOperatorLogoName();
            await ocpWebConsole.waitOverviewCsvEclipseCheOperator();
        });


        test('Click on the Create New, wait CSV yaml', async () => {
            await ocpWebConsole.clickCreateNewCheClusterLink();
            await ocpWebConsole.waitCreateCheClusterYaml();
        });

        test('Change value of OpenShiftOauth field', async () => {
            await ocpWebConsole.selectOpenShiftOAuthFieldInYaml(openShiftOAuthLine);
            await ocpWebConsole.changeValueOpenShiftOAuthField();
        });

        test('Create Che Cluster ', async () => {
            await ocpWebConsole.clickOnCreateCheClusterButton();
            await ocpWebConsole.waitResourcesCheClusterTitle();
            await ocpWebConsole.waitResourcesCheClusterTimestamp();
            await ocpWebConsole.clickOnCheClusterResourcesName();
        });
    });

    suite('Check the Eclipse Che is ready', async () => {
        test('Wait Keycloak Admin Console URL', async () => {
            await ocpWebConsole.clickCheClusterOverviewExpandButton();
            await ocpWebConsole.waitKeycloakAdminConsoleUrl(projectName);
        });

        test('Wait Eclipse Che URL', async () => {
            await ocpWebConsole.waitEclipseCheUrl(projectName);
        });
    });

    suite('Log into Eclipse Che', async () => {
        test('Click on the Eclipse Che URL ', async () => {
            await ocpWebConsole.clickOnEclipseCHeUrl(projectName);
        });

        test('Login to Eclipse Che', async () => {
            await cheLogin.login();
        });

        test('Wait Eclipse Che dashboard', async () => {
            await dashboard.waitPage();
        });
    });
});
