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
import { IOcpLoginPage } from '../interfaces/IOcpLoginPage';
import { injectable, inject } from 'inversify';
import { OcpLoginPage } from './OcpLoginPage';
import { CLASSES } from '../../../configs/inversify.types';
import { Logger } from '../../../utils/Logger';
import { OAuthConstants } from '../../../constants/OAuthConstants';

@injectable()
export class OcpUserLoginPage implements IOcpLoginPage {

    constructor(
        @inject(CLASSES.OcpLoginPage) private readonly ocpLogin: OcpLoginPage) { }

    async login(): Promise<void> {
        Logger.debug();

        if (OAuthConstants.TS_OCP_LOGIN_PAGE_PROVIDER_TITLE !== '') {
            await this.ocpLogin.clickOnLoginProviderTitle();
        }

        await this.ocpLogin.waitOpenShiftLoginWelcomePage();
        await this.ocpLogin.enterUserNameOpenShift(OAuthConstants.TS_SELENIUM_OCP_USERNAME);
        await this.ocpLogin.enterPasswordOpenShift(OAuthConstants.TS_SELENIUM_OCP_PASSWORD);
        await this.ocpLogin.clickOnLoginButton();
        await this.ocpLogin.waitDisappearanceOpenShiftLoginWelcomePage();
    }

}
