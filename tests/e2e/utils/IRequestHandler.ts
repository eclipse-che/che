/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { RequestType } from './RequestType';
import { AxiosResponse } from 'axios';

export interface IRequestHandler {
    processRequest(reqType: RequestType, url: string, data?: string) : Promise<AxiosResponse>;
    setHeaders(): void;
}
