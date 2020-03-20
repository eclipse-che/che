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

    async get(relativeUrl: string): Promise<AxiosResponse> {
        return await axios.get(this.assembleUrl(relativeUrl), await this.headerHandler.get());
    }

    async post(relativeUrl: string, data?: string | any ): Promise<AxiosResponse> {
        return await axios.post(this.assembleUrl(relativeUrl), data, await this.headerHandler.get());
    }

    async delete(relativeUrl: string): Promise<AxiosResponse> {
        return await axios.delete(this.assembleUrl(relativeUrl), await this.headerHandler.get());
    }

    private assembleUrl(relativeUrl: string): string {
        return `${TestConstants.TS_SELENIUM_BASE_URL}/${relativeUrl}`;
    }

}
