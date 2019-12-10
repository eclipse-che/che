"use strict";
/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : new P(function (resolve) { resolve(result.value); }).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
Object.defineProperty(exports, "__esModule", { value: true });
const inversify_config_1 = require("../../inversify.config");
const inversify_types_1 = require("../../inversify.types");
const cheLogin = inversify_config_1.e2eContainer.get(inversify_types_1.TYPES.CheLogin);
const ocpLogin = inversify_config_1.e2eContainer.get(inversify_types_1.TYPES.OcpLogin);
const ocpLoginPage = inversify_config_1.e2eContainer.get(inversify_types_1.CLASSES.OcpLoginPage);
const ocpWebConsole = inversify_config_1.e2eContainer.get(inversify_types_1.CLASSES.OcpWebConsolePage);
const dashboard = inversify_config_1.e2eContainer.get(inversify_types_1.CLASSES.Dashboard);
suite('E2E', () => __awaiter(this, void 0, void 0, function* () {
    suite('Go to OCP and wait console OpenShift', () => __awaiter(this, void 0, void 0, function* () {
        test('Open login page', () => __awaiter(this, void 0, void 0, function* () {
            yield ocpLoginPage.openLoginPageOpenShift();
            yield ocpLoginPage.waitOpenShiftLoginPage();
        }));
        test('Log into OCP', () => __awaiter(this, void 0, void 0, function* () {
            ocpLogin.login();
        }));
    }));
    suite('Subscribe Operator to defined namespace', () => __awaiter(this, void 0, void 0, function* () {
        test('Open Catalog, select OperatorHub', () => __awaiter(this, void 0, void 0, function* () {
            yield ocpWebConsole.waitNavpanelOpenShift();
            yield ocpWebConsole.clickOnCatalogListNavPanelOpenShift();
            yield ocpWebConsole.clickOnOperatorHubItemNavPanel();
            yield ocpWebConsole.waitOperatorHubMainPage();
        }));
        test('Select Operator from catalog and install it', () => __awaiter(this, void 0, void 0, function* () {
            yield ocpWebConsole.clickOnCatalogOperatorIcon();
            yield ocpWebConsole.clickOnInstallButton();
        }));
        test('Select a namespace and subscribe Operator', () => __awaiter(this, void 0, void 0, function* () {
            yield ocpWebConsole.waitCreateOperatorSubscriptionPage();
            yield ocpWebConsole.selectUpdateChannelOnSubscriptionPage();
            yield ocpWebConsole.clickOnDropdownNamespaceListOnSubscriptionPage();
            yield ocpWebConsole.waitListBoxNamespacesOnSubscriptionPage();
            yield ocpWebConsole.selectDefinedNamespaceOnSubscriptionPage();
            yield ocpWebConsole.clickOnSubscribeButtonOnSubscriptionPage();
        }));
        test('Wait the Subscription Overview', () => __awaiter(this, void 0, void 0, function* () {
            yield ocpWebConsole.waitSubscriptionOverviewPage();
            yield ocpWebConsole.waitChannelNameOnSubscriptionOverviewPage();
            yield ocpWebConsole.waitUpgradeStatusOnSubscriptionOverviewPage();
            yield ocpWebConsole.waitCatalogSourceNameOnSubscriptionOverviewPage();
        }));
    }));
    suite('Wait the operator is represented by CSV', () => __awaiter(this, void 0, void 0, function* () {
        test('Select the Installed Operators in the nav panel', () => __awaiter(this, void 0, void 0, function* () {
            yield ocpWebConsole.selectInstalledOperatorsOnNavPanel();
        }));
        test('Wait installed Operator', () => __awaiter(this, void 0, void 0, function* () {
            yield ocpWebConsole.waitInstalledOperatorLogoName();
            yield ocpWebConsole.waitStatusInstalledOperator();
        }));
    }));
    suite('Create new Che cluster', () => __awaiter(this, void 0, void 0, function* () {
        test('Click on the logo-name Che operator', () => __awaiter(this, void 0, void 0, function* () {
            yield ocpWebConsole.clickOnInstalledOperatorLogoName();
            yield ocpWebConsole.waitOverviewCsvEclipseCheOperator();
        }));
        test('Click on the Create New, wait CSV yaml', () => __awaiter(this, void 0, void 0, function* () {
            yield ocpWebConsole.clickCreateNewCheClusterLink();
            yield ocpWebConsole.waitCreateCheClusterYaml();
        }));
        test('Set value of OpenShiftOauth field', () => __awaiter(this, void 0, void 0, function* () {
            yield ocpWebConsole.selectOpenShiftOAuthFieldInYaml();
            yield ocpWebConsole.setValueOpenShiftOAuthField();
        }));
        test('Create Che Cluster ', () => __awaiter(this, void 0, void 0, function* () {
            yield ocpWebConsole.clickOnCreateCheClusterButton();
            yield ocpWebConsole.waitResourcesCheClusterTitle();
            yield ocpWebConsole.waitResourcesCheClusterTimestamp();
            yield ocpWebConsole.clickOnCheClusterResourcesName();
        }));
    }));
    suite('Check the Che is ready', () => __awaiter(this, void 0, void 0, function* () {
        test('Wait Keycloak Admin Console URL', () => __awaiter(this, void 0, void 0, function* () {
            yield ocpWebConsole.waitKeycloakAdminConsoleUrl();
        }));
        test('Wait installed application URL', () => __awaiter(this, void 0, void 0, function* () {
            yield ocpWebConsole.waitInstalledAppUrl();
        }));
    }));
    suite('Log into installed application', () => __awaiter(this, void 0, void 0, function* () {
        test('Click on the insatalled application URL ', () => __awaiter(this, void 0, void 0, function* () {
            yield ocpWebConsole.clickOnInstalledAppUrl();
        }));
        test('Login to application', () => __awaiter(this, void 0, void 0, function* () {
            yield cheLogin.login();
        }));
        test('Wait application dashboard', () => __awaiter(this, void 0, void 0, function* () {
            yield dashboard.waitPage();
        }));
    }));
}));
//# sourceMappingURL=InstallCheByOperatorHub.spec.js.map