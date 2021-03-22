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
import { e2eContainer } from '../../inversify.config';
import { CLASSES } from '../../inversify.types';
import { KubernetesPlugin } from '../../pageobjects/ide/plugins/KubernetesPlugin';

const kubernetesPlugin: KubernetesPlugin = e2eContainer.get(CLASSES.KubernetesPlugin);

suite(`The 'VscodeKubernetesPlugin' test`, async () => {
    suite('Check the "Kubernetes" plugin', async () => {
        test('Check plugin is added to workspace', async () => {
            await kubernetesPlugin.openView(240_000);
        });

        test('Check plugin basic functionality', async () => {
            await kubernetesPlugin.waitListItemContains('/api-ocp46-crw-qe-com:6443/', 240_000);
        });
    });

});
