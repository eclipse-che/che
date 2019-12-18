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
import { TestConstants } from '../../TestConstants';

const cheLogin: ICheLoginPage = e2eContainer.get<ICheLoginPage>(TYPES.CheLogin);
const ocpLogin: IOcpLoginPage = e2eContainer.get<IOcpLoginPage>(TYPES.OcpLogin);
const ocpLoginPage: OcpLoginPage = e2eContainer.get(CLASSES.OcpLoginPage);
const ocpWebConsole: OcpWebConsolePage = e2eContainer.get(CLASSES.OcpWebConsolePage);
const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);

suite('E2E', async () => {

    suite('Go to OCP and wait console OpenShift', async () => {
        test('Open login page', async () => {
            await ocpLoginPage.openLoginPageOpenShift(TestConstants.TS_SELENIUM_WEB_CONSOLE_OCP_URL);
            await ocpLoginPage.waitOpenShiftLoginPage();
        });
        test('Log into OCP', async () => {
            ocpLogin.login();
        });
    });

    suite('Subscribe Operator to defined namespace', async () => {
        test('Go to the OperatorHub main page', async () => {
            await ocpWebConsole.waitNavpanelOpenShift();
            await ocpWebConsole.openOperatorHubMainPageByUrl(TestConstants.TS_SELENIUM_OPERATORHUB_PAGE_URL);
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

    });

    suite('Wait the operator is represented by CSV', async () => {
        test('Select the Installed Operators in the nav panel', async () => {
            await ocpWebConsole.waitNavpanelOpenShift();
            await ocpWebConsole.selectInstalledOperatorsOnNavPanel();
        });

        test('Wait installed Operator', async () => {
            await ocpWebConsole.waitInstalledOperatorLogoName();
        });
    });

    suite('Create new Che cluster', async () => {
        test('Click on the logo-name Che operator', async () => {
            await ocpWebConsole.clickOnInstalledOperatorLogoName();
            await ocpWebConsole.waitOverviewCsvOperator();
        });

        test('Click on the Create New, wait CSV yaml', async () => {
            await ocpWebConsole.clickCreateNewCheClusterLink();
            await ocpWebConsole.waitCreateCheClusterYaml();
        });

        test('Open editor replace widget in the Che Cluster yaml', async () => {
            await ocpWebConsole.openEditorReplaceWidget();
        });

        test('Set value of OpenShiftOauth property', async () => {
            const propertyName = 'openShiftoAuth';
            const propertyDefaultValue = 'true';
            await ocpWebConsole.setValuePropertyInCheClusterYaml(propertyName, propertyDefaultValue, TestConstants.TS_SELENIUM_VALUE_OPENSHIFT_OAUTH);
        });

        test('Set value of TlsSupport property', async () => {
            const propertyName = 'tlsSupport';
            const propertyDefaultValue = 'false';
            await ocpWebConsole.setValuePropertyInCheClusterYaml(propertyName, propertyDefaultValue, TestConstants.TS_SELENIUM_VALUE_TLS_SUPPORT);
        });

        test('Set value of SelfSignedCert property', async () => {
            const propertyName = 'selfSignedCert';
            const propertyDefaultValue = 'false';
            await ocpWebConsole.setValuePropertyInCheClusterYaml(propertyName, propertyDefaultValue, TestConstants.TS_SELENIUM_VALUE_SELF_SIGN_CERT);
        });

        test('Create Che Cluster ', async () => {
            await ocpWebConsole.clickOnCreateCheClusterButton();
            await ocpWebConsole.waitResourcesCheClusterTitle();
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

    suite('Logout from web console OpenShift', async () => {
        test('Logout from temp admin user', async () => {
            await ocpWebConsole.logoutFromWebConsole();
            await ocpWebConsole.waitDisappearanceNavpanelOpenShift();
        });

        test('Go to the insatalled application URL', async () => {
            await ocpLoginPage.openLoginPageOpenShift(TestConstants.TS_SELENIUM_BASE_URL);
        });
    });

    suite('Log into installed application', async () => {
        test('Login to application', async () => {
            cheLogin.login();
        });

        test('Wait application dashboard', async () => {
            await dashboard.waitPage();
        });
    });

});
