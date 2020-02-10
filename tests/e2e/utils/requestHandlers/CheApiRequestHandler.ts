/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import axios, { AxiosResponse } from 'axios';
import { TestConstants } from '../../TestConstants';
import { TYPES } from '../../inversify.types';
import { inject, injectable } from 'inversify';
import { IAuthorizationHeaderHandler } from './headers/IAuthorizationHeaderHandler';

@injectable()
export class CheApiRequestHandler {
    constructor(@inject(TYPES.IAuthorizationHeaderHandler) private readonly headerHandler: IAuthorizationHeaderHandler) { }

    async get(url: string): Promise<AxiosResponse> {
        return await axios.get(this.assembleUrl(url), await this.headerHandler.get());
    }

    async post(url: string, data?: string): Promise<AxiosResponse> {
        if (data === undefined) {
            return await axios.post(this.assembleUrl(url), await this.headerHandler.get());
        } else {
            return await axios.post(this.assembleUrl(url), data, await this.headerHandler.get());
        }
    }

    async delete(url: string): Promise<AxiosResponse> {
        return await axios.delete(this.assembleUrl(url), await this.headerHandler.get());
    }

    private assembleUrl(url: string): string {
        return `${TestConstants.TS_SELENIUM_BASE_URL}/${url}`;
    }

}
