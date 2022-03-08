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
import { injectable } from 'inversify';

@injectable()
export class CheMultiuserAuthorizationHeaderHandler implements IAuthorizationHeaderHandler {

    async get(): Promise<AxiosRequestConfig> {
        //  to-do : Fetch the cookies from user api and pass it here
        return { headers: { 'cookie': `` } };
    }
}
