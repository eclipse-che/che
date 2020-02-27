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

import { TYPES } from '../../../inversify.types';
import { IAuthorizationHeaderHandler } from './IAuthorizationHeaderHandler';
import { injectable, inject } from 'inversify';
import { ITokenHandler } from '../tokens/ITokenHandler';

@injectable()
export class CheMultiuserAuthorizationHeaderHandler implements IAuthorizationHeaderHandler {

    constructor(@inject(TYPES.ITokenHandler) private readonly tokenHandler: ITokenHandler) {
    }

    async get(): Promise<AxiosRequestConfig> {
        const token = await this.tokenHandler.get();
        return { headers: { 'Authorization': `Bearer ${token}` } };
    }
}
