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
import { injectable, inject } from 'inversify';
import { By } from 'selenium-webdriver';
import { CLASSES } from '../../configs/inversify.types';
import { DriverHelper } from '../../utils/DriverHelper';
import { Logger } from '../../utils/Logger';
import { TestConstants } from '../../constants/TestConstants';

const USERNAME_INPUT_ID: string = 'username-verification';
const PASSWORD_INPUT_ID: string = 'password';
const NEXT_BUTTON_ID: string = 'login-show-step2';
const LOGIN_BUTTON_ID: string = 'rh-password-verification-submit-button';
const timeout: number = 10000;

@injectable()
export class RedHatLoginPage {

    constructor(
        @inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) { }

    async waitRedHatLoginWelcomePage(): Promise<void> {
        Logger.debug('RedHatLoginPage.waitRedHatLoginWelcomePage');
        await this.driverHelper.waitVisibility(By.id(USERNAME_INPUT_ID));
    }

    async enterPasswordRedHat(): Promise<void> {
        Logger.debug('RedHatLoginPage.enterPasswordRedHat');
        const passwordFieldLocator: By = By.id(PASSWORD_INPUT_ID);
        await this.driverHelper.waitVisibility(passwordFieldLocator, 3000);
        await this.driverHelper.enterValue(passwordFieldLocator, TestConstants.TS_SELENIUM_PASSWORD, timeout );
    }
    async clickOnLoginButton(): Promise<void> {
        Logger.debug('RedHatLoginPage.clickOnLoginButton');
        const loginButtonLocator: By = By.id(LOGIN_BUTTON_ID);
        await this.driverHelper.waitAndClick(loginButtonLocator, timeout);
    }
    async waitDisappearanceRedHatLoginWelcomePage(): Promise<void> {
        Logger.debug('RedHatLoginPage.waitDisappearanceRedHatLoginWelcomePage');
        await this.driverHelper.waitDisappearance(By.id(LOGIN_BUTTON_ID));
    }
    async enterUserNameRedHat(): Promise<void> {
        Logger.debug('RedHatLoginPage.enterUserNameRedHat');
        const usernameFieldLocator: By = By.id(USERNAME_INPUT_ID);
        await this.driverHelper.waitVisibility(usernameFieldLocator, 20000);
        await this.driverHelper.enterValue(usernameFieldLocator, TestConstants.TS_SELENIUM_USERNAME, timeout );
    }

    async clickNextButton(): Promise<void> {
        Logger.debug('RedHatLoginPage.clickNextButton');
        const nextButtonLocator: By = By.id(NEXT_BUTTON_ID);
        await this.driverHelper.waitAndClick(nextButtonLocator, timeout);
    }
}
