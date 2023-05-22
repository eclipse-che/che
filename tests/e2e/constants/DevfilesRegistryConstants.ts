/*********************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { TestConstants } from './TestConstants';
import axios, { AxiosResponse } from 'axios';
import { Logger } from '../utils/Logger';

function getDevfileRegistryUrl(): string {
    return `${TestConstants.TS_SELENIUM_BASE_URL}/devfile-registry/devfiles/`;
}

export const DevfilesRegistryConstants: Promise<object> = (async () => {
    let response: AxiosResponse | undefined;
try {
    response = await axios.get(getDevfileRegistryUrl());
} catch (error) {
    Logger.error(error);
}
return response?.data;
})();
