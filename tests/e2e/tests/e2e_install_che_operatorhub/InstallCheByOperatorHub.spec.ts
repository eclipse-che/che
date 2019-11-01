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
import { Dashboard } from '../../pageobjects/dashboard/Dashboard';
import { OcpLoginPage } from '../../pageobjects/openshift/OcpLoginPage';
import { OcpWebConsolePage } from '../../pageobjects/openshift/OcpWebConsolePage';

const cheLogin: ICheLoginPage = e2eContainer.get<ICheLoginPage>(TYPES.CheLogin);
const ocpLogin: IOcpLoginPage = e2eContainer.get<IOcpLoginPage>(TYPES.OcpLogin);
const ocpLoginPage: OcpLoginPage = e2eContainer.get(CLASSES.OcpLoginPage);
const ocpWebConsole: OcpWebConsolePage = e2eContainer.get(CLASSES.OcpWebConsolePage);
const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);

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

    suite('Subscribe Operator to defined namespace', async () => {
        test('Open Catalog, select OperatorHub', async () => {
            await ocpWebConsole.waitNavpanelOpenShift();
            await ocpWebConsole.clickOnCatalogListNavPanelOpenShift();
            await ocpWebConsole.clickOnOperatorHubItemNavPanel();
            await ocpWebConsole.waitOperatorHubMainPage();
        });


        test('Select Operator from catalog and install it', async () => {
            await ocpWebConsole.clickOnCatalogOperatorIcon();
            await ocpWebConsole.clickOnInstallButton();
        });

        test('Select a namespace and subscribe Operator', async () => {
            await ocpWebConsole.waitCreateOperatorSubscriptionPage();
            await ocpWebConsole.selectUpdateChannelOnSubscriptionPage();
            await ocpWebConsole.clickOnDropdownNamespaceListOnSubscriptionPage();
            await ocpWebConsole.waitListBoxNamespacesOnSubscriptionPage();
            await ocpWebConsole.selectDefinedNamespaceOnSubscriptionPage();
            await ocpWebConsole.clickOnSubscribeButtonOnSubscriptionPage();
        });

        test('Wait the Subscription Overview', async () => {
            await ocpWebConsole.waitSubscriptionOverviewPage();
            await ocpWebConsole.waitChannelNameOnSubscriptionOverviewPage();
            await ocpWebConsole.waitUpgradeStatusOnSubscriptionOverviewPage();
            await ocpWebConsole.waitCatalogSourceNameOnSubscriptionOverviewPage();
        });
    });

    suite('Wait the operator is represented by CSV', async () => {
        test('Select the Installed Operators in the nav panel', async () => {
            await ocpWebConsole.selectInstalledOperatorsOnNavPanel();
        });

        test('Wait installed Operator', async () => {
            await ocpWebConsole.waitInstalledOperatorLogoName();
            await ocpWebConsole.waitStatusInstalledOperator();
        });
    });

    suite('Create new Che cluster', async () => {
        test('Click on the logo-name Che operator', async () => {
            await ocpWebConsole.clickOnInstalledOperatorLogoName();
            await ocpWebConsole.waitOverviewCsvEclipseCheOperator();
        });


        test('Click on the Create New, wait CSV yaml', async () => {
            await ocpWebConsole.clickCreateNewCheClusterLink();
            await ocpWebConsole.waitCreateCheClusterYaml();
        });

        test('Set value of OpenShiftOauth field', async () => {
            await ocpWebConsole.selectOpenShiftOAuthFieldInYaml();
            await ocpWebConsole.setValueOpenShiftOAuthField();
        });

        test('Create Che Cluster ', async () => {
            await ocpWebConsole.clickOnCreateCheClusterButton();
            await ocpWebConsole.waitResourcesCheClusterTitle();
            await ocpWebConsole.waitResourcesCheClusterTimestamp();
            await ocpWebConsole.clickOnCheClusterResourcesName();
        });
    });

    suite('Check the Che is ready', async () => {
        test('Wait Keycloak Admin Console URL', async () => {
            await ocpWebConsole.waitKeycloakAdminConsoleUrl();
        });

        test('Wait installed application URL', async () => {
            await ocpWebConsole.waitInstalledAppUrl();
        });
    });

    suite('Log into installed application', async () => {
        test('Click on the insatalled application URL ', async () => {
            await ocpWebConsole.clickOnInstalledAppUrl();
        });

        test('Login to application', async () => {
            await cheLogin.login();
        });

        test('Wait application dashboard', async () => {
            await dashboard.waitPage();
        });
    });
});
