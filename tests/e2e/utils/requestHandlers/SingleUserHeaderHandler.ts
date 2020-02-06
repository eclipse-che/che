/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { IHeaderHandler } from './IHeaderHandler';
import { injectable } from 'inversify';

@injectable()
export class SingleUserHeaderHandler implements IHeaderHandler {
    async getHeaders() {
        // no headers needs to be set to single user
        return {};
    }
}


