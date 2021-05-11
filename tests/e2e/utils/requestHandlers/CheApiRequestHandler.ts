/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import axios, { AxiosResponse, AxiosRequestConfig } from 'axios';
import { TestConstants } from '../../TestConstants';
import { TYPES } from '../../inversify.types';
import { inject, injectable } from 'inversify';
import { IAuthorizationHeaderHandler } from './headers/IAuthorizationHeaderHandler';

@injectable()
export class CheApiRequestHandler {

    /**
     * This method adds a request interceptor into axios request interceptors list and returns an ID of the interceptor
     */
    public static enableRequestInteceptor(): number {
        console.log(`CheApiRequestHandler.enableRequestInterceptor`);
        return axios.interceptors.request.use( request => {
                try {
                    let request_censored: AxiosRequestConfig = JSON.parse(JSON.stringify(request));
                    request_censored.headers.Authorization = 'CENSORED';
                    console.log(`RequestHandler request:\n`, request_censored);
                } catch (err) {
                    console.log(`RequestHandler request: Failed to deep clone AxiosRequestConfig:`, err);
                }
            return request;
        });
    }

    /**
     * This method adds a response interceptor into axios response interceptors list and returns an ID of the interceptor
     */
    public static enableResponseInterceptor(): number {
        console.log(`CheApiRequestHandler.enableResponseRedirects`);
        return axios.interceptors.response.use( response => {
                try {
                    let response_censored: AxiosResponse = JSON.parse(JSON.stringify(response, (key, value) => {
                        switch (key) {
                            case 'request': return 'CENSORED';
                            default: return value;
                        }
                    }));
                    response_censored.config.headers.Authorization = 'CENSORED';
                    if (response_censored.data.access_token != null) {
                        response_censored.data.access_token = 'CENSORED';
                    }
                    if (response_censored.data.refresh_token != null) {
                        response_censored.data.refresh_token = 'CENSORED';
                    }
                    console.log(`RequestHandler response:\n`, response_censored);
                } catch (err) {
                    console.log(`RequestHandler response: Failed to deep clone AxiosResponse:`, err);
                }
            return response;
        });
    }

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
