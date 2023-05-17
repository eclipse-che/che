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
import { ShellExecutor } from '../utils/ShellExecutor';

function getFullDevfileRegistryUrl(): string {
    return `${TestConstants.TS_SELENIUM_BASE_URL}/devfile-registry/devfiles/`;
}

export const devfileRegistry: object = JSON.parse(
    (new ShellExecutor).curl(getFullDevfileRegistryUrl()).stdout
);
