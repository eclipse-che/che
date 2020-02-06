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

import { CLASSES } from '../../inversify.types';
import { IHeaderHandler } from './IHeaderHandler';
import { injectable, inject } from 'inversify';
import { TokenHandler } from '../TokenHandler';

@injectable()
export class MultiUserHeaderHandler implements IHeaderHandler {

    constructor(@inject(CLASSES.TokenHandler) private readonly tokenHandler: TokenHandler) {
    }
    async getHeaders() : Promise<AxiosRequestConfig> {
        let token = await this.tokenHandler.getCheBearerToken();
        return { headers: {'Authorization' : `Bearer ${token}`}};
    }
}


