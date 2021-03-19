/*********************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import 'reflect-metadata';
import * as workspaceHandling from '../../../testsLibrary/WorksapceHandlingTests';
import { COMMON_PLUGIN_TESTS_WORKSPACE_NAME } from './CommonPluginTestsDevfile';

suite('Stopping and deleting the workspace', async () => {
    test(`Stop and remowe workspace`, async () => {
        await workspaceHandling.stopAndRemoveWorkspace(COMMON_PLUGIN_TESTS_WORKSPACE_NAME);
    });
});
