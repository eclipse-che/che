/*********************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import 'reflect-metadata';
import { injectable, inject } from 'inversify';
import { CLASSES } from '../../inversify.types';
import { DriverHelper } from '../../utils/DriverHelper';
import { TimeoutConstants } from '../../TimeoutConstants';
import { Logger } from '../../utils/Logger';
import { By } from 'selenium-webdriver';
import { TestConstants } from '../../TestConstants';

@injectable()
export class GitLoginPage {
    private static readonly USERNAME_FIELD_LOCATOR = By.xpath(`//input[@id='login_field']`);
    private static readonly PASSWORD_FIELD_LOCATOR = By.xpath(`//input[@id='password']`);
    private static readonly SIGN_IN_BUTTON_LOCATOR = By.xpath(`//input[@value='Sign in']`);

    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) { }

    async login(timeout: number = TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT) {
        Logger.debug('GitLoginPage.login');

        await this.waitPage(timeout);
        await this.typeUsername(TestConstants.TS_GITHUB_USERNAME, timeout);
        await this.typePassword(TestConstants.TS_GITHUB_PASSWORD, timeout);
        await this.clickSignInButton(timeout);
    }

    async waitPage(timeout: number = TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT) {
        Logger.debug('GitLoginPage.waitPage');

        await this.waitUsernameField(timeout);
        await this.waitPasswordField(timeout);
        await this.waitSignInButton(timeout);
    }

    async waitUsernameField(timeout: number = TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT) {
        Logger.debug('GitLoginPage.waitUsernameField');

        await this.driverHelper.waitVisibility(GitLoginPage.USERNAME_FIELD_LOCATOR, timeout);
    }

    async waitPasswordField(timeout: number = TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT) {
        Logger.debug('GitLoginPage.waitPasswordField');

        await this.driverHelper.waitVisibility(GitLoginPage.PASSWORD_FIELD_LOCATOR, timeout);
    }

    async waitSignInButton(timeout: number = TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT) {
        Logger.debug('GitLoginPage.waitSignInbutton');

        await this.driverHelper.waitVisibility(GitLoginPage.SIGN_IN_BUTTON_LOCATOR, timeout);
    }

    async typeUsername(username: string, timeout: number = TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT) {
        Logger.debug('GitLoginPage.typeUsername');

        await this.driverHelper.type(GitLoginPage.USERNAME_FIELD_LOCATOR, username, timeout);
    }

    async typePassword(password: string, timeout: number = TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT) {
        Logger.debug('GitLoginPage.typePassword');

        await this.driverHelper.type(GitLoginPage.PASSWORD_FIELD_LOCATOR, password, timeout);
    }

    async clickSignInButton(timeout: number = TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT) {
        Logger.debug('GitLoginPage.clickSignInButton');

        await this.driverHelper.waitAndClick(GitLoginPage.SIGN_IN_BUTTON_LOCATOR, timeout);
    }

}
