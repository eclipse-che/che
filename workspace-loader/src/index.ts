/*
 * Copyright (c) 2018-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

require('./style.css');

import { Loader } from './loader/loader';
import { KeycloakLoader } from './keycloak-loader';
import { WorkspaceLoader } from './workspace-loader';

/** Initialize */
if (document.getElementById('workspace-console')) {
    new KeycloakLoader().loadKeycloakSettings().catch(error => {
        if (error) {
            console.log(error);
        }
    }).then(keycloak => {
        new WorkspaceLoader(new Loader(), keycloak).load();
    });
}
