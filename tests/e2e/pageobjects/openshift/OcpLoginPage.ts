/*********************************************************************
 * Copyright (c) 2019-2023 Red Hat, Inc.
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
import { CLASSES } from '../../configs/inversify.types';
import { By } from 'selenium-webdriver';
import { Logger } from '../../utils/Logger';
import { TimeoutConstants } from '../../constants/TimeoutConstants';
import { OAuthConstants } from '../../constants/OAuthConstants';

@injectable()
export class OcpLoginPage {

    private static readonly LOGIN_PAGE_OPENSHIFT_XPATH: string = '//*[contains(text(), \'Welcome\')]';

    constructor(
        @inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) { }

    async waitOpenShiftLoginWelcomePage(): Promise<void> {
        Logger.debug('OcpLoginPage.waitOpenShiftLoginWelcomePage');

        await this.driverHelper.waitVisibility(By.xpath(OcpLoginPage.LOGIN_PAGE_OPENSHIFT_XPATH), TimeoutConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT);
    }

    async clickOnLoginProviderTitle(): Promise<void> {
        Logger.debug('OcpLoginPage.clickOnLoginProviderTitle');

        const loginProviderTitleLocator: By = By.xpath(`//a[text()=\'${OAuthConstants.TS_OCP_LOGIN_PAGE_PROVIDER_TITLE}\']`);
        await this.driverHelper.waitAndClick(loginProviderTitleLocator, TimeoutConstants.TS_SELENIUM_WAIT_FOR_URL);
    }

    async isIdentityProviderLinkVisible(): Promise<boolean> {
        Logger.debug('OcpLoginPage.isIdentityProviderLinkVisible');

        const loginWithHtpaswdLocator: By = By.xpath(`//a[text()=\'${OAuthConstants.TS_OCP_LOGIN_PAGE_PROVIDER_TITLE}\']`);
        return await this.driverHelper.waitVisibilityBoolean(loginWithHtpaswdLocator, 3, 5000);
    }

    async isAuthorizeOpenShiftIdentityProviderPageVisible(): Promise<boolean> {
        Logger.debug('OcpLoginPage.isAuthorizeOpenShiftIdentityProviderPageVisible');

        const authorizeOpenshiftIdentityProviderPageLocator: By = By.xpath('//h1[text()=\'Authorize Access\']');
        return await this.driverHelper.isVisible(authorizeOpenshiftIdentityProviderPageLocator);
    }

    async waitAuthorizeOpenShiftIdentityProviderPage(): Promise<void> {
        Logger.debug('OcpLoginPage.waitAuthorizeOpenShiftIdentityProviderPage');

        const authorizeOpenshiftIdentityProviderPageLocator: By = By.xpath('//h1[text()=\'Authorize Access\']');
        await this.driverHelper.waitVisibility(authorizeOpenshiftIdentityProviderPageLocator, TimeoutConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT);
    }

    async clickOnApproveAuthorizeAccessButton(): Promise<void> {
        Logger.debug('OcpLoginPage.clickOnApproveAuthorizeAccessOpenshift');

        const approveAuthorizeAccessOcpLocator: By = By.css('input[name=\'approve\']');
        await this.driverHelper.waitAndClick(approveAuthorizeAccessOcpLocator);
    }

    async enterUserNameOpenShift(userName: string): Promise<void> {
        Logger.debug(`OcpLoginPage.enterUserNameOpenShift "${userName}"`);

        await this.driverHelper.enterValue(By.id('inputUsername'), userName);
    }

    async enterPasswordOpenShift(passw: string): Promise<void> {
        Logger.debug(`OcpLoginPage.enterPasswordOpenShift"`);

        await this.driverHelper.enterValue(By.id('inputPassword'), passw);
    }

    async clickOnLoginButton(): Promise<void> {
        Logger.debug('OcpLoginPage.clickOnLoginButton');

        const loginButtonLocator: By = By.css('button[type=submit]');
        await this.driverHelper.waitAndClick(loginButtonLocator);
    }

    async waitDisappearanceOpenShiftLoginWelcomePage(): Promise<void> {
        Logger.debug('OcpLoginPage.waitDisappearanceOpenShiftLoginWelcomePage');

        await this.driverHelper.waitDisappearance(By.xpath(OcpLoginPage.LOGIN_PAGE_OPENSHIFT_XPATH));
    }
}
