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
import { IOcpLoginPage } from '../../pageobjects/login/IOcpLoginPage';
import { CLASSES, TYPES } from '../../inversify.types';
import { OcpLoginPage } from '../../pageobjects/openshift/OcpLoginPage';
import { OcpWebConsolePage } from '../../pageobjects/openshift/OcpWebConsolePage';
import { TestConstants } from '../../TestConstants';

const ocpLogin: IOcpLoginPage = e2eContainer.get<IOcpLoginPage>(TYPES.OcpLogin);
const ocpLoginPage: OcpLoginPage = e2eContainer.get(CLASSES.OcpLoginPage);
const ocpWebConsole: OcpWebConsolePage = e2eContainer.get(CLASSES.OcpWebConsolePage);

suite('E2E', async () => {

    suite('Go to OCP and wait console OpenShift', async () => {
        test('Open login page', async () => {
            await ocpLoginPage.openLoginPageOpenShift(TestConstants.TS_SELENIUM_WEB_CONSOLE_OCP_URL);
        });

        test('Log into OCP', async () => {
            ocpLogin.login();
        });
    });

    suite('Subscribe Operator to defined namespace', async () => {
        test('Go to the OperatorHub main page', async () => {
            await ocpWebConsole.waitNavpanelOpenShift();
            await ocpWebConsole.openOperatorHubMainPageByUrl(TestConstants.TS_SELENIUM_WEB_CONSOLE_OCP_URL + '/operatorhub/all-namespaces');
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
});
