/*********************************************************************
 * Copyright (c) 2019-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { AxiosRequestConfig } from 'axios';
import { IAuthorizationHeaderHandler } from './IAuthorizationHeaderHandler';
import { inject, injectable } from 'inversify';
import { DriverHelper } from '../../DriverHelper';
import { CLASSES } from '../../../configs/inversify.types';
import { Logger } from '../../Logger';

@injectable()
export class CheMultiuserAuthorizationHeaderHandler implements IAuthorizationHeaderHandler {
    private authorizationToken: string = '';

    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) { }

    async get(): Promise<AxiosRequestConfig> {
        try {
            let token = await this.driverHelper.getDriver().manage().getCookie('_oauth_proxy');
            if (this.authorizationToken !== token.value) {
                this.authorizationToken = token.value;
            }
        } catch (err) {
            if (this.authorizationToken.length > 0) {
                Logger.warn(`Could not obtain _oauth_proxy cookie from chromedriver, browser session may have been killed. Using stored value.`);
            } else {
                throw new Error(`Could not obtain _oauth_proxy cookie from chromedriver, browser session may have been killed. No stored token present!`);
            }
        }

        return { headers: { 'cookie': `_oauth_proxy=${this.authorizationToken}` } };
    }
}
