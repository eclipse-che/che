/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { inject, injectable } from 'inversify';
import { By } from 'selenium-webdriver';
import { CLASSES } from '../../configs/inversify.types';
import { DriverHelper } from '../../utils/DriverHelper';
import { GitProviderType, TestConstants } from '../../constants/TestConstants';
import { Logger } from '../../utils/Logger';
import { TimeoutConstants } from '../../constants/TimeoutConstants';

@injectable()
export class OauthPage {
    private readonly loginForm: By;
    private readonly passwordForm: By;
    private readonly submitButton: By;
    private readonly approveButton: By;
    private readonly denyAccessButton: By;

    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) {
        switch (TestConstants.TS_SELENIUM_FACTORY_GIT_PROVIDER) {
            case GitProviderType.BITBUCKET : {
                this.loginForm = By.id('j_username');
                this.passwordForm = By.id('j_password');
                this.approveButton = By.xpath('//*[@id="approve"]');
                this.submitButton = By.xpath('//*[@id="submit"]');
                this.denyAccessButton = By.xpath('//*[@id="deny"]');
            }
                break;
            case GitProviderType.GITLAB: {
                this.loginForm = TestConstants.TS_SELENIUM_GIT_PROVIDER_IS_LDAP_LOGIN ? By.id('username') : By.id('user_login');
                this.passwordForm = TestConstants.TS_SELENIUM_GIT_PROVIDER_IS_LDAP_LOGIN ? By.id('password') : By.id('user_password');
                this.submitButton = TestConstants.TS_SELENIUM_GIT_PROVIDER_IS_LDAP_LOGIN ? By.xpath('//input[@data-qa-selector="sign_in_button"]') : By.xpath('//button[@data-qa-selector="sign_in_button"]');
                this.approveButton = By.xpath('//*[@value="Authorize"]');
                this.denyAccessButton = By.xpath('//input[@value="Deny"]');
            }
                break;
            case GitProviderType.GITHUB: {
                this.loginForm = By.id('login_field');
                this.passwordForm = By.id('password');
                this.approveButton = By.xpath('//*[@id="js-oauth-authorize-btn"]');
                this.submitButton = By.xpath('//*[@value="Sign in"]');
                this.denyAccessButton = By.xpath('//button[contains(., "Cancel")]');
            }
                break;
            default: {
                throw new Error(`Invalid git provider. The value should be ${GitProviderType.GITHUB}, ${GitProviderType.GITLAB} or ${GitProviderType.BITBUCKET}`);
            }
        }
    }

    async waitLoginPage(): Promise<void> {
        Logger.debug('OauthPage.waitLoginPage');

        // for gitlab server https://gitlab.cee.redhat.com
        if (!TestConstants.TS_SELENIUM_GIT_PROVIDER_IS_LDAP_LOGIN &&
            TestConstants.TS_SELENIUM_FACTORY_GIT_PROVIDER === GitProviderType.GITLAB) {
            {
                Logger.debug(`OauthPage.login - go to ${TestConstants.TS_SELENIUM_FACTORY_GIT_PROVIDER} Standard login section`);
                await this.driverHelper.waitAndClick(By.xpath('//a[@data-qa-selector="standard_tab"]'));
            }
        }

        await this.driverHelper.waitVisibility(this.loginForm, TimeoutConstants.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM * 3);
    }

    async enterUserName(userName: string): Promise<void> {
        Logger.debug(`this.enterUserName "${userName}"`);

        await this.driverHelper.enterValue(this.loginForm, userName);
    }

    async enterPassword(password: string): Promise<void> {
        Logger.debug(`OauthPage.enterPassword`);

        await this.driverHelper.enterValue(this.passwordForm, password);
    }

    async clickOnLoginButton(): Promise<void> {
        Logger.debug('OauthPage.clickOnLoginButton');

        await this.driverHelper.waitAndClick(this.submitButton);
    }

    async waitClosingLoginPage(): Promise<void> {
        Logger.debug('OauthPage.waitClosingLoginPage');

        await this.driverHelper.waitDisappearance(this.loginForm);
    }

    async waitOauthPage(): Promise<void> {
        Logger.debug('OauthPage.waitOauthPage');

        await this.driverHelper.waitVisibility(this.approveButton);
    }

    async clickOnApproveButton(): Promise<void> {
        Logger.debug('OauthPage.clickOnApproveButton');

        await this.driverHelper.waitAndClick(this.approveButton);
    }

    async clickOnDenyAccessButton(): Promise<void> {
        Logger.debug('OauthPage.clickOnDenyAccessButton');

        await this.driverHelper.waitAndClick(this.denyAccessButton);
    }

    async waitDisappearanceOauthPage(): Promise<void> {
        Logger.debug('OauthPage.waitDisappearanceOauthPage');

        await this.driverHelper.waitDisappearance(this.approveButton);
    }

    async login(): Promise<void> {
        Logger.debug('OauthPage.login');

        await this.waitLoginPage();
        await this.enterUserName(TestConstants.TS_SELENIUM_GIT_PROVIDER_USERNAME);
        await this.enterPassword(TestConstants.TS_SELENIUM_GIT_PROVIDER_PASSWORD);
        await this.clickOnLoginButton();
        await this.waitClosingLoginPage();
    }

    async confirmAccess(): Promise<void> {
        Logger.debug('OauthPage.confirmAccess');

        try {
            await this.clickOnApproveButton();
            await this.waitDisappearanceOauthPage();
        } catch (e) {
            Logger.debug('OauthPage.confirmAccess - access was not confirmed, retrying to click conformation button');
            // workaround for github oauth conformation page (bot security)
            await this.driverHelper.getAction()
                .move({
                    origin: await this.driverHelper.waitPresence(this.approveButton)
                })
                .click()
                .perform();
            await this.waitDisappearanceOauthPage();
        }
    }

    async denyAccess(): Promise<void> {
        Logger.debug('OauthPage.denyAccess');

        await this.clickOnDenyAccessButton();
        await this.waitDisappearanceOauthPage();
    }
}
