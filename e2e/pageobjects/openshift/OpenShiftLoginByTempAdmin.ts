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
import { ILoginPageOcp } from './ILoginPageOcp';
import { injectable, inject } from 'inversify';
import { OpenShiftLoginPage } from './OpenShiftLoginPage';
import { CLASSES } from '../../inversify.types';
import { TestConstants } from '../../TestConstants';

@injectable()
export class OpenShiftLoginByTempAdmin implements ILoginPageOcp {

    constructor(
        @inject(CLASSES.OpenShiftLoginPage) private readonly openShiftLogin: OpenShiftLoginPage) { }

    async login() {
        if (TestConstants.TS_OCP_LOGIN_PAGE_OAUTH) {
            await this.openShiftLogin.clickOnLoginWitnKubeAdmin();
        }

        await this.openShiftLogin.enterUserNameOpenShift(TestConstants.TS_SELENIUM_OPENSHIFT4_USERNAME);
        await this.openShiftLogin.enterPasswordOpenShift(TestConstants.TS_SELENIUM_OPENSHIFT4_PASSWORD);
        await this.openShiftLogin.clickOnLoginButton();
        await this.openShiftLogin.waitDisappearanceLoginPageOpenShift();
    }
}
