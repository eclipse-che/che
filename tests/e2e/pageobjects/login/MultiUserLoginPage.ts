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
import { ICheLoginPage } from './ICheLoginPage';
import { CheLoginPage } from '../openshift/CheLoginPage';
import { injectable, inject } from 'inversify';
import { CLASSES } from '../../configs/inversify.types';
import { TestConstants } from '../../constants/TestConstants';
import { Logger } from '../../utils/Logger';

@injectable()
export class MultiUserLoginPage implements ICheLoginPage {

    constructor(
        @inject(CLASSES.CheLoginPage) private readonly cheLogin: CheLoginPage) { }

    async login() {
        Logger.debug('MultiUserLoginPage.login');

        await this.cheLogin.waitEclipseCheLoginFormPage();
        await this.cheLogin.inputUserNameEclipseCheLoginPage(TestConstants.TS_SELENIUM_USERNAME);
        await this.cheLogin.inputPaswordEclipseCheLoginPage(TestConstants.TS_SELENIUM_PASSWORD);
        await this.cheLogin.clickEclipseCheLoginButton();
    }

}
