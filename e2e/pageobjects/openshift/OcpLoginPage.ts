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
import { DriverHelper } from '../../utils/DriverHelper';
import { CLASSES } from '../../inversify.types';
import { TestConstants } from '../../TestConstants';
import { By } from 'selenium-webdriver';

@injectable()
export class OcpLoginPage {

    private static readonly LOGIN_PAGE_OPENSHIFT: string = 'div[class=container]';

    constructor(
    @inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) { }

    async openLoginPageOpenShift () {
        await this.driverHelper.navigateToUrl(TestConstants.TS_SELENIUM_BASE_URL);
    }

    async waitOpenShiftLoginPage () {
        await this.driverHelper.waitVisibility(By.css(OcpLoginPage.LOGIN_PAGE_OPENSHIFT));
    }

    async clickOnLoginWitnKubeAdmin () {
        const loginWithKubeAdminLocator: By = By.css('a[title=\'Log in with kube:admin\']');
        await this.driverHelper.waitAndClick(loginWithKubeAdminLocator);
    }

    async enterUserNameOpenShift (userName: string) {
        await this.driverHelper.enterValue(By.id('inputUsername'), userName);
    }

    async enterPasswordOpenShift (passw: string) {
        await this.driverHelper.enterValue(By.id('inputPassword'), passw);
    }

    async clickOnLoginButton () {
        const loginButtonlocator: By = By.css('button[type=submit]');
        await this.driverHelper.waitAndClick(loginButtonlocator);
    }

    async waitDisappearanceLoginPageOpenShift () {
        await this.driverHelper.waitDisappearance(By.css(OcpLoginPage.LOGIN_PAGE_OPENSHIFT));
    }
}
