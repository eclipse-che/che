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
import { ICheLoginPage } from './ICheLoginPage';
import { OcpLoginPage } from '../openshift/OcpLoginPage';
import { CheLoginPage } from '../openshift/CheLoginPage';
import { injectable, inject } from 'inversify';
import { CLASSES } from '../../inversify.types';
import { TestConstants } from '../../TestConstants';
import { Logger } from '../../utils/Logger';

@injectable()
export class RegularUserOcpCheLoginPage implements ICheLoginPage {

    constructor(
        @inject(CLASSES.OcpLoginPage) private readonly ocpLogin: OcpLoginPage,
        @inject(CLASSES.CheLoginPage) private readonly cheLogin: CheLoginPage) { }

    async login() {
        Logger.debug('RegularUserOcpCheLoginPage.login');

        if (await this.ocpLogin.isIdentityProviderLinkVisible()) {
            await this.ocpLogin.clickOnLoginProviderTitle();
        }

        await this.ocpLogin.waitOpenShiftLoginWelcomePage();
        await this.ocpLogin.enterUserNameOpenShift(TestConstants.TS_SELENIUM_OCP_USERNAME);
        await this.ocpLogin.enterPasswordOpenShift(TestConstants.TS_SELENIUM_OCP_PASSWORD);
        await this.ocpLogin.clickOnLoginButton();
        await this.ocpLogin.waitDisappearanceOpenShiftLoginWelcomePage();

        if (await this.ocpLogin.isAuthorizeOpenShiftIdentityProviderPageVisible()) {
            await this.ocpLogin.waitAuthorizeOpenShiftIdentityProviderPage();
            await this.ocpLogin.clickOnApproveAuthorizeAccessButton();
        }

        if (await this.cheLogin.isFirstBrokerLoginPageVisible()) {
            await this.cheLogin.waitFirstBrokerLoginPage();
            await this.cheLogin.enterEmailFirstBrokerLoginPage(TestConstants.TS_SELENIUM_EMAIL_USER);
            await this.cheLogin.enterFirstNameBrokerLoginPage(TestConstants.TS_SELENIUM_FIRST_NAME);
            await this.cheLogin.enterLastNameBrokerLoginPage(TestConstants.TS_SELENIUM_LAST_NAME);
            await this.cheLogin.clickOnSubmitButton();
            await this.cheLogin.waitDisappearanceBrokerLoginPage();
        }

        if (await this.ocpLogin.isLinkAccountPageVisible()) {
            await this.ocpLogin.clickOnLinkAccountButton();
            await this.cheLogin.waitEclipseCheLoginFormPage();
            await this.cheLogin.inputPaswordEclipseCheLoginPage(TestConstants.TS_SELENIUM_PASSWORD);
            await this.cheLogin.clickEclipseCheLoginButton();
        }
    }

}
