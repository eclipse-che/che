/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import 'reflect-metadata';
import { ICheLoginPage } from './ICheLoginPage';
import { injectable } from 'inversify';
import { TestConstants } from '../../TestConstants';

@injectable()
export class SingleUserLoginPage implements ICheLoginPage {

    async login(timeout: number = TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT) {
        // do nothing
    }

}
