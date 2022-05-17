/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
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
import { CLASSES } from '../../../inversify.types';

@injectable()
export class CheMultiuserAuthorizationHeaderHandler implements IAuthorizationHeaderHandler {
    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) { }

    async get(): Promise<AxiosRequestConfig> {
        let token = await this.driverHelper.getDriver().manage().getCookie('_oauth_proxy');
        return { headers: { 'cookie': `_oauth_proxy=${token.value}` } };
    }
}
