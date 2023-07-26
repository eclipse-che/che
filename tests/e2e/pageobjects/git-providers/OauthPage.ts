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
import { Logger } from '../../utils/Logger';
import { TimeoutConstants } from '../../constants/TimeoutConstants';
import { FactoryTestConstants, GitProviderType } from '../../constants/FactoryTestConstants';
import { OAuthConstants } from '../../constants/OAuthConstants';

@injectable()
export class OauthPage {
  private static LOGIN_FORM: By;
  private static PASSWORD_FORM: By;
  private static SUBMIT_BUTTON: By;
  private static APPROVE_BUTTON: By;
  private static DENY_ACCESS_BUTTON: By;
  private static DENY_SAVE_CREDENTIALS_BUTTON: By;

  constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) {
    switch (FactoryTestConstants.TS_SELENIUM_FACTORY_GIT_PROVIDER) {
      case GitProviderType.BITBUCKET : {
        OauthPage.LOGIN_FORM = By.id('j_username');
        OauthPage.PASSWORD_FORM = By.id('j_password');
        OauthPage.APPROVE_BUTTON = By.id('approve');
        OauthPage.SUBMIT_BUTTON = By.id('submit');
        OauthPage.DENY_ACCESS_BUTTON = By.id('deny');
      }
        break;
      case GitProviderType.GITLAB: {
        OauthPage.LOGIN_FORM = By.id('user_login');
        OauthPage.PASSWORD_FORM = By.id('user_password');
        OauthPage.SUBMIT_BUTTON = By.xpath('//button[@data-qa-selector="sign_in_button"]');
        OauthPage.APPROVE_BUTTON = By.xpath('//*[@value="Authorize"]');
        OauthPage.DENY_ACCESS_BUTTON = By.xpath('//input[@value="Deny"]');
      }
        break;
      case GitProviderType.GITHUB: {
        OauthPage.LOGIN_FORM = By.id('login_field');
        OauthPage.PASSWORD_FORM = By.id('password');
        OauthPage.APPROVE_BUTTON = By.xpath('//*[@id="js-oauth-authorize-btn"]');
        OauthPage.SUBMIT_BUTTON = By.xpath('//*[@value="Sign in"]');
        OauthPage.DENY_ACCESS_BUTTON = By.xpath('//button[contains(., "Cancel")]');
      }
        break;
      case GitProviderType.AZURE_DEVOPS: {
        OauthPage.LOGIN_FORM = By.xpath('//input[@type="email"]');
        OauthPage.PASSWORD_FORM = By.xpath('//input[@type="password"]');
        OauthPage.APPROVE_BUTTON = By.id('accept-button');
        OauthPage.SUBMIT_BUTTON = By.xpath('//input[@type="submit"]');
        OauthPage.DENY_SAVE_CREDENTIALS_BUTTON = By.xpath('//input[@type="button"]');
        OauthPage.DENY_ACCESS_BUTTON = By.id('deny-button');
      }
        break;
      default: {
        throw new Error(`Invalid git provider. The value should be ${GitProviderType.GITHUB}, ${GitProviderType.GITLAB}, ${GitProviderType.AZURE_DEVOPS} or ${GitProviderType.BITBUCKET}`);
      }
    }
  }

  async waitLoginPage(): Promise<void> {
    Logger.debug();

    await this.driverHelper.waitVisibility(OauthPage.LOGIN_FORM, TimeoutConstants.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM * 3);
  }

  async enterUserName(userName: string): Promise<void> {
    Logger.debug(`"${userName}"`);

    await this.driverHelper.enterValue(OauthPage.LOGIN_FORM, userName);
  }

  async enterPassword(password: string): Promise<void> {
    Logger.debug();

    await this.driverHelper.enterValue(OauthPage.PASSWORD_FORM, password);
  }

  async clickOnSubmitButton(): Promise<void> {
    Logger.debug();

    await this.driverHelper.waitAndClick(OauthPage.SUBMIT_BUTTON);
  }

  async clickOnNotRememberCredentialsButton(): Promise<void> {
    Logger.debug();

    await this.driverHelper.waitAndClick(OauthPage.DENY_SAVE_CREDENTIALS_BUTTON);
  }

  async waitClosingLoginPage(): Promise<void> {
    Logger.debug();

    await this.driverHelper.waitDisappearance(OauthPage.PASSWORD_FORM);
  }

  async waitOauthPage(): Promise<void> {
    Logger.debug();

    await this.driverHelper.waitVisibility(OauthPage.APPROVE_BUTTON);
  }

  async clickOnApproveButton(): Promise<void> {
    Logger.debug();

    await this.driverHelper.waitAndClick(OauthPage.APPROVE_BUTTON);
  }

  async clickOnDenyAccessButton(): Promise<void> {
    Logger.debug();

    await this.driverHelper.waitAndClick(OauthPage.DENY_ACCESS_BUTTON);
  }

  async waitDisappearanceOauthPage(): Promise<void> {
    Logger.debug();

    await this.driverHelper.waitDisappearance(OauthPage.APPROVE_BUTTON);
  }

  async login(): Promise<void> {
    Logger.debug();

    await this.waitLoginPage();
    await this.enterUserName(OAuthConstants.TS_SELENIUM_GIT_PROVIDER_USERNAME);
    if (FactoryTestConstants.TS_SELENIUM_FACTORY_GIT_PROVIDER === GitProviderType.AZURE_DEVOPS) {
      await this.clickOnSubmitButton();
    }
    await this.enterPassword(OAuthConstants.TS_SELENIUM_GIT_PROVIDER_PASSWORD);
    await this.clickOnSubmitButton();
    if (FactoryTestConstants.TS_SELENIUM_FACTORY_GIT_PROVIDER === GitProviderType.AZURE_DEVOPS) {
      await this.clickOnNotRememberCredentialsButton();
    }
    await this.waitClosingLoginPage();
  }

  async confirmAccess(): Promise<void> {
    Logger.debug();

    try {
      await this.clickOnApproveButton();
      await this.waitDisappearanceOauthPage();
    } catch (e) {
      Logger.debug('access was not confirmed, retrying to click confirmation button');
      // workaround for GITHUB, AZURE_DEVOPS oauth confirmation page (bot security)
      await this.driverHelper.getAction()
        .move({
          origin: await this.driverHelper.waitPresence(OauthPage.APPROVE_BUTTON)
        })
        .click()
        .perform();
      await this.waitDisappearanceOauthPage();
    }
  }

  async denyAccess(): Promise<void> {
    Logger.debug();

    try {
      await this.clickOnDenyAccessButton();
      await this.waitDisappearanceOauthPage();
    } catch (e) {
      Logger.debug('deny access was not confirmed, retrying to click confirmation button');
      // workaround for GITHUB, AZURE_DEVOPS oauth confirmation page (bot security)
      await this.driverHelper.getAction()
        .move({
          origin: await this.driverHelper.waitPresence(OauthPage.DENY_ACCESS_BUTTON)
        })
        .click()
        .perform();
      await this.waitDisappearanceOauthPage();
    }
  }
}
